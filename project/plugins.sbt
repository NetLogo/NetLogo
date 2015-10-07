resolvers += "Typesafe Public Repo" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("org.ensime" % "ensime-sbt" % "0.1.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.4")

libraryDependencies +=
  "de.jflex" % "jflex" % "1.4.3"

libraryDependencies +=
  "classycle" % "classycle" % "1.3.2" from
    "http://ccl-artifacts.s3-website-us-east-1.amazonaws.com/classycle-1.3.2.jar"
