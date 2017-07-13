import java.util.{ ArrayList => JArrayList, HashSet => JHashSet, Set => JSet }
import java.net.URI

import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.matching.Regex

import com.vladsch.flexmark.{ Extension, ast, ext, html, parser, util },
  ast.{ AutoLink, Node },
  ext.{ anchorlink, autolink, escaped, tables, toc, typographic, wikilink },
    anchorlink.AnchorLinkExtension,
    autolink.AutolinkExtension,
    escaped.character.EscapedCharacterExtension,
    tables.TablesExtension,
    toc.TocExtension,
    typographic.TypographicExtension,
    wikilink.{ WikiLink, WikiLinkExtension },
  html.{ AttributeProvider, CustomNodeRenderer, HtmlRenderer, HtmlWriter,
         IndependentAttributeProviderFactory, LinkResolver, LinkResolverFactory, renderer },
    renderer.{ AttributablePart, LinkStatus, LinkType, NodeRenderer, NodeRendererContext,
               NodeRendererFactory, NodeRenderingHandler, ResolvedLink },
  parser.{ Parser, ParserEmulationProfile },
  util.html.Attributes,
  util.options.{ DataHolder, MutableDataHolder, MutableDataSet }

object Markdown {
  def apply(
    str:                String,
    addTableOfContents: Boolean = true,
    manualizeLinks:     Boolean = true,
    extName:            Option[String] = None): String = {

    val opts = options(addTableOfContents)
    val parser = Parser.builder(opts).build()
    val renderer = {
      val builder = HtmlRenderer.builder(opts)
      builder.attributeProviderFactory(AutolinkAttributeProvider.Factory)
      if (manualizeLinks)
        builder.linkResolverFactory(ManualLinkResolver.Factory)
      extName foreach { ext =>
        builder.linkResolverFactory(new GitHubStyleLinkResolver.Factory(ext))
      }
      builder.build()
    }
    val document = parser.parse(str)
    renderer.render(document)
  }

  private def options(addTableOfContents: Boolean): MutableDataSet = {
    val extensions = new JArrayList[Extension]()
    val options = new MutableDataSet()

    options.setFrom(ParserEmulationProfile.PEGDOWN)

    extensions.add(PrimLinkExtension)

    extensions.add(EscapedCharacterExtension.create())

    options.set(HtmlRenderer.SOFT_BREAK, "\n")
    options.set(HtmlRenderer.HARD_BREAK, "<br />\n")

    extensions.add(TypographicExtension.create())
    options.set(TypographicExtension.ENABLE_QUOTES, Boolean.box(true))
    options.set(TypographicExtension.ENABLE_SMARTS, Boolean.box(true))

    extensions.add(AutolinkExtension.create())

    options.set(Parser.MATCH_CLOSING_FENCE_CHARACTERS, Boolean.box(false))

    extensions.add(TablesExtension.create())
    
    extensions.add(WikiLinkExtension.create())

    if (addTableOfContents)
      extensions.add(TocExtension.create())
    extensions.add(AnchorLinkExtension.create())
    options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "section-anchor")

    options.set(Parser.EXTENSIONS, extensions)

    options
  }

  object AutolinkAttributeProvider extends AttributeProvider {
    override def setAttributes(node: Node, part: AttributablePart, attributes: Attributes) =
      if (node.isInstanceOf[AutoLink] && part == AttributablePart.LINK)
        attributes.replaceValue("target", "_blank")

    object Factory extends IndependentAttributeProviderFactory {
      override def create(context: NodeRendererContext) = AutolinkAttributeProvider
    }
  }

  object PrimLinkExtension extends HtmlRenderer.HtmlRendererExtension {
    def rendererOptions(options: MutableDataHolder) = {}
    def extend(builder: HtmlRenderer.Builder, rendererType: String) =
      if (rendererType == "HTML")
        builder.nodeRendererFactory(PrimLinkRenderer.Factory)
    
    object PrimLinkRenderer extends CustomNodeRenderer[WikiLink] {
      override def render(node: WikiLink, context: NodeRendererContext, html: HtmlWriter) = {
        if (context.isDoNotRenderLinks) {
          html.srcPos(node.getChars).tag("code")
          html.text(node.getLink)
          html.tag("/code")
        } else {
          val resolvedLink = context.resolveLink(LinkType.LINK,
            "dictionary.html#" +
            node.getLink.toString.stripPrefix("__").stripSuffix("?"), null)
          html.attr("href", resolvedLink.getUrl)
          html.srcPos(node.getChars).withAttr(resolvedLink).tag("a")
          html.srcPos(node.getText).withAttr().tag("code")
          html.text(if (node.getText.isNotNull) node.getText else node.getLink)
          html.tag("/code")
          html.tag("/a")
        }
      }

      object Factory extends NodeRendererFactory {
        def create(options: DataHolder) = new NodeRenderer {
          def getNodeRenderingHandlers = Set[NodeRenderingHandler[_]](
            new NodeRenderingHandler(classOf[WikiLink], PrimLinkRenderer)).asJava
        }
      }
    }
  }

  /** Relativizes links when they appear in the manual.
   *  "http://ccl.northwestern.edu/netlogo/docs/x/y/z" is transformed to "x/y/z"
   */
  object ManualLinkResolver extends LinkResolver {
    val nlDocURL = new Regex("(?:https?://)ccl.northwestern.edu/netlogo/docs/")

    override def resolveLink(node: Node, context: NodeRendererContext, link: ResolvedLink) =
      if (nlDocURL.findPrefixMatchOf(link.getUrl.toString).nonEmpty)
        link.withStatus(LinkStatus.VALID).withUrl(nlDocURL.replaceFirstIn(link.getUrl, ""))
      else if (Try(new URI(link.getUrl).getHost != null).toOption.getOrElse(false))
        link.withStatus(LinkStatus.VALID).withTarget("_blank")
      else
        link

    object Factory extends LinkResolverFactory {
      def getBeforeDependents = null
      def getAfterDependents  = null
      def affectsGlobalScope  = false
      def create(context: NodeRendererContext) = ManualLinkResolver
    }
  }

  /** Changes extension section links using the github convention
   *  <ext-name><prim-name> to use the flexmark-linking convention <ext-name>:<prim-name>
   */
  class GitHubStyleLinkResolver(extName: String) extends LinkResolver {
    val extAnchorPrefix = s"#$extName"
    
    override def resolveLink(node: Node, context: NodeRendererContext, link: ResolvedLink) =
      if (link.getUrl.startsWith(extAnchorPrefix))
        link.withStatus(LinkStatus.VALID)
            .withUrl(link.getUrl.replaceAllLiterally(extAnchorPrefix, extAnchorPrefix + ':'))
      else
        link
  }
  
  object GitHubStyleLinkResolver {
    class Factory(extName: String) extends LinkResolverFactory {
      def getBeforeDependents = null
      def getAfterDependents  = null
      def affectsGlobalScope  = false
      def create(context: NodeRendererContext) = new GitHubStyleLinkResolver(extName)
    }
  }
}
