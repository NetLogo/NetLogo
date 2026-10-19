// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions

import scala.jdk.CollectionConverters.IterableHasAsScala

import org.nlogo.workspace.AbstractWorkspace

package object nw {

  def networkExtension(ws: AbstractWorkspace) =
    ws.getExtensionManager.loadedExtensions.asScala.collect {
      case ext: NetworkExtension => ext
    }.head

}
