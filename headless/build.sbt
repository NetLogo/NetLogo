artifactName := { (_, _, _) => "NetLogoHeadless.jar" }

unmanagedResourceDirectories in Compile <+= baseDirectory { _ / "resources" }

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

sourceGenerators in Compile <+= JFlexRunner.task

resourceGenerators in Compile <+= I18n.resourceGeneratorTask

mainClass in Compile := Some("org.nlogo.headless.Shell")

seq(Testing.settings: _*)

seq(Depend.settings: _*)
