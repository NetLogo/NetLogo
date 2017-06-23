import java.util.{ ArrayList => JArrayList, HashSet => JHashSet, Set => JSet }
import java.net.URI

import scala.util.Try
import scala.util.matching.Regex

import com.vladsch.flexmark.{ Extension, ast, ext, html, parser, util },
  ast.{ AutoLink, Document, Link, InlineLinkNode, Node },
  ext.{ anchorlink, autolink, escaped, tables, toc, typographic },
    anchorlink.AnchorLinkExtension,
    autolink.AutolinkExtension,
    escaped.character.EscapedCharacterExtension,
    tables.TablesExtension,
    toc.TocExtension,
    typographic.TypographicExtension,
  html.{ AttributeProvider, CustomNodeRenderer, HtmlRenderer, HtmlWriter,
         IndependentAttributeProviderFactory, renderer },
    renderer.{ AttributablePart, LinkType, NodeRenderer, NodeRenderingHandler,
               NodeRendererContext, NodeRendererFactory },
  parser.{ Parser, ParserEmulationProfile, block },
    block.{ NodePostProcessor, NodePostProcessorFactory },
  util.{ NodeTracker, options, sequence },
  util.html.Attributes,
    options.{ DataHolder, MutableDataHolder, MutableDataSet },
    sequence.BasedSequence

object Markdown {
  def options(addTableOfContents: Boolean, manualizeLinks: Boolean, extName: Option[String]): MutableDataSet = {
    val extensions = new JArrayList[Extension]()
    val options = new MutableDataSet()

    options.setFrom(ParserEmulationProfile.PEGDOWN)

    extensions.add(EscapedCharacterExtension.create())

    options.set(HtmlRenderer.SOFT_BREAK, "\n")
    options.set(HtmlRenderer.HARD_BREAK, "<br />\n")

    extensions.add(TypographicExtension.create())
    options.set(TypographicExtension.ENABLE_QUOTES, Boolean.box(true))
    options.set(TypographicExtension.ENABLE_SMARTS, Boolean.box(true))

    extensions.add(AutolinkExtension.create())
    extensions.add(AutolinkTargetExtension)

    options.set(Parser.MATCH_CLOSING_FENCE_CHARACTERS, Boolean.box(false))

    extensions.add(TablesExtension.create())

    if (addTableOfContents)
      extensions.add(TocExtension.create())
    if (manualizeLinks)
      extensions.add(ManualLinkExtension)
    if (extName.nonEmpty)
      extensions.add(new GitHubStyleLinkExtension(extName.get))
    extensions.add(AnchorLinkExtension.create())
    options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "section-anchor")

    options.set(Parser.EXTENSIONS, extensions)

