import org.nlogo.build.{ ExtensionDocumentationPlugin, NetLogoExtension }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name       := "Simple R Extension"
version    := "3.1.4"
isSnapshot := true

netLogoClassManager := "org.nlogo.extensions.simpler.SimpleRExtension"
netLogoExtName      := "sr"

lazy val rScriptFiles   = settingKey[Seq[File]]("list of R scripts to include in package and testing")

rScriptFiles := Seq(baseDirectory.value / "src" / "rext.R", baseDirectory.value / "src" / "rlibs.R")

// This version number gets ignored.  The NL version comes from the Language Library.  --Jason B. (8/28/25)
netLogoVersion       := "7.1.0-internal1-0194cd8"
netLogoPackageExtras ++= rScriptFiles.value.map( (f) => (f, None) )
netLogoZipExtras     ++= Seq(baseDirectory.value / "demos", baseDirectory.value / "README.md")

scalaVersion := "3.7.0"

Test / scalaSource    := baseDirectory.value / "test"
Compile / scalaSource := baseDirectory.value / "src" / "main"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings", "-release", "11")

Compile / packageBin / artifactPath := {
  val oldPath = (Compile / packageBin / artifactPath).value.toPath
  val newPath = oldPath.getParent / s"${netLogoExtName.value}.jar"
  newPath.toFile
}

(Test / testOptions) ++= Seq(
  Tests.Setup( () => {
    rScriptFiles.value.foreach( (file) => IO.copyFile(file, baseDirectory.value / ".." / file.getName) )
  })
, Tests.Cleanup( () => {
    rScriptFiles.value.foreach( (file) => IO.delete(baseDirectory.value / ".." / file.getName) )
  })
)

resolvers ++= Seq(
  "netlogo-language-library" at "https://dl.cloudsmith.io/public/netlogo/language-library/maven"
)

libraryDependencies ++= Seq(
  "org.nlogo.languagelibrary" %% "language-library" % "3.3.3-cfbf09b"
)
