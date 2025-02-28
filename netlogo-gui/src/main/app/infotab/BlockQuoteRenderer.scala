// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import com.vladsch.flexmark.ast.BlockQuote
import com.vladsch.flexmark.html.{ CustomNodeRenderer, HtmlWriter }
import com.vladsch.flexmark.html.HtmlRenderer.{ Builder, HtmlRendererExtension }
import com.vladsch.flexmark.html.renderer.{ NodeRenderer, NodeRendererContext, NodeRendererFactory,
                                            NodeRenderingHandler }
import com.vladsch.flexmark.util.options.{ DataHolder, MutableDataHolder }

import java.util.{ HashSet, Set }

class BlockQuoteRenderer extends HtmlRendererExtension {
  private class RendererFactory extends NodeRendererFactory {
    def create(options: DataHolder): NodeRenderer =
      new Renderer
  }

  private class Renderer extends NodeRenderer {
    def getNodeRenderingHandlers: Set[NodeRenderingHandler[_]] = {
      val set = new HashSet[NodeRenderingHandler[_]]

      set.add(new NodeRenderingHandler[BlockQuote](classOf[BlockQuote], new BlockQuoteRenderer))

      set
    }
  }

  private class BlockQuoteRenderer extends CustomNodeRenderer[BlockQuote] {
    def render(node: BlockQuote, context: NodeRendererContext, writer: HtmlWriter) {
      writer.withAttr.tagIndent("blockquote", () => {
        writer.withAttr.tagIndent("div", () => {
          context.renderChildren(node)
        })
      })
    }
  }

  def extend(builder: Builder, rendererType: String) {
    builder.nodeRendererFactory(new RendererFactory)
  }

  def rendererOptions(options: MutableDataHolder) {}
}
