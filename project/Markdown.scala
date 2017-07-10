import java.util.{ ArrayList => JArrayList, HashSet => JHashSet, Set => JSet }
import java.net.URI

import scala.util.Try
import scala.util.matching.Regex

import com.vladsch.flexmark.{ Extension, ast, ext, html, parser, util },
  ast.{ AutoLink, Node },
  ext.{ anchorlink, autolink, escaped, tables, toc, typographic },
    anchorlink.AnchorLinkExtension,
    autolink.AutolinkExtension,
    escaped.character.EscapedCharacterExtension,
    tables.TablesExtension,
    toc.TocExtension,
    typographic.TypographicExtension,
  html.{ AttributeProvider, HtmlRenderer, IndependentAttributeProviderFactory,
         LinkResolver, LinkResolverFactory, renderer },
    renderer.{ AttributablePart, LinkStatus, NodeRendererContext, ResolvedLink },
  parser.{ Parser, ParserEmulationProfile },
  util.html.Attributes,
  util.options.MutableDataSet

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

    extensions.add(EscapedCharacterExtension.create())

    options.set(HtmlRenderer.SOFT_BREAK, "\n")
    options.set(HtmlRenderer.HARD_BREAK, "<br />\n")

    extensions.add(TypographicExtension.create())
    options.set(TypographicExtension.ENABLE_QUOTES, Boolean.box(true))
    options.set(TypographicExtension.ENABLE_SMARTS, Boolean.box(true))

    extensions.add(AutolinkExtension.create())

    options.set(Parser.MATCH_CLOSING_FENCE_CHARACTERS, Boolean.box(false))

    extensions.add(TablesExtension.create())

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
