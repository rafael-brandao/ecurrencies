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

import ecurrencies.pagseguro.domain._
import ecurrencies.pagseguro.domain.TransactionSearchRequest.DateRange

class Fixtures

object Fixtures {

  implicit class XMLExtension(val string: String) extends AnyVal {
    def inline = string.split("\n").foldLeft(new StringBuilder) {
      (builder, line) => builder.append(line.trim)
    }
  }

  lazy val paymentResultXML =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<checkout>
      |    <code>8CF4BE7DCECEF0F004A6DFA0A8243412</code>
      |    <date>2010-12-02T10:11:28.000-02:00</date>
      |</checkout>
    """.stripMargin

  lazy val abandonedTransactionSearchResultXML =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<transactionSearchResult>
      |    <date>2011-02-16T20:14:35.000-02:00</date>
      |    <currentPage>1</currentPage>
      |    <resultsInThisPage>2</resultsInThisPage>
      |    <totalPages>1</totalPages>
      |    <transactions>
      |        <transaction>
      |            <date>2011-02-05T15:46:12.000-02:00</date>
      |            <lastEventDate>2011-02-15T17:39:14.000-03:00</lastEventDate>
      |            <code>EDDDC505-8A26-494E-96C2-53D285A470C2</code>
      |            <type>1</type>
      |            <grossAmount>6.00</grossAmount>
      |        </transaction>
      |        <transaction>
      |            <date>2011-02-07T18:57:52.000-02:00</date>
      |            <lastEventDate>2011-02-14T21:37:24.000-03:00</lastEventDate>
      |            <reference>REFCODE2</reference>
      |            <code>97B1F57E-0EC0-4D03-BF7E-C4694CF6062E</code>
      |            <type>1</type>
      |            <grossAmount>6.00</grossAmount>
      |        </transaction>
      |    </transactions>
      |</transactionSearchResult>
    """.stripMargin

  lazy val transactionSearchResultXML =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<transactionSearchResult>
      |    <date>2011-02-16T20:14:35.000-02:00</date>
      |    <currentPage>1</currentPage>
      |    <resultsInThisPage>10</resultsInThisPage>
      |    <totalPages>1</totalPages>
      |    <transactions>
      |        <transaction>
      |            <date>2011-02-05T15:46:12.000-02:00</date>
      |            <lastEventDate>2011-02-15T17:39:14.000-03:00</lastEventDate>
      |            <code>9E884542-81B3-4419-9A75-BCC6FB495EF1</code>
      |            <reference>REF1234</reference>
      |            <type>1</type>
      |            <status>3</status>
      |            <cancellationSource>1</cancellationSource>
      |            <paymentMethod>
      |                <type>1</type>
      |            </paymentMethod>
      |            <grossAmount>49900.00</grossAmount>
      |            <discountAmount>0.00</discountAmount>
      |            <feeAmount>0.00</feeAmount>
      |            <netAmount>49900.00</netAmount>
      |            <extraAmount>0.00</extraAmount>
      |        </transaction>
      |        <transaction>
      |            <date>2011-02-07T18:57:52.000-02:00</date>
      |            <lastEventDate>2011-02-14T21:37:24.000-03:00</lastEventDate>
      |            <code>2FB07A22-68FF-4F83-A356-24153A0C05E1</code>
      |            <reference>REF5678</reference>
      |            <type>3</type>
      |            <status>4</status>
      |            <paymentMethod>
      |                <type>3</type>
      |            </paymentMethod>
      |            <grossAmount>26900.00</grossAmount>
      |            <discountAmount>0.00</discountAmount>
      |            <feeAmount>0.00</feeAmount>
      |            <netAmount>26900.00</netAmount>
      |            <extraAmount>0.00</extraAmount>
      |        </transaction>
      |    </transactions>
      |</transactionSearchResult>
    """.stripMargin

  lazy val transactionXML =
    """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      |<transaction>
      |    <date>2011-02-10T16:13:41.000-03:00</date>
      |    <code>9E884542-81B3-4419-9A75-BCC6FB495EF1</code>
      |    <reference>REF1234</reference>
      |    <type>1</type>
      |    <status>3</status>
      |    <lastEventDate>2011-02-15T17:39:14.000-03:00</lastEventDate>
      |    <paymentMethod>
      |        <type>1</type>
      |        <code>101</code>
      |    </paymentMethod>
      |    <grossAmount>49900.00</grossAmount>
      |    <discountAmount>0.00</discountAmount>
      |    <feeAmount>0.00</feeAmount>
      |    <netAmount>49900.00</netAmount>
      |    <extraAmount>0.00</extraAmount>
      |    <installmentCount>1</installmentCount>
      |    <itemCount>2</itemCount>
      |    <items>
      |        <item>
      |            <id>0001</id>
      |            <description>Notebook Prata</description>
      |            <quantity>1</quantity>
      |            <amount>24300.00</amount>
      |        </item>
      |        <item>
      |            <id>0002</id>
      |            <description>Notebook Rosa</description>
      |            <quantity>1</quantity>
      |            <amount>25600.00</amount>
      |        </item>
      |    </items>
      |    <sender>
      |        <name>José Comprador</name>
      |        <email>comprador@uol.com.br</email>
      |        <phone>
      |            <areaCode>11</areaCode>
      |            <number>56273440</number>
      |        </phone>
      |    </sender>
      |    <shipping>
      |        <address>
      |            <street>Av. Brig. Faria Lima</street>
      |            <number>1384</number>
      |            <complement>5o andar</complement>
      |            <district>Jardim Paulistano</district>
      |            <postalCode>01452002</postalCode>
      |            <city>Sao Paulo</city>
      |            <state>SP</state>
      |            <country>BRA</country>
      |        </address>
      |        <type>1</type>
      |        <cost>21.50</cost>
      |    </shipping>
      |</transaction>
    """.stripMargin

  lazy val errorsXML =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<errors>
      |    <error>
      |        <code>11004</code>
      |        <message>Currency is required.</message>
      |    </error>
      |    <error>
      |        <code>11005</code>
      |        <message>Currency invalid value: 100</message>
      |    </error>
      |</errors>
    """.stripMargin

