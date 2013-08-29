// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package render

import org.nlogo.api
import org.nlogo.shape.{ LinkShape, VectorShape }
import org.nlogo.util.{ MockSuite, WorldType }

trait TestUsingWorkspace extends MockSuite {

  case class SimpleViewSettings(
    fontSize: Int = 10,
    patchSize:Double = 13,
    viewWidth: Double = 33,
    viewHeight: Double = 33,
    viewOffsetX: Double = 0,
    viewOffsetY: Double = 0,
    drawSpotlight: Boolean = false,
    renderPerspective: Boolean = false,
    perspective: api.Perspective = api.Perspective.Observe) extends api.ViewSettings

  def testUsingWorkspace(testName: String, radius: Int = 5,
                         worldType: WorldType = WorldType.Torus)
                        (f: HeadlessWorkspace => Unit) {
    test(testName) {
      runWorkspaceTest(radius, worldType){ f }
    }
  }

  def mockTestUsingWorkspace(name:String, radius: Int = 5,
                             worldType: WorldType = WorldType.Torus)
                            (f: HeadlessWorkspace => Unit){
    mockTest(name){
      runWorkspaceTest(radius, worldType){ f }
    }
  }

  def runWorkspaceTest(radius: Int = 5, worldType: WorldType = WorldType.Torus)
                      (f: HeadlessWorkspace => Unit) {
    val workspace: HeadlessWorkspace = HeadlessWorkspace.newInstance
    workspace.silent = true
    try {
      ModelCreator.open(workspace, api.WorldDimensions.square(radius))
      workspace.changeTopology(worldType.xWrap, worldType.yWrap)
      workspace.world.turtleShapeList.replaceShapes(
        VectorShape.parseShapes(
          api.ModelReader.defaultShapes.toArray, api.Version.version))
      workspace.world.linkShapeList.replaceShapes(
        LinkShape.parseShapes(
          api.ModelReader.defaultLinkShapes.toArray, api.Version.version))
      f(workspace)
    }
    finally workspace.dispose()
  }
}
