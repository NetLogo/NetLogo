// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Component
import javax.swing.{ BoxLayout, JPanel }

class BoxRow(components: Seq[Component]) extends JPanel with Transparent {
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  components.foreach(add)
}

class BoxColumn(components: Seq[Component]) extends JPanel with Transparent {
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  components.foreach(add)
}
