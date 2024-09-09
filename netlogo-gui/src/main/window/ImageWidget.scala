// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Dimension, Graphics, Image }
import java.io.{ ByteArrayInputStream, File }
import java.nio.file.Files
import java.util.Base64
import javax.imageio.ImageIO
import javax.swing.JPanel

import org.nlogo.api.{ Editable, ExternalResourceManager }
import org.nlogo.core.{ ExternalResource, Image => CoreImage, I18N }
import org.nlogo.swing.OptionDialog

class ImageWidget(resourceManager: ExternalResourceManager) extends SingleErrorWidget with Editable {
  type WidgetModel = CoreImage

  private class ImageFitter extends JPanel {
    private var image: Image = null

    private var width: Int = 0
    private var height: Int = 0

    def setImage(image: Image) {
      this.image = image

      width = image.getWidth(this)
      height = image.getHeight(this)
    }

    override def paintComponent(g: Graphics) {
      if (image != null) {
        if (preserveAspect) {
          val aspect = width.toFloat / height

          if (aspect * getHeight <= getWidth) {
            val width = (aspect * getHeight).toInt
            val x = (getWidth - width) / 2

            g.drawImage(image, x, 0, x + width, getHeight, 0, 0, this.width, height, this)
          }

          else {
            val height = (getWidth / aspect).toInt
            val y = (getHeight - height) / 2

            g.drawImage(image, 0, y, getWidth, y + height, 0, 0, width, this.height, this)
          }
        }

        else
          g.drawImage(image, 0, 0, getWidth, getHeight, 0, 0, width, height, this)
      }
    }
  }

  var imagePath: ExternalResource.Location = ExternalResource.None

  var preserveAspect = true

  def setImage(name: String): Boolean = {
    resourceManager.getResource(name) match {
      case Some(data) =>
        try {
          val image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder.decode(data)))

          if (image == null)
            false
          else {
            imageComponent.setImage(image)

            true
          }
        }

        catch {
          case t: Throwable => false
        }

      case None => false
    }
  }

  def imageError(name: String) {
    OptionDialog.showMessage(this, I18N.gui.get("common.messages.error"), I18N.gui.getN("resource.loadError", name),
                             Array(I18N.gui.get("common.buttons.ok")))
    
    imagePath = ExternalResource.None
  }

  setLayout(new BorderLayout)

  private val imageComponent = new ImageFitter

  add(imageComponent)

  override def classDisplayName =
    I18N.gui.get("tabs.run.widgets.image")

  override def propertySet =
    Properties.image
  
  override def invalidSettings: Seq[(String, String)] = {
    val name = imagePath match {
      case ExternalResource.Existing(name) => name
      case ExternalResource.New(path) =>
        val name = ExternalResourceManager.getName(path)

        resourceManager.addResource(new ExternalResource(name, "image",
          Base64.getEncoder.encodeToString(Files.readAllBytes(new File(path).toPath))))
        
        name
    }

    if (setImage(name))
      Nil
    else
      Seq((I18N.gui.get("edit.image.image"), I18N.gui.getN("resource.loadError", name)))
  }

  override def editFinished(): Boolean = {
    imagePath match {
      case ExternalResource.New(path) =>
        imagePath = ExternalResource.Existing(ExternalResourceManager.getName(path))

      case _ =>
    }

    true
  }

  override def getMinimumSize: Dimension =
    new Dimension(25, 25)

  override def getPreferredSize: Dimension =
    new Dimension(100, 100)
  
  override def model: WidgetModel = {
    val bounds = getBoundsTuple

    imagePath match {
      case ExternalResource.Existing(name) => CoreImage(bounds._1, bounds._2, bounds._3, bounds._4, name,
                                                        preserveAspect)
      case _ => null
    }
  }

  override def load(model: WidgetModel): AnyRef = {
    setSize(model.right - model.left, model.bottom - model.top)

    if (!setImage(model.image))
      imageError(model.image)
    
    imagePath = ExternalResource.Existing(model.image)
    
    preserveAspect = model.preserveAspect

    this
  }
}
