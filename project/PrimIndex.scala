import sbt._
import org.jsoup.Jsoup
import org.apache.commons.lang3.StringEscapeUtils
import scala.util.matching.Regex, Regex.Match

case class IndexElement(anchorName: String, containedPrims: Seq[String], html: String)

object PrimIndex {
  private def adjustHtml(rawHtml: String, sourceFileName: String): String = {
    val otherHref = new Regex("""<a(\s*class="[^"]+"\s*)?href="([^#][^"]*)"""", "class", "link")
    val anchorHref = new Regex("""<a(\s*class="[^"]+"\s*)?href="#([^"]*)"""", "class", "anchor")

    // We used <base href="../" /> in the template, so the link adjustment
    // is no longer needed.
    // I converted everything below to NOP to avoid changing the original
    // behavior of the code.
    def replaceHref(m: Match): String = {
      if (m.group("link").startsWith("http:") || m.group("link").startsWith("https:"))
        m.matched
      else
        s"""<a${m.group("class")}href="${m.group("link")}""""                        // NOP
    }

    def replaceAnchor(m: Match): String =
      s"""<a${m.group("class")}href="./${sourceFileName}#${m.group("anchor")}""""

    val s = rawHtml.replace("""src="images""", """src="images""")  // NOP
    val s1 = otherHref.replaceAllIn(s, replaceHref _)
    val s2 = anchorHref.replaceAllIn(s1, replaceAnchor _)
    s2
  }

  def generate(dictFile: File, target: File, templateFile: File, indexFile: File, headerFile: File, renderVars: Map[String, Object]): Unit = {
    import scala.collection.JavaConverters._

    val html = IO.read(dictFile)

    val doc = Jsoup.parse(html)

    val entries = doc.getElementsByClass("dict_entry").iterator.asScala.toSeq

    // constants are treated specially
    val primIndex = entries.filterNot(_.id.contains("constants")).map(entry =>
      IndexElement(
        entry.id,
        entry.select("h3 > a").iterator.asScala.map(_.ownText).toSeq,
        entry.outerHtml)
    )

    val constIndex = entries.filter(_.id.contains("constants")).map(entry =>
      IndexElement(
        entry.id,
        entry.attr("data-constants").split(" ").toSeq,
        entry.outerHtml)
    )

    val headerHtml = IO.read(headerFile)

    // [ { key: "primName", value: "primHref" }, ... ]
    val primMap = (primIndex ++ constIndex).flatMap { el =>
      el.containedPrims.map(p => Map("key" -> p, "value" ->  (target / s"${el.anchorName}.html").getName).asJava)
    }


    (primIndex ++ constIndex).foreach { el =>
      val vars = Map[String, Object](
        "html"           -> adjustHtml(el.html, dictFile.getName),
        "containedPrims" -> el.containedPrims.asJava,
        "header"         -> headerHtml,
        "primMap"       -> primMap.asJava,
        "primTitle"     -> el.containedPrims.mkString(", "),
      ) ++ renderVars
      Mustache(templateFile, target / s"${el.anchorName}.html", vars)
    }

    val index = (primIndex ++ constIndex).map { el =>
      el.containedPrims.map(p => s"$p ${el.anchorName}.html").mkString("\n")
    }.mkString("\n")

    IO.write(indexFile, index)
  }
}
