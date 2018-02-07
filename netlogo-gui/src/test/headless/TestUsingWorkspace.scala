// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.util.{ MockSuite, TwoDTag, WorldType }
import org.nlogo.api.{ ViewSettings, Perspective }
import org.nlogo.shape.ShapeConverter
import org.nlogo.core.Model

// This seems to be a base trait for a number of older tests and only
// tests against the 2D workspace. This should be considered deprectated
// and new tests should avoid using it. - RG 10/23/17
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
    test(testName, TwoDTag) {
      runWorkspaceTest(radius, worldType){f}
    }
  }

  def mockTestUsingWorkspace(name:String, radius: Int = 5,
                             worldType: WorldType = WorldType.Torus)
                            (f: HeadlessWorkspace => Unit){
    mockTest(name, TwoDTag){
      runWorkspaceTest(radius, worldType){ f }
    }
  }

  def runWorkspaceTest(radius: Int = 5, worldType: WorldType = WorldType.Torus)
                      (f: HeadlessWorkspace => Unit) {
    val workspace: HeadlessWorkspace = HeadlessWorkspace.newInstance(false)
    try {
      workspace.initForTesting(-radius, radius, -radius, radius, HeadlessWorkspace.TestDeclarations)
      workspace.changeTopology(worldType.xWrap, worldType.yWrap)
      workspace.world.turtleShapes.replaceShapes(Model.defaultShapes.map(ShapeConverter.baseVectorShapeToVectorShape))
      workspace.world.linkShapes.replaceShapes(Model.defaultLinkShapes.map(ShapeConverter.baseLinkShapeToLinkShape))
      f(workspace)
    }
    finally {workspace.dispose()}
  }
}
