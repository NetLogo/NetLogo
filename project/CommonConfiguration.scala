import java.io.File

case class CommonConfiguration(
  mainJar:       File,
  launcherClass: String,
  bundledDirs:   Seq[BundledDirectory],
  classpath:     Seq[File], // jars
  runtimeFiles:  Seq[BundledDirectory], // natives
  icons:         Seq[File], // icons
  rootFiles:     Seq[File], // manual, readme, etc.
  configRoot:    File,
  version:       String,
  jdk:           JDK,
  webDirectory:  File)
