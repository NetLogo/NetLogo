scalacOptions += "-deprecation"

// so we can use native2ascii on Linux.
unmanagedJars in Compile += {
  // use JAVA_HOME not the java.home
  // system property because the latter may have "/jre" tacked onto it.
  val home = javaHome.value.getOrElse(file(System.getenv("JAVA_HOME")))
  home / "lib" / "tools.jar"
}

libraryDependencies +=
  "de.jflex" % "jflex" % "1.4.3"

libraryDependencies +=
  "classycle" % "classycle" % "1.4.1" from
    "http://ccl.northwestern.edu/devel/classycle-1.4.1.jar"

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.3.2")

addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.2")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")
