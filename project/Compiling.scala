import sbt._
import Keys._

object Compiling {

  private val java5 = TaskKey[Unit]("java5")

  val settings = Seq(
    java5 <<= (baseDirectory, streams) map { (base, s) =>
      val classes = base / "dist" / "java5" / "classes.jar"
      if(!classes.exists) {
        val url = new java.net.URL("http://ccl.northwestern.edu/devel/java5-classes.jar")
        s.log.info("downloading " + url)
        IO.download(url, classes)
      }
    },
    compile in Compile <<= (compile in Compile).dependsOn(java5)
  )

}

