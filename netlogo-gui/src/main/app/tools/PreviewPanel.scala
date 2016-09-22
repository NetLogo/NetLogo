package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, Image }
import java.awt.event.ActionEvent
import java.io.{ File, IOException }
import javax.imageio.ImageIO
import javax.swing.{ AbstractAction, Action, BorderFactory, JButton, JLabel, JPanel, SwingConstants }

import scala.util.{ Failure, Success }

import org.nlogo.awt.Hierarchy.getFrame
import org.nlogo.swing.ModalProgressTask
import org.nlogo.swing.Implicits.thunk2runnable
import org.nlogo.window.GraphicsPreviewInterface
import org.nlogo.workspace.PreviewCommandsRunner

class PreviewPanel(graphicsPreview: GraphicsPreviewInterface) extends JPanel {
  setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
  setLayout(new BorderLayout)
  val button = new JButton
  graphicsPreview.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1))
  val imageLabel = new JLabel {
    override val getPreferredSize = graphicsPreview.getPreferredSize
    setBorder(BorderFactory.createLineBorder(Color.GRAY, 1))
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
    new AbstractAction("Run Preview Commands") {
      def run(): Unit = {
        runnableCommands match {
          case Some(runnable) =>
            ModalProgressTask.onUIThread(getFrame(PreviewPanel.this), "Running Preview Commands", runnable)
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
  def loadManualPreviewAction(imagePath: Option[String]) =
    new AbstractAction {
      def load(): Unit = {
        putValue(Action.NAME, "Load Manual Preview Image")
        showText(wrap(imagePath.getOrElse("Unknown manual preview image path.")))
        imagePath.foreach { path =>
          ModalProgressTask.onUIThread(getFrame(PreviewPanel.this), "Loading Manual Preview Image", () => {
            try {
              showImage(ImageIO.read(new File(path)))
              putValue(Action.NAME, "Reload Manual Preview Image")
            } catch {
              case e: IOException => showText(wrap(e.getMessage, path))
            }
          })
        }
      }
      def actionPerformed(evt: ActionEvent): Unit = load()
      setEnabled(imagePath.isDefined)
      load()
    }
  add(new JPanel() {
    add(button)
  }, BorderLayout.PAGE_START)
  add(new JPanel() {
    add(imageLabel)
    add(graphicsPreview)
  }, BorderLayout.CENTER)
}
