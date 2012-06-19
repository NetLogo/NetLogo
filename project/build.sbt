scalacOptions += "-deprecation"

// so we can use native2ascii on Linux.  use JAVA_HOME not the java.home
// system property because the latter may have "/jre" tacked onto it.
unmanagedJars in Compile <+= (javaHome) map { home =>
  home.getOrElse(file(System.getenv("JAVA_HOME"))) / "lib" / "tools.jar" }
