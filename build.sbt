import org.nlogo.build.{ ExtensionDocumentationPlugin, NetLogoExtension }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name       := "nw"
version    := "4.2.1"
isSnapshot := true

netLogoExtName      := "nw"
netLogoClassManager := "org.nlogo.extensions.nw.NetworkExtension"
netLogoVersion      := "7.1.0-internal1-0194cd8"
netLogoTestExtras   += baseDirectory.value / "test"

scalaVersion := "3.7.0"
Compile / scalaSource := baseDirectory.value / "src" / "main"
Test / scalaSource    := baseDirectory.value / "src" / "test"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-feature", "-encoding", "us-ascii", "-release", "21")

resolvers ++= Seq(
  "gephi-thirdparty" at "https://raw.github.com/gephi/gephi/mvn-thirdparty-repo/"
)

libraryDependencies ++= Seq(
  "net.sf.jgrapht"              % "jgrapht"             % "0.8.3",
  "net.sourceforge.collections" % "collections-generic" % "4.01",
  "colt"                        % "colt"                % "1.2.0",
  "net.sf.jung"                 % "jung-algorithms"     % "2.0.1",
  "net.sf.jung"                 % "jung-api"            % "2.0.1",
  "net.sf.jung"                 % "jung-graph-impl"     % "2.0.1",
  "net.sf.jung"                 % "jung-io"             % "2.0.1",
  "org.gephi"                   % "gephi-toolkit"       % "0.9.7"
    exclude("org.gephi", "db-drivers")
    exclude("org.gephi", "algorithms-plugin")
    exclude("org.gephi", "preview-plugin")
    exclude("org.gephi", "mostrecentfiles-api")
    exclude("org.gephi", "io-exporter-preview")
    exclude("org.gephi", "appearance-plugin")
    exclude("org.gephi", "visualization-api")
    exclude("com.itextpdf", "itextpdf")
    exclude("net.sf.trove4j", "trove4j")
    exclude("org.apache.poi", "poi-ooxml")
    exclude("org.jfree", "jfreechart")
    exclude("org.netbeans.modules", "org-netbeans-core")
)
