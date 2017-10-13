// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.Femto
import org.nlogo.agent.World
import org.nlogo.api.{ AggregateManagerInterface, SourceOwner }
import org.nlogo.nvm.{ CompilerFlags, JobManagerInterface, JobManagerOwner,
  Optimizations, PresentationCompilerInterface }

object DefaultAbstractWorkspace {
  def defaultDependencies(_world: World,
    _compiler: PresentationCompilerInterface,
    _hubNetManagerFactory: HubNetManagerFactory,
    _sourceOwners: Seq[SourceOwner],
    flags: CompilerFlags) = new WorkspaceDependencies {
    val compiler: PresentationCompilerInterface = _compiler
    val world: World = _world
    val hubNetManagerFactory: HubNetManagerFactory = _hubNetManagerFactory
    val userInteraction: UserInteraction = DefaultUserInteraction
    val messageCenter: WorkspaceMessageCenter = new WorkspaceMessageCenter()
    val owner: JobManagerOwner = new HeadlessJobManagerOwner(messageCenter)
    val modelTracker: ModelTracker = new ModelTrackerImpl(messageCenter)
    val jobManager = Femto.get[JobManagerInterface]("org.nlogo.job.JobManager", owner, world)
    val evaluator = new Evaluator(jobManager, compiler, world, flags)
    val extensionManager =
      new ExtensionManager(userInteraction, evaluator, messageCenter, modelTracker, new JarLoader(modelTracker))
    val compilerServices =
      new LiveCompilerServices(compiler, extensionManager, world, evaluator)
    val sourceOwners = _sourceOwners
  }
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
