package org.nlogo.app.tools

import java.awt.{ GridBagConstraints, GridBagLayout, Image, Insets }
import java.awt.event.ActionEvent
import java.io.{ File, IOException }
import javax.imageio.ImageIO
import javax.swing.{ AbstractAction, Action, JLabel, JPanel, SwingConstants }

import scala.util.{ Failure, Success }

import org.nlogo.awt.Hierarchy.getFrame
import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, ModalProgressTask }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.GraphicsPreviewInterface
import org.nlogo.workspace.PreviewCommandsRunner

class PreviewPanel(graphicsPreview: GraphicsPreviewInterface) extends JPanel(new GridBagLayout) {
  setOpaque(false)
  setBackground(InterfaceColors.TRANSPARENT)
  val button = new Button(null)
  val imageLabel = new JLabel {
    override val getPreferredSize = graphicsPreview.getPreferredSize
    setHorizontalAlignment(SwingConstants.CENTER)
  }
  def showText(text: String): Unit = {
    graphicsPreview.setVisible(false)
    imageLabel.setText(text)
    imageLabel.setVisible(true)
  }
  def showImage(image: Image): Unit = {
    imageLabel.setVisible(false)
    graphicsPreview.setImage(image)
    graphicsPreview.setVisible(true)
  }
  def wrap(ps: String*) = ps
    .map("<p style='text-align:center; margin:10px'><![CDATA[" + _ + "]]></p>")
    .mkString("<html>", "", "</html>")
  def executeCommandsAction(runnableCommands: Option[PreviewCommandsRunner#Runnable]) =
    new AbstractAction(I18N.gui.get("tools.previewPanel.runPreviewCommands")) {
      def run(): Unit = {
        runnableCommands match {
          case Some(runnable) =>
            ModalProgressTask.onUIThread(getFrame(PreviewPanel.this), I18N.gui.get("tools.previewPanel.runningPreviewCommands"), runnable)
            runnable.result.foreach {
              _ match {
                case Success(image) => showImage(image)
                case Failure(e)     => showText(wrap(e.toString, Option(e.getMessage).getOrElse("")))
              }
            }
          case None => setEnabled(false)
        }
      }
      def actionPerformed(evt: ActionEvent): Unit = run()
      setEnabled(runnableCommands.isDefined)
      showText("")
    }
  def loadManualPreviewAction(imagePath: Option[String]) = {
    imagePath.map(loadImageAction _).getOrElse(dummyLoadImageAction)
  }
  def loadImageAction(path: String): Action =
    new AbstractAction {
      putValue(Action.NAME, I18N.gui.get("tools.previewPanel.loadManualPreviewImage"))
      showText(wrap(path))
      setEnabled(true)

      def load(): Unit = {
        ModalProgressTask.onUIThread(getFrame(PreviewPanel.this),
          I18N.gui.get("tools.previewPanel.loadingManualPreviewImage"),
          () => {
            try {
              showImage(ImageIO.read(new File(path)))
              putValue(Action.NAME, I18N.gui.get("tools.previewPanel.reloadManualPreviewImage"))
            } catch {
              case e: IOException => showText(wrap(e.getMessage, path))
            }
          })
      }

      def actionPerformed(evt: ActionEvent): Unit = load()
      load()
    }
  def dummyLoadImageAction: Action = {
    new AbstractAction {
      setEnabled(false)
      putValue(Action.NAME, I18N.gui.get("tools.previewPanel.loadManualPreviewImage"))
      def actionPerformed(evt: ActionEvent): Unit = { }
    }
  }

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.insets = new Insets(6, 0, 0, 0)
    
    add(button, c)
    add(imageLabel, c)
    add(graphicsPreview, c)
  }
}
