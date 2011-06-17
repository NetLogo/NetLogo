package org.nlogo.hubnet.mirroring

case class LinkStamp(
  x1: Double, y1: Double, x2: Double, y2: Double,
  shape: String, color: AnyRef, hidden: Boolean, lineThickness: Double,
  directed: Boolean, destSize: Double, heading: Double, size: Double, erase: Boolean)
