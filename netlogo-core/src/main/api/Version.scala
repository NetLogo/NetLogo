// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ Resource, VersionUtils }

trait Version {

  val noVersion =
    if (is3D)
      "NetLogo 3D (no version)"
    else
      "NetLogo (no version)"

  val (version, versionDropZeroPatch, buildDate, knownVersions) = {
    val lines = Resource.lines("/version.txt").toSeq

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
    val versionDropZeroPatch =
      if (version.endsWith(".0") && version.takeRight(5).forall(_ != ' ')) version.dropRight(2) else version
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
    (version, versionDropZeroPatch, buildDate, knownVersions.toArray)
  }

  def is3D(version: String) =
    Option(version).exists(v =>
      version.containsSlice("3D") || version.containsSlice("3-D"))

  def is3D =
    try java.lang.Boolean.getBoolean("org.nlogo.is3d")
    // can't check arbitrary properties from applets... - ST 10/4/04, 1/31/05
    // no more applets - is this still needed - AAB 05/2012
    catch {
      case _: java.security.AccessControlException => false
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
    // no more applets - is this still needed - AAB 05/2012
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
      // no more applets - is this still needed - AAB 05/2012
      case _: java.security.AccessControlException =>
        false
    }

  def systemDynamicsAvailable =
    try {
      Class.forName("org.jhotdraw.util.StorableInput")
      true
    } catch {
      case _: ClassNotFoundException => false
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

  def compareVersions(appVersion: String, modelVersion: String): Boolean = {
    val modelNum = versionNumber(modelVersion)
    val appNum   = versionNumber(appVersion)
    return (modelVersion == noVersion ||
      modelNum.startsWith(appNum) ||
      compatibleOverrides.getOrElse(appNum, Seq()).contains(modelNum))
  }

  private def compatibleOverrides = Map(
    "6.1" -> Seq("6.0")
  , "6.2" -> Seq("6.1", "6.0")
  , "6.3" -> Seq("6.2", "6.1", "6.0")
  , "6.4" -> Seq("6.3", "6.2", "6.1", "6.0")
  , "7.0" -> Seq("6.4", "6.3", "6.2", "6.1", "6.0")
  )

  private def versionNumber(v: String) =
    if (v.startsWith("NetLogo 3D Preview"))
      v.substring("NetLogo 3D ".length, "NetLogo 3D Preview 5".length)
    else if (v.startsWith("NetLogo 3D 5.")) // Minor version upgrade in 5.x line IS compatible.  FD 6/2/14
      v.substring("NetLogo 3D ".length, "NetLogo 3D 5.".length)
    else if (v.startsWith("NetLogo 5."))
      v.substring("NetLogo ".length, "NetLogo 5.".length)
    else if (v.startsWith("NetLogo 3D"))
      v.substring("NetLogo 3D ".length, "NetLogo 3D 4.0".length)
    else
      v.substring("NetLogo ".length, "NetLogo 4.0".length)

  def fullVersion =
    version + " (" + buildDate + ")"

  // this just exists to avoid breaking anything that might've depended on it before it was moved to core.
  // -Jeremy b June 2021
  def numericValue(v: String): Int =
    VersionUtils.numericValue(v)

}

object Version extends Version
