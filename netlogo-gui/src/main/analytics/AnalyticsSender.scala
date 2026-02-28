// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import java.net.{ HttpURLConnection, NetworkInterface, URI }
import java.net.http.{ HttpClient, HttpRequest, HttpResponse }
import java.nio.file.{ Files, Paths, StandardOpenOption }
import java.util.UUID

import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters.EnumerationHasAsScala
import scala.util.Try

import org.nlogo.core.NetLogoPreferences

import telemetry.TelemetryEventV1

object AnalyticsSender {

  private given ExecutionContext = ExecutionContext.global

  private val domain = "https://telemetry.netlogo.org"

  private var available   = false
  private var sendEnabled = false
  private var silent      = false

  private var lastCheck = 0L

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

  private val cachePath = Paths.get(System.getProperty("user.home"), ".nlogo", "analyticsCache")

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

  private[analytics] def refreshPreference(): Unit = {
    sendEnabled = NetLogoPreferences.getBoolean("sendAnalytics", false)
  }

  private[analytics] def shutdown(): Unit = {
  }

  private def trySending(eventType: AnalyticsEventType, payloadOpt: Option[String]): Future[Unit] = {
    if (silent) {
      Future.successful({})
    } else {
      Future {
        if (sendEnabled) {
          val wasAvailable = available

          if (!available && System.currentTimeMillis() - lastCheck >= 5000)
            checkNetwork()

          if (available) {
            send(eventType, payloadOpt)

            if (!wasAvailable)
              sendCache()
          } else {
            cacheEvent(eventType, payloadOpt)
          }
        }
      }
    }
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
        checkNetwork()
    }

  private def cacheEvent(eventType: AnalyticsEventType, payloadOpt: Option[String]): Unit = {
    Files.createDirectories(cachePath.getParent)

    val line = s"${eventType.ordinal},${payloadOpt.getOrElse("")}\n"

    Files.writeString(cachePath, line, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
  }

  private def sendCache(): Unit = {
    if (Files.exists(cachePath) && !Files.isDirectory(cachePath)) {
      Files.readAllLines(cachePath).forEach { line =>
        val split = line.split(",", 2).map(_.trim)

        if (split.size == 2) {
          split(0).toIntOption.foreach { tpe =>
            Future(send(AnalyticsEventType.fromOrdinal(tpe), Option(split(1)).filter(_.nonEmpty)))
          }
        }
      }

      Files.delete(cachePath)
    }
  }

  private def checkNetwork(): Unit = {
    available =
      NetworkInterface.getNetworkInterfaces().asScala.exists(_.isUp) &&
        Try {
          val url  = URI.create(s"$domain/telemetry/diagnostic").toURL
          val conn = url.openConnection().asInstanceOf[HttpURLConnection]
          conn.setConnectTimeout(5000)
          conn.   setReadTimeout(5000)
          conn.getResponseCode()
        }.toOption.contains(200)
    lastCheck = System.currentTimeMillis()
  }

  // used by GUI tests to prevent GitHub Actions from diluting the analytics data (Isaac B 10/29/25)
  def silence(): Unit = {
    silent = true
  }

}
