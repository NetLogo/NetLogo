import sbt._

import java.lang.{ Boolean => JBoolean }
import java.io.{ File, IOException }
import java.net.{ HttpURLConnection, URI, URLDecoder }
import java.nio.file.{ Files, Paths }
import java.util.{ HashMap => JHashMap }

import scala.util.matching.Regex

object NetLogoDocsTest {
  val linkRegex = new Regex("""<a[^<>]+href="([^"]+)"[^<>]*>""")
  val imgRegex  = new Regex("""<img[^<>]+src="([^"]+)"[^<>]*>""")
  val hrefRegex = new Regex("""(.*/)?([^/#]+)?(#.*)?""")
  def anchorRegex(anchor: String) = {
    val escapedAnchor = anchor
      .replace("?", "\\?")
      .replace("*", "\\*")
      .replace("+", "\\+")

    new Regex(s"""<[^<>]+id="$escapedAnchor"[^<>]*>""")
  }

  // Checks for broken links in the docs.
  // Most importantly, links to (local) non-existent anchors are also considered broken.
  def apply(docsRoot: File): Map[String, Seq[String]] = {
    val testedLinks = new JHashMap[String, JBoolean]

    val fileToLines = docsRoot.listFiles.filter(_.getName.endsWith(".html")).map{ file: File =>
      import scala.collection.JavaConverters._
      file.getName -> Files.readAllLines(file.toPath).asScala
    }.toMap

    fileToLines mapValues { lines =>
      val urls = lines.flatMap{ line =>
        (linkRegex.findAllMatchIn(line) ++ imgRegex.findAllMatchIn(line))
          .map(_.group(1)).toSeq
      }.distinct

      urls filterNot {
        case hrefRegex(null, null, anchor) =>
          lines.exists { line: String =>
            anchorRegex(anchor.substring(1)).findFirstIn(line).isDefined
          }
        case hrefRegex(null, file, anchor) =>
          if (URLDecoder.decode(file, "UTF-8").startsWith("mailto"))
            true
          else if (file.endsWith(".html"))
            fileToLines.contains(file) && (
              anchor == null ||
              fileToLines(file).exists { line: String =>
                anchorRegex(anchor.substring(1)).findFirstIn(line).isDefined
              })
          else
            (docsRoot / URLDecoder.decode(file, "UTF-8")).exists
        case hrefRegex(host, file, _) =>
          val loc = if (file == null) host else host + file
          if (testedLinks.containsKey(loc))
            testedLinks.get(loc)
          else if (host.startsWith("images/")) { // our regex matches images as external sites (because of the slash)
            val res = (docsRoot / URLDecoder.decode(loc, "UTF-8")).exists
            testedLinks.put(loc, Boolean.box(res))
            res
          } else {
            val resCode =
              try
                new URL(loc).openConnection.asInstanceOf[HttpURLConnection].getResponseCode
              catch {
                case _: IOException => 0
              }
            val res = resCode == 200
            testedLinks.put(loc, Boolean.box(res))
            res
          }
      }
    }
  }
}

