import scala.collection.JavaConverters._

trait Launcher {
  def name: String
  def mustachePrefix: String
  def javaOptions: Seq[String]
  def extraProperties: Seq[String]

  def toVariables = {
    // In the properties files the `java-options=` property overwrites, so you don't want
    // to include it if there are no options provided or you'll lose the defaults.
    val jOpts = if (javaOptions.isEmpty) {
      ""
    } else {
      s"java-options=${javaOptions.map((jOpt) => s""""$jOpt"""").mkString(" \\\n  ")}"
    }
    Map(
      "javaOptions"     -> jOpts
    , "extraProperties" -> extraProperties.asJava
    )
  }
}

trait LauncherFamily {
  val netLogo3d: NetLogo3dLauncher
  val hubNetClient: HubNetClientLauncher
  val behaviorsearch: BehaviorsearchLauncher
}

class NetLogo3dLauncher(extraJavaOptions: Seq[String] = Seq(), val extraProperties: Seq[String] = Seq()) extends Launcher {
  def name: String = "NetLogo 3D"
  def mustachePrefix: String = "netlogo-3d-launcher"
  def javaOptions: Seq[String] = Seq(
    "-Xmx1024m"
  , "-Dfile.encoding=UTF-8"
  , "-Dorg.nlogo.is3d=true"
  , "--add-exports=java.base/java.lang=ALL-UNNAMED"
  , "--add-exports=java.desktop/sun.awt=ALL-UNNAMED"
  , "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED"
  ) ++ extraJavaOptions
}

class HubNetClientLauncher(extraJavaOptions: Seq[String] = Seq(), val extraProperties: Seq[String] = Seq()) extends Launcher {
  def name: String = "HubNet Client"
  def mustachePrefix: String = "hubnet-client-launcher"
  def javaOptions: Seq[String] = extraJavaOptions
}

class BehaviorsearchLauncher(extraJavaOptions: Seq[String] = Seq(), val extraProperties: Seq[String] = Seq()) extends Launcher {
  def name: String = "Behaviorsearch"
  def mustachePrefix: String = "behaviorsearch-launcher"
  def javaOptions: Seq[String] = Seq("-Xmx1536m", "-Dfile.encoding=UTF-8") ++ extraJavaOptions
}
