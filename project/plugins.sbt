addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.0.10")

libraryDependencies +=
  "de.jflex" % "jflex" % "1.4.3"

libraryDependencies +=
  "classycle" % "classycle" % "1.3.2" from
    "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/classycle-1.3.2.jar"

libraryDependencies +=
  "net.sf.proguard" % "proguard" % "4.8" from
    "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/proguard-4.8.jar"
