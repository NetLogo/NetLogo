import java.io.File

trait BuildJDK {
  def arch: String
  def version: String
  def javaHome: Option[String]
  def javapackager: String
}

case class SpecifiedJDK(arch: String, version: String, packagerFile: File, javaHome: Option[String] = None) extends BuildJDK {
  def javapackager = packagerFile.getAbsolutePath
}

case object PathSpecifiedJDK extends BuildJDK {
  val arch         = "universal"
  val version      = "1.8"
  val javapackager = "javapackager"
  def javaHome     = None
}
