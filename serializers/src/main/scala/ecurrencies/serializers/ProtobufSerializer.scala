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

import scala.language.reflectiveCalls
import scala.reflect.runtime.universe._

import akka.serialization.Serializer
import com.google.protobuf.MessageLite
import net.sandrogrzicic.scalabuff.{Parser => ScalabuffParser}

private[serializers] object ProtobufSerializer extends Serializer {

  override def identifier = 2
  override def includeManifest = true


  override def toBinary(o: AnyRef) = o match {
    case message: MessageLite => message.toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize [ ${o.getClass.getName}} ] to protobuf byte array")
  }


  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]) = manifest match {
    case Some(clazz)
      if classOf[ScalabuffParser[_]].isAssignableFrom(clazz) => scalabuffParserOf(clazz).parseFrom(bytes)

    case Some(clazz)
      if classOf[MessageLite].isAssignableFrom(clazz) => protobuffParserOf(clazz).parseFrom(bytes)

    case _ =>
      throw new IllegalArgumentException("Need a protobuf message class to be able to deserialize from byte array")
  }


  private def scalabuffParserOf[T: TypeTag](clazz: Class[T]) =
    rootMirror
      .reflectModule(rootMirror.moduleSymbol(clazz))
      .instance
      .asInstanceOf[ {def parseFrom(data: Array[Byte]): MessageLite}]

  private def protobuffParserOf(clazz: Class[_]) = {
    clazz
      .getDeclaredMethod("getParserForType")
      .invoke(null)
      .asInstanceOf[ {def parseFrom(data: Array[Byte]): MessageLite}]
  }
}
