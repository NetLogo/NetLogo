// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Dialog, FileDialog => AWTFileDialog }
import java.awt.event.ActionEvent
import java.nio.file.Paths
import java.net.URI
import javax.swing.{ AbstractAction, JComponent }

import org.nlogo.api.{ FileIO, ModelReader, Version }
import org.nlogo.awt.UserCancelException
import org.nlogo.core.{ LocaleContext, Model }
import org.nlogo.fileformat.{ ConversionError, ConversionWithErrors, ErroredConversion, FailedConversionResult }
import org.nlogo.swing.{ BrowserLauncher, Button, FileDialog, MessageDialog, OptionPane }
import org.nlogo.workspace.{ ModelTracker, OpenModel, SaveModel },
  OpenModel.{ Controller => OpenModelController },
  SaveModel.{ Controller => SaveModelController }

import scala.util.Try

class FileController(owner: Component, modelTracker: ModelTracker) extends OpenModelController with SaveModelController {
  // OpenModel.Controller methods
  def errorOpeningURI(uri: URI, exception: Exception): Unit = {
    println(exception)
    exception.printStackTrace()
    new OptionPane(owner, loc"Error", loc"NetLogo can't open the model ${Paths.get(uri)}:\n\n${exception.getMessage}",
                   OptionPane.Options.Ok, OptionPane.Icons.error)
  }

  def errorAutoconvertingModel(res: FailedConversionResult): Option[Model] =
    showAutoconversionError(res, loc"Model")

  def showAutoconversionError(res: FailedConversionResult, base: String): Option[Model] = {
    res.errors.foreach(_.errors.foreach { e =>
      println(e)
      e.printStackTrace()
    })
    val dialog = new AutoConversionErrorDialog(owner, base)
    dialog.doShow(res)
    dialog.modelToOpen
  }

  @throws(classOf[IllegalStateException])
  def invalidModel(uri: URI): Unit = {
    notifyUserNotValidFile(uri)
  }

  def invalidModelVersion(uri: URI, version: String): Unit = {
    notifyUserNotValidFile(uri)
  }

  def shouldOpenModelOfDifferingArity(arity: Int, version: String): Boolean = {
    try {
      if (arity == 3)
        checkWithUserBeforeOpening3DModelin2D(version)
      else
        checkWithUserBeforeOpening2DModelin3D()
      true
    } catch {
      case ex: UserCancelException => false
    }
  }

  def shouldOpenModelOfUnknownVersion(version: String): Boolean = {
    try {
      checkWithUserBeforeOpeningModelFromFutureVersion(version);
      true
    } catch {
      case ex: UserCancelException => false
    }
  }

  def shouldOpenModelOfLegacyVersion(version: String): Boolean = {
    showVersionWarningAndGetResponse(version)
  }

  @throws(classOf[UserCancelException])
  def checkWithUserBeforeOpeningModelFromFutureVersion(version: String): Unit = {
    val message = loc"""|You are attempting to open a model that was created in a newer version of NetLogo.
                        |(This is ${Version.version}; the model was created in $version.) NetLogo can try to open the
                        |model, but it may or may not work."""

    if (new OptionPane(owner, loc"Warning", message, OptionPane.Options.OkCancel, OptionPane.Icons.warning)
          .getSelectedIndex != 0)
      throw new UserCancelException()
  }

  @throws(classOf[UserCancelException])
  def checkWithUserBeforeOpening3DModelin2D(version: String): Unit = {
    val message = loc"""|You are attempting to open a model that was created in a 3D version of NetLogo. (This is
                        |${Version.version}; the model was created in $version.) NetLogo can try to open the model,
                        |but it may or may not work."""

    if (new OptionPane(owner, loc"Warning", message, OptionPane.Options.OkCancel, OptionPane.Icons.warning)
          .getSelectedIndex != 0)
      throw new UserCancelException()
  }

  @throws(classOf[UserCancelException])
  def checkWithUserBeforeOpening2DModelin3D(): Unit = {
    val message = loc"""|You are attempting to open a 2D model in ${Version.version}. You might need to make changes
                        |before it will work in 3D."""

    if (new OptionPane(owner, loc"Warning", message, OptionPane.Options.OkCancel, OptionPane.Icons.warning)
          .getSelectedIndex != 0)
      throw new UserCancelException()
  }

  @throws(classOf[UserCancelException])
  def notifyUserNotValidFile(uri: URI): Unit = {
    val warningText = Try(Paths.get(uri))
      .toOption
      .map(path => loc"The file $path is not a valid NetLogo model file.")
      .getOrElse(loc"This file is not a valid NetLogo model file.")
    new OptionPane(owner, loc"Error", warningText, OptionPane.Options.Ok, OptionPane.Icons.error)
    throw new UserCancelException()
  }

