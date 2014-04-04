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

import ecurrencies.pagseguro.domain._
import ecurrencies.pagseguro.provider.protocol.xml.PagSeguroXMLProtocol

import akka.actor._
import spray.http.HttpCharsets

class PagSeguroProvider(val system: ActorSystem) extends Extension with PagSeguroXMLProtocol {

  val httpCharset = HttpCharsets.`UTF-8`

  implicit val settings = PagSeguroSettings(system.settings.config)
  import settings._

  private def paymentHttpClient = HttpClientService.props[PaymentRequest, PaymentResponse](
    response => response.result match {
      case Some(result) => response.setResult(result.setRedirectUrl(payment.redirectUrl(result.code)))
      case None => response
    }
  )

  private def transactionNotificationHttpClient =
    HttpClientService.props[TransactionNotificationRequest, TransactionResponse]()

  private def transactionSearchByCodeHttpClient =
    HttpClientService.props[TransactionSearchRequest.ByCode, TransactionResponse]()

  private def transactionSearchByDateHttpClient =
    HttpClientService.props[TransactionSearchRequest.ByDate, TransactionSearchResponse.ByDate]()

  private def transactionSearchAbandonedHttpClient =
    HttpClientService.props[TransactionSearchRequest.Abandoned, TransactionSearchResponse.Abandoned]()

  private val pagseguroGuardian: ActorRef = {
    system.actorOf(Props(new Actor {

      import SupervisorStrategy._

      override def supervisorStrategy = OneForOneStrategy() {
        case _: Exception => Restart
      }

      override def receive = {
        case (props: Props, name: String) => context.actorOf(props, name)
      }
    }), "pagseguro")
  }

  // TODO: Add router configuration here
  pagseguroGuardian ! ((paymentHttpClient, payment.serviceId))
  pagseguroGuardian ! ((transactionNotificationHttpClient, transactionNotification.serviceId))
  pagseguroGuardian ! ((transactionSearchByCodeHttpClient, transactionSearch.byCode.serviceId))
  pagseguroGuardian ! ((transactionSearchByDateHttpClient, transactionSearch.byDate.serviceId))
  pagseguroGuardian ! ((transactionSearchAbandonedHttpClient, transactionSearch.abandoned.serviceId))
}


object PagSeguroProvider extends ExtensionId[PagSeguroProvider] with ExtensionIdProvider {
  override def lookup(): this.type = this
  override def get(system: ActorSystem): PagSeguroProvider = super.get(system)
  override def createExtension(system: ExtendedActorSystem) = new PagSeguroProvider(system)
}

