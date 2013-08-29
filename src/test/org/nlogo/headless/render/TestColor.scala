// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package render

import org.nlogo.shape.VectorShape
import org.nlogo.shape.TestHelpers._
import MockGraphics._

class TestColorRendering extends AbstractTestRenderer {

  testUsingWorkspace("non-recolorable shapes dont respond to 'set color' with no alpha"){ workspace: HeadlessWorkspace =>
    workspace.setShapes(makeSquarePolygon(recolorable = false))
    // size 15 fills up almost the entire world.
    workspace.command("crt 1 [ set shape \"test\" set heading 0 set size 15]")
    // 255, 255, 255 is white
    workspace.testColors(255, 255, 255)
    workspace.command("ask turtles [ set color green ]")
    // this shape is not recolorable, everything should still be white.
    workspace.testColors(255, 255, 255)
  }

  testUsingWorkspace("non-recolorable shapes do respond to 'set color' with alpha"){ workspace: HeadlessWorkspace =>
    workspace.setShapes(makeSquarePolygon(recolorable = false))
    // size 15 fills up almost the entire world.
    workspace.command("crt 1 [ set shape \"test\" set heading 0 set size 15]")
    workspace.testColors(255, 255, 255)
    // set color to a random color with a definite alpha value.
    // the color change shouldnt take effect, because the shape is recolorable.
    // but the alpha change should.
    workspace.command("ask turtles [ set color (list random 255 random 255 random 255 127) ]")
    //workspace.dumpWorldToImage()
    workspace.testColors(127,127,127,255)
  }

  testUsingWorkspace("recolorable shapes respond to 'set color'"){ workspace: HeadlessWorkspace =>
    workspace.setShapes(makeSquarePolygon(recolorable = true))
    // size 15 fills up almost the entire world.
    workspace.command("crt 1 [ set shape \"test\" set heading 0 set size 15 set color white]")
    // 255, 255, 255 is white
    workspace.testColors(255, 255, 255)
    workspace.command("ask turtles [ set color red ]")
    // this shape is recolorable, everything should now be red
    //workspace.dumpWorldToImage()
    workspace.testColors(215, 50, 41)
  }

  testUsingWorkspace("recolorable shapes also respond to 'set color' with alpha"){ workspace: HeadlessWorkspace =>
    workspace.setShapes(makeSquarePolygon(recolorable = true))
    // size 15 fills up almost the entire world.
    workspace.command("crt 1 [ set shape \"test\" set heading 0 set size 15 set color red]")
    workspace.testColors(215, 50, 41)
    workspace.command("ask turtles [ set color [255 255 255 127] ]")
    //workspace.dumpWorldToImage()
    workspace.testColors(127,127,127,255)
  }

  implicit class RichWorkspace(val workspace: HeadlessWorkspace){
    def setShapes(shapes: VectorShape*) {
      // remove all shapes from the world
      import collection.JavaConverters._
      for (shape <- workspace.world.turtleShapeList().getShapes.asScala)
        workspace.world.turtleShapeList().removeShape(shape)
      // add one non-recolorable shape
      shapes.foreach(workspace.world.turtleShapeList().add)
    }
    // test that the entire world is a particular color.
    def testColors(r: Int, g: Int, b: Int, a: Int = 255) {
      val image = workspace.exportView
      // why cut off the borders here?
      // a turtle of size 15 almost fills the screen, but leaves some black border. remove it.
      // a turtle of size 16 overlaps some and then doubles alpha values makes things more confusing.
      for (i <- 5 until image.getSampleModel.getWidth - 5; j <- 5 until image.getSampleModel.getHeight - 5) {
        val pixelColor = java.awt.Color.decode(image.getRGB(i, j).toString)
        //println((i, j) + " " + (pixelColor.getRed,pixelColor.getGreen,pixelColor.getBlue,pixelColor.getAlpha))
        assert((pixelColor.getRed, pixelColor.getGreen, pixelColor.getBlue, pixelColor.getAlpha) === ((r, g, b, a)))
      }
    }
    // for sanity only, dump the world to an image in order to look at it.
    def dumpWorldToImage(){
      workspace.command("export-view \"test.png\"")
    }
  }

}
