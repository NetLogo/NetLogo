import sbt._
import NetLogoPackaging.RunProcess
import java.nio.file.{ Files, Paths }
import java.nio.file.attribute.PosixFilePermission
import java.io.File

object PackageLinuxAggregate {
  // We're given a dummy package with a directory structure like:
  // dummy
  // └── dummy
  //     ├── bin
  //     │   └── dummy
  //     └── lib
  //         ├── app
  //         │   ├── dummy.cfg
  //         │   └── netlogo-6.2.2.jar
  //         ├── dummy.png
  //         ├── libapplauncher.so
  //         └── runtime
  //             ├── conf
  //             ├── legal
  //             ├── lib
  //             └── release
  //
  //  The desire is to create each sub-application, and copy in auxilliary files
  // The resulting structure is
  // └── NetLogo 6.x.x
  //     ├── Behaviorsearch -> bin/Behaviorsearch
  //     ├── HubNet Client  -> bin/HubNet Client
  //     ├── NetLogo        -> bin/NetLogo
  //     ├── NetLogo 3D     -> bin/NetLogo 3D
  //     ├── netlogo-gui.sh
  //     ├── netlogo-headless.sh
  //     ├── NetLogo User Manual.pdf
  //     ├── readme.md
  //     ├── bin
  //     │   ├── Behaviorsearch
  //     │   ├── HubNetClient
  //     │   ├── NetLogo
  //     │   └── NetLogo3D
  //     ├── lib
  //     │   ├── app
  //     │   │   ├── Behaviorsearch.cfg
  //     │   │   ├── HubNetClient.cfg
  //     │   │   ├── NetLogo3D.cfg
  //     │   │   ├── NetLogo.cfg
  //     │   │   ├── behaviorsearch
  //     │   │   ├── docs
  //     │   │   ├── extensions
  //     │   │   ├── models
  //     │   │   ├── natives
  //     │   │   ├── args4j-2.0.12.jar
  //                 ...
  //     │   │   ├── netlogo-6.2.2.jar
  //                 ...
  //     │   │   └── zip4j-2.9.0.jar
  //     │   ├── dummy.png
  //     │   ├── libapplauncher.so
  //     │   └── runtime
  //     │       ├── conf
  //     │       ├── legal
  //     │       ├── lib
  //     │       └── release
  //     ├── Mathematica Link
  //     │   ├── mathematica-link.jar
  //     │   ├── NetLogo.m
  //     │   ├── NetLogo-Mathematica Tutorial.nb
  //     └── └── NetLogo-Mathematica Tutorial.pdf
  //
  // note: this shares a lot of code with the windows aggregate packager.
  // There may be an opportunity for abstraction.
  private def configureSubApplication(sharedAppRoot: File, app: SubApplication, common: CommonConfiguration, variables: Map[String, AnyRef]): Unit = {
    val allVariables =
      variables ++ app.configurationVariables("linux") +
      ("mainClass"      -> app.mainClass) +
      ("mainClassSlash" -> app.mainClass.replaceAllLiterally(".", "/").replaceAllLiterally("$", "")) +
      ("appIdentifier"  -> app.mainClass.split("\\.").init.mkString(".")) +
      ("classpathJars"  ->
        common.classpath
          .map(_.getName)
          .sorted
          .mkString(File.pathSeparator))

    Mustache(common.configRoot / "shared" / "linux" / "NetLogo.cfg.mustache",
      sharedAppRoot / "lib" / "app" / (app.name.replaceAllLiterally(" ", "") + ".cfg"), allVariables)

    app.additionalArtifacts(common.configRoot).foreach { f =>
      FileActions.copyFile(f, sharedAppRoot / "lib" / "app" / f.getName)
    }

  }

  def apply(
    aggregateTarget:        File,
    commonConfig:           CommonConfiguration,
    stubApplicationAndName: (File, String),
    subApplications:        Seq[SubApplication],
    variables:              Map[String, String]): File = {
      import commonConfig.{ jdk, webDirectory }

      val version = variables("version")
      val aggregateLinuxDir = aggregateTarget / s"NetLogo $version"
      IO.delete(aggregateLinuxDir)
      IO.createDirectory(aggregateLinuxDir)
      JavaPackager.copyLinuxStubApplications(
        stubApplicationAndName._1, stubApplicationAndName._2,
        aggregateLinuxDir, subApplications.map(_.name))
      // Create symbolic links in main NetLogo app directory to subApplications
      // in the 'bin' directory
      subApplications.foreach { app =>
      FileActions.createRelativeSoftLink( Paths.get(aggregateLinuxDir.toString, "/", app.name),
        Paths.get(aggregateLinuxDir.toString, "/", "bin", "/", app.name))
      }
      val sharedJars = aggregateLinuxDir / "lib" / "app"
      commonConfig.bundledDirs.foreach { d =>
        d.fileMappings.foreach {
          case (f, p) =>
            val targetFile = sharedJars / p
            if (! targetFile.getParentFile.isDirectory)
              FileActions.createDirectories(targetFile.getParentFile)
            FileActions.copyFile(f, sharedJars / p)
        }
      }

      commonConfig.classpath.foreach { jar =>
        FileActions.copyFile(jar, sharedJars / jar.getName)
      }
      commonConfig.rootFiles.foreach { f =>
        FileActions.copyAny(f, aggregateLinuxDir / f.getName)
      }
      // configure each sub application
      subApplications.foreach { app =>
        configureSubApplication(aggregateLinuxDir, app, commonConfig, variables)
      }

      val headlessClasspath =
        ("classpathJars"  ->
          commonConfig.classpath
            .map(jar => "lib/app/" + jar.getName)
            .sorted
            .mkString(File.pathSeparator))

      val permissions = {
        import PosixFilePermission._
        import scala.collection.JavaConverters._
        Set(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE).asJava
      }

      val headlessFile = aggregateLinuxDir / "netlogo-headless.sh"
      Mustache(commonConfig.configRoot / "shared" / "linux" / "netlogo-headless.sh.mustache",
        headlessFile, variables + headlessClasspath + ("mainClass" -> "org.nlogo.headless.Main"))
      Files.setPosixFilePermissions(headlessFile.toPath, permissions)
      headlessFile.setExecutable(true)

      val guiFile = aggregateLinuxDir / "netlogo-gui.sh"
      Mustache(commonConfig.configRoot / "shared" / "linux" / "netlogo-headless.sh.mustache",
        guiFile, variables + headlessClasspath + ("mainClass" -> "org.nlogo.app.App"))
      Files.setPosixFilePermissions(guiFile.toPath, permissions)
      guiFile.setExecutable(true)

      val archiveName = s"NetLogo-$version-${jdk.arch}.tgz"
      val tarBuildDir = aggregateLinuxDir.getParentFile
      IO.delete(tarBuildDir / archiveName)
      RunProcess(Seq("tar", "-zcf", archiveName, aggregateLinuxDir.getName), tarBuildDir, "tar linux aggregate")
      FileActions.createDirectory(webDirectory)
      val archiveFile = webDirectory / archiveName
      IO.delete(webDirectory / archiveName)

      FileActions.moveFile(tarBuildDir / archiveName, webDirectory / archiveName)

      webDirectory / archiveName
  }
}
