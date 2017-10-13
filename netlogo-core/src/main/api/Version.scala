// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Resource

import scala.util.matching.Regex

trait Version {
  def is3D: Boolean
  def version: String
  def noVersion: String
  protected def additionalKnownVersions: Seq[String]

  protected lazy val versionLines =
    Resource.lines("/version.txt").toSeq

  protected val oldThreeDVersions =
    Seq("NetLogo 3D Preview 5",
        "NetLogo 3D Preview 4",
        "NetLogo 3D Preview 3",
        "NetLogo 3-D Preview 2",
        "NetLogo 3-D Preview 1")

  lazy val buildDate = versionLines(1)

  lazy val versionDropZeroPatch =
    if (version.endsWith(".0") && version.takeRight(5).forall(_ != ' ')) version.dropRight(2) else version

  lazy val allKnownVersions = {
    Seq(version, noVersion) ++
      versionLines.drop(2) ++
      additionalKnownVersions
  }

  @deprecated("Use allKnownVersions instead", "6.1.0")
  def knownVersions = allKnownVersions.toArray

  def versionNumberOnly =
    version.drop("NetLogo ".size)

  def knownVersion(version: String) =
    allKnownVersions.exists(removeRev(version.trim).startsWith)

  def compatibleVersion(modelVersion: String) =
    compareVersions(version, modelVersion)

  def compareVersions(appVersion: String, modelVersion: String): Boolean =
    modelVersion == noVersion ||
      versionNumber(modelVersion).startsWith(versionNumber(appVersion))

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

  private def removeRev(version: String) =
    if(version.takeRight(8).startsWith(" (Rev "))
      version.dropRight(8)
    else
      version

  def fullVersion =
    version + " (" + buildDate + ")"
}

object TwoDVersion extends {
  val is3D      = false
} with Version {
  val version = versionLines(0)
  def noVersion = "NetLogo (no version)"
  def additionalKnownVersions: Seq[String] =
    oldThreeDVersions
}

object ThreeDVersion extends {
  val is3D      = true
} with Version {
  val version = versionLines(0).replaceFirst("NetLogo", "NetLogo 3D")
  def noVersion = "NetLogo 3D (no version)"
  def additionalKnownVersions: Seq[String] =
    versionLines.drop(2).map(_.replaceFirst("NetLogo", "NetLogo 3D"))
}

object Version extends Version {
  @deprecated("Version.version will not reflect whether NetLogo is in 3D mode, check dialect.is3D instead", "6.1.0")
  val version = versionLines(0)

  @deprecated("Use compiler.dialect.is3D instead", "6.1.0")
  def is3D = {
    System.err.println("""|Version.is3D is deprecated and may not reflect the 2D/3D state of the current model.
                          |Query the world type or dialect to determine whether NetLogo is 3D""".stripMargin)
    is3DInternal
  }

  def noVersion = "NetLogo (no version)"

  def additionalKnownVersions: Seq[String] =
    versionLines.drop(2).map(_.replaceFirst("NetLogo", "NetLogo 3D")) ++ oldThreeDVersions

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

  // This is to allow our internal methods (which should themselves be deprecated) to compile without warning
  private[nlogo] def is3DInternal: Boolean = {
    try java.lang.Boolean.getBoolean("org.nlogo.is3d")
    catch {
      case _: java.security.AccessControlException => false
    }
  }