  def showVersionWarningAndGetResponse(version: String): Boolean = {
    val message = loc"""|This model was created in $version. You may need to make changes for it to work in
                        |${Version.version}. If it does not open, you may be able to update it for ${Version.version}
                        |using the Auto-Converter service."""

    val response = new OptionPane(owner, loc"Warning", message,
                                  Seq(loc"Continue", loc"Open Transition Guide", loc"Open Auto-Converter"),
                                  OptionPane.Icons.warning).getSelectedIndex
    response match {
      case 0 => true
      case 1 =>
        BrowserLauncher.openURI(owner,
          new URI(s"https://docs.netlogo.org/${Version.versionNumberNo3D}/transition.html"))
        false
      case 2 =>
        BrowserLauncher.openURI(owner, new URI("https://convert.netlogo.org"))
        false
      case _ => false
    }
  }

  def chooseFilePath(modelType: org.nlogo.api.ModelType): Option[java.net.URI] = {
    val userPath = FileDialog.showFiles(owner, loc"Save As", AWTFileDialog.SAVE, guessFileName,
                                        List[String](ModelReader.modelSuffix))
    val extensionPath = FileIO.ensureExtension(userPath, ModelReader.modelSuffix)
    val path = Paths.get(extensionPath)
    if (!path.toFile.exists || userPath == extensionPath) {
      Some(path.toUri)
    } else {
      FileDialog.confirmFileOverwrite(owner, extensionPath).map((_) => path.toUri)
    }
  }

  /**
   * makes a guess as to what the user would like to save this model as.
   * This is the model name if there is one, "Untitled.nlogox(3d)" otherwise.
   */
  private def guessFileName: String =
    FileIO.ensureExtension(modelTracker.modelNameForDisplay, ModelReader.modelSuffix)

  def shouldSaveModelOfDifferingVersion(version: String): Boolean = {
    Version.compatibleVersion(version) || {
      val message = loc"""|This model was made with $version. If you save it in ${Version.version} it may not work in
                          |the old version anymore."""

      new OptionPane(owner, loc"Error", message, Seq(loc"Save", loc"Cancel"), OptionPane.Icons.error)
        .getSelectedIndex == 0
    }
  }

  def warnInvalidFileFormat(format: String): Unit = {
    new OptionPane(owner, loc"Warning",
                   loc"""NetLogo doesn't know how to save in ".$format" format, please choose a different format.""",
                   OptionPane.Options.Ok, OptionPane.Icons.warning)
  }
}

class AutoConversionErrorDialog(owner: Component, context: String) extends MessageDialog(owner, loc"Cancel") {
  setModalityType(Dialog.ModalityType.DOCUMENT_MODAL)

  var modelToOpen = Option.empty[Model]

  class ConversionAction(name: String) extends AbstractAction(name) {
    val ModelKey = "ConversionModel"
    def putModel(model: Model): Unit = putValue(ModelKey, model)
    def actionPerformed(e: ActionEvent): Unit = {
      AutoConversionErrorDialog.this.modelToOpen =
        Option(getValue(ModelKey).asInstanceOf[Model])
      setVisible(false)
    }
  }

  lazy val bestEffortAction = new ConversionAction(loc"Open Partially Converted $context")
  lazy val originalAction = new ConversionAction(loc"Open Unconverted $context")

  override def makeButtons(): Seq[JComponent] = {
    super.makeButtons() ++ Seq(new Button(bestEffortAction), new Button(originalAction))
  }

  def errorMessage(failure: FailedConversionResult): String =
    loc"""|NetLogo encountered the following errors while trying to automatically convert the $context to the current\n
          |version. Would you like to continue opening it?\n
          |${failure.errors.map(decorateError(_)).mkString("\n\n", "\n", "")}"""

  private def decorateError(error: ConversionError): String = {
    val errorMessages = error.errors.map(e => s"- ${e.getMessage}").mkString("\n", "\n", "")

    loc"""|While running conversion "${error.conversionDescription}" on the
          |${error.componentDescription}: $errorMessages"""
  }

  def doShow(failure: FailedConversionResult): Unit = {
    modelToOpen = None
    failure match {
      case ErroredConversion(original, _) =>
        bestEffortAction.setEnabled(false)
        originalAction.putModel(original)
      case ConversionWithErrors(original, bestEffort, _) =>
        bestEffortAction.setEnabled(true)
        originalAction.putModel(original)
        bestEffortAction.putModel(bestEffort)
    }
    doShow(loc"Error Converting $context", errorMessage(failure), 5, 50)
  }
}
