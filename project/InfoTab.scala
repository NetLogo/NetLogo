import java.io.File
import sbt._
import Keys._

object InfoTab {

  val infoTab = TaskKey[Seq[File]](
    "info-tab", "builds info tab section of User Manual")

  val infoTabTask =
    infoTab <<= (fullClasspath in Test, baseDirectory, runner, streams) map {
      (cp, base, runner, s) =>
        val cache =
          FileFunction.cached(s.cacheDirectory / "infotab",
                              inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              Run.run("org.nlogo.tools.InfoTabDocGenerator",
                      cp.map(_.data), Seq(), s.log)(runner)
              Set(base / "docs" / "infotab.html")
          }
        cache(Set(base / "models" / "Code Examples" / "Info Tab Example.nlogo")).toSeq
      }

}
