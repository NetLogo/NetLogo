// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.{ api, shape }
import ModelCreator.{ Model, Widget }

object InitForTesting {

  def apply(ws: HeadlessWorkspace, dimensions: api.WorldDimensions, source: String = "", widgets: List[Widget] = Nil) {
    import ws._
    openString(
      Model(code = source, dimensions = dimensions, widgets = widgets)
        .toString)
  }

}
