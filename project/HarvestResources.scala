
import java.io.IOException
import java.nio.file.{ attribute, Files, FileSystems, FileVisitor, FileVisitResult, Path }, attribute.BasicFileAttributes
import javax.xml.parsers.{ DocumentBuilder, DocumentBuilderFactory }
import javax.xml.transform.{ Source, Transformer, TransformerFactory }
import javax.xml.stream.{ XMLInputFactory, XMLOutputFactory }
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stax.StAXResult

import org.w3c.dom.{ Document, Element, Node }

import scala.collection.JavaConverters._
import scala.util.matching.Regex

// a brief note on this class.
// This class is a standin for the WiX heat.exe tool to obtain a wxs file from a directory.
// In the process of trying to simplify the harvesting process, it seemed that we were doing more work to
// transform the heat file than it was worth. As added benefits, this can be tested on the Mac and
// integrates better with our UUID archival system.

object HarvestResources {
  val WiXNamespace = "http://wixtoolset.org/schemas/v4/wxs"

  class AuthoringVisitor(root: Path, doc: Document, directoryRefName: String, wixNode: Node,
                         excludedFiles: Set[String], platformVars: PackageWinAggregate.PlatformVars)
    extends FileVisitor[Path] {

    var directoryNodes: List[Element] = Nil
    var componentNodes: List[Element] = Nil
    var generatedUUIDs: Map[String, String] = Map()

    val filesFragment = doc.createElementNS(WiXNamespace, "Fragment")
    wixNode.appendChild(filesFragment)

    def activeComponent = componentNodes.head
    def activeDirectory = directoryNodes.head

    def postVisitDirectory(p: Path, e: IOException): FileVisitResult = {
      val poppedComponent = componentNodes.head
      componentNodes = componentNodes.tail
      val poppedDirectory = directoryNodes.head
      directoryNodes = directoryNodes.tail

      // no empty components
      if (! poppedComponent.hasChildNodes) {
        poppedDirectory.removeChild(poppedComponent)
        generatedUUIDs -= poppedComponent.getAttribute("Id")
      }
      FileVisitResult.CONTINUE
    }

    def preVisitDirectory(p: Path, attrs: BasicFileAttributes): FileVisitResult = {
      if (directoryNodes.isEmpty && componentNodes.isEmpty) {
        val topDirectory = doc.createElementNS(WiXNamespace, "DirectoryRef")
        topDirectory.setAttribute("Id", directoryRefName)
        filesFragment.appendChild(topDirectory)
        directoryNodes = topDirectory :: directoryNodes

        val topComponent = doc.createElementNS(WiXNamespace, "Component")
        val uuid = generateUUID
        generatedUUIDs += "InstallationRootComponent" -> uuid
        topComponent.setAttribute("Guid", "{" + uuid + "}")
        topComponent.setAttribute("Id", "InstallationRootComponent")
        topComponent.setAttribute("Bitness", platformVars.bitness)
        topDirectory.appendChild(topComponent)
        componentNodes = topComponent :: componentNodes
      } else {
        val pathSegments = root.relativize(p).asScala.iterator.toSeq
        val id = "dir_" + pathSegments.mkString("_").takeRight(68)
        val newDirectory = doc.createElementNS(WiXNamespace, "Directory")
        newDirectory.setAttribute("Name", nameSafe(p.getFileName.toString))
        newDirectory.setAttribute("Id", idSafe(id))
        activeDirectory.appendChild(newDirectory)
        directoryNodes = newDirectory :: directoryNodes
        val newComponent = doc.createElementNS(WiXNamespace, "Component")
        val uuid = generateUUID
        val componentId = idSafe(pathSegments.mkString("_"))
        generatedUUIDs += componentId -> uuid
        newComponent.setAttribute("Guid", "{" + uuid + "}")
        newComponent.setAttribute("Id", componentId)
        newComponent.setAttribute("Bitness", platformVars.bitness)
        newDirectory.appendChild(newComponent)
        componentNodes = newComponent :: componentNodes
      }
      FileVisitResult.CONTINUE
    }


    def visitFile(p: Path, attrs: BasicFileAttributes): FileVisitResult = {
      if (! excludedFiles.contains(p.getFileName.toString)) {
        val fileNode = doc.createElementNS(WiXNamespace, "File")
        val pathElements = root.relativize(p).asScala.iterator.map(_.toString).toSeq
        val sourceName = ("SourceDir" +: pathElements).mkString("\\")
        val id = idSafe(pathElements.mkString("__"))
        fileNode.setAttribute("Source", nameSafe(sourceName))
        fileNode.setAttribute("ProcessorArchitecture", platformVars.platformArch)
        fileNode.setAttribute("Id", id)
        activeComponent.appendChild(fileNode)
      }
      FileVisitResult.CONTINUE
    }

    def visitFileFailed(p: Path, e: IOException): FileVisitResult = { throw e }

    private val idIllegalCharacters = new Regex("[\\ \\-&'()]")

    private def idSafe(s: String): String = {
      // Replace $ with . to make scaladoc pages look different
      idIllegalCharacters.replaceAllIn(s, "").replace("$", ".").takeRight(72)
    }

    private def nameSafe(s: String): String = {
      // $ must be replaced by $$ for WiX to find it
      s.replace("$", "$$")
    }

    private def generateUUID: String = java.util.UUID.randomUUID.toString.toUpperCase

  }

  def harvest(dir: Path, directoryRefName: String, componentGroupName: String, excludedFiles: Set[String],
              platformVars: PackageWinAggregate.PlatformVars, outputFile: Path): Map[String, String] = {

    val docBuilderFactory = DocumentBuilderFactory.newInstance
    docBuilderFactory.setNamespaceAware(true)
    val document = docBuilderFactory.newDocumentBuilder.newDocument
    val docRoot = document.createElementNS(WiXNamespace, "Wix")
    document.appendChild(docRoot)

    val visitor = new AuthoringVisitor(dir, document, directoryRefName, docRoot, excludedFiles, platformVars)

    Files.walkFileTree(dir, new java.util.HashSet(), Int.MaxValue, visitor)

    val componentGroupFragment = document.createElementNS(WiXNamespace, "Fragment")
    docRoot.appendChild(componentGroupFragment)

    val componentGroup = document.createElementNS(WiXNamespace, "ComponentGroup")
    componentGroup.setAttribute("Id", componentGroupName)
    componentGroupFragment.appendChild(componentGroup)

    visitor.generatedUUIDs.keys.toSeq.sortBy(_.length).foreach { (componentId) =>
      val cRef = document.createElementNS(WiXNamespace, "ComponentRef")
      cRef.setAttribute("Id", componentId)
      componentGroup.appendChild(cRef)
    }

    val dom = new DOMSource(document)

    val stream = Files.newOutputStream(outputFile)
    val output = XMLOutputFactory.newFactory.createXMLStreamWriter(stream)
    val result = new StAXResult(output)

    val transformer = TransformerFactory.newInstance.newTransformer()
    transformer.transform(dom, result)

    stream.flush
    stream.close()

    visitor.generatedUUIDs
  }
}
