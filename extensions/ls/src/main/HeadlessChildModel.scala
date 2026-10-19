package org.nlogo.ls

import javax.swing.Timer

import org.nlogo.agent.{ CompilationManagement, OutputObject, World, World2D, World3D }
import org.nlogo.api._
import org.nlogo.nvm.{ Context, Instruction }
import org.nlogo.headless.HeadlessWorkspace
import org.nlogo.ls.gui.ViewFrame
import org.nlogo.workspace.AbstractWorkspace

class HeadlessChildModel (parentWorkspace: AbstractWorkspace, path: String, modelID: Int)
  extends ChildModel(parentWorkspace, modelID) {

  val world: World & CompilationManagement = if(Version.is3D) new World3D() else new World2D()

  var frame: Option[ViewFrame] = None

  val workspace: HeadlessWorkspace = new HeadlessWorkspace(
      world,
      new org.nlogo.compile.Compiler(if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect),
      new org.nlogo.render.Renderer(world),
      new org.nlogo.sdm.AggregateManagerLite) {

    override def sendOutput(oo: OutputObject, toOutputArea: Boolean): Unit = {
      if (LevelSpace.isHeadless) {
        workspace.sendOutput(oo, toOutputArea)
      } else {
        frame.foreach { f => onEDT {
          new org.nlogo.window.Events.OutputEvent(false, oo, false, !toOutputArea, System.currentTimeMillis).raise(f)
        }}
      }
    }

    override def runtimeError(owner: JobOwner, context: Context, instruction: Instruction, ex: Exception): Unit = {
      // TODO why isn't this used?
    }

    // `force` is essentially ignored. Painting headless child models is always asynchronous.
    override def requestDisplayUpdate(force: Boolean): Unit = {
      super.requestDisplayUpdate(force)
      updateDisplay(false)
    }

    private val minTimeBetweenRepaints: Long = 30
    // Only the EDT will be modifying this variable, so we don't need it to be atomic. It does need to be volatile
    // since the job thread will be reading it.
    @volatile private var lastRepaintTime: Long = 0
    private def timeSinceLastRepaint: Long = System.currentTimeMillis() - lastRepaintTime
    private val scheduledRepaint: Timer = new Timer(0, { _ =>
      frame.foreach(_.repaint())
      // This should happen after the repaint, as the repaint locks the world, and thus, the repaint call may take time
      // before the repaint actually takes place.
      lastRepaintTime = System.currentTimeMillis()
    })

    scheduledRepaint.setRepeats(false)
    // Since we never block on painting child models, we don't care if we have a world lock or not.
    override def updateDisplay(ignored: Boolean): Unit = {
      if (!LevelSpace.isHeadless) {
        frame.foreach { f =>
          if (f.isVisible && !scheduledRepaint.isRunning) {
            // Not that if we don't max(0) here, the conversion to int can underflow
            val nextRepaint = (minTimeBetweenRepaints - timeSinceLastRepaint).max(0).toInt
            scheduledRepaint.setDelay(nextRepaint)
            scheduledRepaint.start()
          }
        }
      }
    }
  }

  openModelWithoutGenerator(workspace.open(_), path)

  override def show(): Unit = onEDT {
    val f = frame.getOrElse { new ViewFrame(workspace) }
    frame = Some(f)
    updateFrameTitle()
    syncTheme()
    super.show()
    workspace.requestDisplayUpdate(false)
  }

  def isVisible: Boolean =
    !LevelSpace.isHeadless && frame.exists(_.isVisible)

  def setSpeed(d: Double): Unit = {}

  def tryEagerAsk(code: String, lets: Seq[(String, AnyRef)], args: Seq[AnyRef], rng: RNG): Notifying[Unit] =
    evaluator.command(code, lets, args, rng, parallel = usesLevelSpace || isVisible)

  def tryEagerOf(code: String, lets: Seq[(String, AnyRef)], args: Seq[AnyRef], rng: RNG): Notifying[AnyRef] =
    evaluator.report(code, lets, args, rng, parallel = usesLevelSpace || isVisible)

  def syncTheme(): Unit = {
    frame.foreach(_.syncTheme())
  }
}
