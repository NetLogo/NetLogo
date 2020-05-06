scalacOptions += "-deprecation"

resolvers += "Typesafe Public Repo" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

// addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

// so we can use native2ascii on Linux.
unmanagedJars in Compile += {
  // prefer JAVA_HOME to java.home
  val home =
    (javaHome.value orElse
      Option(System.getenv("JAVA_HOME")).map(p => file(p)) orElse
      Option(System.getProperty("java.home")).map(_.stripSuffix("/jre")).map(p => file(p))).getOrElse(
        sys.error("unable to find java home"))
  home / "lib" / "tools.jar"
}

libraryDependencies +=
  "de.jflex" % "jflex" % "1.4.3"

libraryDependencies +=
  "classycle" % "classycle" % "1.4.2" from
    "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/classycle-1.4.2.jar"

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.32")

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.3")

// prevents noise from bintray stuff
libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.0"

resolvers += Resolver.url(
  "publish-versioned-plugin-releases",
    url("http://dl.bintray.com/content/netlogo/publish-versioned"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("org.nlogo" % "publish-versioned-plugin" % "2.1")

resolvers += Resolver.url(
  "NetLogo-JVM",
  url("http://dl.bintray.com/content/netlogo/NetLogo-JVM"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("org.nlogo" % "netlogo-extension-documentation" % "0.8.0")

libraryDependencies ++= Seq(
  "com.github.spullara.mustache.java" % "compiler" % "0.9.5",
  "com.github.spullara.mustache.java" % "scala-extensions-2.10" % "0.9.5",
  "org.jsoup"                         % "jsoup" % "1.10.3",
  "org.apache.commons"                % "commons-lang3" % "3.1",
  "commons-io"                        % "commons-io"    % "2.6"
)

{
  val flexmarkVersion = "0.20.0"
  libraryDependencies ++= Seq(
    "com.vladsch.flexmark" % "flexmark" % flexmarkVersion,
    "com.vladsch.flexmark" % "flexmark-ext-anchorlink" % flexmarkVersion,
    "com.vladsch.flexmark" % "flexmark-ext-autolink" % flexmarkVersion,
    "com.vladsch.flexmark" % "flexmark-ext-aside" % flexmarkVersion,
    "com.vladsch.flexmark" % "flexmark-ext-escaped-character" % flexmarkVersion,
    "com.vladsch.flexmark" % "flexmark-ext-tables" % flexmarkVersion,
    "com.vladsch.flexmark" % "flexmark-ext-toc" % flexmarkVersion,
    "com.vladsch.flexmark" % "flexmark-ext-typographic" % flexmarkVersion,
    "com.vladsch.flexmark" % "flexmark-ext-wikilink" % flexmarkVersion,
    "com.vladsch.flexmark" % "flexmark-util" % flexmarkVersion
  )
}

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0")
