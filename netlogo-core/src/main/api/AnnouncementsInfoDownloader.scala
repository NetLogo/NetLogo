// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.File
import java.net.URL
import java.time.LocalDate

import scala.io.Source
import scala.concurrent.{ ExecutionContext, Future }

import org.json.simple.{ JSONObject, JSONArray }
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

object AnnouncementsInfoDownloader extends InfoDownloader {

  override val prefsKey = "announcements"

  val defaultURL = new URL("https://ccl.northwestern.edu/netlogo/announce.json")

  def fetch(): Future[Seq[Announcement]] = {
    import ExecutionContext.Implicits.global
    this.apply(defaultURL).map(
      _.fold(Seq.empty[Announcement]) { case (file, _) => parse(file) }
    )
  }

  def parse(file: File): Seq[Announcement] = {

    import scala.jdk.CollectionConverters.IteratorHasAsScala

    val contents = Source.fromFile(file).mkString

    val json = new JSONParser()

    try {

      val arr  = json.parse(contents).asInstanceOf[JSONArray]
      val iter = arr.iterator()

      val announcements =
        iter.asScala.toVector.map {
          case obj: JSONObject =>

            val id       = obj.get(      "id").asInstanceOf[Number].intValue
            val title    = obj.get(   "title").asInstanceOf[String]
            val dateStr  = obj.get(    "date").asInstanceOf[String]
            val lifespan = obj.get("lifespan").asInstanceOf[Number].longValue
            val typeStr  = obj.get(    "type").asInstanceOf[String]
            val summary  = obj.get( "summary").asInstanceOf[String]
            val desc     = obj.get(    "desc").asInstanceOf[String]

            val (months, days, years) = dateStr.split("/") match {
              case Array(m, d, y) => (m, d, y)
              case a => throw new IllegalStateException
            }
            val date                       = LocalDate.of(2000 + years.toInt, months.toInt, days.toInt)

            val annType =
              typeStr match {
                case "release"  => Release
                case "event"    => Event
                case "advisory" => Advisory
                case _          => throw new IllegalStateException
              }

            val endDate =
              if (lifespan == 0)
                None
              else
                Option(date.plusDays(lifespan))

            Announcement(id, title, date, endDate, annType, summary, desc)

          case o =>
            throw new IllegalStateException

        }

      announcements.filter((x) => x.endDate.map(_.isAfter(LocalDate.now())).getOrElse(true))

    } catch {
      case ex: ParseException =>
        println(ex)
        Seq.empty
    }

  }

}
