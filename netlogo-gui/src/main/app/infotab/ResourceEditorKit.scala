// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import java.io.{ ByteArrayInputStream, InputStream }
import java.net.{ URL, URLConnection, URLStreamHandler }
import java.util.Base64
import javax.swing.text.{ Element, StyleConstants, View, ViewFactory }
import javax.swing.text.html.{ HTML, HTMLEditorKit, ImageView }

import org.nlogo.api.ExternalResourceManager

import scala.util.Try

// expansion of the default EditorKit that can display images from Base64 data,
// primarily for use with bundled resources (Isaac B 6/18/25)
class ResourceEditorKit(resourceManager: ExternalResourceManager) extends HTMLEditorKit {
  private val viewFactory = new Base64ViewFactory

  // this handler is only used if the image is found in the bundled resource list (Isaac B 6/18/25)
  private val urlHandler = new URLStreamHandler {
    override def openConnection(url: URL): URLConnection =
      new Base64URLConnection(url)
  }

  override def getViewFactory(): ViewFactory =
    viewFactory

  private class Base64ViewFactory extends HTMLEditorKit.HTMLFactory {
    override def create(elem: Element): View = {
      // if it's an img tag, potentially load it as Base64 data (Isaac B 6/18/25)
      elem.getAttributes.getAttribute(StyleConstants.NameAttribute) match {
        case HTML.Tag.IMG =>
          new Base64ImageView(elem)

        case _ =>
          super.create(elem)
      }
    }
  }

  private class Base64ImageView(elem: Element) extends ImageView(elem) {
    override def getImageURL(): URL = {
      val src = elem.getAttributes.getAttribute(HTML.Attribute.SRC).toString

      // if a bundled resource exists with this name, attempt to load it
      // with the custom URL handler (Isaac B 6/18/25)
      resourceManager.getResource(src) match {
        case Some(_) =>
          new URL("", "", 0, src, urlHandler)

        case _ =>
          Try(new URL(src)).getOrElse(null)
      }
    }
  }

  private class Base64URLConnection(url: URL) extends URLConnection(url) {
    // attempt to find resource based on filename of URL (Isaac B 6/18/25)
    private val data: Option[Array[Byte]] = resourceManager.getResource(url.getFile).map { resource =>
      Base64.getDecoder.decode(resource.data)
    }

    // data is in memory, so no actual connection needed (Isaac B 6/18/25)
    override def connect(): Unit = {}

    override def getInputStream: InputStream =
      data.map(new ByteArrayInputStream(_)).getOrElse(super.getInputStream)
  }
}
