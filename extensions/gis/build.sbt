enablePlugins(org.nlogo.build.NetLogoExtension, org.nlogo.build.ExtensionDocumentationPlugin)

name := "gis"
version := "1.6.1"
isSnapshot := true

Compile / javaSource := baseDirectory.value / "src" / "org"
javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path", "-encoding", "us-ascii", "--release", "21")

scalaVersion := "3.7.0"
Test / scalaSource := baseDirectory.value / "src" / "test"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-feature", "-encoding", "us-ascii", "-release", "21")

netLogoVersion      := "7.1.0-internal1-0194cd8"
netLogoClassManager := "org.myworldgis.netlogo.GISExtension"
netLogoTestExtras += (baseDirectory.value / "examples")

libraryDependencies ++= Seq(
  "org.locationtech.jts"       % "jts-core"           % "1.19.0",
  "commons-httpclient"         % "commons-httpclient" % "3.1",
  "commons-logging"            % "commons-logging"    % "1.2",
  "commons-codec"              % "commons-codec"      % "1.10",
  "org.ngs"                    % "ngunits"            % "1.0.0" from "https://s3.amazonaws.com/ccl-artifacts/ngunits-1.0.jar",
  "javax.media"                % "jai_core"           % "1.1.3" from "https://s3.amazonaws.com/ccl-artifacts/jai_core-1.1.3.jar",
  "com.sun.media"              % "jai_codec"          % "1.1.3" from "https://s3.amazonaws.com/ccl-artifacts/jai_codec-1.1.3.jar",
  "org.tinfour"                % "TinfourCore"        % "2.1.7",
  "com.googlecode.json-simple" % "json-simple"        % "1.1.1")
