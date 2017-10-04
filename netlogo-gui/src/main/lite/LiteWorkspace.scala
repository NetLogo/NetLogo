// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.core.{ Model, Femto }
import org.nlogo.agent.World
import org.nlogo.api.{ AggregateManagerInterface, ControlSet, NetLogoThreeDDialect, NetLogoLegacyDialect, RendererInterface, Version }
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.window.{ GUIJobManagerOwner, GUIWorkspace, GUIWorkspaceScala, NetLogoListenerManager, WorkspaceConfig }
import org.nlogo.workspace.{ ModelTrackerImpl, WorkspaceMessageCenter }

object LiteWorkspace {
  def compiler =
    Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler",
      if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect)
}

class LiteWorkspace(config: WorkspaceConfig)
  extends GUIWorkspace(config) {
  def this(world: World, frame: java.awt.Frame, listenerManager: NetLogoListenerManager, controlSet: ControlSet) =
    this(
      WorkspaceConfig
        .default
        .withCompiler(LiteWorkspace.compiler)
        .withKioskLevel(GUIWorkspace.KioskLevel.MODERATE)
        .withWorld(world)
        .withFrame(frame)
        .withLinkParent(frame)
        .withListenerManager(listenerManager)
        .withControlSet(controlSet)
        .withMessageCenter(new WorkspaceMessageCenter())
        .tap(config => config.withModelTracker(new ModelTrackerImpl(config.messageCenter)))
        .tap(config => config.withViewManager(GUIWorkspaceScala.viewManager(config.displayStatusRef)))
        .tap(config =>
            config.withOwner(new GUIJobManagerOwner(config.updateManager,
              config.viewManager, config.displayStatusRef, config.world, config.frame)))
      )

  @deprecated("LiteWorkspace can no longer be an actual applet, omit first two arguments", "6.1.0")
  def this(appletPanel: AppletPanel, isApplet: Boolean, world: World, frame: java.awt.Frame, listenerManager: NetLogoListenerManager, controlSet: ControlSet) =
    this(world, frame, listenerManager, controlSet)

  val aggregateManager =
    Femto.get[AggregateManagerInterface]("org.nlogo.sdm.AggregateManagerLite")
  override def newRenderer = Femto.get[RendererInterface]("org.nlogo.render.Renderer", world)
  override def updateModel(m: Model): Model = m
}
