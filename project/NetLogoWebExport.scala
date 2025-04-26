import java.io.File
import java.nio.file.Files
import org.jsoup.{ Jsoup, nodes }, nodes.Element
import org.jsoup.parser.Tag
import sbt._, Keys.resourceDirectory

object NetLogoWebExport {
  val StandaloneURL = url("https://netlogoweb.org/standalone")
  // it would be better to do this with a JSoup element, but it doesn't like
  // creating custom self-closing tags :P
  val ModelSigil = "______NetLogoModel_______"

  def apply(saveFile: File): Unit = {
    FileActions.download(StandaloneURL, saveFile)
    val parsedHtml = Jsoup.parse(new String(Files.readAllBytes(saveFile.toPath), "UTF-8"))
    val codeElement = parsedHtml.getElementById("nlogo-code")
    codeElement.appendText(ModelSigil)
    codeElement.attributes.put("data-filename", "<NetLogoModelName />")
    Files.write(saveFile.toPath, parsedHtml.outerHtml.replace(ModelSigil, "<NetLogoModel />").getBytes)
  }

  lazy val nlwExportFile =
    settingKey[File]("export template for netlogo web")

  lazy val nlwUpdateExportFile =
    taskKey[Unit]("update netlogo web export file")

  lazy val settings = Seq(
    nlwExportFile := (Compile / resourceDirectory).value / "system" / "net-logo-web.html",
    nlwUpdateExportFile := { apply(nlwExportFile.value) }
  )
}
