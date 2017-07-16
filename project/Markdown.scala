import java.util.{ ArrayList => JArrayList, HashSet => JHashSet, Set => JSet }
import java.net.URI

import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.matching.Regex

import com.vladsch.flexmark.{ Extension, ast, ext, html, parser, util },
  ast.{ AutoLink, Link, Node },
  ext.{ anchorlink, autolink, escaped, tables, toc, typographic, wikilink },
    anchorlink.AnchorLinkExtension,
    autolink.AutolinkExtension,
    escaped.character.EscapedCharacterExtension,
    tables.TablesExtension,
    toc.TocExtension,
    typographic.TypographicExtension,
    wikilink.{ WikiImage, WikiLink, WikiLinkExtension },
  html.{ AttributeProvider, CustomNodeRenderer, HtmlRenderer, HtmlWriter,
         IndependentAttributeProviderFactory, renderer },
    renderer.{ AttributablePart, LinkType, NodeRenderer, NodeRendererContext,
               NodeRendererFactory, NodeRenderingHandler },
  parser.{ Parser, ParserEmulationProfile },
  util.html.Attributes,
  util.options.{ DataHolder, MutableDataHolder, MutableDataSet }

object Markdown {
  def apply(
    str:                String,
    pageName:           String,
    extension:          Boolean): String = {

    val opts = options()
    val parser = Parser.builder(opts).build()
    val renderer =
      HtmlRenderer.builder(opts)
        .attributeProviderFactory(new ManualAttributeProvider.Factory(pageName, extension))
        .build()
    val document = parser.parse(str)
    renderer.render(document)
  }

  private def options(): MutableDataSet = {
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

    extensions.add(PrimLinkExtension)
    extensions.add(WikiLinkExtension.create())
    options.set(WikiLinkExtension.IMAGE_LINKS, Boolean.box(true))

    extensions.add(TocExtension.create())
    extensions.add(AnchorLinkExtension.create())
    options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "section-anchor")

    options.set(Parser.EXTENSIONS, extensions)

    options
  }

  class ManualAttributeProvider(pageName: String, ext: Boolean) extends AttributeProvider {
    val nlDocURL = new Regex("https?://ccl.northwestern.edu/netlogo/docs/")
    val extAnchorPrefix = s"#$pageName"

    def setAttributes(node: Node, part: AttributablePart, attrs: Attributes) =
      if (part == AttributablePart.LINK)
        node match {
          case _: AutoLink =>
            attrs.replaceValue("target", "_blank")
          case link: Link =>
            if (nlDocURL.findPrefixMatchOf(link.getUrl).nonEmpty)
              attrs.replaceValue("href", nlDocURL.replaceFirstIn(link.getUrl, ""))
            else if (link.getUrl.indexOf('/') != -1)
              attrs.replaceValue("target", "_blank")
            else if (ext && link.getUrl.startsWith(extAnchorPrefix)) {
              val anchor = link.getUrl.toString.replaceAllLiterally(extAnchorPrefix, extAnchorPrefix + ':')
              attrs.replaceValue("href", anchor)
            }
          case img: WikiImage if part == AttributablePart.LINK =>
            val srcPrefix = if (img.getLink.startsWith("/")) "images" else s"images/$pageName/"
            val alt = if (img.getText.isNotNull) img.getText else "Screenshot"
            attrs.replaceValue("class", "screenshot")
            attrs.replaceValue("src", srcPrefix + img.getLink)
            attrs.replaceValue("alt", alt)
          case _ =>
        }
  }
  
  object ManualAttributeProvider {
    class Factory(pageName: String, ext: Boolean) extends IndependentAttributeProviderFactory {
      override def create(context: NodeRendererContext) = new ManualAttributeProvider(pageName, ext)
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
}
