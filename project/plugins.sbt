scalacOptions += "-deprecation"

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

resolvers ++= Seq(
  "netlogo-publish-versioned" at "https://dl.cloudsmith.io/public/netlogo/publish-versioned/maven/"
, "netlogo-extension-documentation" at "https://dl.cloudsmith.io/public/netlogo/netlogo-extension-documentation/maven/"
)

addSbtPlugin("org.scalastyle"     %% "scalastyle-sbt-plugin"           % "1.0.0")
addSbtPlugin("org.portable-scala" %  "sbt-scalajs-crossproject"        % "1.2.0")
addSbtPlugin("org.scala-js"       %  "sbt-scalajs"                     % "1.11.0")
addSbtPlugin("org.nlogo"          %  "publish-versioned-plugin"        % "3.0.0")
addSbtPlugin("org.nlogo"          %  "netlogo-extension-documentation" % "0.8.3")
addSbtPlugin("com.timushev.sbt"   %  "sbt-updates"                     % "0.6.4")

libraryDependencies ++= Seq(
  "com.github.spullara.mustache.java" % "compiler"              % "0.9.5"
, "de.jflex"                          % "jflex"                 % "1.8.2"
, "classycle"                         % "classycle"             % "1.4.2" from
  "https://s3.amazonaws.com/ccl-artifacts/classycle-1.4.2.jar"
, "com.github.spullara.mustache.java" % "scala-extensions-2.10" % "0.9.5"
, "org.jsoup"                         % "jsoup"                 % "1.15.3"
, "org.apache.commons"                % "commons-lang3"         % "3.12.0"
, "commons-io"                        % "commons-io"            % "2.11.0"
// prevents noise from bintray stuff
, "org.slf4j"                         % "slf4j-nop"             % "1.7.36"
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
