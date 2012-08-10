artifactName := { (_, _, _) => "NetLogoHeadless.jar" }

unmanagedResourceDirectories in Compile <+= baseDirectory { _ / "resources" }

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

sourceGenerators in Compile <+= Autogen.lexersGeneratorTask

resourceGenerators in Compile <+= I18n.resourceGeneratorTask

mainClass in (Compile, run) := Some("org.nlogo.headless.Shell")

mainClass in (Compile, packageBin) := Some("org.nlogo.headless.Shell")
