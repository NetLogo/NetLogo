// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

// TODO i18n lot of work needed here...

import javax.swing.JTabbedPane

class TabsMenu(name: String, tabs: JTabbedPane) extends NumberedMenu(name) {
  setMnemonic('A')
  override lazy val items =
    for(i <- 0 until tabs.getTabCount)
    yield (tabs.getTitleAt(i), () => tabs.setSelectedIndex(i))
}
