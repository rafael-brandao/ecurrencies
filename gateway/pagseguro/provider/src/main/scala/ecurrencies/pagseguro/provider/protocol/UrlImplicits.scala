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
package ecurrencies.pagseguro.provider.protocol

import ecurrencies.pagseguro.domain.{Credentials, TransactionSearchRequest, TransactionNotificationRequest, PaymentRequest}
import ecurrencies.pagseguro.provider._

private[provider] object UrlImplicits {

  implicit class PaymentRequestUrl(val request: PaymentRequest) extends AnyVal with ToURL {

    protected def parameters = credentialsMap(request.credentials)
    override def toUrl(implicit settings: PagSeguroSettings): String =
      toUrl(settings.payment.urlBuilder)
  }


  implicit class TransactionNotificationUrl(val request: TransactionNotificationRequest)
    extends AnyVal with ToURL {

    override protected def beforeQuery(baseUrl: StringBuilder): StringBuilder =
      baseUrl.append('/').append(request.code)

    protected def parameters: Map[String, Any] = credentialsMap(request.credentials)

    def toUrl(implicit settings: PagSeguroSettings): String =
      toUrl(settings.transactionNotification.urlBuilder)
  }


  implicit class TransactionSearchByCodeUrl(val request: TransactionSearchRequest.ByCode)
    extends AnyVal with ToURL {

    override protected def beforeQuery(baseUrl: StringBuilder): StringBuilder =
      baseUrl.append('/').append(request.code)

    protected def parameters: Map[String, Any] = credentialsMap(request.credentials)

    def toUrl(implicit settings: PagSeguroSettings): String =
      toUrl(settings.transactionSearch.byCode.urlBuilder)
  }


  implicit class TransactionSearchByDateUrl(val request: TransactionSearchRequest.ByDate)
    extends AnyVal with ToURL {

    protected def parameters: Map[String, Any] =
      dateRangeMap(request.dateRange) ++
        paginationMap(request.pagination) ++
        credentialsMap(request.credentials)

    def toUrl(implicit settings: PagSeguroSettings): String =
      toUrl(settings.transactionSearch.byDate.urlBuilder)
  }


  implicit class TransactionSearchAbandonedUrl(val request: TransactionSearchRequest.Abandoned)
    extends AnyVal with ToURL {

    protected def parameters: Map[String, Any] =
      dateRangeMap(request.dateRange) ++
        paginationMap(request.pagination) ++
        credentialsMap(request.credentials)

    override def toUrl(implicit settings: PagSeguroSettings): String =
      toUrl(settings.transactionSearch.abandoned.urlBuilder)
  }


  sealed trait ToURL extends Any {
    protected def parameters: Map[String, Any]
    protected def beforeQuery(baseUrl: StringBuilder): StringBuilder = baseUrl

    protected def toUrl(baseUrl: StringBuilder = new StringBuilder()): String =
      baseUrl.append(parameters.toQueryString).toString()

    def toUrl(implicit settings: PagSeguroSettings): String
  }

  private implicit class ParameterMap(val map: Map[String, Any]) extends AnyVal {
    def toQueryString: StringBuilder =
      map.view.withFilter {
        case (key, value) => value match {
          case v: Option[Any] => v.isDefined
          case v => v != null
        }
      }.map {
        case (key, value) => value match {
          case Some(v) => (key, v)
          case _ => (key, value)
        }
      }.foldLeft(new StringBuilder, true) {
        case ((builder, first), (key, value)) =>
          ( {
            if (!first) builder.append('&')
            else builder.append('?')
          }.append(key).append('=').append(value), false)
      }._1
  }


  private def credentialsMap(credentials: Option[Credentials]): Map[String, Any] =
    credentials match {
      case Some(c) => Map("email" -> c.email, "token" -> c.token)
      case None => Map.empty
    }

  private def dateRangeMap(dateRange: TransactionSearchRequest.DateRange): Map[String, Any] =
    Map("initialDate" -> dateRange.initialDate, "finalDate" -> dateRange.finalDate)

  private def paginationMap(pagination: Option[TransactionSearchRequest.Pagination]): Map[String, Any] =
    pagination match {
      case Some(p) => Map("page" -> p.page, "maxPageResults" -> p.maxPageResults)
      case None => Map.empty
    }
}
