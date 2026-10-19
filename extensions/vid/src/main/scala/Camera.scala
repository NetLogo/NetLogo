package org.nlogo.extensions.vid

import java.awt.image.BufferedImage
import java.awt.Dimension

import org.bytedeco.javacv.{ Java2DFrameUtils, OpenCVFrameGrabber }

import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.concurrent.{ Service, Task, WorkerStateEvent }
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.scene.image.{ Image, ImageView }

import org.nlogo.extensions.vid.util.VideoDeviceUtils

trait CameraFactory {
  def cameraNames:              Seq[String]
  def defaultCameraName:        Option[String]
  def open(cameraName: String): Option[VideoSource]
}

object Camera extends CameraFactory {

  private var devices: Option[Seq[String]] = None
  private val cameraCcl = classOf[Camera].getClassLoader

  def withContextClassLoader[A](f: () => A): A = {
    val oldCcl = Thread.currentThread.getContextClassLoader
    Thread.currentThread.setContextClassLoader(cameraCcl)
    val result = f()
    Thread.currentThread.setContextClassLoader(oldCcl)
    result
  }

  private def initDevices(): Seq[String] = {
    if (VidExtension.isHeadless) {
      Seq()
    } else {
      val ds = devices.getOrElse(withContextClassLoader(() => VideoDeviceUtils.getDeviceNames.toSeq))
      devices = Some(ds)
      ds
    }
  }

  def cameraNames: Seq[String] =
    initDevices()

  def defaultCameraName: Option[String] =
    cameraNames.headOption

  override def open(cameraName: String): Option[VideoSource] = {
    val index = cameraNames.indexWhere(_ == cameraName)
    if (index == -1) {
      None
    } else {
      try {
        Some(new Camera(index))
      } catch {
        case ex: Exception if (PlatformErrors.isPossibleMacOSSecurityError(ex)) =>
          PlatformErrors.showMacOSSecurityMessage(ex)
          None
      }
    }
  }

}

class Camera(deviceIndex: Int) extends VideoSource {
  val grabber = Camera.withContextClassLoader( () => {
    val g = new OpenCVFrameGrabber(deviceIndex)
    g.start()
    g
  })

  var cachedImage = Option.empty[BufferedImage]

  def isPlaying = cachedImage.isEmpty

  private var isClosed = false
  def isOpen = !isClosed

  override def setTime(timeInSeconds: Double): Unit = {}

  override def stop() = {
    cachedImage = Some(captureImage())
  }

  override def play() = {
    cachedImage = None
  }

  override def close() = {
    isClosed = true
    Camera.withContextClassLoader( () => {
      grabber.stop()
    })
  }

  override def captureImage(): BufferedImage = {
    cachedImage.getOrElse(
      Camera.withContextClassLoader( () => {
        val frame = grabber.grab()
        Java2DFrameUtils.toBufferedImage(frame)
      })
    )
  }

  class UpdateImage extends Service[Image] {
    override protected def createTask(): Task[Image] =
      new Task[Image] {
        override protected def call(): Image =
          SwingFXUtils.toFXImage(captureImage(), null)
      }
  }

  class OnUpdateSuccess(imageView: ImageView, imageUpdate: Service[Image])
    extends EventHandler[WorkerStateEvent] {

    def handle(wse: WorkerStateEvent): Unit = {
      imageView.setImage(imageUpdate.getValue)
      imageUpdate.reset()
      imageUpdate.restart()
    }
  }

  private def cameraNode(enforcedBounds: Option[(Double, Double)]): BoundedNode = {
    val imageView = new ImageView()
    enforcedBounds.foreach {
      case (w, h) =>
        imageView.setFitWidth(w)
        imageView.setFitHeight(h)
    }
    val updateImage = new UpdateImage
    val onUpdate = new OnUpdateSuccess(imageView, updateImage)
    updateImage.setOnSucceeded(onUpdate)
    updateImage.start()

    val preferredSize: ObservableValue[Dimension] =
      Bindings.createObjectBinding[Dimension](
        () => new Dimension(imageView.boundsInLocalProperty.get.getWidth.toInt, imageView.boundsInLocalProperty.get.getHeight.toInt),
        imageView.boundsInLocalProperty)

    BoundedNode(imageView, preferredSize, enforcedBounds)
  }

  override def videoNode(bounds: Option[(Double, Double)]): BoundedNode = {
    cameraNode(bounds)
  }
}
