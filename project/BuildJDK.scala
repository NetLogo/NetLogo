import java.io.File

trait BuildJDK {
  def arch: String
  def version: String
  def javaHome: Option[String]
  def jpackage: File

  def nativesArch: String =
    arch match {
      case "universal" => "universal"
      case "32"        => "i586"
      case _           => "amd64"
    }
}

case class SpecifiedJDK(arch: String, version: String, val jpackage: File, javaHome: Option[String] = None) extends BuildJDK {}

case object PathSpecifiedJDK extends BuildJDK {
  val arch     = "universal"
  val version  = "17"
  val jpackage = new File("jpackage")
  def javaHome = None
}
