// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{ Dialect, DummyCompilationEnvironment, Femto }
import org.nlogo.api.{ NetLogoLegacyDialect, NetLogoThreeDDialect, Version }
import org.nlogo.agent.{ World2D, World3D }
import org.nlogo.nvm.{ JobManagerInterface, JobManagerOwner,
  PresentationCompilerInterface, Procedure }

object Helper {
  def defaultDialect =
    if (Version.is3D) NetLogoThreeDDialect
    else              NetLogoLegacyDialect

  def default: Helper = new Helper(defaultDialect)
  def withDialect(dialect: Dialect): Helper = new Helper(dialect)
  class DummyJobManagerOwner extends JobManagerOwner {
    private def unsupported = throw new UnsupportedOperationException
    def ownerFinished(owner: org.nlogo.api.JobOwner): Unit = unsupported
    def periodicUpdate(): Unit = unsupported
    def runtimeError(owner: org.nlogo.api.JobOwner,
      manager: JobManagerInterface,
      context: org.nlogo.nvm.Context,
      instruction: org.nlogo.nvm.Instruction,
      ex: Exception): Unit = unsupported
    def updateDisplay(haveWorldLockAlready: Boolean,forced: Boolean): Unit = unsupported
  }
}

import Helper._

class Helper(dialect: Dialect) {
  lazy val messageCenter = new WorkspaceMessageCenter()
  lazy val modelTracker = new ModelTrackerImpl(messageCenter)
  lazy val world =
    if (dialect.is3D) new World3D()
    else              new World2D()
  lazy val compilationEnvironment =
    new DummyCompilationEnvironment()
  lazy val compiler =
    Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler", dialect)
  lazy val jobManager =
    Femto.get[JobManagerInterface]("org.nlogo.job.JobManager", new DummyJobManagerOwner(), world)
  lazy val evaluator = new Evaluator(jobManager, compiler, world)
  lazy val userInteraction = DefaultUserInteraction
  lazy val jarLoader = new JarLoader(modelTracker)
  lazy val linker = new Evaluator.Linker {
    def link(p: Procedure): Procedure = ???
  }
  lazy val extensionManager =
    new ExtensionManager(userInteraction, evaluator, messageCenter, jarLoader)
}
