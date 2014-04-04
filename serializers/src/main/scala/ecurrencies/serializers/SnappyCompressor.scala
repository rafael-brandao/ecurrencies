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

import akka.serialization.Serializer
import org.xerial.snappy.Snappy

private abstract class SnappyCompressor(serializer: Serializer) extends Serializer {

  override def identifier = Int.MaxValue

  override def includeManifest = true

  override def toBinary(o: AnyRef) = Snappy.compress(serializer.toBinary(o))

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]) =
    serializer.fromBinary(Snappy.uncompress(bytes), manifest)
}

private[serializers] object SnappyProtobufCompressor extends SnappyCompressor(ProtobufSerializer)
