addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.3.2")

addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.1")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")

libraryDependencies +=
  "de.jflex" % "jflex" % "1.4.3"

libraryDependencies +=
  "classycle" % "classycle" % "1.4.1" from
    "http://ccl.northwestern.edu/devel/classycle-1.4.1.jar"

