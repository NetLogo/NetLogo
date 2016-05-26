// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.{ File, IOException }
import java.net.URI
import java.nio.file.Paths

import org.nlogo.api.ModelType
import org.nlogo.nvm.{ CompilerInterface, FileManager }

/** This trait holds the state of the workspace with respect to
 *  various facets related to (but external from) the model.
 *  These include:
 *   - the model path
 *   - the model file name
 *   - the model type (new, library, "normal")
 *
 *  At some point in the future this trait *could* be
 *  modified to hold the model itself.
 *  At the moment, that bit of state is handled by ModelSaver,
 *  but only because nothing in the workspace needs it directly.
 *  RG 5/12/16
 */
trait ModelTracker {

  def compiler: CompilerInterface

  def getExtensionManager(): ExtensionManager

  val fileManager: FileManager = new DefaultFileManager(this)

  /**
   * path to the directory from which the current model was loaded. NetLogo
   * uses this as the default path for file I/O, when reloading models,
   * locating HubNet clients, etc. This is null if this is a new (unsaved)
   * model.
   */
  private[workspace] var modelDir: String = null

  /**
   * name of the currently loaded model. Will be null if this is a new
   * (unsaved) model. To get a version for display to the user, see
   * modelNameForDisplay(). This is NOT a full path name, however, it does
   * end in ".nlogo".
   */
  var _modelFileName: String = null

  /**
   * type of the currently loaded model. Certain aspects of NetLogo's
   * behavior depend on this, i.e. whether to force a save-as and so on.
   */
  private var _modelType: ModelType = ModelType.New

  def getModelType: ModelType = _modelType
  def setModelType(tpe: ModelType): Unit = {
    _modelType = tpe
  }

  /**
   * instantly converts the current model to ModelTypeJ.NORMAL. This is used
   * by the __edit command to enable quick saving of library models. It
   * shouldn't be used anywhere else.
   */
  @throws(classOf[IOException])
  def convertToNormal(): String = {
    val git = new File(ModelsLibrary.modelsRoot(), ".git")
    if (!git.exists()) {
      throw new IOException("The models folder is not under version control.")
    }
    _modelType = ModelType.Normal
    getModelPath
  }

  /**
   * whether the user needs to enter a new filename to save this model.
   * We need to do a "save as" if the model is new, from the
   * models library, or converted.
   * <p/>
   * Basically, only normal models can get silently saved.
   */
  def forceSaveAs: Boolean = {
    _modelType == ModelType.New || _modelType == ModelType.Library
  }

  /**
   * returns the full path to the directory from which the current model was
   * loaded. May be null if, for example, this is a new model.
   */
  def getModelDir: String = modelDir

  /**
   * returns the name of the file from which the current model was loaded.
   * May be null if, for example, this is a new model.
   */
  def getModelFileName: String = _modelFileName

  def modelFileName: String = _modelFileName

  /**
   * returns the full pathname of the currently loaded model, if any. This
   * may return null in some cases, for instance if this is a new model.
   */
  def getModelPath: String = {
    if (modelDir == null || modelFileName == null)
      null
    else
      modelDir + java.io.File.separatorChar + modelFileName
  }

  def getModelFileUri: Option[URI] = {
    Option(getModelPath).map(p => Paths.get(p).toUri)
  }

  def setModelPath(modelPath: String): Unit = {
    if (modelPath == null) {
      _modelFileName = null
      modelDir = null
    } else {
      val file = new File(modelPath).getAbsoluteFile
      _modelFileName = file.getName
      modelDir = file.getParent
      if (modelDir == "") {
        modelDir = null
      }
      if (modelDir != null) {
        fileManager.setPrefix(modelDir)
      }
    }
  }

  def modelNameForDisplay: String =
    Option(modelFileName)
      .map(name =>
          if (getModelType == ModelType.New) "Untitled"
          else if (name.endsWith(".nlogo")) name.stripSuffix(".nlogo")
          else if (name.endsWith(".nlogo3d")) name.stripSuffix(".nlogo3d")
          else name)
      .getOrElse("Untitled")
}
