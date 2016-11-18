import java.io.File
import sbt._
import Keys._

object InfoTab {

  val infoTab = TaskKey[Seq[File]](
    "info-tab", "builds info tab section of User Manual")

  val infoTabTask =
    infoTab := {
      val cache =
        FileFunction.cached(streams.value.cacheDirectory / "infotab",
          inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              IO.createDirectory(baseDirectory.value / "docs")
              Run.run("org.nlogo.tools.InfoTabDocGenerator",
                (fullClasspath in Test).value.map(_.data), Seq(), streams.value.log)(runner.value)
              Set(baseDirectory.value / "docs" / "infotab.html")
          }
          cache(Set(baseDirectory.value / "models" / "Code Examples" / "Info Tab Example.nlogo")).toSeq
    }

}
