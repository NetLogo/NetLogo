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

import org.nlogo.core.NetLogoPreferences

object AnnouncementsInfoDownloader extends InfoDownloader {

  override val prefsKey = "announcements"

  val defaultURL = new URL("https://backend.netlogo.org/items/announcements")

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

      val topObj = json.parse(contents).asInstanceOf[JSONObject]
      val arr    = topObj.get("data").asInstanceOf[JSONArray]
      val iter   = arr.iterator()

      val announcements =
        iter.asScala.toVector.map {
          case obj: JSONObject =>

            val id       = obj.get(      "id").asInstanceOf[Number].intValue
            val title    = obj.get(   "title").asInstanceOf[String]
            val dateStr  = obj.get(    "date").asInstanceOf[String]
            val lifespan = obj.get("lifespan").asInstanceOf[Number].longValue
            val typeStr  = obj.get(    "type").asInstanceOf[String]
            val summary  = obj.get( "summary").asInstanceOf[String]
            val content  = obj.get( "content").asInstanceOf[String]

            val (years, months, days) = dateStr.split("-") match {
              case Array(y, m, d) => (y, m, d)
              case a => throw new IllegalStateException
            }
            val date = LocalDate.of(2000 + years.toInt, months.toInt, days.toInt)

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

            Announcement(id, title, date, endDate, annType, summary, content)

          case o =>
            throw new IllegalStateException

        }

      if (NetLogoPreferences.get("announce.debug", "false") != "true")
        announcements.filter((x) => x.endDate.map(_.isAfter(LocalDate.now())).getOrElse(true))
      else
        announcements

    } catch {
      case ex: ParseException =>
        println(ex)
        Seq.empty
    }

  }

}
