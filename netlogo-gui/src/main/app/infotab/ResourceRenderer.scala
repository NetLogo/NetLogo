// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.html.{ CustomNodeRenderer, HtmlRenderer, HtmlWriter }
import com.vladsch.flexmark.html.renderer.{ NodeRenderer, NodeRendererContext, NodeRendererFactory,
                                            NodeRenderingHandler }
import com.vladsch.flexmark.util.options.{ DataHolder, MutableDataHolder }

import java.nio.file.Paths
import java.util.{ Base64, HashSet, Set }

import org.nlogo.api.ExternalResourceManager

class ResourceRenderer(modelDir: String, resourceManager: ExternalResourceManager)
  extends HtmlRenderer.HtmlRendererExtension {

  override def rendererOptions(options: MutableDataHolder): Unit = {}

  override def extend(builder: HtmlRenderer.Builder, rendererType: String): Unit = {
    builder.nodeRendererFactory(new ResourceRendererFactory)
  }

  private class ResourceRendererFactory extends NodeRendererFactory {
    def create(options: DataHolder): NodeRenderer =
      new ResourceRenderer
  }

  private class ResourceRenderer extends NodeRenderer {
    def getNodeRenderingHandlers: Set[NodeRenderingHandler[?]] = {
      new HashSet[NodeRenderingHandler[?]] {
        add(new NodeRenderingHandler[Image](classOf[Image], new ImageRenderer))
      }
    }
  }

  private class ImageRenderer extends CustomNodeRenderer[Image] {
    def render(node: Image, context: NodeRendererContext, writer: HtmlWriter): Unit = {
      val url = node.getUrl.toString.replaceAll("%20", " ")

      val source = {
        if (url.startsWith("file:")) {
          s"file:${Paths.get(Option(modelDir).getOrElse("")).resolve(url.stripPrefix("file:"))}"
        } else {
          resourceManager.getResource(url).fold(url) { resource =>
            val data = {
              if ("^[A-Za-z0-9+/=]*$".r.matches(resource.data)) {
                resource.data
              } else {
                Base64.getEncoder.encodeToString(resource.data.getBytes)
              }
            }

            s"data:image/${resource.extension};base64,$data"
          }
        }
      }

      writer.withAttr.attr("alt", node.getText).attr("src", source).tag("img")
    }
  }
}
