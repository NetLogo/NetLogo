package org.nlogo.gl.render

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import org.nlogo.api.HeadlessRendererInterface
import org.nlogo.api.TrailDrawerInterface
import org.nlogo.api.ViewSettings
import org.nlogo.api.World
import javax.media.opengl.GLCanvas
import javax.media.opengl.GLCapabilities
import javax.media.opengl.GLPbuffer
import javax.media.opengl.GLDrawableFactory
import org.nlogo.api.Perspective
import com.sun.opengl.util.Screenshot

object Headless3DViewSettings extends ViewSettings with GLViewSettings {
  val wireframeOn = true
  val fontSize: Int = 10
  val patchSize: Double = 13
  val viewWidth: Double = 400
  val viewHeight: Double = 400
  val viewOffsetX: Double = 0
  val viewOffsetY: Double = 0
  val drawSpotlight: Boolean = false
  val renderPerspective: Boolean = false
  val isHeadless: Boolean = true
  val perspective: Perspective = Perspective.Observe
}

class Headless3DRenderer(
  world: World,
  val trailDrawer: TrailDrawerInterface)
  extends Renderer3D(world, Headless3DViewSettings, trailDrawer, Headless3DViewSettings)
  with HeadlessRendererInterface {

  def renderLabelsAsRectangles_=(b: Boolean): Unit = () // ignore the value
  def resetCache(patchSize: Double): Unit = () // do nothing
  def changeTopology(wrapX: Boolean, wrapY: Boolean): Unit = () // do nothing

  def exportView(g: Graphics2D, settings: ViewSettings): Unit =
    throw new UnsupportedOperationException // TODO

  def exportView(settings: ViewSettings): BufferedImage = {
    val capabilities = new GLCapabilities
    capabilities.setSampleBuffers(true)
    capabilities.setNumSamples(4)
    capabilities.setStencilBits(1)
    val width = Headless3DViewSettings.viewWidth.toInt
    val height = Headless3DViewSettings.viewHeight.toInt
    val buffer = GLDrawableFactory.getFactory
      .createGLPbuffer(capabilities, null, width, height, null)
    val exporter = createExportRenderer()
    buffer.addGLEventListener(exporter)
    buffer.display()
    buffer.removeGLEventListener(exporter)
    println("before make current")
    buffer.getContext.makeCurrent()
    val bufferedImage = Screenshot.readToBufferedImage(width, height)
    buffer.destroy()
//    val bufferedImage = new BufferedImage(
//      buffer.getWidth, buffer.getHeight,
//      BufferedImage.TYPE_INT_ARGB
//    )
//    bufferedImage.setRGB(
//      0, 0, buffer.getWidth, buffer.getHeight,
//      exporter.pixelInts, 0, buffer.getWidth
//    )
    bufferedImage

  }
}
