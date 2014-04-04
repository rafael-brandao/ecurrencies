/**
 * Copyright (c) 2014 Rafael Brandão <rafa.bra@gmail.com>
 *
 * This is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ecurrencies.integration.rabbitmq

import scala.concurrent.Future
import scala.util.Random

import ecurrencies.{EcurrencyServiceException, Message}
import ecurrencies.integration.rabbitmq.internal.ChannelActor
import ecurrencies.pagseguro.domain.Credentials

import akka.actor._
import akka.pattern.pipe
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestKit}
import com.rabbitmq.client.Channel
import com.rabbitmq.client.AMQP.BasicProperties
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Matchers.{any, anyBoolean, anyLong, anyString, eq => equalTo}
import org.mockito.Mockito.{never, verify}
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class RabbitMQConsumerSpec
  extends TestKit(ActorSystem()) with RabbitMQTestFixtures
  with WordSpecLike with ShouldMatchers with BeforeAndAfterAll with MockitoSugar {

  "Any RabbitMQConsumer" which {
    """
    receives a response with valid routingKey, a replyTo property
    and supported contentEncoding and contentType""" should {

      "reply response to channel and ack delivery if there was no EcurrencyServiceException" in new TestScope {
        val message =
          new MessageWithReplyToAndCorrelationId(validRoutingKey, supportedContentEncoding, contentType, payload)
        val inOrder = Mockito.inOrder(channel)

        consumer.handleDelivery _ tupled message.args

        inOrder.verify(channel).basicPublish(equalTo(""), equalTo(replyTo), any[BasicProperties], equalTo(payload))
        inOrder.verify(channel).basicAck(message.envelope.getDeliveryTag, false)
      }
    }


    """
    receives a response that has a valid routingKey, no replyTo property
    and supported contentEncoding and contentType""" should {

      "make channel and ack delivery if there was no EcurrencyServiceException" in new TestScope {
        val message = new MessageWithoutReplyTo(validRoutingKey, supportedContentEncoding, contentType, payload)

        consumer.handleDelivery _ tupled message.args

        verify(channel).basicAck(message.envelope.getDeliveryTag, false)

        verify(channel, never).basicPublish(any[String], any[String], any[BasicProperties], any[Array[Byte]])
        verify(channel, never).basicReject(any[Long], any[Boolean])
      }
    }


    "receives a response that has a valid routing key but an unsupported contentEncoding" should {
      "make channel reject response delivery tag and not requeue" in new TestScope {
        val message = new MessageWithReplyTo(validRoutingKey, unsupportedContentEncoding, "any", "any".getBytes)

        consumer.handleDelivery _ tupled message.args

        verify(channel).basicReject(message.envelope.getDeliveryTag, false)

        verify(channel, never).basicAck(any[Long], any[Boolean])
        verify(channel, never).basicPublish(any[String], any[String], any[BasicProperties], any[Array[Byte]])
      }
    }


    """
    receives a response that has a valid routing key, supported contentEncoding and contentType,
    but has to deal with a recoverable Exception""" should {

      "make channel reject response and requeue it" in new TestScope {
        val message = new MessageWithReplyTo(recoverableRoutingKey, supportedContentEncoding, contentType, payload)

        consumer.handleDelivery _ tupled message.args

        verify(channel).basicReject(message.envelope.getDeliveryTag, true)

        verify(channel, never).basicAck(any[Long], any[Boolean])
        verify(channel, never).basicPublish(any[String], any[String], any[BasicProperties], any[Array[Byte]])
      }
    }


    """
    receives a response that has valid routing key, content encoding and content type,
    but has to deal with an unrecoverable Exception""" should {

      "make channel reject response and not requeue it" in new TestScope {

        val message = new MessageWithReplyTo(unrecoverableRoutingKey, supportedContentEncoding, contentType, payload)

        consumer.handleDelivery _ tupled message.args

        verify(channel).basicReject(message.envelope.getDeliveryTag, false)

        verify(channel, never).basicAck(any[Long], any[Boolean])
        verify(channel, never).basicPublish(any[String], any[String], any[BasicProperties], any[Array[Byte]])
      }
    }


    "receives any response that has an invalid routing key" should {
      "make channel reject response delivery tag and not requeue" in new TestScope {
        val message = new MessageWithReplyTo(invalidRoutingKey, "anyEncoding", "anyType", "anyMessage".getBytes)

        consumer.handleDelivery _ tupled message.args

        verify(channel).basicReject(message.envelope.getDeliveryTag, false)

        verify(channel, never).basicAck(anyLong, anyBoolean)
        verify(channel, never).basicPublish(anyString, anyString, any(classOf[BasicProperties]), any(classOf[Array[Byte]]))
      }
    }


    "receives any response that has a valid, but unreachable routing key" should {
      "make channel reject response delivery tag and not requeue" in new TestScope {
        val message = new MessageWithReplyTo(unreachableRoutingKey, supportedContentEncoding, contentType, payload)

        consumer.handleDelivery _ tupled message.args

        verify(channel).basicReject(message.envelope.getDeliveryTag, false)

        verify(channel, never).basicAck(anyLong, anyBoolean)
        verify(channel, never).basicPublish(anyString, anyString, any(classOf[BasicProperties]), any(classOf[Array[Byte]]))
      }
    }
  }


  trait TestScope {
    import Random._
    import internal.ChannelConsumer

    val protoMessage = Credentials(email = nextString(30), token = nextString(10))

    val contentType = protoMessage.getClass.getName
    val payload = protoMessage.toByteArray

    val message: RabbitMQTestMessage
    lazy val properties = message.properties
    lazy val replyTo = properties.getReplyTo

    val channel = mock[Channel]

    val channelActorRef = TestActorRef(Props(new ChannelActor))
    channelActorRef ! channel

    val consumer = new ChannelConsumer(channel, channelActorRef)
  }

  override protected def beforeAll() {
    system.eventStream.subscribe(
      TestActorRef(Props[RequestDeadLetterListener]),
      classOf[DeadLetter]
    )

    val ecurrencyProvider = TestActorRef(Props(new ProviderGuardian), validEcurrencyId)

    ecurrencyProvider ! ((Props(new ProtobufReplyServiceActor), validServiceId))
    ecurrencyProvider ! ((Props(new ProtobufRecoverableServiceActor), recoverableServiceId))
    ecurrencyProvider ! ((Props(new ProtobufUnrecoverableServiceActor), unrecoverableServiceId))
  }

  override protected def afterAll() {
    shutdown()
  }

  implicit val ec = system.dispatchers.lookup(CallingThreadDispatcher.Id)

  class ProviderGuardian extends Actor {
    override def receive = {
      case (props: Props, name: String) => TestActorRef(props, self, name)
    }
  }

  class ProtobufReplyServiceActor extends Actor {
    override def receive = {
      case message: Message => Future(message) pipeTo sender()
    }
  }

  class ProtobufRecoverableServiceActor extends Actor {
    override def receive = {
      case message: Message =>
        Future(throw EcurrencyServiceException(Some(message), isRecoverable = true)) pipeTo sender()
    }
  }

  class ProtobufUnrecoverableServiceActor extends Actor {
    override def receive = {
      case message: Message =>
        Future(throw EcurrencyServiceException(Some(message), isRecoverable = false)) pipeTo sender()
    }
  }

}
