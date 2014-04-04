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

import ecurrencies.integration.rabbitmq.RabbitMQ

import akka.actor.{ActorContext, ActorRef, ExtendedActorSystem}
import com.rabbitmq.client.Channel
import com.thenewmotion.akka.rabbitmq.ConnectionActor


private[rabbitmq] class RabbitMQProvider(implicit val system: ExtendedActorSystem)
  extends RabbitMQ {

  lazy val settings = new RabbitMQSettings(system.settings.config)


  lazy val connectionActor: ActorRef =
    system.actorOf(ConnectionActor.props(settings.connection.buildFactory), "rabbitmq")


  def configureChannel(channel: Channel, self: ActorRef) {
    import settings.{exchange, queue, bindingKey, channel => channelSettings}
    channel.exchangeDeclare(
      exchange.name, exchange.`type`, exchange.durable, exchange.autoDelete, exchange.internal, null
    )
    channel.queueDeclare(queue.name, queue.durable, queue.exclusive, queue.autoDelete, null)
    channel.queueBind(queue.name, exchange.name, bindingKey)
    channel.basicQos(channelSettings.prefetchCount)
    channel.basicConsume(queue.name, false, new ChannelConsumer(channel, self))
  }
}
