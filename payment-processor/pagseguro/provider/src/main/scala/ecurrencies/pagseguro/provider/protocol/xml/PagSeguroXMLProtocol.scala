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
package ecurrencies.pagseguro.provider.protocol.xml

import spray.http.{HttpEntity, ContentType, HttpCharset}
import ecurrencies.pagseguro.provider.PagSeguroSettings
import ecurrencies.pagseguro.domain._

import spray.http.MediaTypes.`application/xml`
import spray.client.pipelining.{Post, Get}


trait PagSeguroXMLProtocol {
  def httpCharset: HttpCharset

  implicit val settings: PagSeguroSettings

  private lazy val contentType = ContentType(`application/xml`, httpCharset)

  implicit val `xmlMarshaller[PaymentRequest]` = XMLMarshaller[PaymentRequest]

  implicit val `xmlUnmarshaller[PaymentResponse]` = XMLUnmarshaller[PaymentResponse]
  implicit val `xmlUnmarshaller[TransactionResponse]` = XMLUnmarshaller[TransactionResponse]
  implicit val `xmlUnmarshaller[TransactionSearchResponse.ByDate]` = XMLUnmarshaller[TransactionSearchResponse.ByDate]
  implicit val `xmlUnmarshaller[TransactionSearchResponse.Abandoned]` = XMLUnmarshaller[TransactionSearchResponse.Abandoned]


  import ecurrencies.pagseguro.provider.protocol.UrlImplicits._

  implicit val paymentRequestToHttp = (request: PaymentRequest) =>
      Post(request.toUrl, request).mapEntity(entity => HttpEntity(contentType, entity.data))

  implicit val transactionNotificationToHttp = (request: TransactionNotificationRequest) => Get(request.toUrl)
  implicit val transactionSearchByCodeHttp = (request: TransactionSearchRequest.ByCode) => Get(request.toUrl)
  implicit val transactionSearchByDateHttp = (request: TransactionSearchRequest.ByDate) => Get(request.toUrl)
  implicit val transactionSearchByAbandonedHttp = (request: TransactionSearchRequest.Abandoned) => Get(request.toUrl)

}


