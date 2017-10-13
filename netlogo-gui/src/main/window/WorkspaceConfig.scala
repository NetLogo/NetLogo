// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Frame }

import org.nlogo.core.Femto
import org.nlogo.api.{ ControlSet, SourceOwner }
import org.nlogo.agent.{ CompilationManagement, World }
import org.nlogo.nvm.{ CompilerFlags, JobManagerInterface, PresentationCompilerInterface }
import org.nlogo.workspace.{ Evaluator, ExtensionManager, HubNetManagerFactory, JarLoader,
  LiveCompilerServices, ModelTracker, WorkspaceDependencies,
  WorkspaceMessageCenter, ModelTrackerImpl, UserInteraction }

object WorkspaceConfig {
  def default = {
    new WorkspaceConfig()
      .withMonitorManager(NullMonitorManager)
      .tap(config => config.withModelTracker(new ModelTrackerImpl(config.messageCenter)))
      .tap(config => config.withViewManager(GUIWorkspaceScala.viewManager(config.displayStatusRef)))
  }
}

class WorkspaceConfig extends WorkspaceDependencies {
  var compiler: PresentationCompilerInterface = _
  var compilerServices: LiveCompilerServices = _
  var controlSet: ControlSet = _
  var displayStatusRef = GUIWorkspaceScala.initialDisplayStatus
  var evaluator: Evaluator = _
  var extensionManager: ExtensionManager = _
  var externalFileManager: ExternalFileManager = _
  var flags: CompilerFlags = _
  var frame: Frame = _
  var hubNetManagerFactory: HubNetManagerFactory = _
  var jobManager: JobManagerInterface = _
  var kioskLevel: GUIWorkspace.KioskLevel = GUIWorkspace.KioskLevel.NONE
  var linkParent: Component = _
  var listenerManager: NetLogoListenerManager = _
  var messageCenter: WorkspaceMessageCenter = _
  var modelTracker: ModelTracker = _
  var monitorManager: MonitorManager = _
  var owner: GUIJobManagerOwner = _
  var sourceOwners: Seq[SourceOwner] = Seq()
  var updateManager: UpdateManagerInterface = _
  var userInteraction: UserInteraction = _
  var viewManager: ViewManager = _
  var world: World with CompilationManagement = _

  private var compilerServicesSet = false
  private var evaluatorSet = false
  private var extensionManagerSet = false
  private var jobManagerSet = false

  def withCompiler(c: PresentationCompilerInterface): WorkspaceConfig = {
    compiler = c
    withDefaultEvaluator
  }

  def withCompilerServices(c: LiveCompilerServices): WorkspaceConfig = {
    compilerServices = c
    compilerServicesSet = true
    this
  }

  def withDefaultCompilerServices: WorkspaceConfig = {
    if (! compilerServicesSet && compiler != null && world != null && extensionManager != null && evaluator != null) {
      compilerServices = new LiveCompilerServices(compiler, extensionManager, world, evaluator)
    }
    this
  }

  def withControlSet(c: ControlSet): WorkspaceConfig = {
    controlSet = c
    this
  }

  def withDisplayStatus(d: GUIWorkspaceScala.DisplayStatusRef): WorkspaceConfig = {
    displayStatusRef = d
    this
  }

  def withEvaluator(e: Evaluator): WorkspaceConfig = {
    evaluator = e
    evaluatorSet = true
    withDefaultExtensionManager
  }

  def withFlags(e: CompilerFlags): WorkspaceConfig = {
    flags = e
    this
  }

  private def withDefaultEvaluator: WorkspaceConfig = {
    if (! evaluatorSet && jobManager != null && compiler != null && world != null && flags != null)
      evaluator = new Evaluator(jobManager, compiler, world, flags)
    withDefaultExtensionManager
  }

  def withExtensionManager(e: ExtensionManager): WorkspaceConfig = {
    extensionManager = e
    extensionManagerSet = true
    this
  }

  private def withDefaultExtensionManager: WorkspaceConfig = {
    if (! extensionManagerSet && userInteraction != null && evaluator != null && messageCenter != null && modelTracker != null) {
      extensionManager = new ExtensionManager(userInteraction, evaluator, messageCenter, modelTracker, new JarLoader(modelTracker))
    }
    withDefaultCompilerServices
  }

  def withExternalFileManager(efm: ExternalFileManager): WorkspaceConfig = {
    externalFileManager = efm
    this
  }

  def withFrame(f: Frame): WorkspaceConfig = {
    frame = f
    if (linkParent == null)
      linkParent = f
    if (userInteraction == null) {
      userInteraction = new GUIWorkspaceScala.SwingUserInteraction(f)
      withDefaultExtensionManager
    }
    this
  }

  def withHubNetManagerFactory(hnmf: HubNetManagerFactory): WorkspaceConfig = {
    hubNetManagerFactory = hnmf
    this
  }

  def withJobManager(j: JobManagerInterface): WorkspaceConfig = {
    jobManager = j
    jobManagerSet = true
    this
  }

  private def withDefaultJobManager: WorkspaceConfig = {
    if (! jobManagerSet && owner != null && world != null) {
      jobManager = Femto.get[JobManagerInterface]("org.nlogo.job.JobManager", owner, world)
    }
    withDefaultEvaluator
  }

  def withKioskLevel(l: GUIWorkspace.KioskLevel): WorkspaceConfig = {
    kioskLevel = l
    this
  }

  def withLinkParent(p: Component): WorkspaceConfig = {
    linkParent = p
    this
  }

  def withListenerManager(lm: NetLogoListenerManager): WorkspaceConfig = {
    listenerManager = lm
    this
  }

  def withMessageCenter(m: WorkspaceMessageCenter): WorkspaceConfig = {
    messageCenter = m
    this
  }

  def withModelTracker(t: ModelTracker): WorkspaceConfig = {
    modelTracker = t
    this
  }

  def withMonitorManager(m: MonitorManager): WorkspaceConfig = {
    monitorManager = m
    this
  }

  def withOwner(o: GUIJobManagerOwner): WorkspaceConfig = {
    owner = o
    withDefaultJobManager
  }

  def withSourceOwner(s: SourceOwner): WorkspaceConfig = {
    sourceOwners :+= s
    this
  }

  def withUpdateManager(m: UpdateManagerInterface): WorkspaceConfig = {
    updateManager = m
    this
  }

  def withUserInteraction(ui: UserInteraction): WorkspaceConfig = {
    userInteraction = ui
    withDefaultExtensionManager
  }

  def withViewManager(m: ViewManager): WorkspaceConfig = {
    viewManager = m
    this
  }

  def withWorld(w: World with CompilationManagement): WorkspaceConfig = {
    world = w
    withDefaultJobManager
  }

  def tap(f: WorkspaceConfig => WorkspaceConfig): WorkspaceConfig = {
    f(this)
  }
}

