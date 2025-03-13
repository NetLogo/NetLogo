// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.util.matching.Regex

object VersionUtils {

  def isNetLogoVersionString(v: String): Boolean = {
    val netLogoVersionRegex = new Regex("NetLogo (?:3D )?(.)*")
    v match {
      case netLogoVersionRegex(_) => true
      case _                      => false
    }
  }

  def numericValue(modelVersion: String): Int = {
    def calculateVersion(major: Int, minor: Int, patch: Int): Int =
      major * 100000 + minor * 1000 + patch * 10
    if (modelVersion.contains("(no version)"))
      0
    else {
      val standardModifier = new Regex("(\\w+)(\\d+)")
      val nonStandardModifier = new Regex("([a-zA-Z0-9\\-]*)")
      val versionRegex = new Regex("NetLogo (?:3D )?(\\d)\\.(\\d+)(?:\\.(\\d+))?(?:-(.*))?")
      val oldVersion = new Regex("NetLogo (\\d)\\.(\\d)(?:\\w+(\\d+))?")
      val previewRegex = new Regex("NetLogo 3[-]?D Preview (\\d)")
      modelVersion match {
        case versionRegex(major, minor, patch, null) =>
          calculateVersion(major.toInt, minor.toInt, Option(patch).map(_.toInt).getOrElse(0))
        case versionRegex(majorText, minorText, patchText, standardModifier(modifier, modifierNum)) =>
          val (major, minor, patch) = (majorText.toInt, minorText.toInt, Option(patchText).map(_.toInt).getOrElse(0))
          val baseVersion = calculateVersion(major, minor, patch)
          (major, minor, patch) match {
            case (m, 0, 0) => baseVersion - 10000 +
              (modifierNum.toInt - 1) + (if (modifier == "RC" || modifier == "BETA" || modifier == "INTERNAL" || modifier ==  "rc" || modifier == "beta" || modifier == "internal") 5000 else 0)
            case (m, n, 0) => baseVersion - 200 +
              (modifierNum.toInt - 1) + (if (modifier == "RC" || modifier == "BETA" || modifier == "INTERNAL" || modifier ==  "rc" || modifier == "beta" || modifier == "internal") 100 else 0)
            case (m, n, p) => baseVersion - 10 +
              (modifierNum.toInt) + (if (modifier == "RC" || modifier == "BETA"|| modifier == "INTERNAL" || modifier ==  "rc" || modifier == "beta" || modifier == "internal") 5 else 0)
          }
        case versionRegex(major, minor, patch, nonStandardModifier(_)) =>
          calculateVersion(major.toInt, minor.toInt, Option(patch).map(_.toInt).getOrElse(0)) - 10000
        case oldVersion(major, minor, modifier) =>
          if (modifier == null)
            calculateVersion(major.toInt, minor.toInt, 0)
          else if (minor.toInt == 0)
            calculateVersion(major.toInt, minor.toInt, 0) - 10000 + modifier.toInt
          else
            calculateVersion(major.toInt, minor.toInt, 0) - 100 + modifier.toInt
        case previewRegex(previewNum) => 390000 + previewNum.toInt * 10
        case _ => -1
      }
    }
  }

}
