// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, FileDialog => AwtFileDialog, Frame }
import java.awt.EventQueue.isDispatchThread
import java.awt.image.BufferedImage
import java.io.{ File, FileOutputStream, IOException, PrintWriter }
import javax.swing.JOptionPane
import javax.imageio.ImageIO

import org.nlogo.agent.World
import org.nlogo.api.{ ControlSet, Exceptions, LocalFile, ModelSettings, FileIO }
import org.nlogo.awt.{ EventQueue, Hierarchy, UserCancelException }
import org.nlogo.core.{ I18N, FileMode }
import org.nlogo.plot.Plot
import org.nlogo.swing.{ FileDialog, OptionDialog, ModalProgressTask }
import org.nlogo.swing.Implicits.thunk2runnable
import org.nlogo.shape.ShapeConverter
import org.nlogo.workspace.{ AbstractWorkspaceScala, ExportOutput, HubNetManagerFactory }
import org.nlogo.window.Events.{ ExportPlotEvent, ExportWidgetEvent, ExportWorldEvent, LoadModelEvent }

import scala.util.{ Failure, Success, Try }
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.{ Duration, MILLISECONDS }

abstract class GUIWorkspaceScala(world: World,
  hubNetManagerFactory: HubNetManagerFactory,
  protected val frame: Frame,
  controlSet: ControlSet)
  extends AbstractWorkspaceScala(world, hubNetManagerFactory)
  with ExportPlotEvent.Handler
  with ExportWidgetEvent.Handler
  with LoadModelEvent.Handler {

  def view: View

  val viewManager = new ViewManager()

  def getFrame: Frame = frame

  val plotExportControls = new PlotExportControls(plotManager)

  // for grid snap
  private var _snapOn: Boolean = false

  def setSnapOn(snapOn: Boolean): Unit = {
    _snapOn = snapOn
  }

  def snapOn = _snapOn

  override def exportDrawingToCSV(writer: PrintWriter): Unit = {
    view.renderer.trailDrawer.exportDrawingToCSV(writer)
  }

  override def exportOutputAreaToCSV(writer: PrintWriter): Unit = {
    new Events.ExportWorldEvent(writer).raise(this)
  }

  def handle(e: LoadModelEvent): Unit = {
    loadFromModel(e.model)
    world.turtleShapes.replaceShapes(e.model.turtleShapes.map(ShapeConverter.baseShapeToShape))
    world.linkShapes.replaceShapes(e.model.linkShapes.map(ShapeConverter.baseLinkShapeToLinkShape))
    e.model.optionalSectionValue[ModelSettings]("org.nlogo.modelsection.modelsettings") match {
      case Some(settings: ModelSettings) => setSnapOn(settings.snapToGrid)
      case _ =>
    }
  }

  def handle(e: ExportWidgetEvent): Unit = {
    try {
      val guessedName = guessExportName(e.widget.getDefaultExportName)
      val exportPath = FileDialog.show(e.widget, I18N.gui.get("menu.file.export"), AwtFileDialog.SAVE, guessedName)
      val exportToPath = Future.successful(exportPath)
      e.widget match {
        case pw: PlotWidget =>
          new ExportPlotEvent(PlotWidgetExport.ExportSinglePlot(pw.plot), exportPath, {() => }).raise(pw)
        case ow: OutputWidget =>
          import java.util.StringTokenizer
          exportToPath
            .map((filename: String) => (filename, ow.valueText))(SwingUnlockedExecutionContext)
            .onComplete({
              case Success((filename: String, text: String)) =>
                ExportOutput.silencingErrors(filename, text)
              case Failure(_) =>
            })(NetLogoExecutionContext.backgroundExecutionContext)
        case ib: InputBox =>
          exportToPath
            .map((filename: String) => (filename, ib.valueText))(SwingUnlockedExecutionContext)
            .map[Unit]({ // background
              case (filename: String, text: String) => FileIO.writeFile(filename, text, true); ()
            })(NetLogoExecutionContext.backgroundExecutionContext).failed.foreach({ // on UI Thread
              case ex: java.io.IOException =>
                ExportControls.displayExportError(Hierarchy.getFrame(ib),
                  I18N.gui.get("menu.file.export.failed"),
                  I18N.gui.getN("tabs.input.export.error", ex.getMessage))
            })(SwingUnlockedExecutionContext)
        case _ =>
      }
    } catch {
      case uce: UserCancelException => Exceptions.ignore(uce)
    }
  }

  protected def plotExportOperation(e: ExportPlotEvent): Future[Unit] = {
    def runAndComplete(f: () => Unit, onError: IOException => Unit): Unit = {
      try {
        f()
      } catch {
        case ex: IOException =>
          e.onCompletion.run()
          onError(ex)
      } finally {
        e.onCompletion.run()
      }
    }
    e.plotExport match {
      case PlotWidgetExport.ExportAllPlots =>
        if (plotManager.getPlotNames.isEmpty) {
          plotExportControls.sorryNoPlots(getFrame)
          e.onCompletion.run()
          Future.successful(())
        } else
          Future {
            runAndComplete(
              () => super.exportAllPlots(e.exportFilename),
              (ex) => plotExportControls.allPlotExportFailed(getFrame, e.exportFilename, ex))
          } (new LockedBackgroundExecutionContext(world))
      case PlotWidgetExport.ExportSinglePlot(plot) =>
        Future {
          runAndComplete(
            () => super.exportPlot(plot.name, e.exportFilename),
            (ex) => plotExportControls.singlePlotExportFailed(getFrame, e.exportFilename, plot, ex))
        } (new LockedBackgroundExecutionContext(world))
    }
  }

  def handle(e: ExportPlotEvent): Unit = {
    plotExportOperation(e)
  }

  def getExportWindowFrame: Component =
    viewManager.getPrimary.getExportWindowFrame

  def exportView: BufferedImage =
    viewManager.getPrimary.exportView

  @throws(classOf[IOException])
  def exportView(filename: String, format: String): Unit = {
    if (jobManager.onJobThread) {
      val viewFuture =
        Future(viewManager.getPrimary.exportView)(new SwingLockedExecutionContext(world))
      val image = awaitFutureFromJobThread(viewFuture)
      FileIO.writeImageFile(image, filename, format)
    } else {
      exportViewFromUIThread(filename, format)
    }
  }

  @throws(classOf[java.io.IOException])
  def exportViewFromUIThread(filename: String, format: String, onCompletion: () => Unit = {() => }): Unit = {
    val f =
      Future[BufferedImage](viewManager.getPrimary.exportView)(new SwingLockedExecutionContext(world))
    f.foreach({ image =>
      try {
        FileIO.writeImageFile(image, filename, format)
      } catch {
        case ex: java.io.IOException =>
          onCompletion()
          ExportControls.displayExportError(getExportWindowFrame, ex.getMessage)
      } finally {
        onCompletion()
      }
    })(NetLogoExecutionContext.backgroundExecutionContext)
  }

  def doExportView(exportee: LocalViewInterface): Unit = {
    val exportPathOption =
      try {
        Some(FileDialog.show(getExportWindowFrame, I18N.gui.get("menu.file.export.view"), AwtFileDialog.SAVE, guessExportName("view.png")))
      } catch {
        case ex: UserCancelException =>
          Exceptions.ignore(ex)
          None
      }
    exportPathOption.foreach { exportPath =>
      ModalProgressTask.onBackgroundThreadWithUIData(
        getFrame, I18N.gui.get("dialog.interface.export.task"), () => (exportee.exportView, exportPath),
        { (data: (BufferedImage, String)) =>
          data match {
            case (exportedView, path) =>
              try {
                FileIO.writeImageFile(exportedView, path, "png")
              } catch {
                case ex: IOException =>
                  ExportControls.displayExportError(getExportWindowFrame, ex.getMessage)
              }
        } })
    }
  }

  private def awaitFutureFromJobThread[A](future: Future[A]): A = {
    var res = Option.empty[A]
    while (res.isEmpty) {
      world.synchronized { world.wait(50) }
      try {
        res = Some(awaitFuture(future, 1))
      } catch {
        case e: java.util.concurrent.TimeoutException => // ignore
      }
    }
    res.get
  }

  private def awaitFuture[A](future: Future[A], millis: Long): A = {
    Await.result(future, Duration(millis, MILLISECONDS))
  }

  @throws(classOf[IOException])
  def exportInterface(filename: String): Unit = {
    if (jobManager.onJobThread) {
      // we treat the job thread differently because it will be holding the world lock
      FileIO.writeImageFile(awaitFutureFromJobThread(controlSet.userInterface), filename, "png")
    } else {
      exportInterfaceFromUIThread(filename, { () => })
    }
  }

  def exportInterfaceFromUIThread(filename: String, onCompletion: () => Unit = { () => }): Unit = {
    controlSet.userInterface.foreach({ uiImage =>
      try {
        FileIO.writeImageFile(uiImage, filename, "png")
      } catch {
        case ex: IOException =>
          onCompletion()
          ExportControls.displayExportError(getExportWindowFrame, ex.getMessage)
      } finally {
        onCompletion()
      }
    })(NetLogoExecutionContext.backgroundExecutionContext)
  }

  @throws(classOf[IOException])
  def exportOutput(filename: String): Unit = {
    if (jobManager.onJobThread) {
      val text = awaitFutureFromJobThread(controlSet.userOutput)
      ExportOutput.throwingErrors(filename, text)
    } else
      ModalProgressTask.onBackgroundThreadWithUIData(frame, I18N.gui.get("dialog.interface.export.task"),
        () => controlSet.userOutput,
        (textFuture: Future[String]) => ExportOutput.silencingErrors(filename, awaitFuture(textFuture, 1000)))
  }
}
