// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.util.MockSuite
import org.nlogo.api.{ModelReader, Version, ViewSettings, Perspective}
import org.nlogo.shape.{LinkShape, VectorShape}
import org.nlogo.util.WorldType

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
    isHeadless: Boolean = true,
    perspective: Perspective = Perspective.Observe) extends ViewSettings

  def testUsingWorkspace(testName: String, radius: Int = 5,
                         worldType: WorldType = WorldType.Torus)
                        (f: HeadlessWorkspace => Unit) {
    test(testName) {
      runWorkspaceTest(radius, worldType){f}
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
    try {
      workspace.initForTesting(-radius, radius, -radius, radius, HeadlessWorkspace.TestDeclarations)
      workspace.changeTopology(worldType.xWrap, worldType.yWrap)
      workspace.world.turtleShapeList.replaceShapes(
        VectorShape.parseShapes(
          ModelReader.defaultShapes.toArray, Version.version))
      workspace.world.linkShapeList.replaceShapes(
        LinkShape.parseShapes(
          ModelReader.defaultLinkShapes.toArray, Version.version))
      f(workspace)
    }
    finally {workspace.dispose()}
  }
}
