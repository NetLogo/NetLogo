scalaVersion := "2.12.20"

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++=
  Seq( "io.circe" %% "circe-yaml"    % "0.14.2"
     , "io.circe" %% "circe-generic" % "0.14.13"
     )
