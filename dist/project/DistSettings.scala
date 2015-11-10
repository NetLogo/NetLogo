import sbt._
import Keys.baseDirectory

object DistSettings {
  lazy val netLogoRoot = settingKey[File]("Root directory of NetLogo project")

  // build application jar, resources
  lazy val buildNetLogo = taskKey[Unit]("build NetLogo")

  lazy val settings = Seq(
    buildNetLogo := {
      val netLogoDir = baseDirectory.value.getParentFile

      def netLogoCmd(cmd: String): Unit = {
        val res = Process(Seq("./sbt", cmd), netLogoDir).!
        if (res != 0)
          sys.error("netlogo " + cmd + "failed! Aborting.")
      }

      netLogoCmd("package")
      netLogoCmd("extensions")
      netLogoCmd("model-index")
      netLogoCmd("native-libs")
    }
  )

}
