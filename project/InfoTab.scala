import java.io.File
import sbt._
import Keys._

object InfoTab {

  val infoTab = TaskKey[Seq[File]](
    "info-tab", "builds info tab section of User Manual")

  val infoTabTask =
    infoTab := {
      val streamsValue = streams.value
      val testCpValue = (Test / fullClasspath).value
      val baseDirectoryValue = baseDirectory.value
      val runnerValue = runner.value
      val cache =
        FileFunction.cached(streams.value.cacheDirectory / "infotab",
          inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              IO.createDirectory(baseDirectoryValue / "docs")
              Run.run("org.nlogo.tools.InfoTabDocGenerator",
                testCpValue.map(_.data), Seq(), streamsValue.log)(runnerValue)
              Set(baseDirectoryValue / "docs" / "infotab.html")
          }
          cache(Set(baseDirectory.value / "models" / "Code Examples" / "Info Tab Example.nlogox")).toSeq
    }

}
