// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render

import java.awt.Graphics2D
import java.awt.image.BufferedImage

import org.nlogo.api.{ Color, GraphicsInterface, Graphics2DWrapper, Link, Patch, Turtle, ViewSettings,
                       WorldWithWorldRenderable }
import org.nlogo.core.AgentKind

import scala.jdk.CollectionConverters.{ IterableHasAsScala, IteratorHasAsScala }

class Renderer(world: WorldWithWorldRenderable, renderLabelsAsRectangles: Boolean = false)
  extends AbstractRenderer(world, world.turtleShapes, world.linkShapes, renderLabelsAsRectangles) {

  def paint(g: Graphics2D, settings: ViewSettings): Unit = {
    paint(new Graphics2DWrapper(g, renderLabelsAsRectangles), settings)
  }

  override def paintPatchLabels(g: GraphicsInterface, patchSize: Double): Unit = {
    // then draw the patch labels
    if (world.patchesWithLabels > 0) {
      world.patches.agents.asScala.foreach {
        case patch: Patch if patch.hasLabel =>
          drawPatchLabel(g, patch, patchSize)
      }
    }

    outlineAgent match {
      case patch: Patch =>
        topology.drawWrappedRect(g, Color.getComplement(Color.getColor(patch.pcolor)), 1, patch.pxcor, patch.pycor, 1,
                                 patchSize, false)

      case _ =>
    }
  }

  override def paintLinks(g: GraphicsInterface, patchSize: Double): Unit = {
    var linksDrawn = 0

    // traverse breeds in reverse order of declaration
    world.program.linkBreeds.keys.map(world.getLinkBreed).foreach(_.agents.iterator.asScala.foreach {
      case link: Link =>
        linkDrawer.drawLink(g, topology, link, patchSize, false)
        linksDrawn += 1

      case _ =>
    })

    if (linksDrawn < world.links.count) {
      // uh oh, we have some unbreeded turtles we need to go back and draw
      world.links.agents.iterator.asScala.foreach {
        case link: Link if link.getBreed == world.links =>
          linkDrawer.drawLink(g, topology, link, patchSize, false)

        case _ =>
      }
    }

    outlineAgent match {
      case link: Link =>
        linkDrawer.drawLink(g, topology, link, patchSize, true)

      case _ =>
    }
  }

  override def paintTurtles(g: GraphicsInterface, patchSize: Double): Unit = {
    var turtlesDrawn = 0

    // traverse breeds in reverse order of declaration
    world.program.breeds.keys.map(world.getBreed).foreach {
      case breed if breed.kind == AgentKind.Turtle =>
        breed.agents.iterator.asScala.foreach {
          case turtle: Turtle =>
            turtleDrawer.drawTurtle(g, topology, turtle, patchSize)
            turtlesDrawn += 1

          case _ =>
        }

      case _ =>
    }

    if (turtlesDrawn < world.turtles.count) {
      // uh oh, we have some unbreeded turtles we need to go back and draw
      world.turtles.agents.iterator.asScala.foreach {
        case turtle: Turtle if turtle.getBreed == world.turtles =>
          turtleDrawer.drawTurtle(g, topology, turtle, patchSize)

        case _ =>
      }
    }

    outlineAgent match {
      case turtle: Turtle =>
        turtleDrawer.drawTurtleWithOutline(g, topology, turtle, patchSize)

      case _ =>
    }
  }

  /// Labels

  private def drawPatchLabel(g: GraphicsInterface, patch: Patch, patchSize: Double): Unit = {
    topology.drawLabelHelper(g, patch.pxcor, patch.pycor, patch.labelString, patch.labelColor, patchSize, 1)
  }

  override def getSpotlightImage(settings: ViewSettings): BufferedImage = {
    val (xcor, ycor, spotlightSize, wrap) = world.observer.targetAgent match {
      case turtle: Turtle => (turtle.xcor, turtle.ycor, turtle.size * 2, true)
      case link: Link => (link.midpointX, link.midpointY, link.size, false)
      case patch: Patch => (patch.pxcor.toDouble, patch.pycor.toDouble, 2.0, false)
      case _ => throw new IllegalStateException
    }

    spotlightDrawer.getImage(topology, xcor, ycor, getWidth(settings.patchSize), getHeight(settings.patchSize),
                             settings.patchSize, spotlightSize, darkenPeripheral(settings), wrap)
  }

  override def anyTurtles: Boolean =
    world.turtles.count > 0

  /// NetLogo coords -> pixel coords

  def graphicsX(xcor: Double, patchSize: Double, viewOffsetX: Double): Double =
    topology.graphicsX(xcor, patchSize, viewOffsetX)

  def graphicsY(ycor: Double, patchSize: Double, viewOffsetY: Double): Double =
    topology.graphicsY(ycor, patchSize, viewOffsetY)

  ///

  def exportView(settings: ViewSettings): BufferedImage = {
    // unfortunately we can't just call org.nlogo.awt.Images.paintToImage()
    // here because we need to do a few nonstandard things
    // (namely call graphicsPainter's paint method instead of
    // our own, and grab the world lock) - ST 6/12/04
    val image = new BufferedImage(getWidth(settings.patchSize), getHeight(settings.patchSize),
                                  BufferedImage.TYPE_INT_ARGB)

    val g: Graphics2D = image.getGraphics.asInstanceOf[Graphics2D]

    g.setFont(g.getFont.deriveFont(settings.fontSize.toFloat))

    world synchronized {
      paint(g, settings)
    }

    image
  }

  def exportView(g: Graphics2D, settings: ViewSettings): Unit = {
    // unfortunately we can't just call org.nlogo.awt.Images.paintToImage()
    // here because we need to do a few nonstandard things
    // (namely call graphicsPainter's paint method instead of
    // our own, and grab the world lock) - ST 6/12/04, 10/12/05
    world synchronized {
      paint(g, settings)
    }
  }
}
