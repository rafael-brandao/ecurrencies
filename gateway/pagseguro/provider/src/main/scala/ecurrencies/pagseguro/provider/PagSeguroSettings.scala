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
package ecurrencies.pagseguro.provider

import com.typesafe.config.Config

class PagSeguroSettings(config : Config) {

    lazy val domain = config.getString("ecurrencies.provider.pagseguro.domain")

    object payment {
      val path = config.getString("ecurrencies.provider.pagseguro.payment.path")
      val redirectUrlBase = config.getString("ecurrencies.provider.pagseguro.payment.redirectUrlBase")
      val serviceId = config.getString("ecurrencies.provider.pagseguro.payment.serviceId")
      def redirectUrl(code: String) = s"$redirectUrlBase$code"
      def urlBuilder = baseUrlBuilder.append(path)
    }

    object transactionNotification {
      val path = config.getString("ecurrencies.provider.pagseguro.transactionNotification.path")
      val serviceId = config.getString("ecurrencies.provider.pagseguro.transactionNotification.serviceId")
      def urlBuilder = baseUrlBuilder.append(path)
    }

    object transactionSearch {
      object byCode {
        val path = config.getString("ecurrencies.provider.pagseguro.transactionSearch.byCode.path")
        val serviceId = config.getString("ecurrencies.provider.pagseguro.transactionSearch.byCode.serviceId")
        def urlBuilder = baseUrlBuilder.append(path)
      }
      object byDate {
        val path = config.getString("ecurrencies.provider.pagseguro.transactionSearch.byDate.path")
        val serviceId = config.getString("ecurrencies.provider.pagseguro.transactionSearch.byDate.serviceId")
        def urlBuilder = baseUrlBuilder.append(path)
      }
      object abandoned {
        val path = config.getString("ecurrencies.provider.pagseguro.transactionSearch.abandoned.path")
        val serviceId = config.getString("ecurrencies.provider.pagseguro.transactionSearch.abandoned.serviceId")
        def urlBuilder = baseUrlBuilder.append(path)
      }
    }

    private def baseUrlBuilder =
      new StringBuilder().append("https://").append(domain).append('/')
}

object PagSeguroSettings {
  def apply(config : Config) = new PagSeguroSettings(config)
}
