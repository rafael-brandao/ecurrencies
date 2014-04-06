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

import java.io.ByteArrayInputStream

import scala.collection.immutable.Seq
import scala.xml.{Elem, NodeSeq, XML}

import ecurrencies.pagseguro.domain._

object ScalaXMLReaderProtocol {

  trait `XMLReaderOf[PaymentResponse]` extends XMLReader[PaymentResponse] {

    override def read(status: String, bytes: Array[Byte]) =
      parse(readXML(bytes), paymentResult)(PaymentResponse(status, _, _))
  }


  trait `XMLReaderOf[TransactionResponse]` extends XMLReader[TransactionResponse] {

    override def read(status: String, bytes: Array[Byte]) =
      parse(readXML(bytes), transaction)(TransactionResponse(status, _, _))
  }

  trait `XMLReaderOf[TransactionSearchResponse.ByDate]` extends XMLReader[TransactionSearchResponse.ByDate] {

    override def read(status: String, bytes: Array[Byte]) =
      parse(readXML(bytes), transactionSearchResult.byDate) {
        TransactionSearchResponse.ByDate(status, _, _)
      }
  }

  trait `XMLReaderOf[TransactionSearchResponse.Abandoned]`
    extends XMLReader[TransactionSearchResponse.Abandoned] {

    override def read(status: String, bytes: Array[Byte]) =
      parse(readXML(bytes), transactionSearchResult.abandoned) {
        TransactionSearchResponse.Abandoned(status, _, _)
      }
  }


  private def paymentResult(elem: NodeSeq) = PaymentResult(
    code = (elem \ "code").map(_.text).head,
    date = (elem \ "date").map(_.text).head
  )

  private def transaction(elem: NodeSeq) = {
    val summary = transactionSummary(elem)
    Transaction(
      date = summary.date,
      lastEventDate = summary.lastEventDate,
      code = summary.code,
      reference = summary.reference,
      `type` = summary.`type`,
      status = summary.status,
      cancellationSource = summary.cancellationSource,
      paymentMethod = summary.paymentMethod,
      grossAmount = summary.grossAmount,
      discountAmount = summary.discountAmount,
      feeAmount = summary.feeAmount,
      netAmount = summary.netAmount,
      extraAmount = summary.extraAmount,
      escrowEndDate = (elem \ "escrowEndDate").map(_.text).headOption,
      installmentCount = (elem \ "installmentCount").map(_.toInt).head,
      itemCount = (elem \ "itemCount").map(_.toInt).head,
      items = (elem \ "items" \ "item").map(item),
      sender = (elem \ "sender").map(sender).head,
      shipping = (elem \ "shipping").map(shipping).head
    )
  }

  private object transactionSearchResult {

    import TransactionSearchResult._

    def byDate(elem: NodeSeq) = dateAndPagination(elem) {
      (date, pagination) => ByDate(
        date = date,
        pagination = pagination,
        transactions = (elem \ "transactions" \ "transaction").map(transactionSummary)
      )
    }

    def abandoned(elem: NodeSeq) = dateAndPagination(elem) {
      (date, pagination) => Abandoned(
        date = date,
        pagination = pagination,
        transactions = (elem \ "transactions" \ "transaction").map(abandonedTransaction)
      )
    }

    private def pagination(elem: NodeSeq) = Pagination(
      currentPage = (elem \ "currentPage").map(_.toInt).head,
      resultsInThisPage = (elem \ "resultsInThisPage").map(_.toInt).head,
      totalPages = (elem \ "totalPages").map(_.toInt).head
    )

    private def dateAndPagination[T](elem: NodeSeq)(fn: (String, Pagination) => T) =
      fn((elem \ "date").map(_.text).head, pagination(elem))
  }

  private def transactionSummary(elem: NodeSeq) = {
    val common = abandonedTransaction(elem)
    TransactionSummary(
      date = common.date,
      lastEventDate = common.lastEventDate,
      code = common.code,
      reference = common.reference,
      `type` = common.`type`,
      status = (elem \ "status").map(_.toInt.toTransactionStatus).head,
      cancellationSource = (elem \ "cancellationSource").map(_.toInt.toTransactionCancellationSource).headOption,
      paymentMethod = (elem \ "paymentMethod").map(paymentMethod).head,
      grossAmount = common.grossAmount,
      discountAmount = (elem \ "discountAmount").map(_.toDouble).head,
      feeAmount = (elem \ "feeAmount").map(_.toDouble).head,
      netAmount = (elem \ "netAmount").map(_.toDouble).head,
      extraAmount = (elem \ "extraAmount").map(_.toDouble).head
    )
  }

