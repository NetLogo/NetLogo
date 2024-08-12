import sbt._
import sbt.io.Using
import sbt.util.Logger

import java.io.File
import java.nio.file.{ Files, Path => JPath, Paths }
import java.io.IOException
import java.util.jar.Manifest

import NetLogoPackaging.RunProcess

object PackageMacAggregate {
  val CodesigningIdentity = "Developer ID Application: Northwestern University (E74ZKF37E6)"

  def signJarLibs(jarFile: File, options: Seq[String], libsToSign: Seq[String]): Unit = {
    val tmpDir = IO.createTemporaryDirectory
    println(tmpDir)
    IO.unzip(jarFile, tmpDir)

    val libPaths = libsToSign.map( (libToSign) => (tmpDir / libToSign).toString )
    runCodeSign(options, libPaths, "jar libraries")

    val manifest = Using.fileInputStream(tmpDir / "META-INF" / "MANIFEST.MF") { is =>
      new Manifest(is)
    }
    IO.delete(tmpDir / "META-INF")
    IO.jar(Path.allSubpaths(tmpDir), jarFile, manifest, None)

    IO.delete(tmpDir)
  }

  def runCodeSign(options: Seq[String], paths: Seq[String], taskName: String, workingDirectory: Option[File] = None): Unit = {
    RunProcess(Seq("codesign", "-v", "--force", "--sign", CodesigningIdentity) ++ options ++ paths, workingDirectory, s"codesign of $taskName")
  }

  def createBundleDir(log: Logger, version: String, destDir: File, configDir: File, launchers: Seq[Launcher]): File = {
    val buildName = s"NetLogo-$version"

    val bundleDir     = destDir / s"NetLogo $version"
    val bundleLibsDir = bundleDir / "app"
    IO.createDirectory(bundleDir)
    IO.createDirectory(bundleLibsDir)

    val plistConfig = Map(
      "NetLogo" -> Map(
        "appName"             -> s"NetLogo $version"
      , "bundleIdentifier"    -> "org.nlogo.NetLogo"
      , "bundleName"          -> "NetLogo"
      , "bundleSignature"     -> "nLo1"
      , "fileAssociation"     -> "nlogo"
      , "fileAssociationIcon" -> "Model.icns"
      , "iconFile"            -> s"NetLogo $version.icns"
      , "packageID"           -> "APPLnLo1"
      , "version"             -> version
     )
    , "NetLogo 3D" -> Map(
        "appName"             -> s"NetLogo 3D $version"
      , "bundleIdentifier"    -> "org.nlogo.NetLogo3D"
      , "bundleName"          -> "NetLogo"
      , "bundleSignature"     -> "nLo1"
      , "fileAssociation"     -> "nlogo3d"
      , "fileAssociationIcon" -> "Model.icns"
      , "iconFile"            -> s"NetLogo 3D $version.icns"
      , "packageID"           -> "APPLnLo1"
      , "version"             -> version
     )
    , "HubNet Client" -> Map(
        "appName"             -> s"HubNet Client $version"
      , "bundleIdentifier" -> "org.nlogo.HubNetClient"
      , "bundleName"       -> "HubNet Client"
      , "bundleSignature"  -> "????"
      , "iconFile"         -> s"HubNet Client $version.icns"
      , "packageID"        -> "APPL????"
      , "version"          -> version
     )
    , "Behaviorsearch" -> Map(
        "appName"             -> s"Behaviorsearch $version"
      , "bundleIdentifier"    -> "org.nlogo.Behaviorsearch"
      , "bundleName"          -> "Behaviorsearch"
      , "bundleSignature"     -> "????"
      , "fileAssociation"     -> "bsearch"
      , "fileAssociationIcon" -> s"Behaviorsearch $version.icns"
      , "iconFile"            -> s"Behaviorsearch $version.icns"
      , "packageID"           -> "APPL????"
      , "version"             -> version
      )
    )

    var first = true
    launchers.foreach( (launcher) => {
      log.info(s"Cleaning up ${launcher.name}.app")
      val appDir     = destDir / s"${launcher.name}.app"
      val runtimeDir = appDir / "Contents" / "runtime"
      val appLibsDir = appDir / "Contents" / "app"
      val jars       = appLibsDir.listFiles.filter( (f) => f.getName().endsWith(".jar") )
      if (first) {
        log.info("  First app we're cleaning, copying Java runtime and application jars.")
        FileActions.copyDirectory(runtimeDir, bundleDir / runtimeDir.getName)
        jars.foreach( (jar) => FileActions.copyFile(jar, bundleLibsDir / jar.getName) )
        first = false
      }
      FileActions.remove(runtimeDir)
      jars.foreach(FileActions.remove)

      // Each app has its own `$APPDIR` that we have to replace with the relative path to get to the root of the NetLogo
      // installation folder do they can share the needed jars.  But we also want to be able to specify the plain old
      // "root directory" without `app/` appended on for the extra bundled directories, so that's why we also use the
      // `{{{ROOTDIR}}}` placeholder.
      // -Jeremy B September 2022
      log.info("  Reworking the config file with the new paths.")
      val configFile = appLibsDir / s"${launcher.name}.cfg"
      val config1 = Files.readString(configFile.toPath)
      val config2 = config1.replace("$APPDIR/", "$APPDIR/../../../app/")
      val config3 = config2.replace("{{{ROOTDIR}}}", "$APPDIR/../../..")
      val splitPoint = "[Application]\n".length
      val config4 = s"${config3.substring(0, splitPoint)}app.runtime=$$APPDIR/../../../runtime/\n${config3.substring(splitPoint)}"
      Files.writeString(configFile.toPath, config4)

      log.info("  Generating Info.plist and PkgInfo files.")
      val variables = plistConfig.getOrElse(launcher.id, throw new Exception(s"No variables for this launcher? ${launcher.id} : $plistConfig"))
      Mustache(configDir / "macosx" / "Info.plist.mustache", appDir / "Contents" / "Info.plist", variables)
      Mustache(configDir / "macosx" / "PkgInfo.mustache",    appDir / "Contents" / "PkgInfo", variables)

      log.info("  Move to the bundle directory.")
      FileActions.copyDirectory(appDir, bundleDir / appDir.getName)
      FileActions.remove(appDir)
    })

    bundleDir
  }

