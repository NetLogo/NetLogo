import sbt._
import org.jsoup.Jsoup
import scala.collection.JavaConverters._
import org.apache.commons.lang3.StringEscapeUtils
import scala.util.matching.Regex, Regex.Match

case class IndexElement(anchorName: String, containedPrims: Seq[String], html: String)

object PrimIndex {
  private def adjustHtml(rawHtml: String, sourceFileName: String): String = {
    val otherHref = new Regex("""<a href="([^#][^"]*)"""", "link")
    val anchorHref = new Regex("""<a href="#([^"]*)"""", "anchor")

    def replaceHref(m: Match): String = {
      if (m.group("link").startsWith("http:"))
        m.matched
      else
        s"""<a href="../${m.group("link")}""""
    }

    def replaceAnchor(m: Match): String =
      s"""<a href="../${sourceFileName}#${m.group("anchor")}""""

    val s = rawHtml.replaceAllLiterally("""src="images""", """src="../images""")
    val s1 = otherHref.replaceAllIn(s, replaceHref _)
    val s2 = anchorHref.replaceAllIn(s1, replaceAnchor _)
    s2
  }

  def generate(dictFile: File, target: File, templateFile: File, indexFile: File): Unit = {
    val html = IO.read(dictFile)

    val doc = Jsoup.parse(html)

    val entries = doc.getElementsByClass("dict_entry").iterator.asScala.toSeq

    // constants are treated specially
    val primIndex = entries.filterNot(_.id.contains("constants")).map(entry =>
      IndexElement(
        entry.id,
        entry.select("h3 > a").iterator.asScala.map(_.text).toSeq,
        entry.outerHtml)
    )

    val constIndex = entries.filter(_.id.contains("constants")).map(entry =>
      IndexElement(
        entry.id,
        entry.attr("data-constants").split(" ").toSeq,
        entry.outerHtml)
    )

    (primIndex ++ constIndex).foreach { el =>
      val vars = Map[String, Object](
        "html"           -> adjustHtml(el.html, dictFile.getName),
        "containedPrims" -> el.containedPrims.asJava
      )
      Mustache(templateFile, target / s"${el.anchorName}.html", vars)
    }

    val index = (primIndex ++ constIndex).map { el =>
      el.containedPrims.map(p => s"$p ${el.anchorName}.html").mkString("\n")
    }.mkString("\n")

    IO.write(indexFile, index)
  }
}
