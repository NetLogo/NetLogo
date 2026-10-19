package org.nlogo.extensions.vid

import java.awt.Dimension

import javafx.beans.value.ObservableValue
import javafx.scene.Node

case class BoundedNode(node: Node,
  preferredSize: ObservableValue[Dimension],
  enforcedBounds: Option[(Double, Double)])

trait Player {
  def boundedSize: Option[(Double, Double)]
  def isShowing: Boolean
  def show(): Unit
  def hide(): Unit
  def present(boundedNode: BoundedNode): Unit
  def emptyNode(bounds: Option[(Double, Double)]): BoundedNode
}
