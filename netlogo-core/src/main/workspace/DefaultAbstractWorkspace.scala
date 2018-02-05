// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.Femto
import org.nlogo.agent.World
import org.nlogo.api.{ AggregateManagerInterface, SourceOwner }
import org.nlogo.nvm.{ CompilerFlags, JobManagerInterface, JobManagerOwner,
  Optimizations, PresentationCompilerInterface }

object DefaultAbstractWorkspace {
  class DefaultDependencies(_world: World,
    _compiler: PresentationCompilerInterface,
    _hubNetManagerFactory: HubNetManagerFactory,
    _sourceOwners: Seq[SourceOwner],
    flags: CompilerFlags) extends WorkspaceDependencies {
    val world: World = _world
    val compiler: PresentationCompilerInterface = _compiler
    val hubNetManagerFactory: HubNetManagerFactory = _hubNetManagerFactory
    val sourceOwners = _sourceOwners
    lazy val userInteraction: UserInteraction = DefaultUserInteraction
    lazy val messageCenter: WorkspaceMessageCenter = new WorkspaceMessageCenter()
    lazy val owner: JobManagerOwner = new HeadlessJobManagerOwner(messageCenter)
    lazy val modelTracker: ModelTracker = new ModelTrackerImpl(messageCenter)
    lazy val jobManager = Femto.get[JobManagerInterface]("org.nlogo.job.JobManager", owner, world)
    lazy val evaluator = new Evaluator(jobManager, compiler, world, flags)
    lazy val extensionManager =
      new ExtensionManager(userInteraction, evaluator, messageCenter, modelTracker, new JarLoader(modelTracker))
    lazy val compilerServices =
      new LiveCompilerServices(compiler, extensionManager, world, evaluator)
  }
  def defaultDependencies(_world: World,
    _compiler: PresentationCompilerInterface,
    _hubNetManagerFactory: HubNetManagerFactory,
    _sourceOwners: Seq[SourceOwner],
    flags: CompilerFlags) =
      new DefaultDependencies(_world, _compiler, _hubNetManagerFactory, _sourceOwners, flags)
}

abstract class DefaultAbstractWorkspace(deps: WorkspaceDependencies) extends AbstractWorkspace(deps) {
  def this(world: World, compiler: PresentationCompilerInterface,
    hubNetManagerFactory: HubNetManagerFactory, sourceOwners: Seq[SourceOwner],
    flags: CompilerFlags) = this(
      DefaultAbstractWorkspace.defaultDependencies(
        world, compiler, hubNetManagerFactory, sourceOwners, flags))

  def this(world: World,
    compiler: PresentationCompilerInterface,
    hubNetManagerFactory: HubNetManagerFactory,
    aggregateManager: AggregateManagerInterface) =
      this(world, compiler, hubNetManagerFactory, Seq(aggregateManager),
        CompilerFlags(optimizations = Optimizations.standardOptimizations))
}
