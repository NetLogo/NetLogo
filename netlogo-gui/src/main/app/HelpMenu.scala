// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.editor.Colorizer
import org.nlogo.editor.Actions
import org.nlogo.swing.BrowserLauncher
import org.nlogo.api.{I18N, Version}

// note that multiple instances of this class may exist as there are now multiple frames that each
// have their own menu bar and menus - ev 8/25/05

class HelpMenu(app: App, colorizer: Colorizer[_])
        extends org.nlogo.swing.Menu(I18N.gui.get("menu.help"))
{
  implicit val i18nName = I18N.Prefix("menu.help")

  def action(name: String, fn: () => Unit) =
    new javax.swing.AbstractAction(name) {
      def actionPerformed(e:java.awt.event.ActionEvent) {
        fn()
      } }
  def launch(name: String, isLocal: Boolean, url: String) =
    action(name, () => BrowserLauncher.openURL(HelpMenu.this, url, isLocal))

  setMnemonic('H')
  def docPath(docName: String): String =
    System.getProperty("netlogo.docs.dir", "docs") + "/" + docName

  addMenuItem(
    I18N.gui("lookUpInDictionary(F1)"),
    Actions.quickHelpAction(colorizer, I18N.gui.get _))
  addSeparator()
  addMenuItem(
    launch(I18N.gui("netLogoUserManual"), true,
           docPath("index.html")))
  addMenuItem(
    launch(I18N.gui("netLogoDictionary"), true,
           docPath("index2.html")))
  addMenuItem(
    launch(I18N.gui("netLogoUsersGroup"), false,
           "http://groups.yahoo.com/group/netlogo-users/"))
  addSeparator()
  addMenuItem(
    launch(I18N.gui("donate"), false,
           "http://ccl.northwestern.edu/netlogo/giving.shtml"))
  if(!System.getProperty("os.name").startsWith("Mac"))
    addMenuItem(
      action("About " + Version.versionDropZeroPatch + "...",
             app.showAboutWindow _))
}