    options
  }

  def apply(
    str:                String,
    addTableOfContents: Boolean = true,
    manualizeLinks:     Boolean = true,
    extName:            Option[String] = None): String = {

    val opts = options(addTableOfContents, manualizeLinks, extName)
    val parser = Parser.builder(opts).build()
    val renderer = HtmlRenderer.builder(opts).build()
    val document = parser.parse(str)
    renderer.render(document)
  }

  object AutolinkTargetExtension extends HtmlRenderer.HtmlRendererExtension {
    override def rendererOptions(options: MutableDataHolder): Unit = {}

    override def extend(rendererBuilder: HtmlRenderer.Builder, rendererType: String) =
      rendererBuilder.attributeProviderFactory(new IndependentAttributeProviderFactory {
        override def create(context: NodeRendererContext) = AutolinkAttributeProvider
      })

    object AutolinkAttributeProvider extends AttributeProvider {
      override def setAttributes(node: Node, part: AttributablePart, attributes: Attributes) =
        if (node.isInstanceOf[AutoLink] && part == AttributablePart.LINK)
          attributes.replaceValue("target", "_blank")
    }
  }

  // this extension relativizes links when they appear in the manual.
  // "http://ccl.northwestern.edu/netlogo/docs/x/y/z" is transformed to "/x/y/z"
  object ManualLinkExtension extends Parser.ParserExtension with HtmlRenderer.HtmlRendererExtension {
    def extend(builder: Parser.Builder): Unit = {
      builder.postProcessorFactory(ManualLinkPostProcessorFactory)
    }

    def rendererOptions(options: MutableDataHolder): Unit = {}
    def parserOptions(options: MutableDataHolder): Unit = {}

    def extend(builder: HtmlRenderer.Builder, rendererType: String): Unit = {
      if (rendererType == "HTML")
        builder.nodeRendererFactory(ManualLinkNodeRendererFactory)
    }

    class ManualLink(other: Link, docUrlString: String) extends InlineLinkNode(
      other.getChars,
      other.getTextOpeningMarker,
      other.getText,
      other.getTextClosingMarker,
      other.getLinkOpeningMarker,
      other.getUrl.subSequence(docUrlString.length),
      other.getTitleOpeningMarker,
      other.getTitle,
      other.getTitleClosingMarker,
      other.getLinkClosingMarker) {

      override def setTextChars(textChars: BasedSequence): Unit = {
        val textCharsLength = textChars.length
        textOpeningMarker = textChars.subSequence(0, 1)
        text = textChars.subSequence(1, textCharsLength - 1).trim
        textClosingMarker = textChars.subSequence(textCharsLength - 1, textCharsLength)
      }
    }

    class ExternalLink(other: Link) extends InlineLinkNode(
      other.getChars,
      other.getTextOpeningMarker,
      other.getText,
      other.getTextClosingMarker,
      other.getLinkOpeningMarker,
      other.getUrl,
      other.getTitleOpeningMarker,
      other.getTitle,
      other.getTitleClosingMarker,
      other.getLinkClosingMarker) {

      override def setTextChars(textChars: BasedSequence): Unit = {
        val textCharsLength = textChars.length
        textOpeningMarker = textChars.subSequence(0, 1)
        text = textChars.subSequence(1, textCharsLength - 1).trim
        textClosingMarker = textChars.subSequence(textCharsLength - 1, textCharsLength)
      }
    }

    object ManualLinkPostProcessorFactory extends NodePostProcessorFactory(false) {
      addNodes(classOf[Link])

      def create(document: Document): NodePostProcessor = ManualLinkPostProcessor
    }

    object ManualLinkPostProcessor extends NodePostProcessor {
      val nlDocURL = new Regex("(?:https?://)ccl.northwestern.edu/netlogo/docs/")

      def process(tracker: NodeTracker, node: Node): Unit = {
        val maybeNewNode =
          node match {
            case link: Link if nlDocURL.findPrefixMatchOf(link.getUrl).nonEmpty =>
              for {
                m <- nlDocURL.findPrefixMatchOf(link.getUrl)
              } yield new ManualLink(link, m.matched)
            case link: Link if Try(new URI(link.getUrl.toString).getHost != null).toOption.getOrElse(false) =>
              Some(new ExternalLink(link))
            case _ => None
          }
        maybeNewNode.foreach { newNode =>
          newNode.takeChildren(node)
          node.insertBefore(newNode)
          node.unlink()
          tracker.nodeRemoved(node)
          tracker.nodeAddedWithChildren(newNode)
        }
      }
    }

    object ManualLinkNodeRendererFactory extends NodeRendererFactory {
      override def create(options: DataHolder): NodeRenderer = ManualLinkRenderer
    }

    object ManualLinkRenderer extends NodeRenderer {
      override def getNodeRenderingHandlers: JSet[NodeRenderingHandler[_]] = {
        val set = new JHashSet[NodeRenderingHandler[_]]()
        set.add(new NodeRenderingHandler[ManualLink](
          classOf[ManualLink],
          new CustomNodeRenderer[ManualLink]() {
            override def render(node: ManualLink, context: NodeRendererContext, html: HtmlWriter): Unit = {
              ManualLinkRenderer.this.render(node, context, html)
            }
          }))
        set.add(new NodeRenderingHandler[ExternalLink](
          classOf[ExternalLink],
          new CustomNodeRenderer[ExternalLink]() {
            override def render(node: ExternalLink, context: NodeRendererContext, html: HtmlWriter): Unit = {
              ManualLinkRenderer.this.render(node, context, html)
            }
          }))
        set
      }

      def render(node: ManualLink, context: NodeRendererContext, html: HtmlWriter): Unit = {
        if (context.isDoNotRenderLinks) {
          context.renderChildren(node)
        } else {
          val resolvedLink = context.resolveLink(LinkType.LINK, node.getUrl.unescape, null)
          html.attr("href", resolvedLink.getUrl)
          if (node.getTitle.isNotNull) {
            html.attr("title", node.getTitle.unescape)
          }
          html.srcPos(node.getChars).withAttr(resolvedLink).tag("a")
          context.renderChildren(node)
          html.tag("/a")
        }
      }

      def render(node: ExternalLink, context: NodeRendererContext, html: HtmlWriter): Unit = {
        if (context.isDoNotRenderLinks) {
          context.renderChildren(node)
        } else {
          val resolvedLink = context.resolveLink(LinkType.LINK, node.getUrl.unescape, null)
          html.attr("href", resolvedLink.getUrl)
          if (node.getTitle.isNotNull) {
            html.attr("title", node.getTitle.unescape)
          }
          html.attr("target", "_blank")
          html.srcPos(node.getChars).withAttr(resolvedLink).tag("a")
          context.renderChildren(node)
          html.tag("/a")
        }
      }
    }
  }

  // this extension changes extension section links using the github convention
  // <ext-name><prim-name> to use the flexmark-linking convention <ext-name>:<prim-name>
  class GitHubStyleLinkExtension(extName: String)
    extends Parser.ParserExtension with HtmlRenderer.HtmlRendererExtension {

    def extend(builder: Parser.Builder): Unit = {
      builder.postProcessorFactory(GitHubStyleLinkPostProcessorFactory)
    }

    def rendererOptions(options: MutableDataHolder): Unit = {}
    def parserOptions(options: MutableDataHolder): Unit = {}

    def extend(builder: HtmlRenderer.Builder, rendererType: String): Unit = {
      if (rendererType == "HTML")
        builder.nodeRendererFactory(GitHubStyleLinkNodeRendererFactory)
    }

    class GitHubStyleLink(other: Link) extends InlineLinkNode(
      other.getChars,
      other.getTextOpeningMarker,
      other.getText,
      other.getTextClosingMarker,
      other.getLinkOpeningMarker,
      other.getUrl,
      other.getTitleOpeningMarker,
      other.getTitle,
      other.getTitleClosingMarker,
      other.getLinkClosingMarker) {

      override def setTextChars(textChars: BasedSequence): Unit = {
        val textCharsLength = textChars.length
        textOpeningMarker = textChars.subSequence(0, 1)
        text = textChars.subSequence(1, textCharsLength - 1).trim
        textClosingMarker = textChars.subSequence(textCharsLength - 1, textCharsLength)
      }
    }

    object GitHubStyleLinkPostProcessorFactory extends NodePostProcessorFactory(false) {
      addNodes(classOf[Link])

      def create(document: Document): NodePostProcessor = GitHubStyleLinkPostProcessor
    }

    object GitHubStyleLinkPostProcessor extends NodePostProcessor {
      val extAnchorURL = new Regex(s"#${extName}")

        def process(tracker: NodeTracker, node: Node): Unit = {
          node match {
            case link: Link if extAnchorURL.findPrefixMatchOf(link.getUrl).nonEmpty =>
              for {
                m <- extAnchorURL.findPrefixMatchOf(link.getUrl)
                } {
                  val manualLink = new GitHubStyleLink(link)
                  manualLink.takeChildren(node)
                  node.insertBefore(manualLink)
                  node.unlink()
                  tracker.nodeRemoved(node)
                  tracker.nodeAddedWithChildren(manualLink)
                }
            case _ =>
          }
        }
    }

    object GitHubStyleLinkNodeRendererFactory extends NodeRendererFactory {
      override def create(options: DataHolder): NodeRenderer = GitHubStyleLinkRenderer
    }

    object GitHubStyleLinkRenderer extends NodeRenderer {
      override def getNodeRenderingHandlers: JSet[NodeRenderingHandler[_]] = {
        val set = new JHashSet[NodeRenderingHandler[_]]()
        set.add(new NodeRenderingHandler[GitHubStyleLink](
          classOf[GitHubStyleLink],
          new CustomNodeRenderer[GitHubStyleLink]() {
            override def render(node: GitHubStyleLink, context: NodeRendererContext, html: HtmlWriter): Unit = {
              GitHubStyleLinkRenderer.this.render(node, context, html)
            }
          }))
        set
      }

      def render(node: GitHubStyleLink, context: NodeRendererContext, html: HtmlWriter): Unit = {
        if (context.isDoNotRenderLinks) {
          context.renderChildren(node)
        } else {
          val url = node.getUrl.unescape
          val fixedUrl =
            url.replaceAllLiterally(s"#$extName", s"#$extName:").replaceAllLiterally("::", ":")
          val resolvedLink = context.resolveLink(LinkType.LINK, fixedUrl, null)
          html.attr("href", resolvedLink.getUrl)
          if (node.getTitle.isNotNull) {
            html.attr("title", node.getTitle.unescape)
          }
          html.srcPos(node.getChars).withAttr(resolvedLink).tag("a")
          context.renderChildren(node)
          html.tag("/a")
        }
      }
    }
  }
}
