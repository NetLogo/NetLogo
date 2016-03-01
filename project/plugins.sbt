scalacOptions += "-deprecation"

resolvers += "Typesafe Public Repo" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.4")

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
  "classycle" % "classycle" % "1.4.2" from
    "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/classycle-1.4.2.jar"

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

addSbtPlugin("org.ensime" % "ensime-sbt" % "0.1.7")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.5")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.2")

// prevents noise from bintray stuff
libraryDependencies +=
  "org.slf4j" % "slf4j-nop" % "1.6.0"

resolvers += Resolver.url(
  "publish-versioned-plugin-releases",
    url("http://dl.bintray.com/content/netlogo/publish-versioned"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("org.nlogo" % "publish-versioned-plugin" % "2.0")

libraryDependencies ++= Seq(
  "com.github.spullara.mustache.java" % "compiler" % "0.9.1",
  "com.github.spullara.mustache.java" % "scala-extensions-2.10" % "0.9.1",
  "org.jsoup"                         % "jsoup" % "1.8.3",
  "org.pegdown"                       % "pegdown" % "1.1.0",
  "org.apache.commons"                % "commons-lang3" % "3.1"
)
