// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import java.awt.Frame

import org.nlogo.core.{ Model, Femto }
import org.nlogo.agent.{ CompilationManagement, World }
import org.nlogo.api.{ AggregateManagerInterface, ControlSet, NetLogoThreeDDialect, NetLogoLegacyDialect, RendererInterface }
import org.nlogo.nvm.{ CompilerFlags, Optimizations, PresentationCompilerInterface }
import org.nlogo.window.{ GUIJobManagerOwner, GUIWorkspace, GUIWorkspaceScala, NetLogoListenerManager, UpdateManager, WorkspaceConfig }
import org.nlogo.workspace.{ ModelTrackerImpl, WorkspaceMessageCenter }

object LiteWorkspace {
  def compiler(is3D: Boolean) =
    Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler",
      if (is3D) NetLogoThreeDDialect else NetLogoLegacyDialect)
}

class LiteWorkspace(config: WorkspaceConfig)
  extends GUIWorkspace(config) {

  def this(world: World with CompilationManagement, frame: Frame, listenerManager: NetLogoListenerManager, controlSet: ControlSet, is3D: Boolean) =
    this(
      WorkspaceConfig
        .default
        .withCompiler(LiteWorkspace.compiler(is3D))
        .withKioskLevel(GUIWorkspace.KioskLevel.MODERATE)
        .withWorld(world)
        .withFlags(CompilerFlags(optimizations = if (is3D) Optimizations.gui3DOptimizations else Optimizations.guiOptimizations))
        .withFrame(frame)
        .withLinkParent(frame)
        .withListenerManager(listenerManager)
        .withControlSet(controlSet)
        .withMessageCenter(new WorkspaceMessageCenter())
        .tap(config => config.withModelTracker(new ModelTrackerImpl(config.messageCenter)))
        .tap(config => config.withViewManager(GUIWorkspaceScala.viewManager(config.displayStatusRef)))
        .tap(config =>
            config.withUpdateManager(new UpdateManager(config.world.tickCounter)))
        .tap(config =>
            config.withOwner(new GUIJobManagerOwner(config.updateManager,
              config.viewManager, config.displayStatusRef, config.world, config.frame)))
      )

  @deprecated("LiteWorkspace can no longer be an actual applet, omit first two arguments", "6.1.0")
  def this(appletPanel: AppletPanel, isApplet: Boolean, world: World with CompilationManagement, frame: java.awt.Frame, listenerManager: NetLogoListenerManager, controlSet: ControlSet) =
    this(world, frame, listenerManager, controlSet, false)

  val aggregateManager =
    Femto.get[AggregateManagerInterface]("org.nlogo.sdm.AggregateManagerLite")
  override def newRenderer = Femto.get[RendererInterface]("org.nlogo.render.Renderer", world)
  override def updateModel(m: Model): Model = m
}
