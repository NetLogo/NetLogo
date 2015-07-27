scalacOptions += "-deprecation"

// so we can use native2ascii on Linux.  use JAVA_HOME not the java.home
// system property because the latter may have "/jre" tacked onto it.
unmanagedJars in Compile <+= (javaHome) map { home =>
  val env = Option(System.getenv("JAVA_HOME")).orElse(
    sys.error(
      "JAVA_HOME not set. run './sbt', not 'sbt'"))
  (home.orElse(env.map(file)).get) / "lib" / "tools.jar"
}
