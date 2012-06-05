// until the plugin is explicitly published against 0.11.3, we need
// some extra junk here to ask for the 0.11.2 version - ST 5/7/12
libraryDependencies +=
  Defaults.sbtPluginExtra(
    "org.ensime" % "ensime-sbt-cmd" % "0.0.10",
    "0.11.2",
    "2.9.1")

libraryDependencies +=
  "de.jflex" % "jflex" % "1.4.3"

libraryDependencies +=
  "classycle" % "classycle" % "1.3.2" from
    "http://ccl.northwestern.edu/devel/classycle-1.3.2.jar"
