// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.{ File, IOException }
import java.net.URI
import java.nio.file.Paths

import org.nlogo.api.{ FileIO, ModelType, ThreeDVersion, TwoDVersion, Version }
import org.nlogo.core.Model
import org.nlogo.nvm.{ ModelTracker => NvmModelTracker }

import scala.util.Try

/** This trait holds the state of the workspace with respect to
 *  various facets related to (but external from) the model.
 *  These include:
 *   - the model path
 *   - the model file name
 *   - the model type (new, library, "normal")
 *   - the working copy of the model itself
 *
 *  The workspace doesn't make use of the working copy of the model,
 *  but it's nice to have available to clients of the workspace.
 *
 *  RG 5/12/16, 10/31/17
 */
trait ModelTracker extends NvmModelTracker {
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
   * Representation of the active model.
   */
  private var _model: Model = Model()
  def model: Model = _model

  /**
   * type of the currently loaded model. Certain aspects of NetLogo's
   * behavior depend on this, i.e. whether to force a save-as and so on.
   */
  private var _modelType: ModelType = ModelType.New

  def getModelType: ModelType = _modelType
  def setModelType(tpe: ModelType): Unit = {
    _modelType = tpe
  }

  def currentVersion: Version =
    if (Version.is3D(model.version)) ThreeDVersion
    else TwoDVersion

  /**
   * instantly converts the current model to ModelTypeJ.NORMAL. This is used
   * by the __edit command to enable quick saving of library models. It
   * shouldn't be used anywhere else.
   */
  @throws(classOf[IOException])
  def convertToNormal(): String = {
    val git = new File(ModelsLibrary.modelsRoot, ".git")
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
    }
  }

  /**
   * attaches the current model directory to a relative path, if necessary.
   * If filePath is an absolute path, this method simply returns it.
   * If it's a relative path, then the current model directory is prepended
   * to it. If this is a new model, the user's platform-dependent home
   * directory is prepended instead.
   */
  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String = {
    FileIO.resolvePath(filePath,
      Option(getModelPath).flatMap(s => Try(Paths.get(s)).toOption))
        .map(_.toString)
        .getOrElse(filePath)
  }


  def modelNameForDisplay: String =
    Option(modelFileName)
      .map(name =>
          if (getModelType == ModelType.New) "Untitled"
          else if (name.endsWith(".nlogo")) name.stripSuffix(".nlogo")
          else if (name.endsWith(".nlogo3d")) name.stripSuffix(".nlogo3d")
          else name)
      .getOrElse("Untitled")

  def guessExportName(defaultName: String): String = {
    val modelName = getModelFileName
    if (modelName == null) {
      defaultName
    } else if (modelName.startsWith("empty.nlog")) {
      defaultName
    } else {
      val index = modelName.lastIndexOf(".nlogo");

      val baseName =
        if (index > -1) modelName.substring(0, index)
        else            modelName

      s"${baseName} ${defaultName}"
    }
  }

  def updateModel(f: Model => Model): Unit = {
    _model = f(_model)
  }

  def processWorkspaceEvent(e: WorkspaceEvent): Unit = {
    e match {
      case SwitchModel(modelPath, modelTpe) =>
        setModelPath(modelPath.orNull)
        setModelType(modelTpe)
      case _ =>
    }
  }
}

class ModelTrackerImpl(messageCenter: WorkspaceMessageCenter) extends ModelTracker {
  override def setModelPath(dir: String): Unit = {
    super.setModelPath(dir)

    messageCenter.send(ModelPathChanged(Option(_modelFileName), Option(modelDir)))
  }
}

// This trait is used to implement the methods from api.Workspace.
// While we may want these methods exposed for extensions, internal classes should depend on ModelTracker
// directly wherever possible to make it possible to factor out this functionality from Workspace in the future.
trait ModelTracking {
  def modelTracker: ModelTracker
  def convertToNormal(): String =
    modelTracker.convertToNormal()
  def attachModelDir(filePath: String): String =
    modelTracker.attachModelDir(filePath)
  def getModelDir: String =
    modelTracker.getModelDir
  def modelNameForDisplay: String =
    modelTracker.modelNameForDisplay
  def getModelFileName: String =
    modelTracker.getModelFileName
  def getModelPath: String =
    modelTracker.getModelPath
  def setModelPath(path: String): Unit =
    modelTracker.setModelPath(path)
}