  def apply(
    log: sbt.util.Logger
  , version: String
  , destDir: File
  , bundleDir: File
  , configDir: File
  , webDir: File
  , launchers: Seq[Launcher]
  ): File = {
    val buildName = s"NetLogo $version"
    (bundleDir / "runtime" / "Contents" / "Home" / "lib" / "jspawnhelper").setExecutable(true)

    log.info("Creating NetLogo_Console sym link")
    FileActions.createRelativeSoftLink(bundleDir / "NetLogo_Console", bundleDir / s"$buildName.app" / "Contents" / "MacOS" / buildName)

    log.info("Gathering files to sign")
    val appNames = launchers.map(_.id)
    val apps     = launchers.map( (l) => (bundleDir / s"${l.name}.app") )

    val filesToBeSigned =
      (apps :+ (bundleDir / "natives") :+ (bundleDir / "runtime" / "Contents" / "Home" / "lib")).flatMap( a => FileActions.enumeratePaths(a.toPath).filterNot( p => Files.isDirectory(p) ) )

    val filesToMakeExecutable =
      filesToBeSigned.filter( p => p.getFileName.toString.endsWith(".dylib") || p.getFileName.toString.endsWith(".jnilib") )

    filesToMakeExecutable.foreach(_.toFile.setExecutable(true))

    // ensure applications are signed *after* their libraries and resources
    val orderedFilesToBeSigned =
      filesToBeSigned.sortBy {
        case p if appNames.contains(p.getFileName.toString) => 2
        case _ => 1
      }

    val dmgName = s"NetLogo-$version.dmg"

    // Apple requires a "hardened" runtime for notarization -Jeremy B July 2020
    val appSigningOptions = Seq("--options", "runtime", "--entitlements", (configDir / "macosx" / "entitlements.xml").toString)

    // In theory instead of hardcoding these we could search all jars for any libs that
    // aren't signed or are signed incorrectly.  But Apple will do that search for us when
    // we submit for notarization and these libraries don't change that often.  -Jeremy B
    // July 2020

    // This map is officially bonkers enough that I'd consider switching to an automated
    // solution.  Although most of the libs are from the Vid extension and could go away
    // if we ever get onto a single camera capture library for it.  -Jeremy B August 2022
    val jarLibsToSign = Map(
    ("extensions/.bundled/gogo/hid4java-develop-SNAPSHOT.jar", Seq("darwin-x86-64/libhidapi.dylib", "darwin-aarch64/libhidapi.dylib"))
    , ("extensions/.bundled/vid/core-video-capture-1.4-20220209.101851-153.jar", Seq("org/openimaj/video/capture/nativelib/darwin_universal/libOpenIMAJGrabber.dylib"))
    , ("extensions/.bundled/vid/javacpp-1.5.7-macosx-arm64.jar", Seq("org/bytedeco/javacpp/macosx-arm64/libjnijavacpp.dylib"))
    , ("extensions/.bundled/vid/javacpp-1.5.7-macosx-x86_64.jar", Seq("org/bytedeco/javacpp/macosx-x86_64/libjnijavacpp.dylib"))
    , ("extensions/.bundled/vid/openblas-0.3.19-1.5.7-macosx-arm64.jar", Seq("org/bytedeco/openblas/macosx-arm64/libjniopenblas_nolapack.dylib", "org/bytedeco/openblas/macosx-arm64/libjniopenblas.dylib", "org/bytedeco/openblas/macosx-arm64/libopenblas.0.dylib"))
    , ("extensions/.bundled/vid/openblas-0.3.19-1.5.7-macosx-x86_64.jar", Seq("org/bytedeco/openblas/macosx-x86_64/libgfortran.dylib", "org/bytedeco/openblas/macosx-x86_64/libjniopenblas_nolapack.dylib", "org/bytedeco/openblas/macosx-x86_64/libjniopenblas.dylib", "org/bytedeco/openblas/macosx-x86_64/libgfortran.4.dylib", "org/bytedeco/openblas/macosx-x86_64/libquadmath.0.dylib", "org/bytedeco/openblas/macosx-x86_64/libgcc_s.1.dylib", "org/bytedeco/openblas/macosx-x86_64/libopenblas.0.dylib"))
    , ("extensions/.bundled/vid/opencv-4.5.5-1.5.7-macosx-arm64.jar", Seq("org/bytedeco/opencv/macosx-arm64/libopencv_plot.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_face.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_barcode.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_dnn_superres.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_flann.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_intensity_transform.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_img_hash.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_quality.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_optflow.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_videoio.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_imgproc.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_face.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_structured_light.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_aruco.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_videostab.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_optflow.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_tracking.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_quality.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_structured_light.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_xfeatures2d.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_features2d.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_text.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_saliency.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_bioinspired.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_stitching.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_rapid.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_ml.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_rapid.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_core.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_highgui.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_photo.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_phase_unwrapping.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_ml.405.dylib", "org/bytedeco/opencv/macosx-arm64/opencv_interactive-calibration", "org/bytedeco/opencv/macosx-arm64/libopencv_calib3d.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_flann.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_imgcodecs.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_shape.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_aruco.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_ximgproc.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_mcc.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_tracking.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_java.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_xfeatures2d.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_superres.405.dylib", "org/bytedeco/opencv/macosx-arm64/opencv_annotation", "org/bytedeco/opencv/macosx-arm64/libjniopencv_features2d.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_plot.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_superres.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_core.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_video.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_dnn.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_imgcodecs.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_objdetect.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_python3.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_highgui.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_stitching.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_barcode.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_wechat_qrcode.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_ximgproc.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_xphoto.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_videoio.405.dylib", "org/bytedeco/opencv/macosx-arm64/opencv_version", "org/bytedeco/opencv/macosx-arm64/libopencv_videostab.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_bgsegm.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_img_hash.405.dylib", "org/bytedeco/opencv/macosx-arm64/opencv_visualisation", "org/bytedeco/opencv/macosx-arm64/libjniopencv_video.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_photo.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjnicvkernels.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_bioinspired.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_bgsegm.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_objdetect.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_java.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_dnn.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_intensity_transform.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_imgproc.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_dnn_superres.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_shape.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_mcc.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_wechat_qrcode.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_xphoto.405.dylib", "org/bytedeco/opencv/macosx-arm64/libopencv_saliency.405.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_calib3d.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_phase_unwrapping.dylib", "org/bytedeco/opencv/macosx-arm64/libjniopencv_text.dylib"))
    , ("extensions/.bundled/vid/opencv-4.5.5-1.5.7-macosx-x86_64.jar", Seq("org/bytedeco/opencv/macosx-x86_64/libopencv_plot.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_face.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_barcode.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_dnn_superres.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_flann.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_intensity_transform.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_img_hash.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_quality.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_optflow.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_videoio.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_imgproc.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_face.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_structured_light.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_aruco.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_videostab.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_optflow.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_tracking.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_quality.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_structured_light.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_xfeatures2d.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_features2d.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_text.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_saliency.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_bioinspired.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_stitching.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_rapid.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_ml.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_rapid.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_core.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_highgui.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_photo.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_phase_unwrapping.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_ml.405.dylib", "org/bytedeco/opencv/macosx-x86_64/opencv_interactive-calibration", "org/bytedeco/opencv/macosx-x86_64/libopencv_calib3d.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_flann.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_imgcodecs.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_shape.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_aruco.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_ximgproc.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_mcc.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_tracking.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_java.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_xfeatures2d.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_superres.405.dylib", "org/bytedeco/opencv/macosx-x86_64/opencv_annotation", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_features2d.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_plot.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_superres.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_core.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_video.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_dnn.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_imgcodecs.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_objdetect.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_python3.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_highgui.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_stitching.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_barcode.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_wechat_qrcode.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_ximgproc.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_xphoto.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_videoio.405.dylib", "org/bytedeco/opencv/macosx-x86_64/opencv_version", "org/bytedeco/opencv/macosx-x86_64/libopencv_videostab.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_bgsegm.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_img_hash.405.dylib", "org/bytedeco/opencv/macosx-x86_64/opencv_visualisation", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_video.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_photo.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjnicvkernels.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_bioinspired.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_bgsegm.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_objdetect.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_java.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_dnn.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_intensity_transform.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_imgproc.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_dnn_superres.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_shape.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_mcc.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_wechat_qrcode.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_xphoto.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libopencv_saliency.405.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_calib3d.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_phase_unwrapping.dylib", "org/bytedeco/opencv/macosx-x86_64/libjniopencv_text.dylib", "org/bytedeco/opencv/macosx-x86_64/python/cv2.cpython-310-darwin.so"))
    , ("app/java-objc-bridge-1.0.0.jar", Seq("libjcocoa.dylib"))
    )

    log.info("Signing libs inside jars.")
    jarLibsToSign.foreach { case (jarPath: String, libsToSign: Seq[String]) => signJarLibs(bundleDir / jarPath, appSigningOptions, libsToSign) }

    // It's odd that we have to sign `libjli.dylib`, but it works so I'm not going to
    // worry about it.  We should try to remove it once we're on a more modern JDK version
    // and the package and notarization tools have better adapted to Apple's requirements.
    // More info:  https://github.com/AdoptOpenJDK/openjdk-support/issues/97 -Jeremy B
    // July 2020
    val extraLibsToSign = Seq(
      "runtime/Contents/MacOS/libjli.dylib"
    ).map( (p) => (bundleDir / p).toString )

    log.info("Signing standalone libs in bundles and natives.")
    runCodeSign(appSigningOptions, orderedFilesToBeSigned.map(_.toString) ++ extraLibsToSign, "app bundles")

    log.info(s"Creating $dmgName")
    val dmgPath = Paths.get(dmgName)
    Files.deleteIfExists(dmgPath)

    val dmgArgs = Seq("hdiutil", "create",
      dmgName,
      "-srcfolder", destDir.getAbsolutePath,
      "-size", "1200m",
      "-fs", "HFS+",
      "-volname", buildName, "-ov"
    )
    RunProcess(dmgArgs, destDir, "disk image (dmg) packaging")

    log.info(s"Signing dmg.")
    runCodeSign(Seq(), Seq(dmgName), "disk image (dmg)", Some(destDir))

    log.info(s"Moving dmg file to final location.")
    val archiveFile = webDir / dmgName
    Files.deleteIfExists(archiveFile.toPath)
    FileActions.createDirectory(webDir)
    FileActions.moveFile(destDir / dmgName, archiveFile)

    log.info("\n**Note**: The NetLogo macOS packaging and signing are complete, but you must **notarize** the .dmg file if you intend to distribute it.\n")

    archiveFile
  }
}
