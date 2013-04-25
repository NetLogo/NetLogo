// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.editor.Actions
import org.nlogo.api.{I18N, Version}

// note that multiple instances of this class may exist as there are now multiple frames that each
// have their own menu bar and menus - ev 8/25/05

class HelpMenu(app: App)
        extends org.nlogo.swing.Menu(I18N.gui.get("menu.help"))
{
  implicit val i18nName = I18N.Prefix("menu.help")
  def action(name: String, fn: () => Unit) =
    new javax.swing.AbstractAction(name) {
      def actionPerformed(e:java.awt.event.ActionEvent) {
        fn()
      } }
  if(!System.getProperty("os.name").startsWith("Mac"))
    addMenuItem(
      action("About " + Version.version + "...",
             app.showAboutWindow _))
}
