import java.io.File

case class JDK(vendor: String, architecture: String, version: String, path: String) {

  def javaHome: File = new File(path)

  def majorVersion: Int = version.split("\\.")(0).toInt

  def jpackage: File = {
    import sbt._
    val fileExt = if (System.getProperty("os.name").toLowerCase == "windows") ".exe" else ""
    javaHome / "bin" / s"jpackage$fileExt"
  }

  def nativesArch: String =
    architecture match {
      case "universal" => "universal"
      case "32"        => "i586"
      case "64"        => "amd64"
      case _           => throw new Exception(s"Unknown architecture: $architecture")
    }

}
