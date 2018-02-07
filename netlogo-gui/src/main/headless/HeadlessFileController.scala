// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.net.URI

import org.nlogo.core.Model
import org.nlogo.fileformat.FailedConversionResult
import org.nlogo.workspace.OpenModel

object HeadlessFileController extends OpenModel.Controller {
  def errorOpeningURI(uri: URI,exception: Exception): Unit = {
    System.err.println(s"While attempting to open: $uri, NetLogo encountered the following exception: ${exception}")
    exception.printStackTrace(System.err)
  }
  def invalidModel(uri: URI): Unit = {
    System.err.println(s"The file at $uri is not a valid NetLogo model")
  }
  def invalidModelVersion(uri: URI,version: String): Unit = {
    System.err.println(s"NetLogo wasn't able to open the model at $uri. The NetLogo file had a missing or invalid version string")
  }
  def shouldOpenModelOfDifferingArity(arity: Int,version: String): OpenModel.VersionResponse = {
    val (expectedArity, actualArity) = if (arity == 2) ("3D ", "") else (" ", "3D ")
    System.err.println(s"Expected to find a NetLogo ${expectedArity}model, but found a NetLogo ${actualArity}model. Aborting...")
    OpenModel.CancelOpening
  }
  def shouldOpenModelOfLegacyVersion(currentVersion: String, openVersion: String): Boolean = {
    System.err.println(s"This NetLogo model has version ${openVersion}, the current version is ${currentVersion}, please resave the model in NetLogo ${currentVersion} before opening headlessly. Aborting...")
    false
  }
  def shouldOpenModelOfUnknownVersion(currentVersion: String, openVersion: String): Boolean = {
    System.err.println(s"This NetLogo model has version ${openVersion}, the current version is ${currentVersion}, please resave the model in NetLogo ${currentVersion} before opening headlessly. Aborting...")
    false
  }
  def errorAutoconvertingModel(res: FailedConversionResult): Option[Model] = {
    System.err.println(s"While attempting to convert the NetLogo mdoel, NetLogo encountered the following errors:")
    res.errors.foreach(_.errors.foreach(e => System.err.println(e.getMessage)))
    System.err.println("aborting...")
    None
  }
}
