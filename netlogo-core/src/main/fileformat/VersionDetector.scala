// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.{ ConfigurableModelLoader, TwoDVersion, ThreeDVersion, Version }

object VersionDetector {
  def fromPath(path: String, loader: ConfigurableModelLoader): Option[Version] = {
    if (path.endsWith(".nlogo"))
      Some(TwoDVersion)
    else if (path.endsWith(".nlogo3d"))
      Some(ThreeDVersion)
    else if (path.endsWith(".nlogox")) // TODO: This won't be correct once we allow for 3D models
      Some(TwoDVersion)
    else
      None
  }

  def fromModelContents(contents: String, loader: ConfigurableModelLoader): Option[Version] = {
    findSuffix(contents).map { suffix =>
      if (suffix == "nlogo") TwoDVersion
      else if (suffix == "nlogo3d") ThreeDVersion
      // TODO: once we support 3D nlogox, this might actually have to load the model to
      // determine the correct version.
      else TwoDVersion
    }
  }

  // NOTE: Eventually we would like this to return ".nlogox",
  // but realistically we're likely to see it return nlogo/nlogo3d
  // for the forseeable future.
  def findSuffix(modelString: String): Option[String] = {
    val sections =
      modelString
        .split(AbstractNLogoFormat.SeparatorRegex)
    if (sections.length >= 5) {
      if (Version.is3D(sections(4)))
        Some("nlogo3d")
      else
        Some("nlogo")
    }
    else if (modelString.trim.startsWith("<"))
      Some("nlogox")
    else
      None
  }
}
