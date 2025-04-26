// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.File
import java.nio.file.{ Files, Path }
import javax.swing.tree.DefaultMutableTreeNode

import org.nlogo.api.{ FileIO, Version }
import org.nlogo.core.I18N

import scala.annotation.tailrec
import scala.math.Ordering

object ModelsLibrary {
  var rootNode: Option[Node] = None

  def modelsRoot: String = System.getProperty("netlogo.models.dir", "models")

  def getModelPaths: Array[String] = getModelPaths(false)

  def getModelPaths(exclusive: Boolean): Array[String] = getModelPaths(exclusive, true)

  def getModelPaths(exclusive: Boolean, useExtensionExamples: Boolean): Array[String] = {
    scanForModels(exclusive, useExtensionExamples)
    val fileSep = File.separator
    rootNode.map {
      _.depthFirstIterable.filter {
        case Leaf(name, path) => ! path.contains(s"${fileSep}models${fileSep}test${fileSep}")
        case _ => false
      }
      .map(_.path)
      .toArray[String]
    }.getOrElse(Array[String]())
  }

  def getModelPathsAtRoot(path: String): Array[String] = {
    val rnode = scanForModelsAtRoot(path, false)
    rnode.map {
      _.depthFirstIterable.collect {
        case Leaf(_, path) => path
      }.toArray[String]
    }.getOrElse(Array[String]())
  }

  def findModelsBySubstring(targetName: String): Seq[String] = {
    scanForModels(false)

    def exactMatch(node: Node): Option[Seq[String]] =
      node.depthFirstIterable
        .find(n => n.name.toUpperCase.startsWith(s"${targetName.toUpperCase}.NLOGOX"))
        .filter(_.isLeaf)
        .map(n => Seq(n.name))

    def initialMatch(node: Node): Seq[String] =
      node.depthFirstIterable
        .filter(n => n.name.toUpperCase.startsWith(targetName.toUpperCase))
        .filter(_.isLeaf)
        .map(n => n.name)
        .toSeq

    def anywhereMatch(node: Node): Seq[String] =
      node.depthFirstIterable
        .filter(n => n.name.toUpperCase.contains(targetName.toUpperCase))
        .filter(_.isLeaf)
        .map(n => n.name)
        .toSeq

    def partialMatch(node: Node): Seq[String] =
      (initialMatch(node) ++ anywhereMatch(node)).distinct

    rootNode.map { node =>
      exactMatch(node) getOrElse partialMatch(node)
    }.getOrElse(Seq[String]())
  }

  /**
   * scans for and returns the full path name to the given model in the
   * models library.
   *
   * @param targetName the name of the model to scan for, not including the
   *                   ".nlogox(3d)" extension.
   * @return the path to the model, or None if no such model is in the library.
   */
  def getModelPath(targetName: String): Option[String] = {
    scanForModels(false)
    rootNode.flatMap {
      _.depthFirstIterable
        .find(n =>
            n.path.toUpperCase.split(File.separator(0)).last.startsWith(s"${targetName.toUpperCase}"))
        .map(_.path)
    }
  }

  def needsModelScan: Boolean = rootNode.isEmpty

  def scanForModels(exclusive: Boolean): Unit = scanForModels(exclusive, true)

