package org.nlogo.extensions.vid

import org.nlogo.api.{ DefaultClassManager, ExtensionManager, PrimitiveManager }

object VidExtension {
  var isHeadless = true
}

class VidExtension(movies: MovieFactory, cameras: CameraFactory, player: Player, selector: Selector, recorder: Recorder)
  extends DefaultClassManager with VideoSourceContainer {

  def this() = this(Movie, Camera, new JavaFXPlayer, NetLogoSelector, new MP4Recorder)

  override def runOnce(em: ExtensionManager): Unit = {
    VidExtension.isHeadless = !em.workspaceContext.workspaceGUI
  }

  override def load(manager: PrimitiveManager) = {
    manager.addPrimitive("camera-names",      new CameraNames(cameras))
    manager.addPrimitive("camera-open",       new CameraOpen(this, cameras))
    manager.addPrimitive("camera-select",     new CameraSelect(this, cameras, selector))
    manager.addPrimitive("capture-image",     new CaptureImage(this))
    manager.addPrimitive("close",             new CloseVideoSource(this))
    manager.addPrimitive("hide-player",       new HidePlayer(player))
    manager.addPrimitive("movie-open",        new MovieOpen(this, movies))
    manager.addPrimitive("movie-open-remote", new MovieOpenRemote(this, movies))
    manager.addPrimitive("movie-select",      new MovieSelect(this, movies, selector))
    manager.addPrimitive("recorder-status",   new RecorderStatus(recorder))
    manager.addPrimitive("record-interface",  new RecordInterface(recorder))
    manager.addPrimitive("record-source",     new RecordSource(recorder, this))
    manager.addPrimitive("record-view",       new RecordView(recorder))
    manager.addPrimitive("reset-recorder",    new ResetRecorder(recorder))
    manager.addPrimitive("save-recording",    new SaveRecording(recorder))
    manager.addPrimitive("set-time",          new SetTime(this))
    manager.addPrimitive("show-player",       new ShowPlayer(player, this))
    manager.addPrimitive("start",             new StartSource(this))
    manager.addPrimitive("start-recorder",    new StartRecorder(recorder))
    manager.addPrimitive("status",            new ReportStatus(this))
    manager.addPrimitive("stop",              new StopSource(this))
  }

  override def unload(em: ExtensionManager) = {
    closeSource()
    recorder.reset()
  }

  def closeSource(): Unit = {
    _videoSource.foreach(_.close())
  }

  var _videoSource: Option[VideoSource] = None

  def videoSource: Option[VideoSource] =
    _videoSource

  def videoSource_=(source: Option[VideoSource]): Unit = {
    try {
      if (player.isShowing) {
        val boundedNode = source.map(n => b => n.videoNode(b))
          .getOrElse(player.emptyNode(_))
          .apply(player.boundedSize)
        player.present(boundedNode)
      }
      closeSource()
    } catch {
      case e: Exception =>
        println("VID Extension Exception")
        println(e.getMessage)
        e.printStackTrace()
    }
    _videoSource = source
  }
}
