import sbt._
import Keys.baseDirectory

object DistSettings {
  lazy val netLogoRoot = settingKey[File]("Root directory of NetLogo project")

  // build application jar, resources
  lazy val buildNetLogo = taskKey[Unit]("build NetLogo")

  lazy val modelCrossReference = taskKey[Unit]("add model cross references")


  lazy val settings = Seq(
    buildNetLogo := {
      def netLogoCmd(cmd: String): Unit = {
        val res = Process(Seq("./sbt", cmd), netLogoRoot.value).!
        if (res != 0)
          sys.error("netlogo " + cmd + "failed! Aborting.")
      }

      netLogoCmd("package")
      netLogoCmd("extensions")
      netLogoCmd("model-index")
      netLogoCmd("native-libs")
    },
    buildNetLogo <<= buildNetLogo dependsOn modelCrossReference,
    modelCrossReference := {
      ModelCrossReference(netLogoRoot.value)
    }
  )
}
