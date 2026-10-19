package org.nlogo.ls

import javax.swing.{ JFrame, SwingUtilities }

import org.nlogo.api.{ CommandRunnable, ExtensionException, Version, Workspace }
import org.nlogo.workspace.AbstractWorkspace

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.IterableHasAsScala

abstract class ChildModel(val parentWorkspace: Workspace, val modelID: Int) {
  lazy val evaluator = new Evaluator(modelID, name, workspace, parentWorkspace.asInstanceOf[AbstractWorkspace])

  private var _name: Option[String] = None
  def name_= (newName: String): Unit = {
    _name = Some(newName)
    updateFrameTitle()
  }
  def name: String = _name.getOrElse(workspace.getModelFileName)

  def ask(code: String, lets: Seq[(String, AnyRef)], args: Seq[AnyRef], rng: RNG): Notifying[Unit] =
    evaluator.command(code, lets, args, rng, parallel = true) // parallel is safe, so it's the default

  def of(code: String, lets: Seq[(String, AnyRef)], args: Seq[AnyRef], rng: RNG): Notifying[AnyRef] =
    evaluator.report(code, lets, args, rng, parallel = true) // parallel is safe, so it's the default

  def kill(): Unit = {
    Future {
      workspace.dispose
    }
    val disposeRunnable = new CommandRunnable { def run(): Unit = frame.foreach(_.dispose) }
    if (java.awt.EventQueue.isDispatchThread) {
      disposeRunnable.run()
    } else {
      parentWorkspace.waitFor(disposeRunnable)
    }
  }

  def halt(): Unit = workspace.halt()

  def path: String = workspace.getModelPath

  protected def updateFrameTitle(): Unit = frame.foreach(_.setTitle(frameTitle))
  def frameTitle = s"$name (LevelSpace model #$modelID)"
  def frame: Option[JFrame]

  def setSpeed(d: Double): Unit
  def workspace: AbstractWorkspace

  // can't change once model is loaded
  lazy val usesLevelSpace: Boolean =
    workspace.getExtensionManager.loadedExtensions.asScala.exists(
      _.getClass.toString == classOf[LevelSpace].toString
    )

  def show(): Unit = frame.foreach { f => onEDT { f.setVisible(true) } }
  def hide(): Unit = frame.foreach { f => onEDT { f.setVisible(false)} }
  def showAll(): Unit = {
    show()
    if (usesLevelSpace) {
      ask("ls:show-all ls:models", Seq(), Seq(), AuxRNG).waitFor
    }
  }
  def hideAll(): Unit = {
    hide()
    if (usesLevelSpace) {
      ask("ls:hide-all ls:models", Seq(), Seq(), AuxRNG).waitFor
    }
  }

  def seedRNG(rng: RNG, seed: Long): Unit = if (usesLevelSpace) {
    ask("ls:random-seed seed", Seq("seed" -> Double.box(seed.toDouble)), Seq(), rng).waitFor
  } else {
    rng(workspace).setSeed(seed)
  }

  /**
   * If on EDT already, runs the given function, otherwise, invokes it async on the EDT.
   * NoOps in headless.
   **/
  def onEDT(f: => Unit): Unit =
    if (!LevelSpace.isHeadless) {
      if (SwingUtilities.isEventDispatchThread)
        f
      else
        SwingUtilities.invokeLater(() => f)
    }

  private val modelsLoadingKey = "org.nlogo.ls.modelsLoading"
  private val noGeneratorKey = "org.nlogo.noGenerator"
  private val oldNoGeneratorKey = "org.nlogo.ls.oldNoGenerator"

  /**
    * Opens the model specified by the given path with the bytecode generator disabled.
    *
    * See https://github.com/NetLogo/LevelSpace/issues/123 for context. Basically, the bytecode generator results in
    * excessive numbers of classes being created for child model, resulting often in a CompressedClassSpace OOM.
    * We solve this by disabling the bytecode generator. This also significantly speeds up child model creation,
    * resulting in much better runtimes for many models.
    *
    * See https://github.com/NetLogo/LevelSpace/pull/140#issuecomment-533915713 for why this is so complicated.
    * Basically, we want to allow models to be opened in parallel (primarily for BehaviorSpace, but also for child
    * models that use LevelSpace), so don't want one model to turn the generator back on before another has checked
    * to see if it should use the generator or not. This is solves by tracking how many models are currently being
    * created. However, this is complicated by the fact that each LevelSpace instance exists in its own classloader, so
    * can't easily share a singleton to share this information. Using a system property to track the number of models
    * was the best solution I could think of. Another solution would require, for instance, walking up the tree of
    * classloaders to get to topmost LevelSpace's classloader to retrieve a singleton tracking this count. But that
    * seemed significantly more complicated.
    *
    * Other solutions considered:
    *
    * - Only allow one model to open at a time by synchronizing on a shared object. This significantly increases
    *   congestion and results in a significant performance hit.
    * - Re-enable the generator on `unload` for the topmost LevelSpace. However, this may result in the generator being
    *   disabled at inappropriate times, such as while compiling strings for `run` and `runresult`. This might still
    *   happen with the current solution, but it is significantly less likely. I also don't fully trust `unload` to
    *   always run.
    * @param opener Should be, e.g., workspace.open(String)
    * @param modelPath The path to the model
    */
  def  openModelWithoutGenerator(opener: String => Unit, modelPath: String): Unit = {
    // We synchronize on Version because it is shared by all LevelSpaces and because it is responsible for determining
    // whether or not to use the generator (and is thus at least somewhat related to the task at hand). Note that we
    // need a synchronized at all because, although the system properties methods are atomic, we need an atomic
    // read-and-update, which is not provided.
    Version.synchronized {
      val modelsLoading = Integer.getInteger(modelsLoadingKey, 0)
      if (modelsLoading <= 0) {
        // Store current value of noGenerator. Note that we don't know which model will end up restoring it, so we store
        // it in a system property.
        System.setProperty(oldNoGeneratorKey, System.getProperty(noGeneratorKey, "false"))
      }
      System.setProperty(modelsLoadingKey, (modelsLoading + 1).toString)
      System.setProperty(noGeneratorKey, "true")
    }
    try {
      opener(modelPath)
    } catch {
      case e: IllegalStateException =>
        throw new ExtensionException(
          s"$modelPath is from an incompatible version of NetLogo. Try opening it in NetLogo to convert it.", e
        )
    } finally {
      Version.synchronized {
        val modelsLeft = Integer.getInteger(modelsLoadingKey) - 1
        if (modelsLeft == 0) {
          // Turn the generator back on when all models are done loading
          System.setProperty(noGeneratorKey, System.getProperty(oldNoGeneratorKey))
        }
        System.setProperty(modelsLoadingKey, modelsLeft.toString)
      }
    }
  }
}
