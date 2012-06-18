import sbt._

trait Fork extends DefaultProject {
  
  // The way we get the is3d flag to work could perhaps be improved upon.  Originally we did it
  // the "basic" way shown at code.google.com/p/simple-build-tool/wiki/Forking but it didn't work
  // for running the 3D app because the JVM options were being evaluated too early.  So I fumbled
  // around and produced the below which seems to work, but I don't really understand everything
  // about this - ST 1/27/11

  def forkConfiguration = new ForkScalaRun {
    override def scalaJars = {
      val si = buildScalaInstance
      si.libraryJar :: si.compilerJar :: Nil
    }
    override def runJVMOptions = Seq(
      "-XX:-OmitStackTraceInFastThrow",  // https://github.com/NetLogo/NetLogo/issues/104
      "-XX:MaxPermSize=128m",
      "-Xmx1024m",
      "-Dfile.encoding=UTF-8",
      "-Djava.ext.dirs=",
      "-Dapple.awt.graphics.UseQuartz=true",
      // these will pick up the default language and country
      // from the JVM or OS, but they can be overridden.
      // See Internationalization.scala in this directory.
      "-Duser.language=" + System.getProperty("user.language"),
      "-Duser.country=" + System.getProperty("user.country")) ++
    (if(System.getProperty("os.name").startsWith("Mac"))
      Seq("-Xdock:name=NetLogo")
     else Seq()) ++
    (if(System.getProperty("org.nlogo.is3d") == "true")
      Seq("-Dorg.nlogo.is3d=true")
     else Seq()) ++
    (if(System.getProperty("org.nlogo.noGenerator") == "true")
      Seq("-Dorg.nlogo.noGenerator=true")
     else Seq())
  }

}
