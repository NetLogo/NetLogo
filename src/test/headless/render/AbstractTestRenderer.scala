// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package render

import org.nlogo.util.WorldType, WorldType._
import MockGraphics._

abstract class AbstractTestRenderer(worldType: WorldType = Torus) extends TestUsingWorkspace {

  type Point = (Double,Double)
  case class TurtleDrawnAt(ps:Point*){
    def expectedResults(turtleSize:Int) = (for (p <- ps) yield {
      // this 13 here is really patch size....should probably be fixed.
      Circle(Location(p._1.toDouble,p._2.toDouble), Size(13.0 * turtleSize,13.0 * turtleSize), filled=true)
    }).toList
  }
  case class LabelDrawnAt(ps:Point*){
    def expectedResults(labelSize: LabelSize) = (for(p<-ps) yield Label(Location(p._1.toDouble, p._2.toDouble))).toList
  }
  case class Patch(x: Int, y:Int)
  object Origin extends Patch(0,0)
  case class Turtle(at:Point, size:Int=1)

  abstract class BaseTest {
    run()
    def command: String
    def expectedResults: List[Operation]
    def setup(g:MockGraphics){}
    def run(){
      mockTestUsingWorkspace(this.toString, radius = 16, worldType = AbstractTestRenderer.this.worldType) { workspace =>
        val g = new MockGraphics(AbstractTestRenderer.this)
        setup(g)
        when{
          workspace.command(command)
          workspace.renderer.paint(g, SimpleViewSettings(patchSize = 13))
          testOperations(g, List(Rect(Location(0.0,0.0), Size(429.0,429.0), filled=true)) ::: expectedResults.toList)
        }
      }
    }
  }

  def testOperations(g: MockGraphics, expecteds: List[Operation]) {
//    info("expected: " + expecteds.mkString("\n"))
//    info("actual: " + g.operations.mkString("\n"))
    assert(expecteds.mkString("\n") === g.toString)
  }
}
