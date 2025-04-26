// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.File

import org.nlogo.core.ExternalResource

import scala.collection.mutable.Buffer

object ExternalResourceManager {

  def getName(path: String): String =
    new File(path).getName

  def getName(location: ExternalResource.Location): String = {
    location match {
      case ExternalResource.Existing(name) => name
      case ExternalResource.New(path)      => getName(path)
    }
  }

}

class ExternalResourceManager {

  private val resources = Buffer[ExternalResource]()

  def getResources: Seq[ExternalResource] =
    resources.toSeq

  def setResources(rs: Seq[ExternalResource]): Unit = {
    resources.clear()
    resources ++= rs
  }

  def getResource(name: String): Option[ExternalResource] =
    resources.find(_.name == name)

  def addResource(resource: ExternalResource): Boolean = {
    if (resources.find(_.name == resource.name).isEmpty) {
      resources += resource

      true
    }

    else
      false
  }

  def removeResource(name: String): Unit = {
    val index = resources.indexWhere(_.name == name)

    if (index != -1)
      resources.remove(index)
  }

}
