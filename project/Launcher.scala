import scala.collection.JavaConverters._

trait Launcher {
  def id: String
  def name: String = id
  def appName =
    s"$name $version"
  def description: String = appName
  def icon: Option[String]
  def version: String
  def mustachePrefix: String
  def javaOptions: Seq[String]
  def extraProperties: Seq[String]
  def mainJar: String
  def mainClass: String

  def toVariables = {
    // In the properties files the `java-options=` property overwrites, so you don't want
    // to include it if there are no options provided or you'll lose the defaults.
    val jOpts = if (javaOptions.isEmpty) {
      ""
    } else {
      s"java-options=${javaOptions.map((jOpt) => s""""$jOpt"""").mkString(" \\\n  ")}"
    }
    val iconString = icon.map( (i: String) => s"icon=$i" ).getOrElse("")
    Map(
      "javaOptions"     -> jOpts
    , "extraProperties" -> extraProperties.asJava
    , "mainJar"         -> mainJar
    , "mainClass"       -> mainClass
    , "icon"            -> iconString
    )
  }
}

object Launcher {
  val defaultJavaOptions: Seq[String] = Seq(
    "-XX:MaxRAMPercentage=50"
  , "-Dfile.encoding=UTF-8"
  , "--add-exports=java.base/java.lang=ALL-UNNAMED"
  , "--add-exports=java.desktop/sun.awt=ALL-UNNAMED"
  , "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED"
  )
}

// Technically Windows and Linux don't create a NetLogo "launcher" because it's the primary output of the `jpackage`
// call, but for convenience we use it to bundle all the info in one spot so it's easier to share with macOS.  -Jeremy B
// September 2022
class NetLogoLauncher(
  val version: String
, val icon: Option[String] = None
, extraJavaOptions: Seq[String] = Seq()
, val extraProperties: Seq[String] = Seq()
, customMainJar: Option[String] = None
, customMainClass: Option[String] = None
) extends Launcher {
  def id: String = "NetLogo"
  def mustachePrefix: String = "n/a"
  def javaOptions: Seq[String] = Launcher.defaultJavaOptions ++ extraJavaOptions
  def mainJar: String = customMainJar.getOrElse(s"netlogo-$version.jar")
  def mainClass: String = customMainClass.getOrElse("org.nlogo.app.App")
}

class NetLogo3dLauncher(
  val version: String
, val icon: Option[String] = None
, extraJavaOptions: Seq[String] = Seq()
, val extraProperties: Seq[String] = Seq()
, customMainJar: Option[String] = None
, customMainClass: Option[String] = None
) extends Launcher {
  def id: String = "NetLogo 3D"
  def mustachePrefix: String = "netlogo-3d-launcher"
  def javaOptions: Seq[String] = Launcher.defaultJavaOptions ++ Seq("-Dorg.nlogo.is3d=true") ++ extraJavaOptions
  def mainJar: String = customMainJar.getOrElse(s"netlogo-$version.jar")
  def mainClass: String = customMainClass.getOrElse("org.nlogo.app.App")
}

class HubNetClientLauncher(
  val version: String
, val icon: Option[String] = None
, extraJavaOptions: Seq[String] = Seq()
, val extraProperties: Seq[String] = Seq()
, customMainJar: Option[String] = None
, customMainClass: Option[String] = None
) extends Launcher {
  def id: String = "HubNet Client"
  def mustachePrefix: String = "hubnet-client-launcher"
  def javaOptions: Seq[String] = Launcher.defaultJavaOptions ++ extraJavaOptions
  def mainJar: String = customMainJar.getOrElse(s"netlogo-$version.jar")
  def mainClass: String = customMainClass.getOrElse("org.nlogo.hubnet.client.App")
}

class BehaviorsearchLauncher(
  val version: String
, val icon: Option[String] = None
, extraJavaOptions: Seq[String] = Seq()
, val extraProperties: Seq[String] = Seq()
, customMainJar: Option[String] = None
, customMainClass: Option[String] = None
) extends Launcher {
  def id: String = "Behaviorsearch"
  def mustachePrefix: String = "behaviorsearch-launcher"
  def javaOptions: Seq[String] = Seq(
    // Behaviorsearch has issues with the post Java 8 GCs, particularly on 32-bit systems.
    // -Jeremy B September 2022
    "-XX:MaxRAMPercentage=50"
  , "-XX:+UseParallelGC"
  , "-Dfile.encoding=UTF-8"
  ) ++ extraJavaOptions
  def mainJar: String = customMainJar.getOrElse(s"behaviorsearch.jar")
  def mainClass: String = customMainClass.getOrElse("bsearch.fx.MainGUIEntry")
}
