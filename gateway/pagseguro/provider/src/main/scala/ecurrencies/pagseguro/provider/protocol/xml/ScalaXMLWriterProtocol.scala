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

import java.io.{BufferedWriter, OutputStreamWriter}
import java.nio.charset.Charset

import scala.xml.{MinimizeMode, Node, NodeSeq, Utility, XML}

import ecurrencies.pagseguro.domain._

import akka.util.{ByteString, ByteStringBuilder}


object ScalaXMLWriterProtocol {

  trait `XMLWriterOf[PaymentRequest]` extends XMLWriter[PaymentRequest] {
    override def write(request: PaymentRequest, charset: Charset) = writeXML(request.toNodeSeq, charset)
  }

  private implicit class PaymentRequestXML(val request : PaymentRequest) extends AnyVal with AsNodeSeq {
    override def toNodeSeq =
      <checkout>
        <currency>{request.currency}</currency>
        {if (request.items.length > 0) <items>{ for(item <- request.items) yield item.toNodeSeq }</items>}
        {(for(reference <- request.reference) yield <reference>{reference}</reference>).getOrElse(null)}
        {(for(sender <- request.sender) yield sender.toNodeSeq).getOrElse(null)}
        {(for(shipping <- request.shipping) yield shipping.toNodeSeq).getOrElse(null)}
        {(for(ea <- request.extraAmount) yield <extraAmount>{ea}</extraAmount>).getOrElse(null)}
        {(for(ru <- request.redirectURL) yield <redirectURL>{ru}</redirectURL>).getOrElse(null)}
        {(for(nu <- request.notificationURL) yield <notificationURL>{nu}</notificationURL>).getOrElse(null)}
        {(for(mu <- request.maxUses) yield <maxUses>{mu}</maxUses>).getOrElse(null)}
        {(for(ma <- request.maxAge) yield <maxAge>{ma}</maxAge>).getOrElse(null)}
        {if (request.metadataItems.length > 0) <metadata>{for(item <- request.metadataItems) yield item.toNodeSeq}</metadata>}
      </checkout>
  }

  private implicit class ItemXML(val item : Item) extends AnyVal with AsNodeSeq {
    override def toNodeSeq =
      <item>
        <id>{item.id}</id>
        <description>{item.description}</description>
        <amount>{item.amount}</amount>
        <quantity>{item.quantity}</quantity>
        {(for(weight <- item.weight) yield <weight>{weight}</weight>).getOrElse(null)}
        {(for(sc <- item.shippingCost) yield <shippingCost>{sc}</shippingCost>).getOrElse(null)}
      </item>
  }

  private implicit class SenderXML(val sender : Sender) extends AnyVal with AsNodeSeq {
    override def toNodeSeq =
      <sender>
        {(for(name <- sender.name) yield <name>{name}</name>).getOrElse(null)}
        {(for(email <- sender.email) yield <email>{email}</email>).getOrElse(null)}
        {(for(phone <- sender.phone) yield phone.toNodeSeq).getOrElse(null)}
        {if (sender.documents.length > 0) <documents>{for(d <- sender.documents) yield d.toNodeSeq}</documents>}
        {(for(bornDate <- sender.bornDate) yield <bornDate>{bornDate}</bornDate>).getOrElse(null)}
      </sender>
  }

  private implicit class DocumentXML(val document:Document) extends AnyVal with AsNodeSeq {
    override def toNodeSeq =
      <document>
        <type>{document.`type`.name}</type>
        <value>{document.value}</value>
      </document>
  }

  private implicit class PhoneXML(val phone:Phone) extends AnyVal with AsNodeSeq {
    override def toNodeSeq =
      <phone>
        <areaCode>{phone.areaCode}</areaCode>
        <number>{phone.number}</number>
      </phone>
  }

  private implicit class ShippingXML(val shipping:Shipping) extends AnyVal with AsNodeSeq {
    override def toNodeSeq =
      <shipping>
        <type>{shipping.`type`.id}</type>
        {(for(cost <- shipping.cost) yield <cost>{cost}</cost>).getOrElse(null)}
        {(for(address <- shipping.address) yield <address>{address.toNodeSeq}</address>).getOrElse(null)}
      </shipping>
  }

  private implicit class AddressXML(val address:Address) extends AnyVal with AsNodeSeq{
    override def toNodeSeq =
      <address>
        <street>{address.street}</street>
        <number>{address.number}</number>
        {(for(c <- address.complement) yield <complement>{c}</complement>).getOrElse(null)}
        <district>{address.district}</district>
        <postalCode>{address.postalCode}</postalCode>
        <city>{address.city}</city>
        <state>{address.state}</state>
        <country>{address.country}</country>
      </address>
  }

  private implicit class MetadataItemXML(val item: MetadataItem) extends AnyVal with AsNodeSeq {
    override def toNodeSeq =
      <item>
        <key>{item.key}</key>
        <value>{item.value}</value>
        {(for(group <- item.group) yield <group>{group}</group>).getOrElse(null) }
      </item>
  }

  private[this] trait AsNodeSeq extends Any {
    def toNodeSeq: NodeSeq
  }

  private[this] def writeXML(node: => Node, charset: Charset): ByteString = {

    def writeTo(builder: ByteStringBuilder): ByteStringBuilder = {
      val writer = new BufferedWriter(new OutputStreamWriter(builder.asOutputStream, charset.name))
      writer.write(s"<?xml version='1.0' encoding='${charset.name}' standalone='yes'?>")
      XML.write(writer, Utility.trim(node), charset.name, xmlDecl = false, null, MinimizeMode.Default)
      writer.close()
      builder
    }

    writeTo(new ByteStringBuilder).result()
  }
}
