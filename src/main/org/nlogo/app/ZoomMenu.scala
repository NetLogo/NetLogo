// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.I18N
// note that multiple instances of this class may exist as there are now multiple frames that each
// have their own menu bar and menus  ev 8/25/05
private class ZoomMenu(app:App) extends org.nlogo.swing.Menu(I18N.gui.get("menu.zoom")) {

  implicit val i18nName = I18N.Prefix("menu.zoom")
  
  addMenuItem('=',new javax.swing.AbstractAction(I18N.gui("larger")) {
    def actionPerformed(e:java.awt.event.ActionEvent) {
      app.zoomLarger()
    }})
  addMenuItem('0',new javax.swing.AbstractAction(I18N.gui("normalSize")) {
    def actionPerformed(e:java.awt.event.ActionEvent) {
      app.resetZoom()
    }})
  addMenuItem('-',new javax.swing.AbstractAction(I18N.gui("smaller")) {
    def actionPerformed(e:java.awt.event.ActionEvent) {
      app.zoomSmaller()
    }})
}