  private def abandonedTransaction(elem: NodeSeq) = AbandonedTransaction(
    date = (elem \ "date").map(_.text).head,
    lastEventDate = (elem \ "lastEventDate").map(_.text).head,
    code = (elem \ "code").map(_.text).head,
    reference = (elem \ "reference").map(_.text).headOption,
    `type` = (elem \ "type").map(_.toInt.toTransactionType).head,
    grossAmount = (elem \ "grossAmount").map(_.toDouble).head
  )

  private def paymentMethod(elem: NodeSeq) = PaymentMethod(
    `type` = (elem \ "type").map(_.toInt.toPaymentMethodType).head,
    code = (elem \ "code").map(_.toInt.toPaymentMethodCode).headOption
  )

  private def item(elem: NodeSeq) = Item(
    id = (elem \ "id").map(_.text).head,
    description = (elem \ "description").map(_.text).head,
    amount = (elem \ "amount").map(_.toDouble).head,
    quantity = (elem \ "quantity").map(_.toInt).head,
    weight = (elem \ "weight").map(_.toLong).headOption,
    shippingCost = (elem \ "shippingCost").map(_.toDouble).headOption
  )


  private def sender(elem: NodeSeq) = Sender(
    name = (elem \ "name").map(_.text).headOption,
    email = (elem \ "email").map(_.text).headOption,
    phone = (elem \ "phone").map(phone).headOption,
    documents = (elem \ "documents" \ "document").map(document),
    bornDate = (elem \ "bornDate").map(_.text).headOption
  )

  private def phone(elem: NodeSeq) = Phone(
    areaCode = (elem \ "areaCode").map(_.text).head,
    number = (elem \ "number").map(_.text).head
  )

  private def document(elem: NodeSeq) = Document(
    `type` = (elem \ "type").map(_.toInt.toDocumentType).head,
    value = (elem \ "value").map(_.toLong).head
  )

  private def shipping(elem: NodeSeq) = Shipping(
    `type` = (elem \ "type").map(_.toInt.toShippingType).head,
    cost = (elem \ "cost").map(_.toDouble).headOption,
    address = (elem \ "address").map(address).headOption
  )

  private def address(elem: NodeSeq) = Address(
    street = (elem \ "street").map(_.text).head,
    number = (elem \ "number").map(_.text).head,
    complement = (elem \ "complement").map(_.text).headOption,
    district = (elem \ "district").map(_.text).head,
    city = (elem \ "city").map(_.text).head,
    state = (elem \ "state").map(_.text).head,
    country = (elem \ "country").map(_.text.toCountry).head,
    postalCode = (elem \ "postalCode").map(_.text).head
  )

  private def error(elem: NodeSeq) = Error(
    code = (elem \ "code").map(_.toInt).head,
    message = (elem \ "message").map(_.text).headOption
  )

  private def parse[T, Result](elem: Option[NodeSeq], fn_result: (NodeSeq => Result))
                              (fn_t: (Seq[Error], Option[Result]) => T): T = {

    def parseErrors(elem: NodeSeq)(fn: (Seq[Error] => T)): T = fn((elem \ "error").map(error))

    elem match {
      case Some(e) =>
        parseErrors(e) {
          errors => fn_t(errors, {
            if (errors.size == 0) Some(fn_result(e)) else None
          })
        }
      case None => fn_t(Seq.empty, None)
    }
  }

  private val readXML : Array[Byte] => Option[Elem] = {
    case byteArray if byteArray.length > 0 => Some(XML.load(new ByteArrayInputStream(byteArray)))
    case _ => None
  }

  private implicit class ExtendedNodeSeq(val nodeSeq: NodeSeq) extends AnyVal {
    def toInt = nodeSeq.text.toInt
    def toLong = nodeSeq.text.toLong
    def toDouble = nodeSeq.text.toDouble
  }

  private implicit class ExtendedInt(val i: Int) extends AnyVal {
    def toTransactionCancellationSource = TransactionCancellationSource.valueOf(i)
    def toTransactionStatus = TransactionStatus.valueOf(i)
    def toTransactionType = TransactionType.valueOf(i)
    def toPaymentMethodType = PaymentMethodType.valueOf(i)
    def toPaymentMethodCode = PaymentMethodCode.valueOf(i)
    def toShippingType = ShippingType.valueOf(i)
    def toDocumentType = DocumentType.valueOf(i)
  }

  private implicit class ExtendedString(val string: String) extends AnyVal {
    def toCountry = Country.values.filter(_.name == string).head
  }
}
