// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import
  java.util.zip.GZIPInputStream

import
  java.io.{ BufferedReader, ByteArrayInputStream, InputStreamReader }

import
  java.net.{ ConnectException, URL, URI }

import
  javax.swing.JOptionPane

import
  org.nlogo.core.I18N

import
  org.nlogo.api.{ ModelType, Version }

import
  org.nlogo.awt.EventQueue

import
  org.nlogo.window.GUIWorkspaceScala

import
  scala.io.{ Codec, Source }

object LoadModelFromUrl {
  val ImportWorldURLProp = "netlogo.world_state_url"
  val ImportRawWorldURLProp = "netlogo.raw_world_state_url"
}

import LoadModelFromUrl._

class LoadModelFromUrl(fileManager: FileManager, workspace: GUIWorkspaceScala) {

  def apply(url: String, currentVersion: Version): Unit = {
    try {
      fileManager.openFromURI(new URI(url), ModelType.Library)

      Option(System.getProperty(ImportRawWorldURLProp)) map {
        url => // `io.Source.fromURL(url).bufferedReader` steps up to bat and... manages to fail gloriously here! --JAB (8/22/12)
          EventQueue.invokeLater {
            () =>
              workspace.importWorld(new BufferedReader(new InputStreamReader(new URL(url).openStream())))
              workspace.view.dirty()
              workspace.view.repaint()
          }
        } orElse (Option(System.getProperty(ImportWorldURLProp)) map {
            url =>


              val source = Source.fromURL(url)(Codec.ISO8859)
              val bytes  = source.map(_.toByte).toArray
              val bais   = new ByteArrayInputStream(bytes)
              val gis    = new GZIPInputStream(bais)
              val reader = new InputStreamReader(gis)

              EventQueue.invokeLater {
                () => {
                  workspace.importWorld(reader)
                  workspace.view.dirty()
                  workspace.view.repaint()
                  source.close()
                  bais.close()
                  gis.close()
                  reader.close()
                }
              }

          })
    }
    catch {
      case ex: ConnectException =>
        fileManager.newModel(currentVersion)
        JOptionPane.showConfirmDialog(null,
          I18N.gui.getN("file.open.error.unloadable.message", url),
          I18N.gui.get("file.open.error.unloadable.title"), JOptionPane.DEFAULT_OPTION)
    }
  }
}
