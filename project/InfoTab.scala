import java.io.File
import sbt._
import Keys._

object InfoTab {

  val info = TaskKey[Seq[File]](
    "infotab", "builds info tab section of User Manual")

  val infoTabTask =
    info <<= ((fullClasspath in Test, baseDirectory, cacheDirectory, runner, streams) map {
      (cp, base, cacheDir, runner, s) =>
        val cache =
          FileFunction.cached(cacheDir / "infotab",
                              inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              Run.run("org.nlogo.tools.InfoTabDocGenerator",
                      cp.map(_.data), Seq(), s.log)(runner)
              Set(base / "docs" / "infotab.html")
          }
        cache(Set(base / "models" / "Code Examples" / "Info Tab Example.nlogo")).toSeq
      }).dependsOn(compile in Test)

}