  // don't use the generator in the applet because it requires CustomClass loading which is not
  // allowed in the applet.
  def useGenerator =
    try
      !java.lang.Boolean.getBoolean("org.nlogo.noGenerator") && {
        Class.forName("org.nlogo.generate.Generator");
        true
      }
    catch {
      case _: ClassNotFoundException =>
        false
      // can't check arbitrary properties from applets... - ST 10/4/04, 1/31/05
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

  def getCurrent(is3D: Boolean): Version =
    if (is3D) ThreeDVersion
    else      TwoDVersion

  def getCurrent(versionString: String): Version =
    if (is3D(versionString)) ThreeDVersion
    else      TwoDVersion

  def is3D(version: String) =
    Option(version).exists(v =>
      version.containsSlice("3D") || version.containsSlice("3-D"))

  // it's gruesome this is a mutable global, but can't really do anything about it in the short term
  // - ST 11/11/10
  private var _isLoggingEnabled = false
  def isLoggingEnabled = _isLoggingEnabled
  def startLogging() { _isLoggingEnabled = true }
  def stopLogging() { _isLoggingEnabled = false }

  // I did a bit of research to try to correlate 3D previews with
  // NetLogo versions. I couldn't come up with a 100% correct list,
  // but these values are close enough, given that we don't often
  // interact with NetLogo versions older than 4
  private lazy val previewVersionMap =
    Map(
      "NetLogo 3-D Preview 1" -> numericValue("NetLogo 3.0"),
      "NetLogo 3-D Preview 2" -> numericValue("NetLogo 3.1"),
      "NetLogo 3D Preview 3" -> numericValue("NetLogo 3.2"),
      "NetLogo 3D Preview 4" -> numericValue("NetLogo 3.2"),
      "NetLogo 3D Preview 5" -> numericValue("NetLogo 4.0"))

  def numericValue(modelVersion: String): Int = {
    def calculateVersion(major: Int, minor: Int, patch: Int): Int =
      major * 100000 + minor * 1000 + patch * 10
    if (modelVersion.contains("(no version)"))
      0
    else {
      val standardModifier = new Regex("(\\w+)(\\d+)")
      val nonStandardModifier = new Regex("([a-zA-Z0-9\\-]*)")
      val versionRegex = new Regex("NetLogo (?:3D )?(\\d)\\.(\\d+)(?:\\.(\\d+))?(?:-(.*))?")
      val oldVersion = new Regex("NetLogo (?:3D )?(\\d)\\.(\\d)(?:(\\w+)(\\d+))?")
      val previewRegex = new Regex("NetLogo 3[-]?D Preview (\\d)")
      modelVersion match {
        case versionRegex(major, minor, patch, null) =>
          calculateVersion(major.toInt, minor.toInt, Option(patch).map(_.toInt).getOrElse(0))
        case versionRegex(majorText, minorText, patchText, standardModifier(modifier, modifierNum)) =>
          val (major, minor, patch) = (majorText.toInt, minorText.toInt, Option(patchText).map(_.toInt).getOrElse(0))
          val baseVersion = calculateVersion(major, minor, patch)
          (major, minor, patch) match {
            case (m, 0, 0) => baseVersion - 10000 +
              (modifierNum.toInt - 1) + (if (modifier == "RC" || modifier == "BETA") 5000 else 0)
            case (m, n, 0) => baseVersion - 200 +
              (modifierNum.toInt - 1) + (if (modifier == "RC" || modifier == "BETA") 100 else 0)
            case (m, n, p) => baseVersion - 10 +
              (modifierNum.toInt) + (if (modifier == "RC" || modifier == "BETA") 5 else 0)
          }
        case versionRegex(major, minor, patch, nonStandardModifier(_)) =>
          calculateVersion(major.toInt, minor.toInt, Option(patch).map(_.toInt).getOrElse(0)) - 10000
        case oldVersion(major, minor, qualifier, modifier) =>
          val modifierValue =
            (qualifier, modifier) match {
              case ("pre", modifier)   => modifier.toInt
              case ("alpha", modifier) => 30 + modifier.toInt
              case ("beta", modifier)  => 60 + modifier.toInt
              case (_, modifier) if modifier != null => modifier.toInt
              case (_, _) => 0
            }
          if (modifier == null)
            calculateVersion(major.toInt, minor.toInt, 0)
          else if (minor.toInt == 0)
            calculateVersion(major.toInt, minor.toInt, 0) - 10000 + modifierValue
          else
            calculateVersion(major.toInt, minor.toInt, 0) - 100 + modifierValue
        case previewRegex(previewNum) => previewVersionMap(modelVersion)
        case _ => -1
      }
    }
  }
}
