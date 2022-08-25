import java.io.File

trait BuildJDK {
  def arch: String
  def version: String
  def javaHome: Option[String]
  def jpackage: File
}

case class SpecifiedJDK(arch: String, version: String, val jpackage: File, javaHome: Option[String] = None) extends BuildJDK {}

case object PathSpecifiedJDK extends BuildJDK {
  val arch     = "universal"
  val version  = "17"
  val jpackage = new File("jpackage")
  def javaHome = None
}
