// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.util.Try

import sttp.client4.{ Backend, BasicBodyPart, Request }
import sttp.model.headers.CookieWithMeta
import sttp.model.Part

import ujson.Value

// this class keeps all the request code in one place, as well as initial error handling (Isaac B 8/22/25)
object JsonRequest {
  def apply(request: String, backend: Backend[Future]): Try[JsonResponse] =
    send(build(request), backend)

  def apply(request: String, body: Map[String, String], backend: Backend[Future]): Try[JsonResponse] =
    send(build(request).body(body), backend)

  def apply(request: String, cookies: Seq[CookieWithMeta], backend: Backend[Future]): Try[JsonResponse] =
    send(build(request).cookies(cookies), backend)

  def apply(request: String, body: Map[String, String], cookies: Seq[CookieWithMeta],
            backend: Backend[Future]): Try[JsonResponse] =
    send(build(request).body(body).cookies(cookies), backend)

  def apply(request: String, multipartBody: Seq[Part[BasicBodyPart]], cookies: Seq[CookieWithMeta],
            backend: Backend[Future]): Try[JsonResponse] =
    send(build(request).multipartBody(multipartBody).cookies(cookies), backend)

  private def build(request: String): Request[String] = {
    // this string escape nonsense is to prevent STTP's UriContext from
    // escaping the slashes in the request (Isaac B 8/22/25)
    import sttp.client4.quick.{ quickRequest, UriContext }
    quickRequest.post(uri"${s"https://modelingcommons.org$request"}").header("Accept", "application/json")
  }

  private def send(request: Request[String], backend: Backend[Future]): Try[JsonResponse] = {
    Try {
      val response = Await.result(request.send(backend), Duration(15, "seconds"))

      if (response.isSuccess) {
        try {
          JsonResponse(ujson.read(response.body), response.unsafeCookies)
        } catch {
          case _ => throw new ServerException
        }
      } else {
        throw new ServerException
      }
    }
  }
}

case class JsonResponse(json: Value, cookies: Seq[CookieWithMeta])
