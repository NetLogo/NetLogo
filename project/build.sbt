lazy val root = project.in(file("."))
  .addSbtFiles(Option(file("tasks").listFiles).map(_.toSeq).getOrElse(Seq()): _*)
  .settings(
    scalacOptions += "-deprecation",
    // so we can use native2ascii on Linux.  use JAVA_HOME not the java.home
    // system property because the latter may have "/jre" tacked onto it.
    unmanagedJars in Compile <+= (javaHome) map { home =>
      val jhome: File =
        if ((home == null || home.isEmpty) &&
          System.getProperty("os.name").toLowerCase.contains("windows"))
        file("""C:\Program Files\Java\jdk1.6.0_45""")
        else if ((home == null || home.isEmpty) &&
          System.getProperty("java.home") != null) {
            val javaDotHome = file(System.getProperty("java.home"))
            if (javaDotHome.getName == "jre")
              javaDotHome.getParentFile
            else
              javaDotHome
          } else
            home.get
          jhome / "lib" / "tools.jar"
    }
  )
