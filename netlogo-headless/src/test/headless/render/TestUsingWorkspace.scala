// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package render

import org.nlogo.api.{ Perspective, ViewSettings, WorkspaceContext, WorldType }
import org.nlogo.core.{ Model, View }
import org.nlogo.shape.ShapeConverter
import org.nlogo.util.MockSuite

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
    perspective: Perspective = Perspective.Observe,
    workspaceContext: WorkspaceContext) extends ViewSettings

  def testUsingWorkspace(testName: String, radius: Int = 5,
                         worldType: WorldType = WorldType.Torus)
                        (f: HeadlessWorkspace => Unit): Unit = {
    test(testName) {
      runWorkspaceTest(radius, worldType){ f }
    }
  }

  def mockTestUsingWorkspace(name:String, radius: Int = 5,
                             worldType: WorldType = WorldType.Torus)
                            (f: HeadlessWorkspace => Unit): Unit ={
    mockTest(name){
      runWorkspaceTest(radius, worldType){ f }
    }
  }

  def runWorkspaceTest(radius: Int = 5, worldType: WorldType = WorldType.Torus)
                      (f: HeadlessWorkspace => Unit): Unit = {
    val workspace: HeadlessWorkspace = HeadlessWorkspace.newInstance
    workspace.silent = true
    try {
      workspace.openModel(Model(widgets = List(View.square(radius))))
      workspace.changeTopology(worldType.xWrap, worldType.yWrap)
      workspace.world.turtleShapes.replaceShapes(
        Model.defaultTurtleShapes.map(ShapeConverter.baseVectorShapeToVectorShape))
      workspace.world.linkShapes.replaceShapes(
        Model.defaultLinkShapes.map(ShapeConverter.baseLinkShapeToLinkShape))
      f(workspace)
    }
    finally workspace.dispose()
  }
}
