// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Frame
import javax.swing.{ JDialog, JLabel }

import org.nlogo.api.Version
import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, DialogButton, ButtonPanel, Positioning, ScrollPane, TextArea,
                         Zoomable, ZoomableBorder, ZoomableWindow }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.concurrent.duration.DurationInt

import sttp.client4.DefaultFutureBackend
import sttp.client4.quick.{ quickRequest, UriContext }

import ujson.Obj

class ReportNotHelpfulDialog(parent: Frame, error: Exception)
  extends JDialog(parent, I18N.gui.get("error.dialog.notHelpful"), true) with ZoomableWindow(Option(parent))
  with ThemeSync {

  private val message = new JLabel(I18N.gui.get("error.dialog.notHelpful.message")) with Zoomable

  private val comment = new TextArea(8, 40)
  private val scroll = new ScrollPane(comment)

  private val reportButton = new DialogButton(true, I18N.gui.get("dialog.error.report"), () => report())
  private val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), () => cancel())

  setContentPane(new BoxColumn(Seq(
    new BoxRow(message, BoxAlign.Start),
    scroll,
    new ButtonPanel(Seq(reportButton, cancelButton))
  ), 6) {
    setOpaque(true)
    setBorder(new ZoomableBorder(6, 6, 6, 6))
  })

  syncTheme()

  pack()

  Positioning.center(this, parent)

  setVisible(true)

  private def report(): Unit = {
    val json: String = ujson.write(Obj(
      "version" -> Version.versionNumberOnly,
      "error" -> error.getMessage.trim,
      "stack_trace" -> error.getStackTrace.mkString("\n").trim,
      "comment" -> comment.getText.trim
    ))

    quickRequest.post(uri"https://backend.netlogo.org/items/NetLogo_Desktop_Unhelpful_Errors")
                .body(json)
                .contentType("application/json")
                .readTimeout(15.seconds)
                .send(DefaultFutureBackend())

    setVisible(false)
  }

  private def cancel(): Unit = {
    setVisible(false)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    message.setForeground(InterfaceColors.dialogText())

    scroll.setBackground(InterfaceColors.textAreaBackground())

    comment.syncTheme()
    reportButton.syncTheme()
    cancelButton.syncTheme()
  }
}
