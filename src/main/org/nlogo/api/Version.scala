// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api


object Version {

  val noVersion =
    if (is3D)
      "NetLogo 3D (no version)"
    else
      "NetLogo (no version)";
  
  val (version, buildDate, knownVersions) = {
    val lines = org.nlogo.util.Utils.getResourceAsStringArray("/system/version.txt")
    val lines2 = Array("NetLogo 3D Preview 5",
                       "NetLogo 3D Preview 4",
                       "NetLogo 3D Preview 3",
                       "NetLogo 3-D Preview 2",
                       "NetLogo 3-D Preview 1")
    val version =
      if(is3D)
        lines(0).replaceFirst("NetLogo", "NetLogo 3D")
      else
        lines(0)
    val buildDate = lines(1)
    val knownVersions = collection.mutable.ArrayBuffer[String]()
    knownVersions += version
    knownVersions ++= lines.drop(2)
    knownVersions ++=
      (if (is3D)
         lines.drop(2).map(_.replaceFirst("NetLogo", "NetLogo 3D"))
       else
         lines2)
    knownVersions += noVersion
    (version, buildDate, knownVersions.toArray)
  }

  def is3D(version: String) =
    Option(version).exists(v =>
      version.containsSlice("3D") || version.containsSlice("3-D"))

  def is3D =
    try java.lang.Boolean.getBoolean("org.nlogo.is3d")
    // can't check arbitrary properties from applets... - ST 10/4/04, 1/31/05
    catch {
      case _: java.security.AccessControlException => false
    }

  // it's gruesome this is a mutable global, but can't really do anything about it in the short term
  // - ST 11/11/10
  private var _isLoggingEnabled = false
  def isLoggingEnabled = _isLoggingEnabled
  def startLogging() { _isLoggingEnabled = true }
  def stopLogging() { _isLoggingEnabled = false }

  // Turning the optimizer off may be useful when testing or modifying the compiler.  This flag is
  // public so we can conditionalize tests on it, since the results of some tests are affected by
  // whether the optimizer is enabled or not.  The results are no less correct either way, just
  // different, since the optimizer is free to make changes that cause floating point operations to
  // happen in a different order or use a different amount of random numbers and thus leave the RNG
  // in a different state. - ST 3/9/06
  def useOptimizer =
    try !java.lang.Boolean.getBoolean("org.nlogo.noOptimizer")
    // can't check arbitrary properties from applets... - ST 10/4/04, 1/31/05
    catch {
      case _: java.security.AccessControlException =>
        false
    }

  // don't use the generator in the applet because it requires CustomClass loading which is not
  // allowed in the applet.
  def useGenerator =
    try 
      !java.lang.Boolean.getBoolean("org.nlogo.noGenerator") && {
        Class.forName("org.nlogo.generator.Generator");
        true
      }
    catch {
      case _: ClassNotFoundException =>
        false
      // can't check arbitrary properties from applets... - ST 10/4/04, 1/31/05
      case _: java.security.AccessControlException =>
        false
    }

  def knownVersion(version: String) =
    knownVersions.exists(removeRev(version.trim).startsWith)

  def removeRev(version: String) =
    if(version.takeRight(8).startsWith(" (Rev "))
      version.dropRight(8)
    else
      version

  def versionNumberOnly =
    version.drop("NetLogo ".size)

  def compatibleVersion(modelVersion: String) =
    compareVersions(version, modelVersion)

  def compareVersions(appVersion: String, modelVersion: String) =
    modelVersion == noVersion ||
      versionNumber(modelVersion).startsWith(versionNumber(appVersion))

  private def versionNumber(v: String) =
    if (v.startsWith("NetLogo 3D Preview"))
      v.substring("NetLogo 3D ".length, "NetLogo 3D Preview 5".length)
    else if (v.startsWith("NetLogo 3D"))
      v.substring("NetLogo 3D ".length, "NetLogo 3D 4.0".length)
    else
      v.substring("NetLogo ".length, "NetLogo 4.0".length)

  def fullVersion =
    version + " (" + buildDate + ")"

}
