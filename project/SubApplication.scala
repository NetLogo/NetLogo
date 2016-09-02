import sbt._
import sbt.File

import scala.collection.JavaConverters._

trait SubApplication {
  def name:          String
  def shortName:     String = "NetLogo"
  def jarName:       String
  def mainClass:     String = "org.nlogo.app.App$"
  def jvmOptions:    Seq[String]
  def jvmArguments:  Seq[String]
  def jvmProperties: Map[String, String] = Map()
  def additionalArtifacts(config: File): Seq[File] = Seq()
  def configurationVariables: Map[String, AnyRef] =
    Map(
      "appName"        -> name,
      "appShortName"   -> shortName,
      "jvmOptions"     -> (jvmOptions ++ jvmProperties.map(t => s"-D${t._1}=${t._2}")).asJava,
      "additionalArgs" -> jvmArguments.asJava
      )
  def iconName: String = "NetLogo"
}

object NetLogoCoreApp extends SubApplication {
  override def name          = "NetLogo"
  override def jarName       = "NetLogo"
  override def jvmOptions    = Seq()
  override def jvmArguments  = Seq()
}

object NetLogoThreeDApp extends SubApplication {
  override def name          = "NetLogo 3D"
  override def jarName       = "NetLogo"
  override def jvmOptions    = Seq("-Dorg.nlogo.is3d=true")
  override def jvmArguments  = Seq()
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
  override def jvmProperties = Map("apple.laf.useScreenMenuBar" -> "true")
  override def iconName      = "HubNet Client"
}

object DummyApp extends SubApplication {
  override def name          = "Dummy"
  override def jarName       = "NetLogo"
  override def jvmOptions    = Seq()
  override def jvmArguments  = Seq()
  override def jvmProperties = Map()
}
