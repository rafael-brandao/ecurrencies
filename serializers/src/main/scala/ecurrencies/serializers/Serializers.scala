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
package ecurrencies.serializers

import scala.util.Try

import akka.serialization.Serializer

import Serializers._

private object Serializers {
  val serializers = Map(
    "protobuf" -> ProtobufSerializer,
    "snappy-protobuf" -> SnappyProtobufCompressor)

  def process[T](contentEncoding: String)(f: Serializer => T) =
    Try(serializers.get(contentEncoding).map(f).get)
}

trait Serializers {

  def serialize(message: AnyRef, contentEncoding: String) =
    process(contentEncoding) {
      serializer => (serializer.toBinary(message), message.getClass.getName)
    }

  def deserialize(payload: Array[Byte], contentEncoding: String, contentType: String) =
    process(contentEncoding) {
      serializer => serializer.fromBinary(payload, Option(Class.forName(contentType)))
    }
}