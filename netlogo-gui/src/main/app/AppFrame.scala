// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Dimension, Frame }
import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.{ JFrame, WindowConstants }

import org.nlogo.api.Exceptions
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.{ ModalProgress, NetLogoIcon, ZoomableWindow }
import org.nlogo.theme.ThemeSync
import org.nlogo.window.LinkRoot
import org.nlogo.window.Event.LinkParent
import org.nlogo.window.Events.IconifiedEvent

class AppFrame(app: App) extends JFrame with LinkParent with LinkRoot with NetLogoIcon with ModalProgress
                         with ZoomableWindow(None) with ThemeSync {

  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent): Unit = {
      try app.quit()
      catch { case ex: UserCancelException => Exceptions.ignore(ex) }
    }
    override def windowIconified(e: WindowEvent): Unit = {
      new IconifiedEvent(AppFrame.this, true).raise(AppFrame.this)
    }
    override def windowDeiconified(e: WindowEvent): Unit = {
      new IconifiedEvent(AppFrame.this, false).raise(AppFrame.this)
    }
  })

  def smartPack(targetSize: Dimension, allowShrink: Boolean): Unit = {
    if (getExtendedState != Frame.MAXIMIZED_BOTH) {
      val gc = getGraphicsConfiguration
      val maxBounds = gc.getBounds
      val insets = getToolkit.getScreenInsets(gc)
      val maxWidth = maxBounds.width - insets.left - insets.right
      val maxHeight = maxBounds.height - insets.top - insets.bottom
      val maxBoundsX = maxBounds.x + insets.left
      val maxBoundsY = maxBounds.y + insets.top
      val maxX = maxBoundsX + maxWidth
      val maxY = maxBoundsY + maxHeight

      val (currentWidth, currentHeight) = (getWidth, getHeight)

      // Maybe grow the window, but never shrink it
      var newWidth  = targetSize.width.max(app.tabManager.interfaceTab.getMinimumWidth).min(maxWidth)
      var newHeight = targetSize.height.min(maxHeight)
      if (!allowShrink) {
        newWidth = newWidth.max(currentWidth)
        newHeight = newHeight.max(currentHeight)
      }

      // move up/left to get more room if possible and necessary
      val moveLeft = 0.max(getLocation().x + newWidth  - maxX)
      val moveUp   = 0.max(getLocation().y + newHeight - maxY)

      // now we can compute our new position
      val newX = maxBoundsX.max(getLocation().x - moveLeft)
      val newY = maxBoundsY.max(getLocation().y - moveUp)

      // and now that we know our position, we can compute our new size
      newWidth  = newWidth.min(maxX - newX)
      newHeight = newHeight.min(maxY - newY)

      // now do it!
      setBounds(newX, newY, newWidth, newHeight)
      validate()

      setMinimumSize(new Dimension(app.tabManager.interfaceTab.getMinimumWidth / 2, 300))

      // not sure why this is sometimes necessary - ST 11/24/03
      app.tabManager.mainTabs.requestFocus()
    }
  }

  override def packWindow(): Unit = {
    smartPack(getPreferredSize, false)
  }

  override def syncTheme(): Unit = {
    app.syncWindowThemes()
  }
}
