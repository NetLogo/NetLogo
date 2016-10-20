// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Component
import javax.swing.Action

import org.nlogo.awt.UserCancelException
import org.nlogo.agent.ImportPatchColors
import org.nlogo.api.LocalFile
import org.nlogo.plot.Plot
import org.nlogo.swing.{ Implicits, UserAction },
  Implicits.thunk2runnable,
  UserAction.{ ActionCategoryKey, ActionSubcategoryKey, FileCategory, FileExportSubcategory, FileImportSubcategory }
import org.nlogo.window.{ Events => WindowEvents, FileController, GUIWorkspace, PlotWidgetExport },
  WindowEvents.{ ExportPlotEvent }

object FileActions {

  def apply(workspace: GUIWorkspace, parent: java.awt.Component): Seq[Action] = {
    val baseActions = Seq(
      new ExportWorldAction(workspace, parent),
      new ExportGraphicsAction(workspace, parent),
      new ExportOutputAction(workspace, parent),
      new ExportPlotAction(workspace, parent),
      new ExportAllPlotsAction(workspace, parent),
      new ExportInterfaceAction(workspace, parent),
      new ImportWorldAction(workspace, parent),
      new ImportPatchColorsAction(workspace, parent),
      new ImportPatchColorsRGBAction(workspace, parent)
    )
    val twodSpecificActions = Seq(new ImportDrawingAction(workspace, parent))
    baseActions ++ twodSpecificActions
  }

  class ExportWorldAction(workspace: GUIWorkspace, parent: Component) extends ExportAction("world", workspace.guessExportName("world.csv"), parent, workspace.exportWorld _) {
    putValue(ActionCategoryKey,    FileCategory)
    putValue(ActionSubcategoryKey, FileExportSubcategory)
  }

  class ExportGraphicsAction(workspace: GUIWorkspace, parent: Component)
    extends ExportBackgroundAction[String](parent, "view", workspace.guessExportName("view.png")) {
      putValue(ActionCategoryKey,    FileCategory)
      putValue(ActionSubcategoryKey, FileExportSubcategory)

      def beforeModalDialog(): String = promptForFilePath()

      def inModalDialog(filename: String, closeDialog: () => Unit): Unit = {
        workspace.exportViewFromUIThread(filename, "png", closeDialog)
      }
  }

  class ExportOutputAction(workspace: GUIWorkspace, parent: Component)
    extends ExportAction("output", workspace.guessExportName("output.txt"), parent, { exportPath =>
    workspace.exportOutput(exportPath)
  }) {
    putValue(ActionCategoryKey,    FileCategory)
    putValue(ActionSubcategoryKey, FileExportSubcategory)
  }

  class ExportPlotAction(workspace: GUIWorkspace, parent: Component)
    extends ExportBackgroundAction[(String, Plot)](parent, "plot", workspace.guessExportName("plot.csv")) {

    putValue(ActionCategoryKey,    FileCategory)
    putValue(ActionSubcategoryKey, FileExportSubcategory)

    def beforeModalDialog(): (String, Plot) = {
      val plot = workspace.plotExportControls.choosePlot(frame)
        .getOrElse(throw new UserCancelException())
      val filepath = promptForFilePath()
      (filepath, plot)
    }

    def inModalDialog(filenameAndPlot: (String, Plot), closeDialog: () => Unit): Unit = {
      new ExportPlotEvent(PlotWidgetExport.ExportSinglePlot(filenameAndPlot._2), filenameAndPlot._1, closeDialog)
        .raise(parent)
    }
  }

  class ExportAllPlotsAction(workspace: GUIWorkspace, parent: Component)
    extends ExportBackgroundAction[String](parent, "allPlots", workspace.guessExportName("plots.csv")) {

    putValue(ActionCategoryKey,    FileCategory)
    putValue(ActionSubcategoryKey, FileExportSubcategory)

    def beforeModalDialog(): String = {
      if (workspace.plotExportControls.plotNames.isEmpty) {
        workspace.plotExportControls.sorryNoPlots(frame)
        throw new UserCancelException()
      } else
        promptForFilePath()
    }

    def inModalDialog(filename: String, closeDialog: () => Unit): Unit = {
      new ExportPlotEvent(PlotWidgetExport.ExportAllPlots, filename, closeDialog)
        .raise(parent)
    }
  }

  class ExportInterfaceAction(workspace: GUIWorkspace, parent: Component)
    extends ExportBackgroundAction[String](parent, "interface", workspace.guessExportName("interface.png")) {

    putValue(ActionCategoryKey,    FileCategory)
    putValue(ActionSubcategoryKey, FileExportSubcategory)

    def beforeModalDialog(): String = promptForFilePath()

    def inModalDialog(filename: String, closeDialog: () => Unit): Unit = {
      workspace.exportInterfaceFromUIThread(filename, closeDialog)
    }
  }

  class ImportWorldAction(workspace: GUIWorkspace, parent: Component)
  extends ImportAction("world", parent, { importPath =>
    workspace.importWorld(importPath)
    workspace.view.dirty()
    workspace.view.repaint()
    }) {
      putValue(ActionCategoryKey,    FileCategory)
      putValue(ActionSubcategoryKey, FileImportSubcategory)
    }

  class ImportPatchColorsAction(workspace: GUIWorkspace, parent: Component)
  extends ImportAction("patchColors", parent, { importPath =>
    // We can't wait for the thread to complete, or we end
    // up locking up the app since the Model Dialog and the
    // job wedge against one another. -- CLB 07/19/05
    ImportPatchColors.importPatchColors(
      new LocalFile(importPath),
      workspace.world, true)
    workspace.view.dirty()
    workspace.view.repaint()
  }) {
    putValue(ActionCategoryKey,    FileCategory)
    putValue(ActionSubcategoryKey, FileImportSubcategory)
  }

  class ImportPatchColorsRGBAction(workspace: GUIWorkspace, parent: Component)
  extends ImportAction("patchColorsRGB", parent, { importPath =>
    // We can't wait for the thread to complete, or we end
    // up locking up the app since the Model Dialog and the
    // job wedge against one another. -- CLB 07/19/05
    ImportPatchColors.importPatchColors(
      new LocalFile(importPath), workspace.world, false)
    workspace.view.dirty()
    workspace.view.repaint()
  }) {
    putValue(ActionCategoryKey,    FileCategory)
    putValue(ActionSubcategoryKey, FileImportSubcategory)
  }

  class ImportDrawingAction(workspace: GUIWorkspace, parent: Component)
  extends ImportAction("drawing", parent, { importPath =>
    workspace.importDrawing(importPath);
    workspace.view.dirty();
    workspace.view.repaint();
  }) {
    putValue(ActionCategoryKey,    FileCategory)
    putValue(ActionSubcategoryKey, FileImportSubcategory)
  }
}
