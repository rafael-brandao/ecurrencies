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

import java.nio.charset.Charset

import scala.annotation.implicitNotFound

import akka.util.ByteString

import spray.http._
import spray.http.MediaTypes.`application/xml`
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.{Deserializer, Unmarshaller}
import spray.http.HttpResponse


package object xml {

  @implicitNotFound("No implicit evidence of type class XMLWriter in scope for ${T}")
  trait XMLWriter[T] {
    def write(value: T, charset: Charset): ByteString
  }

  object XMLWriter {
    implicit object `XMLWriter[PaymentRequest]` extends ScalaXMLWriterProtocol.`XMLWriterOf[PaymentRequest]`
  }

  @implicitNotFound("No implicit evidence of type class XMLReader in scope for ${T}")
  trait XMLReader[T] {
    def read(status : String, bytes: Array[Byte]): T
  }

  object XMLReader {
    implicit object `XMLReaderOf[PaymentResponse]`
      extends ScalaXMLReaderProtocol.`XMLReaderOf[PaymentResponse]`

    implicit object `XMLReaderOf[TransactionResponse]`
      extends ScalaXMLReaderProtocol.`XMLReaderOf[TransactionResponse]`

    implicit object `XMLReaderOf[TransactionSearchResponse.ByDate]`
      extends ScalaXMLReaderProtocol.`XMLReaderOf[TransactionSearchResponse.ByDate]`

    implicit object `XMLReaderOf[TransactionSearchResponse.Abandoned]`
      extends ScalaXMLReaderProtocol.`XMLReaderOf[TransactionSearchResponse.Abandoned]`
  }

  object XMLMarshaller {
    def apply[T : XMLWriter] = Marshaller.of[T](`application/xml`) {
      (value, contentType, ctx) => ctx.marshalTo(
        HttpEntity(contentType, implicitly[XMLWriter[T]].write(value, contentType.charset.nioCharset))
      )
    }
  }
  object XMLUnmarshaller{
    def apply[T: XMLReader] : HttpResponse => T = {
      case HttpResponse(status, entity, _, _) =>
        entity match {
          case HttpEntity.NonEmpty(contentType, data)
            if contentType.mediaType == `application/xml` && data.nonEmpty =>

            implicitly[XMLReader[T]].read(status.toString(), data.toByteArray)

          case _ => implicitly[XMLReader[T]].read(status.toString(), Array.empty)
        }
    }
  }
}
