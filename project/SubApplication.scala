import sbt._
import sbt.File

trait SubApplication {
  def name:          String
  def shortName:     String = "NetLogo"
  def jarName:       String
  def mainClass:     String = "org.nlogo.app.App$"
  def jvmOptions:    Seq[String]
  def jvmArguments:  Seq[String]
  def jvmProperties(platformName: String): Map[String, String] = Map()
  def additionalArtifacts(config: File): Seq[File] = Seq()
  def configurationVariables(platformName: String): Map[String, AnyRef] = {
    import scala.collection.JavaConverters._
    Map(
      "appName"        -> name,
      "appShortName"   -> shortName,
      "jvmOptions"     -> (jvmOptions ++ jvmProperties(platformName).map(t => s"-D${t._1}=${t._2}")).asJava,
      "maxMemory"      -> maxMemory,
      "additionalArgs" -> jvmArguments.asJava
      )
  }
  def iconName: String = "NetLogo"
  def maxMemory: String = "1024m"
  def allIcons: Seq[String] = Seq(iconName)
}

object NetLogoCoreApp extends SubApplication {
  override def name          = "NetLogo"
  override def jarName       = "NetLogo"
  override def jvmOptions    = Seq()
  override def jvmArguments  = Seq()
  override def allIcons: Seq[String] = Seq(iconName) :+ "Model"
  override def additionalArtifacts(config: File): Seq[File] =
    Seq(config / "NetLogo Logging" / "netlogo_logging.xml", config / "NetLogo Logging" / "netlogo_logging.dtd")
}

object NetLogoThreeDApp extends SubApplication {
  override def name          = "NetLogo 3D"
  override def jarName       = "NetLogo"
  override def jvmOptions    = Seq("-Dorg.nlogo.is3d=true")
  override def jvmArguments  = Seq()
  override def allIcons: Seq[String] = Seq(iconName) :+ "Model"
}

object NetLogoLoggingApp extends SubApplication {
  override def name          = "NetLogo Logging"
  override def jarName       = "NetLogo"
  override def jvmOptions    = Seq()
  override def jvmArguments  = Seq("--logging", "netlogo_logging.xml")
  override def additionalArtifacts(config: File): Seq[File] =
    Seq(config / "NetLogo Logging" / "netlogo_logging.xml", config / "NetLogo Logging" / "netlogo_logging.dtd")
}

object HubNetClientApp extends SubApplication {
  override def name          = "HubNet Client"
  override def mainClass     = "org.nlogo.hubnet.client.App$"
  override def jarName       = "NetLogo"
  override def shortName     = "HubNet"
  override def jvmOptions    = Seq()
  override def jvmArguments  = Seq()
  override def jvmProperties(platformName: String) =
    if (platformName == "macosx")
      Map("apple.laf.useScreenMenuBar" -> "true")
    else
      super.jvmProperties(platformName)
  override def iconName      = "HubNet Client"
}

object BehaviorsearchApp extends SubApplication {
  override def name          = "Behaviorsearch"
  override def mainClass     = "bsearch.fx.MainGUI"
  override def jarName       = "behaviorsearch.jar"
  override def shortName     = "Behaviorsearch"
  override def jvmOptions    = Seq()
  override def jvmArguments  = Seq()
  override def jvmProperties(platformName: String) =
    platformName match {
      case "macosx" => Map(
        "bsearch.appfolder" -> "$APPDIR/../../behaviorsearch",
        "bsearch.startupfolder" -> "$APPDIR/../../"
      )
      case _ => super.jvmProperties(platformName)
    }
  override def maxMemory     = "1536m"
  override def iconName      = "Behaviorsearch"
}

object DummyApp extends SubApplication {
  override def name          = "Dummy"
  override def jarName       = "NetLogo"
  override def jvmOptions    = Seq()
  override def jvmArguments  = Seq()
}
