// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.util.MockSuite
import org.nlogo.api.{ Version, ViewSettings, Perspective, WorldType }
import org.nlogo.shape.ShapeConverter
import org.nlogo.core.Model

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
                        (f: HeadlessWorkspace => Unit): Unit = {
    if (!Version.is3D) {
      test(testName) {
        runWorkspaceTest(radius, worldType){f}
      }
    }
  }

  def mockTestUsingWorkspace(name:String, radius: Int = 5,
                             worldType: WorldType = WorldType.Torus)
                            (f: HeadlessWorkspace => Unit): Unit ={
    if (!Version.is3D) {
      mockTest(name){
        runWorkspaceTest(radius, worldType){ f }
      }
    }
  }

  def runWorkspaceTest(radius: Int = 5, worldType: WorldType = WorldType.Torus)
                      (f: HeadlessWorkspace => Unit): Unit = {
    val workspace: HeadlessWorkspace = HeadlessWorkspace.newInstance
    try {
      workspace.initForTesting(-radius, radius, -radius, radius, HeadlessWorkspace.TestDeclarations)
      workspace.changeTopology(worldType.xWrap, worldType.yWrap)
      workspace.world.turtleShapes.replaceShapes(
        Model.defaultTurtleShapes.map(ShapeConverter.baseVectorShapeToVectorShape))
      workspace.world.linkShapes.replaceShapes(Model.defaultLinkShapes.map(ShapeConverter.baseLinkShapeToLinkShape))
      f(workspace)
    }
    finally {workspace.dispose()}
  }
}
