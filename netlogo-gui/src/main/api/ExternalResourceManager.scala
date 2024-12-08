// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.File

import org.nlogo.core.ExternalResource

object ExternalResourceManager {
  def getName(path: String): String =
    new File(path).getName

  def getName(location: ExternalResource.Location): Option[String] = {
    location match {
      case ExternalResource.Existing(name) => Some(name)
      case ExternalResource.New(path) => Some(getName(path))
      case ExternalResource.None => None
    }
  }
}

class ExternalResourceManager {
  private var resources = Seq[ExternalResource]()

  def getResources: Seq[ExternalResource] =
    resources

  def setResources(resources: Seq[ExternalResource]) {
    this.resources = resources
  }

  def getResource(name: String): Option[String] =
    resources.find(_.name == name).map(_.data)

  def addResource(resource: ExternalResource) {
    if (resources.find(_.name == resource.name).isEmpty)
      resources = resources :+ resource
  }
}
