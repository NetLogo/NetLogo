// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.SourceOwner
import org.nlogo.agent.World
import org.nlogo.nvm.{ JobManagerOwner,
  JobManagerInterface, PresentationCompilerInterface }

// This trait exists because adding default parameters which depend on each other
// to contructors is a Massive pain.
trait WorkspaceDependencies {
  def compiler: PresentationCompilerInterface
  def compilerServices: LiveCompilerServices
  def extensionManager: ExtensionManager
  def evaluator: Evaluator
  def hubNetManagerFactory: HubNetManagerFactory
  def jobManager: JobManagerInterface
  def messageCenter: WorkspaceMessageCenter
  def modelTracker: ModelTracker
  def owner: JobManagerOwner
  def sourceOwners: Seq[SourceOwner]
  def userInteraction: UserInteraction
  def world: World
}