  lazy val paymentRequestProto =
    PaymentRequest(
      credentials = Some(Credentials(email = "joe@mail.com", token = "654649849845689")),
      currency = Currency.BRL,
      items = List(
        Item(id = "0001", description = "Notebook Prata", amount = 24300.00, quantity = 1, weight = Option(1000)),
        Item(id = "0002", description = "Notebook Rosa", amount = 25600.00, quantity = 2, weight = Option(750))
      ),
      reference = Option("REF1234"),
      sender = Option(Sender(
        name = Option("José Comprador"),
        email = Option("comprador@uol.com.br"),
        phone = Option(Phone(areaCode = "11", number = "56273440"))
      )),
      shipping = Option(Shipping(
        `type` = ShippingType.PAC,
        address = Option(Address(
          street = "Av. Brig. Faria Lima",
          number = "1384",
          complement = Option("5o andar"),
          district = "Jardim Paulistano",
          postalCode = "01452002",
          city = "Sao Paulo",
          state = "SP",
          country = Country.BRA
        ))
      ))
    )

  lazy val transactionSearchByDateProto = TransactionSearchRequest.ByDate(
    dateRange = DateRange(
      initialDate = "2014-01-01T00:00",
      finalDate = "2014-01-25T00:00"
    )
  )

  lazy val errorsProtoList = List(
    Error(11004, Some("Currency is required.")),
    Error(11005, Some("Currency invalid value: 100"))
  )

  lazy val paymentResponseProto = PaymentResponse(
    errors = List(),
    result = Some(PaymentResult(
      code = "8CF4BE7DCECEF0F004A6DFA0A8243412", date = "2010-12-02T10:11:28.000-02:00", redirectUrl = None
    ))
  )

