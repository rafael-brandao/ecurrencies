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

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.Envelope
import scala.util.Random._

trait RabbitMQTestFixtures {

  val validEcurrencyId = "pagseguro"

  val validServiceId = "protobuf-reply"
  val validRoutingKey = s"$validEcurrencyId.$validServiceId"

  val recoverableServiceId = "protobuf-recoverable"
  val recoverableRoutingKey = s"$validEcurrencyId.$recoverableServiceId"

  val unrecoverableServiceId = "protobuf-unrecoverable"
  val unrecoverableRoutingKey = s"$validEcurrencyId.$unrecoverableServiceId"

  val invalidRoutingKey = "pagseguro"
  val unreachableRoutingKey = "pagseguro.unreachable"

  val supportedContentEncoding = "protobuf"
  val unsupportedContentEncoding = "avro"

  trait RabbitMQTestMessage {
    protected def propertiesBuilder =
      new BasicProperties.Builder().contentEncoding(_contentEncoding).contentType(_contentType)

    val _routingKey: String
    val _contentEncoding: String
    val _contentType: String

    val payload: Array[Byte]
    val properties = propertiesBuilder.build()
    val envelope = new Envelope(nextInt(Int.MaxValue - 1) + 1, nextBoolean(), nextString(10), _routingKey)
    val consumerTag = nextString(15)

    def args = (consumerTag, envelope, properties, payload)
  }

  class MessageWithoutReplyTo(val _routingKey: String,
                              val _contentEncoding: String,
                              val _contentType: String,
                              val payload: Array[Byte]) extends RabbitMQTestMessage

  class MessageWithReplyTo(val _routingKey: String,
                           val _contentEncoding: String,
                           val _contentType: String,
                           val payload: Array[Byte]) extends RabbitMQTestMessage {

    override protected def propertiesBuilder = super.propertiesBuilder.replyTo(nextString(10))
  }

  class MessageWithReplyToAndCorrelationId(override val _routingKey: String,
                                           override val _contentEncoding: String,
                                           override val _contentType: String,
                                           override val payload: Array[Byte])
    extends MessageWithReplyTo(_routingKey, _contentEncoding, _contentType, payload) {

    override protected def propertiesBuilder = super.propertiesBuilder.correlationId(nextString(10))
  }
}
