import sbt.File

trait SubApplication {
  def name:         String
  def jvmOptions:   Seq[String]
  def jvmArguments: Seq[String]
}

object NetLogoCoreApp extends SubApplication {
  override def name         = "NetLogo"
  override def jvmOptions   = Seq()
  override def jvmArguments = Seq()
}

object NetLogoThreeDApp extends SubApplication {
  override def name         = "NetLogo 3D"
  override def jvmOptions   = Seq("-Dorg.nlogo.is3d=true")
  override def jvmArguments = Seq()
}

object NetLogoLoggingApp extends SubApplication {
  override def name         = "NetLogo Logging"
  override def jvmOptions   = Seq()
  override def jvmArguments = Seq("--logging", "netlogo_logging.xml")
}
