import sbt.File

trait SubApplication {
  def name:                String
  def mainClass:           String
  def jvmOptions:          Seq[String]
  def additionalResources: Seq[File]
}

trait NetLogoCoreApp extends SubApplication {
  override def name = "NetLogo"
  override def jvmOptions = Seq[String]()
  override def additionalResources = Seq[File]()
}

trait NetLogoThreeDApp extends SubApplication {
  override def name = "NetLogo 3D"
  override def jvmOptions = Seq("-Dorg.nlogo.is3d=true")
}

object MacNetLogoApp extends NetLogoCoreApp {
  override def mainClass = "org.nlogo.app.MacApplication"
}

object MacNetLogoThreeD extends NetLogoCoreApp with NetLogoThreeDApp {
  override def mainClass = "org.nlogo.app.MacApplication"
}

object WinLinuxNetLogoApp extends NetLogoCoreApp {
  override def mainClass = "org.nlogo.app.App"
}

object WinLinuxThreeD extends NetLogoCoreApp with NetLogoThreeDApp {
  override def mainClass = "org.nlogo.app.App"
}
