// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, FileDialog => AWTFileDialog }
import java.awt.event.ActionEvent
import java.nio.file.Paths
import java.net.URI
import javax.swing.{ AbstractAction, JButton, JComponent, JDialog }

import org.nlogo.api.{ ModelReader, ModelType, Version }, ModelReader.modelSuffix
import org.nlogo.awt.{ EventQueue, UserCancelException }
import org.nlogo.core.{ I18N, Model }
import org.nlogo.fileformat.FailedConversionResult
import org.nlogo.swing.{ BrowserLauncher, FileDialog, Implicits, MessageDialog, OptionDialog },
  Implicits.thunk2runnable
import org.nlogo.workspace.{ ModelTracker, OpenModel, SaveModel },
  OpenModel.{ Controller => OpenModelController },
  SaveModel.{ Controller => SaveModelController }

import scala.util.Try

class BackgroundFileController(dialog: JDialog, foregroundController: FileController) extends OpenModelController with SaveModelController {

  def runOnUIThread[A](f: () => Unit) = {
    EventQueue.invokeLater { () =>
      dialog.setVisible(false)
      f()
      dialog.setVisible(true)
    }
  }

  def runOnUIThreadForResult[A](f: () => A): A = {
    import scala.concurrent.{ Await, Promise }
    import scala.concurrent.duration.{ Duration, MILLISECONDS }
    val promise = Promise[A]()

    runOnUIThread(() => promise.success(f()))

    Await.result(promise.future, Duration.Inf)
  }

  // Members declared in org.nlogo.workspace.SaveModel.Controller
  def chooseFilePath(modelType: ModelType): Option[URI] =
    runOnUIThreadForResult(() => foregroundController.chooseFilePath(modelType))

  def shouldSaveModelOfDifferingVersion(version: String): Boolean =
    runOnUIThreadForResult(() => foregroundController.shouldSaveModelOfDifferingVersion(version))

  def warnInvalidFileFormat(format: String): Unit =
    runOnUIThread(() => foregroundController.warnInvalidFileFormat(format))

  // Members declared in org.nlogo.workspace.OpenModel.Controller
  def errorOpeningURI(uri: java.net.URI,exception: Exception): Unit =
    runOnUIThread(() => foregroundController.errorOpeningURI(uri, exception))

  def errorAutoconvertingModel(res: FailedConversionResult): Boolean =
    runOnUIThreadForResult(() => foregroundController.errorAutoconvertingModel(res))
  def invalidModel(uri: java.net.URI): Unit =
    runOnUIThread(() => foregroundController.invalidModel(uri))
  def invalidModelVersion(uri: java.net.URI,version: String): Unit =
    runOnUIThread(() => foregroundController.invalidModelVersion(uri, version))
  def shouldOpenModelOfDifferingArity(arity: Int,version: String): Boolean =
    runOnUIThreadForResult(() => foregroundController.shouldOpenModelOfDifferingArity(arity, version))
  def shouldOpenModelOfLegacyVersion(version: String): Boolean =
    runOnUIThreadForResult(() => foregroundController.shouldOpenModelOfLegacyVersion(version))
  def shouldOpenModelOfUnknownVersion(version: String): Boolean =
    runOnUIThreadForResult(() => foregroundController.shouldOpenModelOfUnknownVersion(version))
}

class FileController(owner: Component, modelTracker: ModelTracker) extends OpenModelController with SaveModelController {
  // OpenModel.Controller methods
  def errorOpeningURI(uri: URI, exception: Exception): Unit = {
    val options = Array[Object](I18N.gui.get("common.buttons.ok"))
    println(exception)
    exception.printStackTrace()
    OptionDialog.show(owner, "NetLogo",
      I18N.gui.getN("file.open.error.unableToOpen",
        Paths.get(uri).toString, exception.getMessage),
      options)
    throw new UserCancelException()
  }