  def scanForModels(exclusive: Boolean, useExtensionExamples: Boolean): Unit = {
    if (! needsModelScan) {
      return
    }
    try {
      val directoryRoot =
        if (!Version.is3D || !exclusive) new File(modelsRoot, "").getCanonicalFile
        else new File(modelsRoot, "3D").getCanonicalFile

      def getExtensionExamples(): Option[Node] = {
        val unverified              = I18N.shared.get("modelsLibrary.unverified")
        val extensionManagerSamples = I18N.shared.get("modelsLibrary.extensionManagerSamples")

        def unverifyIfTree(c: Node): Node = c match {
          case Tree(name, path, children) => Tree(name = s"${name} $unverified", path = path, children = children)
          case n                          => n
        }

        val extensionsRoot = new File(FileIO.perUserDir("extensions", true), "").getCanonicalFile
        val extensionsNode = scanDirectory(extensionsRoot.toPath, exclusive, Some(extensionManagerSamples))

        extensionsNode match {
          case Some(Tree(name, path, children)) =>
            val unverifiedChildren = children.map(unverifyIfTree)
            Some(Tree(name = name, path = path, children = unverifiedChildren))

          case en => en
        }
      }

      rootNode = scanDirectory(directoryRoot.toPath, exclusive) match {
        case Some(Tree(name, _, children)) if useExtensionExamples => {
          val extensionsNode = getExtensionExamples()
          val allChildren    = children ++ extensionsNode.map((en) => Seq(en)).getOrElse(Seq())
          Some(Tree(name = name, path = "", children = allChildren)(new TopLevelOrdering(exclusive)))
        }
        case rn => rn
      }
    } catch {
      case e: java.io.IOException =>
        System.err.println("error: IOException canonicalizing models library path")
        System.err.println(e.getMessage)
        e.printStackTrace()
    }
  }

  def scanForModelsAtRoot(path: String, exclusive: Boolean): Option[Node] =
    scanDirectory(new File(path, "").toPath, exclusive)

  def getImagePath(filePath: String): String = {
    val index = filePath.indexOf(".nlogox");
    val path =
      if (index != -1) filePath.substring(0, index)
      else             filePath
    s"${path}.png"
  }

  private def getChildPaths(directory: Path): Seq[Path] = {
    val fileList = Files.list(directory)

    try {
      import scala.jdk.CollectionConverters.IteratorHasAsScala
      // use `toList` to force so that we can close the file iterator
      fileList.iterator.asScala.toList
    } catch {
      case ex: Exception => println(ex)
      Seq()
    } finally {
      fileList.close()
    }
  }

  private def scanDirectory(directory: Path, exclusive: Boolean, nameOverride: Option[String] = None): Option[Node] = {
    if (!Files.isDirectory(directory)) {
      None
    } else {

      val children =
        getChildPaths(directory).sortBy(_.getFileName.toString)(NLogoModelOrdering)
          .filterNot(p => isBadName(p.getFileName.toString))
          .flatMap { (p: Path) =>
            if (Files.isDirectory(p)) {
              scanDirectory(p, exclusive, nameOverride)
            } else {
              val fileName = p.getFileName.toString.toUpperCase
              if (fileName.endsWith(".NLOGO") || fileName.endsWith(".NLOGO3D") ||
                  fileName.endsWith(".NLOGOX") || fileName.endsWith(".NLOGOX3D")) {
                Some(Leaf(p.getFileName.toString, p.toString))
              } else {
                None
              }
            }
        }

      // don't add empty folders
      if (children.nonEmpty) {
        val path        = directory.toString + File.separator
        val displayName = nameOverride.getOrElse(directory.getFileName.toString)
        Some(Tree(displayName, path, children.toSeq)(NLogoModelOrdering))
      } else {
        None
      }
    }
  }

  /// helpers

  // we use this so that "Foo.nlogox" sorts before "Foo
  // Alternate.nlogox", not after - ST 8/31/04
  object NLogoModelOrdering
    extends scala.math.Ordering[String] {
    def compare(s1: String, s2: String): Int =
      if (s2.toUpperCase == "UNVERIFIED")
        -1
      else if (s1.toUpperCase == "UNVERIFIED")
        1
      else
        String.CASE_INSENSITIVE_ORDER.compare(munge(s1), munge(s2))

    private def munge(_s: String): String = {
      val s = _s.toUpperCase()
      if (s.endsWith(".NLOGOX"))        s.substring(0, s.length - 7)
      else if (s.endsWith(".NLOGOX3D")) s.substring(0, s.length - 9)
      else                              s
    }
  }

