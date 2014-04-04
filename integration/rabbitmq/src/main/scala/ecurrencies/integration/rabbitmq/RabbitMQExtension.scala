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

import ecurrencies.EcurrencyServiceException
import ecurrencies.integration.rabbitmq.internal.RabbitMQSettings

import akka.actor._
import com.rabbitmq.client.Channel
import com.thenewmotion.akka.rabbitmq.CreateChannel


trait RabbitMQ extends Extension {

  private[rabbitmq] val system: ActorSystem

  private[rabbitmq] val settings: RabbitMQSettings

  private[rabbitmq] def connectionActor: ActorRef

  private[rabbitmq] def configureChannel(channel: Channel, self: ActorRef)

  def start(): this.type = {
    system.eventStream.subscribe(
      system.actorOf(Props[RequestDeadLetterListener]),
      classOf[DeadLetter]
    )

    import settings.{channel, consumer}
    (1 to consumer.instances) foreach {
      index =>
        connectionActor ! CreateChannel(
          internal.ChannelActor.props(configureChannel)
            .withMailbox(channel.mailbox),
          Some(s"consumer-$index")
        )
    }
    this
  }
}


object RabbitMQExtension extends ExtensionId[RabbitMQ] with ExtensionIdProvider {
  override def get(system: ActorSystem): RabbitMQ = super.get(system)

  override def lookup(): this.type = this

  override def createExtension(system: ExtendedActorSystem) = new internal.RabbitMQProvider()(system).start()
}

private[rabbitmq] class RequestDeadLetterListener extends Actor {


  import internal.RabbitMQMessage


  override def receive = {
    case d: DeadLetter if d.message.isInstanceOf[RabbitMQMessage] =>
      d.sender ! Status.Failure(
        EcurrencyServiceException(Some(d.message.asInstanceOf[RabbitMQMessage]), isRecoverable = false)
      )
  }
}