  def errorAutoconvertingModel(res: FailedConversionResult): Boolean = {
    val options = Seq(
      I18N.gui.get("common.buttons.continue"), I18N.gui.get("common.buttons.cancel"))
    println(res.error)
    res.error.printStackTrace()
    val dialog = new AutoConversionErrorDialog(owner)
    dialog.doShow(res)
    dialog.shouldContinue
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
      if (arity == 2)
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

  lazy val continueAndCancelOptions = Array[Object](
    I18N.gui.get("common.buttons.continue"),
    I18N.gui.get("common.buttons.cancel"))

  @throws(classOf[UserCancelException])
  def checkWithUserBeforeOpeningModelFromFutureVersion(version: String): Unit = {
    val message = I18N.gui.getN("file.open.warn.version.newer", Version.version, version)
    if (OptionDialog.show(owner, "NetLogo", message, continueAndCancelOptions) != 0) {
      throw new UserCancelException()
    }
  }

  @throws(classOf[UserCancelException])
  def checkWithUserBeforeOpening3DModelin2D(version: String): Unit = {
    val message = I18N.gui.getN("file.open.warn.intwod.openthreed", Version.version, version)
    if (OptionDialog.show(owner, "NetLogo", message, continueAndCancelOptions) != 0) {
      throw new UserCancelException()
    }
  }

  @throws(classOf[UserCancelException])
  def checkWithUserBeforeOpening2DModelin3D(): Unit = {
    val message = I18N.gui.getN("file.open.warn.inthreed.opentwod", Version.version)
    if (OptionDialog.show(owner, "NetLogo", message, continueAndCancelOptions) != 0) {
      throw new UserCancelException()
    }
  }

  @throws(classOf[UserCancelException])
  def notifyUserNotValidFile(uri: URI): Unit = {
    val warningText = Try(Paths.get(uri))
      .toOption
      .map(path => I18N.gui.getN("file.open.error.invalidmodel.withPath", path.toString))
      .getOrElse(I18N.gui.get("file.open.error.invalidmodel"))
    val options = Array[Object](I18N.gui.get("common.buttons.ok"))
    OptionDialog.show(owner, "NetLogo", warningText, options)
    throw new UserCancelException()
  }

  def showVersionWarningAndGetResponse(version: String): Boolean = {
    val message = I18N.gui.getN("file.open.warn.version.older", version, Version.version)
    val options = Array[Object](
      I18N.gui.get("common.buttons.continue"),
      I18N.gui.get("file.open.warn.version.transitionGuide"),
      I18N.gui.get("common.buttons.cancel"))
    val response =
      OptionDialog.show(owner, I18N.gui.get("common.messages.warning"), message, options)
    response match {
      case 0 => true
      case 1 =>
        BrowserLauncher.openURL(owner, I18N.gui.get("file.open.transitionGuide.url"), false)
        showVersionWarningAndGetResponse(version)
      case 2 => false
    }
  }

  def chooseFilePath(modelType: org.nlogo.api.ModelType): Option[java.net.URI] = {
    val newFileName = guessFileName

    // we only default to saving in the model dir for normal
    // models. for library and new models, we use the current
    // FileDialog dir.
    if (modelTracker.getModelType == ModelType.Normal) {
      FileDialog.setDirectory(modelTracker.getModelDir)
    }

    var path = FileDialog.show(
      owner, I18N.gui.get("menu.file.saveAs"), AWTFileDialog.SAVE,
      newFileName)
    if(! path.endsWith("." + modelSuffix)) {
      path += "." + modelSuffix
    }
    Some(Paths.get(path).toUri)
  }

  /**
   * makes a guess as to what the user would like to save this model as.
   * This is the model name if there is one, "Untitled.nlogo" otherwise.
   */
  private def guessFileName: String =
    modelTracker.modelNameForDisplay + "." + modelSuffix

  def shouldSaveModelOfDifferingVersion(version: String): Boolean = {
    Version.compatibleVersion(version) || {
      val options = Array[Object](
        I18N.gui.get("common.buttons.save"),
        I18N.gui.get("common.buttons.cancel"))
      val message = I18N.gui.getN("file.save.warn.savingInNewerVersion", version, Version.version)
      OptionDialog.show(owner, "NetLogo", message, options) == 0
    }
  }

  def warnInvalidFileFormat(format: String): Unit = {
    val options = Array[Object](I18N.gui.get("common.buttons.ok"))
    val message = I18N.gui.getN("file.save.warn.invalidFormat", format)
    OptionDialog.show(owner, I18N.gui.get("common.messages.warning"), message, options)
  }
}

class AutoConversionErrorDialog(owner: Component) extends MessageDialog(owner) {
  var shouldContinue = false

  override def dismissName = I18N.gui.get("common.buttons.cancel")

  override def makeButtons(): Seq[JComponent] = {
    val continueAction = new AbstractAction(I18N.gui.get("common.buttons.continue")) {
      def actionPerformed(e: ActionEvent): Unit = {
        shouldContinue = true
        setVisible(false)
      }
    }
    super.makeButtons() :+ new JButton(continueAction)
  }

  def errorMessage(failure: FailedConversionResult): String =
    I18N.gui.get("file.open.warn.autoconversion.error") + "\n\n" +
      I18N.gui.getN("file.open.warn.autoconversion.detail", failure.error.getMessage)

  def doShow(failure: FailedConversionResult): Unit = {
    doShow(I18N.gui.get("file.open.warn.autoconversion.title"),
      errorMessage(failure), 5, 50)
  }
}
