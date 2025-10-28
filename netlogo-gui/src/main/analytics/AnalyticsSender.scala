// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import java.net.URI
import java.net.http.{ HttpClient, HttpRequest, HttpResponse }
import java.util.UUID

import org.nlogo.core.NetLogoPreferences

import scala.concurrent.{ ExecutionContext, Future }

import telemetry.TelemetryEventV1

object AnalyticsSender {

  private given ExecutionContext = ExecutionContext.global

  private val domain = "https://telemetry.netlogo.org"

  private var sendEnabled = false
  private var silent      = false

  private val isDeveloper = System.getProperty("org.nlogo.release") != "true"

  private val myUUID = {

    val Sentinel = "sentinel"
    val UUIDKey  = "analytics.uuid"

    val retrieved = NetLogoPreferences.get(UUIDKey, Sentinel)

    if (retrieved != Sentinel) {
      UUID.fromString(retrieved)
    } else {
      val newUUID = UUID.randomUUID()
      NetLogoPreferences.put(UUIDKey, newUUID.toString)
      newUUID
    }

  }

  private val client =
    HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .build()

  private val networkTracker = new NetworkTracker(domain)

  private[analytics] def apply(eventType: AnalyticsEventType): Future[Unit] =
    trySending(eventType, None)

  private[analytics] def apply(eventType: AnalyticsEventType, payload: Map[String, Any]): Future[Unit] = {
    // non-recursively builds a simple subset of JSON to avoid unnecessary
    // dependencies or object structures (Isaac B 7/2/25)
    def buildJson(properties: Map[String, Any]): String = {
      properties.map {
        case (key, value: String)  => s"\"$key\": \"$value\""
        case (key, value: Double)  => s"\"$key\": $value"
        case (key, value: Int)     => s"\"$key\": $value"
        case (key, value: Boolean) => s"\"$key\": $value"
        case (key, value)          => s"\"$key\": \"null\""
      }.mkString("{ ", ", ", " }")
    }
    trySending(eventType, Option(buildJson(payload)))
  }

  private[analytics] def setPreference(enabled: Boolean): Unit = {
    sendEnabled = enabled
  }

  private[analytics] def shutdown(): Unit = {
  }

  private def trySending(eventType: AnalyticsEventType, payloadOpt: Option[String]): Future[Unit] =
    Future {
      if (!silent && sendEnabled && networkTracker.isAvailable())
        send(eventType, payloadOpt)
    }

  private def send(eventType: AnalyticsEventType, payloadOpt: Option[String]): Unit =
    try {

      val event =
        TelemetryEventV1(
          formatVersion = 1
        , uuid1         = myUUID. getMostSignificantBits
        , uuid2         = myUUID.getLeastSignificantBits
        , isDeveloper   = isDeveloper
        , eventType     = eventType.ordinal
        , payload       = payloadOpt.getOrElse("")
        )

      val request =
        HttpRequest.newBuilder()
          .uri(URI.create(s"$domain/telemetry/v2/upload"))
          .header("Content-Type", "application/x-protobuf")
          .POST(HttpRequest.BodyPublishers.ofByteArray(event.toByteArray))
          .build()

      client.send(request, HttpResponse.BodyHandlers.ofString())

    } catch {
      case e: Exception =>
        println(s"Telemetry exception: $e")
        networkTracker.checkNetwork()
        if (networkTracker.isAvailable()) {
          println(s"Network is available.  Retrying telemetry event of type '$eventType'....")
          send(eventType, payloadOpt)
        } else {
          println(s"Network unavailable.  Not retrying telemetry event of type '$eventType'.")
        }
    }

  // used by GUI tests to prevent GitHub Actions from diluting the analytics data (Isaac B 10/29/25)
  def silence(): Unit = {
    silent = true
  }

}
