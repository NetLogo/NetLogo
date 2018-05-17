// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Frame
import javax.swing.{ JDialog, JTabbedPane }

import org.nlogo.core.I18N

class LibrariesDialog(parent: Frame, manager: LibraryManager)
extends JDialog(parent, I18N.gui.get("tools.libraries"), false) {
  locally {
    val tabs = new JTabbedPane
    manager.listModels.foreach { case (name, contents) =>
      tabs.addTab(I18N.gui.get("tools.libraries.categories." + name), new LibrariesTab(contents))
    }
    add(tabs)
    setSize(500, 300)
  }
}
