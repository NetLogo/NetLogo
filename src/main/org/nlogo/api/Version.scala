// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api


object Version {

  val noVersion = "NetLogo (no version)"

  val (version, buildDate, knownVersions) = {
    val lines = org.nlogo.util.Utils.getResourceLines("/system/version.txt").toStream
    val version = lines.head
    val buildDate = lines.tail.head
    val knownVersions = collection.mutable.ArrayBuffer[String]()
    knownVersions += version
    knownVersions ++= lines.drop(2)
    knownVersions += noVersion
    (version, buildDate, knownVersions.toArray)
  }

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
        Class.forName("org.nlogo.generate.Generator")
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
    compareVersions(versionForSaving, modelVersion)

  def compareVersions(appVersion: String, modelVersion: String) =
    modelVersion == noVersion ||
      versionNumber(modelVersion).startsWith(versionNumber(appVersion))

  // 5.2 hasn't incompatibly diverged from 5.0 yet
  def versionForSaving =
    "NetLogo 5.0"

  private def versionNumber(v: String) =
    v.substring("NetLogo ".length, "NetLogo 4.0".length)

  def fullVersion =
    version + " (" + buildDate + ")"

}
