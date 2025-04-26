// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render

import java.awt.{ AlphaComposite, Graphics2D }
import java.awt.color.ColorSpace
import java.awt.geom.AffineTransform
import java.awt.image.{ AffineTransformOp, BufferedImage }
import java.lang.{ Double => JDouble }
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, InputStream, IOException, PrintWriter }
import java.util.Base64
import javax.imageio.{ IIOException, ImageIO }

import org.nlogo.api.{ Color, Dump, Graphics2DWrapper, World }
import org.nlogo.core.{ File => NLFile }

class TrailDrawer(world: World, turtleDrawer: TurtleDrawer, linkDrawer: LinkDrawer)
  extends TrailDrawerJ(world, turtleDrawer, linkDrawer) {

  def exportDrawingToCSV(writer: PrintWriter): Unit = {

    if (!drawingBlank) {

      val patchSize = JDouble.toString(world.patchSize)

      val baos = new ByteArrayOutputStream
      ImageIO.write(drawingImage, "png", baos)
      baos.flush()
      val bytes = baos.toByteArray
      baos.close()

      val base64 = s"data:image/png;base64,${Base64.getEncoder.encodeToString(bytes)}"

      Seq("DRAWING", patchSize, base64).foreach(line => writer.println(Dump.csv.encode(line)))

    }

    writer.println()

  }

  @throws(classOf[IOException])
  override def importDrawingBase64(base64: String): Unit = {
    val pair        = base64.split(",")
    val bytes       = Base64.getDecoder.decode(pair(1))
    val contentType = pair(0).replaceFirst("^data:", "").replaceFirst(";base64$", "")
    importDrawing(new ByteArrayInputStream(bytes), Option(contentType))
  }

  @throws(classOf[IOException])
  override def importDrawing(is: InputStream, mimeType: Option[String] = None): Unit = {

    if (drawingImage == null) {
      setUpDrawingImage()
    }

    val image = javax.imageio.ImageIO.read(is)

    if (image == null) {
      throw new javax.imageio.IIOException("Unsupported image format.")
    }

    val scalex = getWidth .toFloat / image.getWidth .toFloat
    val scaley = getHeight.toFloat / image.getHeight.toFloat
    val scale  = if (scalex < scaley) scalex else scaley

    val scaledImage =
      if (scale == 1) {
        image
      } else {

        val trans =
          new AffineTransformOp( AffineTransform.getScaleInstance(scale, scale)
                                , AffineTransformOp.TYPE_BILINEAR)

        // To workaround a java bug, if our image was read
        // into a grayscale color space BufferedImage, than we
        // want to make sure we scale to the same color model
        // so that the colors don't get brightened.  However,
        // we can't do this for image buffers with alpha
        // values, or the scaling gets hosed too.  A curse
        // upon all "open source" languages with closed source
        // implementations. -- CLB
        if ( image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY &&
            !image.getColorModel().hasAlpha()) {
          val scaled = trans.createCompatibleDestImage(image, image.getColorModel())
          trans.filter(image, scaled)
          scaled
        } else {
          trans.filter(image, null)
        }

      }

    val xOffset = (getWidth  - scaledImage.getWidth ) / 2
    val yOffset = (getHeight - scaledImage.getHeight) / 2
    drawingImage.createGraphics.drawImage(scaledImage, xOffset, yOffset, null)
    markDirty()

    sendPixels(true)

  }

  @throws(classOf[IOException])
  def importDrawing(file: NLFile): Unit = {
    try importDrawing(file.getInputStream)
    catch {
      case ex: IIOException =>
        throw new javax.imageio.IIOException(s"Unsupported image format: ${file.getPath}", ex)
    }
  }

  def setColors(colors: Array[Int], width: Int, height: Int): Unit = {

    setUpDrawingImage()

    // rather than directly setting the values in the drawing buffer
    // make a temporary one and copy the image.  otherwise on OS 10.4
    // the drawing disappears when we try and draw to it again.
    // I expect has to do with the accessing the pixels directly
    // v. through the graphics object problem. ev 6/22/05

    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    image.setRGB(0, 0, width, height, colors, 0, width)
    drawingImage.createGraphics.drawImage(image, 0, 0, null)

    drawingBlank = false
    drawingDirty = true
    sendPixels(true)

  }

  def clearDrawing(): Unit = {
    if (drawingImage != null)
      setUpDrawingImage()
  }

  def rescaleDrawing(): Unit = {
    val oldImage = drawingImage
    setUpDrawingImage()
    if (oldImage != null && drawingImage != null) {
      drawingImage.createGraphics.drawImage(oldImage, 0, 0, width, height, null)
      drawingDirty = true
    }
  }

  // sometimes we want to make sure that a drawing is created
  // (like for the api method getDrawing)
  // and sometimes we only want to get the drawing if it has already
  // been created (like for the 3D view or hubnet view mirroring)
  // in which case it's ok to return null.
  // getAndCreateDrawing is the only method available from the api
  // ev 7/31/06
  def getAndCreateDrawing(dirty: Boolean): BufferedImage = {

    if (drawingImage == null) {
      setUpDrawingImage()
    }

    if (dirty) {
      drawingBlank = false
      drawingDirty = true
    }

    drawingImage

  }

  // for hubnet client
  @throws(classOf[IOException])
  def readImage(is: InputStream): Unit = {
    setUpDrawingImage()
    readImage(ImageIO.read(is))
  }

  def drawLine( x1: Double, y1: Double, x2: Double, y2: Double
              , penColor: AnyRef, penSize: Double, penMode: String): Unit = {

    if (drawingImage == null) {
      setUpDrawingImage()
    }

    val tg = new Graphics2DWrapper(drawingImage.getGraphics.asInstanceOf[Graphics2D])
    tg.setPenWidth(penSize)

    if (penMode.equals("erase")) {
      tg.setComposite(AlphaComposite.Clear)
      drawWrappedLine(tg, x1, y1, x2, y2, penSize)
      tg.setComposite(AlphaComposite.SrcOver)
    } else {
      tg.antiAliasing(true)
      tg.setColor(Color.getColor(penColor))
      drawWrappedLine(tg, x1, y1, x2, y2, penSize)
      tg.antiAliasing(false)
    }

  }

  private def setUpDrawingImage(): Unit = {

    width  = StrictMath.round(world.patchSize * world.worldWidth ).toInt
    height = StrictMath.round(world.patchSize * world.worldHeight).toInt

    drawingImage =
      if (width > 0 && height > 0) {
        colorArray = null
        new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
      } else {
        null
      }

    drawingBlank = true

  }

}
