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

import com.typesafe.config.Config
import com.rabbitmq.client.ConnectionFactory
import java.util.concurrent.TimeUnit


private[rabbitmq] class RabbitMQSettings(config: Config) {

  def bindingKey = config.getString("ecurrencies.integration.rabbitmq.binding-key")

  object connection {
    def host = config.getString("ecurrencies.integration.rabbitmq.connection.host")

    def port = config.getInt("ecurrencies.integration.rabbitmq.connection.port")

    def virtualHost = config.getString("ecurrencies.integration.rabbitmq.connection.virtual-host")

    def username = config.getString("ecurrencies.integration.rabbitmq.connection.username")

    def password = config.getString("ecurrencies.integration.rabbitmq.connection.password")

    def buildFactory = {
      val factory = new ConnectionFactory
      factory.setHost(host)
      factory.setPort(port)
      factory.setVirtualHost(virtualHost)
      factory.setUsername(username)
      factory.setPassword(password)
      factory
    }
  }

  object exchange {
    def name = config.getString("ecurrencies.integration.rabbitmq.exchange.name")

    def `type` = config.getString("ecurrencies.integration.rabbitmq.exchange.type")

    def durable = config.getBoolean("ecurrencies.integration.rabbitmq.exchange.durable")

    def autoDelete = config.getBoolean("ecurrencies.integration.rabbitmq.exchange.auto-delete")

    def internal = config.getBoolean("ecurrencies.integration.rabbitmq.exchange.internal")
  }

  object queue {
    def name = config.getString("ecurrencies.integration.rabbitmq.queue.name")

    def durable = config.getBoolean("ecurrencies.integration.rabbitmq.queue.durable")

    def exclusive = config.getBoolean("ecurrencies.integration.rabbitmq.queue.exclusive")

    def autoDelete = config.getBoolean("ecurrencies.integration.rabbitmq.exchange.auto-delete")
  }

  object channel {
    def prefetchCount = config.getInt("ecurrencies.integration.rabbitmq.channel.prefetch-count")

    def mailbox = "ecurrencies.integration.rabbitmq.channel.mailbox"
  }

  object consumer {
    def instances = config.getInt("ecurrencies.integration.rabbitmq.consumer.instances")

    def timeout = config.getDuration("ecurrencies.integration.rabbitmq.consumer.timeout", TimeUnit.SECONDS)
  }

}