  lazy val transactionSearchResponseProto = TransactionSearchResponse.ByDate(
    errors = List(),
    result = Some(TransactionSearchResult.ByDate(
      date = "2011-02-16T20:14:35.000-02:00",
      pagination = TransactionSearchResult.Pagination(currentPage = 1, resultsInThisPage = 10, totalPages = 1),
      List(
        TransactionSummary(
          date = "2011-02-05T15:46:12.000-02:00",
          lastEventDate = "2011-02-15T17:39:14.000-03:00",
          code = "9E884542-81B3-4419-9A75-BCC6FB495EF1",
          reference = Some("REF1234"),
          `type` = TransactionType.PAYMENT,
          status = TransactionStatus.PAID,
          cancellationSource = Some(TransactionCancellationSource.INTERNAL),
          paymentMethod = PaymentMethod(`type` = PaymentMethodType.CREDIT_CARD, code = None),
          grossAmount = 49900.0,
          discountAmount = 0.0,
          feeAmount = 0.0,
          netAmount = 49900.0,
          extraAmount = 0.0
        ),
        TransactionSummary(
          date = "2011-02-07T18:57:52.000-02:00",
          lastEventDate = "2011-02-14T21:37:24.000-03:00",
          code = "2FB07A22-68FF-4F83-A356-24153A0C05E1",
          reference = Some("REF5678"),
          `type` = TransactionType.FUND_ADDITION,
          status = TransactionStatus.AVAILABLE,
          cancellationSource = None,
          paymentMethod = PaymentMethod(`type` = PaymentMethodType.ONLINE_TRANSFER, code = None),
          grossAmount = 26900.0,
          discountAmount = 0.0,
          feeAmount = 0.0,
          netAmount = 26900.0,
          extraAmount = 0.0
        )
      )
    ))
  )

  lazy val transactionResponseProto = TransactionResponse(
    errors = List(),
    transaction = Some(Transaction(
      date = "2011-02-10T16:13:41.000-03:00",
      lastEventDate = "2011-02-15T17:39:14.000-03:00",
      code = "9E884542-81B3-4419-9A75-BCC6FB495EF1",
      reference = Some("REF1234"),
      `type` = TransactionType.PAYMENT,
      status = TransactionStatus.PAID,
      cancellationSource = None,
      paymentMethod = PaymentMethod(
        `type` = PaymentMethodType.CREDIT_CARD,
        code = Some(PaymentMethodCode.VISA_CREDIT_CARD)
      ),
      grossAmount = 49900.0,
      discountAmount = 0.0,
      feeAmount = 0.0,
      netAmount = 49900.0,
      extraAmount = 0.0,
      escrowEndDate = None,
      installmentCount = 1,
      itemCount = 2,
      items = List(
        Item(id = "0001", description = "Notebook Prata", amount = 24300.0, quantity = 1, weight = None, shippingCost = None),
        Item(id = "0002", description = "Notebook Rosa", amount = 25600.0, quantity = 1, weight = None, shippingCost = None)
      ),
      Sender(
        name = Some("José Comprador"),
        email = Some("comprador@uol.com.br"),
        phone = Some(Phone("11", "56273440")),
        documents = List(),
        bornDate = None
      ),
      Shipping(
        `type` = ShippingType.PAC,
        cost = Some(21.5),
        address = Some(Address(
          street = "Av. Brig. Faria Lima",
          number = "1384",
          complement = Some("5o andar"),
          district = "Jardim Paulistano",
          city = "Sao Paulo",
          state = "SP",
          country = Country.BRA,
          postalCode = "01452002"
        ))
      )
    ))
  )

  lazy val abandonedTransactionSearchResponseProto = TransactionSearchResponse.Abandoned(
    errors = List(),
    result = Some(TransactionSearchResult.Abandoned(
      date = "2011-02-16T20:14:35.000-02:00",
      pagination = TransactionSearchResult.Pagination(currentPage = 1, resultsInThisPage = 2, totalPages = 1),
      transactions = List(
        AbandonedTransaction(
          date = "2011-02-05T15:46:12.000-02:00",
          lastEventDate = "2011-02-15T17:39:14.000-03:00",
          code = "EDDDC505-8A26-494E-96C2-53D285A470C2",
          reference = None,
          `type` = TransactionType.PAYMENT,
          grossAmount = 6.0
        ),
        AbandonedTransaction(
          date = "2011-02-07T18:57:52.000-02:00",
          lastEventDate = "2011-02-14T21:37:24.000-03:00",
          code = "97B1F57E-0EC0-4D03-BF7E-C4694CF6062E",
          reference = Some("REFCODE2"),
          `type` = TransactionType.PAYMENT,
          grossAmount = 6.0
        )
      )
    ))
  )

  def main(args: Array[String]) {
    println(transactionXML)
    println(transactionXML.inline)
  }
}
