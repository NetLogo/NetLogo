// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.File

import org.nlogo.core.ExternalResource

object ExternalResourceManager {
  def getResourceName(path: String): String =
    new File(path).getName
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

  def addResource(resource: ExternalResource): Boolean = {
    if (resources.find(_.name == resource.name).isDefined)
      return false

    resources = resources :+ resource

    true
  }
}
