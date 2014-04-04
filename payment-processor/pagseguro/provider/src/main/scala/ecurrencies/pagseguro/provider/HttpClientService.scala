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
package ecurrencies.pagseguro.provider

import scala.concurrent.Future
import scala.reflect.ClassTag

import ecurrencies.{EcurrencyServiceException, Message}

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}

class HttpClientService[A <: AnyRef, B <: AnyRef](transformResponse: B => B = (response: B) => response)
  (implicit buildHttpRequest: A => HttpRequest, deserialize: HttpResponse => B, classTag: ClassTag[A])
    extends Actor {

  import context.dispatcher

  private val pipeline: A => Future[B] = buildHttpRequest andThen sendReceive ~> deserialize

  override final def receive = {
    case request: Message if request.isAssignableFrom[A] =>
      pipe {
        pipeline(request.bodyAs[A]).transform(
          transformResponse andThen request.mapBody, {
            case ex: EcurrencyServiceException => ex
            case ex: Throwable => EcurrencyServiceException(request, isRecoverable = true, ex)
          }
        )
      } to sender()
  }
}


object HttpClientService {
  def props[A <: AnyRef, B <: AnyRef](transformResponse: B => B = (response: B) => response)
    (implicit buildHttpRequest: A => HttpRequest, deserialize: HttpResponse => B, classTag: ClassTag[A]) = {

    Props(new HttpClientService(transformResponse)(buildHttpRequest, deserialize, classTag))
  }
}
