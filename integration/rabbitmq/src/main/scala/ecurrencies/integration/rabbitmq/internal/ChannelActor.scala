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
package ecurrencies.integration.rabbitmq.internal

import scala.util.{Failure, Success}

import ecurrencies.EcurrencyServiceException
import ecurrencies.serializers.Serializers

import akka.actor._
import com.rabbitmq.client.{Channel, Envelope}
import com.rabbitmq.client.AMQP.BasicProperties


class ChannelActor(configureChannel: (Channel, ActorRef) => Unit = (_, _) => {})
  extends Actor with ActorLogging with Serializers {


  override def receive: Receive = {
    case channel: Channel =>
      // This might look unintuitive but this way we guarantee that this actor
      // is fully initialized before receiving responses.
      context.become(start(channel))
      configureChannel(channel, self)
  }


  def start(channel: Channel): Receive = {
    case RabbitMQMessage(payload, envelope, properties) =>
      handleResponse(payload, envelope, properties, channel)

    case
      Status.Failure(EcurrencyServiceException(Some(RabbitMQMessage(_, env, props)), isRecoverable, _)) =>
      channel.basicReject(env.getDeliveryTag, isRecoverable)
  }


  def handleResponse(payload: AnyRef, envelope: Envelope, properties: BasicProperties, channel: Channel) {
    import envelope.{getDeliveryTag => deliveryTag}
    import properties.{getContentEncoding => contentEncoding, getCorrelationId => correlationId}

    for {
      replyTo <- Option(properties.getReplyTo)
    } yield {
      {
        for {
          (byteArray, contentType) <- serialize(payload, contentEncoding)
        } yield (byteArray, contentType)
      } match {

        case Success((byteArray, contentType)) =>
          val replyProperties = new BasicProperties.Builder()
            .contentEncoding(contentEncoding)
            .contentType(contentType)
            .correlationId(correlationId)
            .build

          channel.basicPublish("", replyTo, replyProperties, byteArray)

        case Failure(ex) =>
          log.error(ex, s"Error serializing message [ $payload ].Could not publish a reply to [ $replyTo ].")
      }
    }
    channel.basicAck(deliveryTag, false)
  }
}

object ChannelActor {
  def props(configureChannel: (Channel, ActorRef) => Unit) = Props(new ChannelActor(configureChannel))
}
