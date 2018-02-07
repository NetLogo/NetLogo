// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  java.nio.file.Paths

import
  org.nlogo.api.{ ConfigurableModelLoader, TwoDVersion, ThreeDVersion, Version }

object VersionDetector {
  def fromPath(path: String, loader: ConfigurableModelLoader): Option[Version] = {
    if (path.endsWith(".nlogo"))
      Some(TwoDVersion)
    else if (path.endsWith(".nlogo3d"))
      Some(ThreeDVersion)
    else if (path.endsWith(".nlogox"))
      loader.readModel(Paths.get(path).toUri)
        .toOption
        .map(m => Version.getCurrent(m.version))
    else
      None
  }

  def fromModelContents(contents: String, loader: ConfigurableModelLoader): Option[Version] = {
    findSuffix(contents).flatMap { suffix =>
      if (suffix == "nlogo") Some(TwoDVersion)
      else if (suffix == "nlogo3d") Some(ThreeDVersion)
      else loader.readModel(contents, "nlogox").toOption.map(m => Version.getCurrent(m.version))
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
