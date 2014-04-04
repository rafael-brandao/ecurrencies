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
package ecurrencies

@SerialVersionUID(1L)
case class EcurrencyServiceException(message: Option[Message], isRecoverable: Boolean, cause: Throwable)
  extends Exception(cause) {

  def this(isRecoverable: Boolean) = this(None, isRecoverable, null)
  def this(message: Option[Message], isRecoverable: Boolean) = this(message, isRecoverable, null)
  def this(isRecoverable: Boolean, cause: Throwable) = this(None, isRecoverable, cause)
  def this(message: Message, isRecoverable: Boolean) = this(Option(message), isRecoverable, null)
  def this(message: Message, isRecoverable: Boolean, cause: Throwable) = this(Option(message), isRecoverable, cause)
}

object EcurrencyServiceException {
  def apply(isRecoverable: Boolean) = new ecurrencies.EcurrencyServiceException(isRecoverable)

  def apply(message: Option[Message], isRecoverable: Boolean) =
    new ecurrencies.EcurrencyServiceException(message, isRecoverable, null)

  def apply(isRecoverable: Boolean, cause: Throwable) =
    new ecurrencies.EcurrencyServiceException(None, isRecoverable, cause)

  def apply(message: Message, isRecoverable: Boolean) =
    new ecurrencies.EcurrencyServiceException(Option(message), isRecoverable, null)

  def apply(message: Message, isRecoverable: Boolean, cause: Throwable) =
    new ecurrencies.EcurrencyServiceException(Option(message), isRecoverable, cause)
}
