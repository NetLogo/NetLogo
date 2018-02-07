// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import java.util.{ HashSet => JHashSet, List => JList, Set => JSet }

import com.vladsch.flexmark.ast.{ Block, CodeBlock, FencedCodeBlock, IndentedCodeBlock }
import com.vladsch.flexmark.html.{ CustomNodeRenderer, HtmlRenderer, HtmlWriter }
import com.vladsch.flexmark.html.renderer.{ NodeRenderer, NodeRendererContext,
  NodeRenderingHandler, NodeRendererFactory }
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.block.{ BlockPreProcessor, BlockPreProcessorFactory, ParserState }
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.util.options.{ DataHolder, MutableDataHolder }

import org.nlogo.core.Dialect
import org.nlogo.app.common.CodeToHtml

class CodeBlockRenderer(dialect: Dialect) extends Parser.ParserExtension with HtmlRenderer.HtmlRendererExtension {
  class NetLogoCodeBlock(content: BasedSequence, contentLines: JList[BasedSequence]) extends CodeBlock(content, contentLines) {
  }

  class NetLogoCodeBlockProcessor(options: DataHolder) extends BlockPreProcessor {
    def preProcess(state: ParserState, block: Block) = {
      val replacementNode = block match {
        case i: IndentedCodeBlock =>
          Some(new NetLogoCodeBlock(i.getContentChars, i.getContentLines))
        case f: FencedCodeBlock if f.getInfo.trim.isEmpty =>
          Some(new NetLogoCodeBlock(f.getContentChars, f.getContentLines))
        case _ => None
      }
      for {
        newNode <- replacementNode
      } {
        block.insertBefore(newNode)
        block.unlink()
        state.blockAdded(newNode)
        state.blockRemoved(block)
      }
    }
  }

  class NetLogoCodeBlockProcessorFactory extends BlockPreProcessorFactory {
    override def getBlockTypes(): JSet[Class[_ <: Block]] = {
      val set = new JHashSet[Class[_ <: Block]]()
      set.add(classOf[FencedCodeBlock])
      set.add(classOf[IndentedCodeBlock])
      set
    }
    def create(state: ParserState): BlockPreProcessor = new NetLogoCodeBlockProcessor(state.getProperties)
    def affectsGlobalScope(): Boolean = true
    def getAfterDependents(): JSet[Class[_ <: BlockPreProcessorFactory]] = {
      null
    }
    def getBeforeDependents(): JSet[Class[_ <: BlockPreProcessorFactory]] = {
      null
    }
  }

  object Factory extends NodeRendererFactory {
    override def create(options: DataHolder): NodeRenderer = new Renderer(options)
  }

  class Renderer(options: DataHolder) extends NodeRenderer {

    override def getNodeRenderingHandlers: java.util.Set[NodeRenderingHandler[_]] = {
      val set = new JHashSet[NodeRenderingHandler[_]]()
      set.add(
        new NodeRenderingHandler[NetLogoCodeBlock](classOf[NetLogoCodeBlock], new CustomNodeRenderer[NetLogoCodeBlock]() {
          override def render(node: NetLogoCodeBlock, context: NodeRendererContext, html: HtmlWriter): Unit = {
            Renderer.this.render(node, context, html)
          }
        }))
      return set;
    }
    val converter = CodeToHtml.newInstance(dialect)

    def render(node: NetLogoCodeBlock, context: NodeRendererContext, html: HtmlWriter): Unit = {
      html.line()
      html.srcPosWithTrailingEOL(node.getChars).withAttr.tag("pre").openPre()
      html.srcPosWithTrailingEOL(node.getContentChars).tag("code")
      html.line()
      html.raw(converter.convert(node.getContentChars.unescape, wrapped = false))
      html.tag("/code")
      html.tag("/pre").closePre()
      html.lineIf(context.getHtmlOptions.htmlBlockCloseTagEol)
    }
  }

  override def parserOptions(options: MutableDataHolder): Unit = {}

  override def extend(builder: Parser.Builder): Unit = {
    builder.blockPreProcessorFactory(new NetLogoCodeBlockProcessorFactory)
  }

  override def rendererOptions(options: MutableDataHolder): Unit = {}

  override def extend(rendererBuilder: HtmlRenderer.Builder, rendererType: String): Unit = {
    if (rendererType == "HTML") {
      rendererBuilder.nodeRendererFactory(Factory)
    }
  }

}