  // This is only used at top-level, so we don't need to munge
  private class TopLevelOrdering(exclusive: Boolean)
    extends scala.math.Ordering[String] {
      val orderedNames =
        if (! Version.is3D)
          Seq(
            "SAMPLE MODELS", "CURRICULAR MODELS", "CODE EXAMPLES",
            "HUBNET ACTIVITIES", "IABM TEXTBOOK", "ALTERNATIVE VISUALIZATIONS",
            "EXTENSION MANAGER SAMPLES")
        else if (Version.is3D && exclusive)
          Seq("3D")
        else if (Version.is3D && ! exclusive)
          Seq(
            "3D", "SAMPLE MODELS", "CURRICULAR MODELS", "CODE EXAMPLES",
            "HUBNET ACTIVITIES", "IABM TEXTBOOK", "ALTERNATIVE VISUALIZATIONS",
            "EXTENSION MANAGER SAMPLES")
        else
          Seq()

      def indexOf(s: String) = {
        val i = orderedNames.indexOf(s.toUpperCase)
        if (i == -1) orderedNames.length
        else i
      }

      def compare(s1: String, s2: String): Int = {
        val index1 = indexOf(s1)
        val index2 = indexOf(s2)
        val d = index1 - index2
        if (d == 0)
          String.CASE_INSENSITIVE_ORDER.compare(s1, s2)
        else
          d
      }
    }

  private def isBadName(name: String): Boolean = {
    // ignore invisible stuff
    name.startsWith(".") ||
    // ignore the directory containing the sample beats
    // for the Beatbox model
    name == "BEATS" ||
    // when we're not 3D ignore the 3D models.
    (!Version.is3D &&
      (name == "3D" ||
        // the vrml extension is our only 3D extension at present
        // so just special case it - ST 6/12/08
        name == "VRML"))
  }

  ///

  // Normally nothing in this package should depend on Swing stuff,
  // but actually DefaultMutableTreeNode isn't a GUI class, and in
  // fact if you look at Sun's source for it there's a comment:
  //   // ISSUE: this class depends on nothing in AWT -- move to java.util?
  // so I think it's kosher for us to use it here. - ST 9/18/03
  class JNode(val name: String, val path: String, val isFolder: Boolean)
    extends DefaultMutableTreeNode {
    allowsChildren = isFolder

    def getName = name

    def getFilePath = path

    override def toString = name
    override def isLeaf = ! isFolder
  }

  sealed trait Node {
    def name: String
    def path: String
    def depthFirstIterable: Iterable[Node]
    def breadthFirstIterable: Iterable[Node]
    def isLeaf: Boolean
    def isFolder: Boolean
    def allowsChildren: Boolean = isFolder
  }

  case class Leaf(name: String, path: String) extends Node {
    def depthFirstIterable: Iterable[Node] = Seq(this)
    def breadthFirstIterable: Iterable[Node] = Seq(this)
    def isLeaf = true
    def isFolder = false
  }

  object Tree {
    implicit val defaultOrdering: org.nlogo.workspace.ModelsLibrary.NLogoModelOrdering.type = NLogoModelOrdering
  }

  case class Tree(name: String, path: String, children: Seq[Node])(implicit childOrdering: Ordering[String]) extends Node {
    def depthFirstIterable: Iterable[Node] = children.flatMap(_.depthFirstIterable) ++ Seq(this)
    def breadthFirstIterable: Iterable[Node] = traverseBreadthFirst(Seq(this))
    def isLeaf = false
    def isFolder = true
    private def traverseBreadthFirst(elems: Seq[Node]): List[Node] = {
      @tailrec
      def traverseBreadthFirstRec(acc: List[Node], elems: Seq[Node]): List[Node] = {
        if (elems.isEmpty)
          acc
        else
          elems.head match {
            case l: Leaf => traverseBreadthFirstRec(l :: acc, elems.tail)
            case t: Tree => traverseBreadthFirstRec(t :: acc, elems.tail ++ t.children)
          }
      }

      traverseBreadthFirstRec(Nil, elems).reverse
    }
    def updateChildren(f: Seq[Node] => Seq[Node]) =
      copy(name, path, children = f(children).sortBy(_.name)(childOrdering))
  }
}
