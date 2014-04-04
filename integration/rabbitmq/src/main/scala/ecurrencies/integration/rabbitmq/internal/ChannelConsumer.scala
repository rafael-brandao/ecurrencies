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

import scala.util.{Failure, Success, Try}

import ecurrencies.serializers.Serializers

import akka.actor._
import com.rabbitmq.client.{Channel, DefaultConsumer, Envelope}
import com.rabbitmq.client.AMQP.BasicProperties


private[rabbitmq] class ChannelConsumer(channel: Channel, self: ActorRef)(implicit system : ActorSystem)
  extends DefaultConsumer(channel) with Serializers {

  override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties,
                              body: Array[Byte]) {

    import ChannelConsumer._
    import envelope._
    import properties._

    {
      for {
        (ecurrencyId, serviceId) <- decodeRoutingKey(getRoutingKey)
        service <- Try(system.actorSelection(s"/user/$ecurrencyId/$serviceId"))
        payload <- deserialize(body, getContentEncoding, getContentType)
      } yield (payload, service)
    } match {

      case Success((payload, service)) =>
        service.tell(new RabbitMQMessage(payload, envelope, properties), self)

      case Failure(ex) =>
        system.log.warning("Could not handle response {}. Cause: {}\n{}",
          getDeliveryTag, ex, exceptionWrapper(ex).getStackTraceString
        )
        channel.basicReject(getDeliveryTag, false)
    }
  }
}

private[rabbitmq] object ChannelConsumer {

  private def decodeRoutingKey(routingKey: String) = Try {
    val split = routingKey.split("\\.")
    val ecurrencyId = split(0)
    val serviceId = split(1)

    require(ecurrencyId != null && ecurrencyId.length > 0)
    require(serviceId != null && serviceId.length > 0)

    (ecurrencyId, serviceId)
  }
}
