// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ Component, FileDialog }
import java.io.{ File, IOException }
import java.nio.file.{ Files, Paths }
import java.util.Locale

import org.nlogo.api.FileIO
import org.nlogo.app.common.{ AppTabsInterface, Dialogs, ModelConfig }
import org.nlogo.awt.UserCancelException
import org.nlogo.core.{ I18N, Model }
import org.nlogo.nvm.IncludeSource
import org.nlogo.swing.{ CloseableTab, FileDialog => SwingFileDialog, OptionPane, RenameableTab, UserAction }
import org.nlogo.util.PathUtils
import org.nlogo.window.{ Events => WindowEvents, ExternalFileInterface, GUIWorkspace }

import scala.util.matching.Regex

object TemporaryCodeTab {
  private[app] def stripPath(filename: String): String = filename.split(Regex.quote("/")).last
}

class TemporaryCodeTab(workspace: GUIWorkspace,
  tabs:                           AppTabsInterface,
  private var _filename:          AppTabsInterface.Filename,
  externalFileManager:            ExternalFileManager,
  conversionAction:               TemporaryCodeTab => UserAction.MenuAction,
  separateCodeWindow:             Boolean)
  extends CodeTab(workspace, tabs) with CloseableTab with RenameableTab {

  private var includesTable: Option[Map[String, IncludeSource]] = getIncludesTable

  var closing = false
  var saveNeeded = false // Has the buffer changed since the file was saved?

  checkCompilable(false)

  def filename: Either[String, String] = _filename

  def filename_=(newName: Either[String, String]): Unit = {
    val oldFilename = _filename
    _filename = newName.map(PathUtils.standardize)
    if (oldFilename.isRight && oldFilename != _filename) {
      externalFileManager.nameChanged(oldFilename.getOrElse(null), _filename.getOrElse(null))
    } else if (oldFilename.isLeft) {
      externalFileManager.add(this)
    }
  }

  def reload: Boolean = {
    var loaded: Boolean = false

    filename foreach { path =>
      val newPath: String = ModelConfig.findIncludeAutoSave(Paths.get(path).toString).fold(path) { autosavePath =>
        if (new OptionPane(workspace.getFrame, I18N.gui.get("file.autosave.recover"),
                           I18N.gui.get("file.autosave.recover.include"), OptionPane.Options.YesNo,
                           OptionPane.Icons.info).getSelectedIndex == 0) {
          autosavePath.toString
        } else {
          path
        }
      }

      try {
        innerSource = FileIO.fileToString(newPath).replaceAll("\r\n", "\n")
        dirty = false // Has the buffer changed since it was compiled?
        saveNeeded = false
        loaded = true
      } catch {
        case _: IOException => innerSource = ""
      }

      if (path == newPath) {
        ModelConfig.discardNewIncludeAutoSaves(path)
      } else {
        save(false)
      }
    }

    loaded
  }

  // We're actually loading _filename for the first time here.
  if (reload) {
    externalFileManager.add(this)
  }

  setSeparate(separateCodeWindow)
  lineNumbersVisible = tabs.lineNumbersVisible
  setCompleteOnType(tabs.getCompleteOnType)

  override def activeMenuActions: Seq[UserAction.MenuAction] =
    super.activeMenuActions ++ filename.fold(_ => Seq(), name => Seq(conversionAction(this)))

  // if included file is not saved and not referenced in the main Code tab,
  // disable Check button and show warning banner (Isaac B 6/26/25)
  private def checkCompilable(dirty: Boolean): Unit = {
    if (includesTable.exists(_.exists(_._2.file == filename.getOrElse(null)))) {
      compileButton.setEnabled(dirty)
      errorLabel.setWarning(None)
    } else {
      compileButton.setEnabled(false)
      errorLabel.setWarning(Some(I18N.gui.get("tabs.code.cannotCompile")))
    }
  }

  override def dirty_=(d: Boolean) = {
    checkCompilable(d)

    _dirty = d

    if (d) {
      saveNeeded = true

      new WindowEvents.DirtyEvent(Some(filename.merge)).raise(this)
    }
  }

  def filenameForDisplay = (filename map TemporaryCodeTab.stripPath).merge

  def save(saveAs: Boolean) = {
    if (saveAs || filename.isLeft) {
      val path: String = userChooseSavePath()
      val name: String = new File(path).getName.stripSuffix(".nlm")

      if (workspace.isValidIdentifier(name, workspace.getExtensionManager)) {
        filename = Right(path)
      } else {
        new OptionPane(workspace.getFrame, I18N.gui.get("tabs.code.invalidName"),
                       I18N.gui.getN("tabs.code.invalidName.message", s"$name.nlm"), OptionPane.Options.Ok,
                       OptionPane.Icons.error)

        throw new UserCancelException
      }
    }

    // Stop watching here so that the watcher thread doesn't detect our own write. (Isaac B 11/26/25)
    tabs.stopWatcherThread()

    FileIO.writeFile(filename.getOrElse(null), text.getText)
    saveNeeded = false

    tabs.externalFileSaved(filename.merge)

    compile()
  }

  override def rename(): Unit = {
    val original: Either[String, String] = filename

    try {
      save(true)

      original.foreach(path => Files.delete(Paths.get(path)))
    } catch {
      case _: UserCancelException =>
    }
  }

  override def close(): Unit = {
    if (saveNeeded) {
      try {
        if (Dialogs.userWantsToSaveFirst(filenameForDisplay, this)) {
          // The user is saving the file with its current name
          save(false)
        }
      } catch {
        case _: UserCancelException =>
          return
      }
    }
    closing = true
    tabs.closeExternalFile(_filename)
  }

  override def loadModel(model: Model): Unit = {}

  override def handle(e: WindowEvents.CompiledEvent) = {
    def setErrorLabel() = errorLabel.setError(Option(e.error), e.sourceOwner.headerSource.size)

    dirty = false
    e.sourceOwner match {
      case file: ExternalFileInterface if file.getFileName == filename.getOrElse(null) => setErrorLabel()
      // if the Code tab compiles then get rid of the error ev 7/26/07
      case tab: CodeTab if e.error == null =>
        includesTable = getIncludesTable

        setErrorLabel()

        val include: Boolean = includesTable.exists(_.exists {
          case (_, IncludeSource(file, _)) if file == filename.getOrElse(null) =>
            true

          case _ =>
            false
        })

        if (include) {
          text.setCoreProgram(false)
          setProgram()
        } else {
          text.setCoreProgram(true)
          text.unsetProgram()
        }

      case _ =>
    }
  }

  override def switchedTabs(oldTab: Component, newTab: Component): Unit = {
    if (!closing)
      super.switchedTabs(oldTab, newTab)
  }

  private def userChooseSavePath(): String = {
    def appendIfNecessary(str: String, suffix: String) = if (str.endsWith(suffix)) str else str + suffix

    val newFileName = appendIfNecessary(filenameForDisplay, ".nlm")
    val path = SwingFileDialog.showFiles(this, I18N.gui.get("file.save.external"), FileDialog.SAVE, newFileName)
    appendIfNecessary(path, ".nlm")
  }

  override protected def getProcedures(): Seq[String] = {
    if (filename.merge.toUpperCase(Locale.ROOT).endsWith(".NLM")) {
      workspace.procedures.keys.collect {
        case (proc, Some(module)) if filename.merge == module =>
          proc
      }.toSeq
    } else {
      super.getProcedures()
    }
  }

  override def setProgram(): Unit = {
    filename match {
      case Right(x) => {
        val extensionManager = workspace.getExtensionManager
        val extensionCommands = extensionManager.extensionCommandNames.filter(extensionManager.isVisibleIn(_, Some(x)))
        val extensionReporters = extensionManager.extensionReporterNames.filter(extensionManager.isVisibleIn(_, Some(x)))

        text.setProgram(workspace.world.program, getProcedures(), extensionCommands.toSeq, extensionReporters.toSeq)
      }
      case _ => {
        text.setProgram(workspace.world.program, getProcedures(), Seq(), Seq())
      }
    }
  }
}
