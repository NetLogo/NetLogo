package org.nlogo.app.github

import scala.concurrent.Future

import org.apache.commons.codec.binary.Base64

import com.ning.http.client.AsyncHttpClientConfig

import play.api.libs.ws.WSAuthScheme
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ning.NingWSClient

object ApiClient {
  implicit class RichWSResponse(r: WSResponse) {
    def content = new String(Base64.decodeBase64((r.json \ "content").as[String]), "UTF-8")
    def nextPageUrl: Option[String] = r.header("Link").flatMap {
      _.split(", ") // separate the links
        .map(_.split("; ")) // split each one between url and rel
        .find(_.last == "rel=\"next\"") // find the link to the next page
        .map(_.head.tail.init) // and keep the url with the first ("<") and last (">") chars removed
    }
  }
}

class ApiClient(user: () => String, password: () => String) {

  // TODO: close client when quitting NetLogo
  val client = new NingWSClient(new AsyncHttpClientConfig.Builder().build())

  def get(uri: String, parameters: (String, String)*): Future[WSResponse] = {
    client
      .url(uri.toString)
      .withFollowRedirects(true)
      .withHeaders("Accept" -> "application/vnd.github.v3+json")
      .withAuth(user(), password(), WSAuthScheme.BASIC)
      .withQueryString(parameters: _*)
      .get
  }
}
