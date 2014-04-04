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

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import scala.util.{Success, Failure}
import java.util.NoSuchElementException
import ecurrencies.pagseguro.domain.Credentials

@RunWith(classOf[JUnitRunner])
class SerializersSpec extends WordSpecLike with ShouldMatchers {


  "Serializers trait" when {

    "method 'deserialize' is called with a supported content encoding, valid payload and content type" should {
      "return a Success(Message)" in new TestScope {
        val validMessage = protobufMessage
        deserialize(validMessage.toByteArray, protobufEncoding, validMessage.getClass.getName) match {
          case Success(message) => message shouldEqual validMessage
          case Failure(ex) => fail("test should not get any exceptions.", ex)
        }
        val bytes = protobufMessage.toByteArray

      }
    }

    "method 'deserialize' is called with an unsupported content encoding" should {
      "return a Failure(NoSuchElementException)" in new TestScope {
        val invalidMessage = "I am a serializable String"

        deserialize(invalidMessage.getBytes, "java-serializable", classOf[String].getName) match {
          case Failure(ex) => ex.getClass shouldBe classOf[NoSuchElementException]
          case Success(_) => fail("test should not get here.")
        }

      }
    }

  }

  import scala.util.Random._

  trait TestScope extends Serializers {
    val protobufEncoding = "protobuf"

    def protobufMessage = Credentials(nextString(5), nextString(5))
  }

}
