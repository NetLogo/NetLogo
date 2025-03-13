package org.nlogo.app

import java.awt.Toolkit
import java.net.URI
import java.time.Duration
import java.util.{ Locale, UUID }
import java.util.{ UUID }

import org.matomo.java.tracking.{ MatomoRequests, MatomoTracker, TrackerConfiguration }
import org.matomo.java.tracking.parameters.{ AcceptLanguage, Country, DeviceResolution, VisitorId }

object Analytics {

  private type Getter = (String) => Option[String]
  private type Setter = (String, String) => Unit

  def phoneHome(getFromStorage: Getter, writeToStorage: Setter, baseVars: Map[String, AnyRef]): Unit = {

    if (System.getProperty("org.nlogo.tracking.disabled") != "true") {

      val matomoConfig =
        TrackerConfiguration
          .builder()
          .apiEndpoint(URI.create("http://hubnetweb.org:9010/matomo.php")) // TODO: We really need to send this over HTTPS, but I'm having a tough time convincing Matomo to read API calls from over HTTPS --Jason B. (3/16/25)
          .defaultSiteId(1)
          .defaultAuthToken("05479cbd874c8a04c1ad219bb6938394") // TODO: Replace with actual secure system for handling these tokens --Jason B. (3/16/25)
          .logFailedTracking(true)
          .connectTimeout(Duration.ofSeconds(5))
          .userAgent(s"NLMatomoClient/1.0 (${System.getProperty("os.name")}) Irrelevant () Irrelevant")
          .build()

      val tracker = new MatomoTracker(matomoConfig)

      val country = Country.fromCode(Locale.getDefault().getCountry)

      try {

        val extraVars = Map("country" -> country, "system-bitness" -> checkBitness().toString)

        val json =
          (baseVars ++ extraVars).toSeq.map {
            case (k, v) => s""""${k.replaceAll("\"", "\\\"")}": "${v.toString.replaceAll("\"", "\\\"")}""""
          }.mkString("{ ", ", ", " }")

        val request =
          MatomoRequests
            .event("NetLogo", "Launch", json, 1)
            .headerAcceptLanguage(language(getFromStorage))
            .visitorId(visitorID(getFromStorage, writeToStorage))
            .deviceResolution(resolution())
            // I'd like to do `.visitorCountry(country)` here, but that causes these events to fail to register
            // in Matomo.... --Jason B. (3/13/25)
            .visitorIp("")
            .build()

        tracker.sendRequestAsync(request)

      } catch {
        case e: Exception =>
          System.err.println(s"Failed to send Matomo request: ${e.getMessage}")
      } finally {
        tracker.close()
      }

    }

  }

  // I'm not certain that this method will even work.  There are mixed messages about this online.  Many
  // people seem to be claiming that `os.arch` reports the JVM's architecture, not the OS's architecture.
  // The JavaDocs say otherwise.  Some say it's only on Windows that it reports JVM arch .  Others say
  // that it's JVM arch everywhere.  Who knows!  --Jason B. (3/14/25)
  private def checkBitness(): Bitness = {

    val is64 =
      if (System.getProperty("os.name").startsWith("Windows")) {
        val arch      = Option(System.getenv("PROCESSOR_ARCHITECTURE")).getOrElse("unknown")
        val wow64Arch = Option(System.getenv("PROCESSOR_ARCHITEW6432")).getOrElse("unknown")
        arch.endsWith("64") || wow64Arch.endsWith("64")
      } else {
        System.getProperty("os.arch").endsWith("64")
      }

    if (is64)
      Bitness64
    else
      Bitness32

  }

  private def resolution(): DeviceResolution = {
    val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
    val width      = screenSize.getWidth()
    val height     = screenSize.getHeight()
    new DeviceResolution(width.toInt, height.toInt)
  }

  private def language(getFromStorage: Getter): AcceptLanguage = {
    import scala.collection.JavaConverters.asScalaBufferConverter
    val lang  = getFromStorage("user.language").getOrElse("en")
    val langs = Locale.LanguageRange.parse(lang).asScala.head
    AcceptLanguage.builder.languageRange(langs).build
  }

  private def visitorID(getFromStorage: Getter, writeToStorage: Setter): VisitorId = {

    val uuidKey = "user.uuid"

    val uuid =
      getFromStorage(uuidKey).fold {
        val rand = UUID.randomUUID()
        writeToStorage(uuidKey, rand.toString)
        rand
      } (UUID.fromString _)

    VisitorId.fromUUID(uuid)

  }

  private sealed trait Bitness
  private object Bitness32 extends Bitness { override def toString = "32" }
  private object Bitness64 extends Bitness { override def toString = "64" }

}
