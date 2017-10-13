// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.File
import java.nio.file.{ Files, Path }
import javax.swing.tree.DefaultMutableTreeNode

import org.nlogo.api.Version

import scala.annotation.tailrec
import scala.math.Ordering

object ModelsLibrary {
  var rootNodes: Map[Version, Node] = Map()

  def modelsRoot: String = System.getProperty("netlogo.models.dir", "models")

  def getModelPaths(version: Version): Array[String] = getModelPaths(version, false)

  def getModelPaths(version: Version, exclusive: Boolean): Array[String] = {
    scanForModels(version, exclusive)
    val fileSep = File.separator
    rootNodes.get(version).map {
      _.depthFirstIterable.filter {
        case Leaf(name, path) => ! path.contains(s"${fileSep}models${fileSep}test${fileSep}")
        case _ => false
      }
      .map(_.path)
      .toArray[String]
    }.getOrElse(Array[String]())
  }

  def getModelPathsAtRoot(path: String, version: Version): Array[String] = {
    val rnode = scanForModelsAtRoot(path, version, false)
    rnode.map {
      _.depthFirstIterable.collect {
        case Leaf(_, path) => path
      }.toArray[String]
    }.getOrElse(Array[String]())
  }

  def findModelsBySubstring(targetName: String, version: Version): Seq[String] = {
    scanForModels(version, false)

    def exactMatch(node: Node): Option[Seq[String]] =
      node.depthFirstIterable
        .find(n => n.name.toUpperCase.startsWith(s"${targetName.toUpperCase}.NLOGO"))
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

    rootNodes.get(version).map { node =>
      exactMatch(node) getOrElse partialMatch(node)
    }.getOrElse(Seq[String]())
  }

  /**
   * scans for and returns the full path name to the given model in the
   * models library.
   *
   * @param targetName the name of the model to scan for, not including the
   *                   ".nlogo" extension.
   * @return the path to the model, or None if no such model is in the library.
   */
  def getModelPath(targetName: String, version: Version): Option[String] = {
    scanForModels(version, false)
    rootNodes.get(version).flatMap {
      _.depthFirstIterable
        .find(n =>
            n.path.toUpperCase.split(File.separator(0)).last.startsWith(s"${targetName.toUpperCase}"))
        .map(_.path)
    }
  }

  def needsModelScan(version: Version): Boolean = ! rootNodes.isDefinedAt(version)

  def scanForModels(version: Version, exclusive: Boolean): Unit = {
    if (! needsModelScan(version)) {
      return
    }
    try {
      val directoryRoot =
        if (!version.is3D || !exclusive) new File(modelsRoot, "").getCanonicalFile
        else new File(modelsRoot, "3D").getCanonicalFile
      scanDirectory(directoryRoot.toPath, true, version, exclusive).foreach { dir =>
        rootNodes = rootNodes + (version -> dir)
      }
    } catch {
      case e: java.io.IOException =>
        System.err.println("error: IOException canonicalizing models library path")
        System.err.println(e.getMessage)
    }
  }

  def scanForModelsAtRoot(path: String, version: Version, exclusive: Boolean): Option[Node] =
    scanDirectory(new File(path, "").toPath, true, version, exclusive)

  def getImagePath(filePath: String): String = {
    val index = filePath.indexOf(".nlogo");
    val path =
      if (index != -1) filePath.substring(0, index)
      else             filePath
    s"${path}.png"
  }

  private def getChildPaths(directory: Path): Seq[Path] = {
    val fileList = Files.list(directory)

    try {
      import scala.collection.JavaConverters._
      // use `toList` to force so that we can close the file iterator
      fileList.iterator.asScala.toList
    } catch {
      case ex: Exception => println(ex)
      Seq()
    } finally {
      fileList.close()
    }
  }

  private def scanDirectory(directory: Path, topLevel: Boolean, version: Version, exclusive: Boolean): Option[Node] = {
    if (! Files.isDirectory(directory) || Files.isSymbolicLink(directory)) {
      None
    }

    val ordering =
      if (topLevel)
        new TopLevelOrdering(version, exclusive)
      else
        NLogoModelOrdering

    val children =
      getChildPaths(directory).sortBy(_.getFileName.toString)(ordering)
        .filterNot(p => isBadName(p.getFileName.toString, version))
        .flatMap { (p: Path) =>
        if (Files.isDirectory(p))
          scanDirectory(p, false, version, exclusive)
        else {
          val fileName = p.getFileName.toString.toUpperCase
          if (fileName.endsWith(".NLOGO") || fileName.endsWith(".NLOGO3D"))
            Some(Leaf(
              p.getFileName.toString,
              p.toString))
          else
            None
        }
      }

    // don't add empty folders
    if (children.nonEmpty) {
      val path = if (topLevel) "" else directory.toString + File.separator
      val displayName = directory.getFileName.toString
      Some(Tree(displayName, path, children.toSeq)(ordering))
    } else
      None
  }

  /// helpers

  // we use this so that "Foo.nlogo" sorts before "Foo
  // Alternate.nlogo", not after - ST 8/31/04
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
      if (s.endsWith(".NLOGO"))        s.substring(0, s.length - 6)
      else if (s.endsWith(".NLOGO3D")) s.substring(0, s.length - 8)
      else                             s
    }
  }

  // This is only used at top-level, so we don't need to munge
  private class TopLevelOrdering(version: Version, exclusive: Boolean)
    extends scala.math.Ordering[String] {
      val orderedNames =
        if (! version.is3D)
          Seq(
            "SAMPLE MODELS", "CURRICULAR MODELS", "CODE EXAMPLES",
            "HUBNET ACTIVITIES", "IABM TEXTBOOK", "ALTERNATIVE VISUALIZATIONS")
        else if (version.is3D && exclusive)
          Seq("3D")
        else if (version.is3D && ! exclusive)
          Seq(
            "3D", "SAMPLE MODELS", "CURRICULAR MODELS", "CODE EXAMPLES",
            "HUBNET ACTIVITIES", "IABM TEXTBOOK", "ALTERNATIVE VISUALIZATIONS")
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

  private def isBadName(name: String, version: Version): Boolean = {
    // ignore invisible stuff
    name.startsWith(".") ||
    // ignore the directory containing the sample beats
    // for the Beatbox model
    name == "BEATS" ||
    // when we're not 3D ignore the 3D models.
    (!version.is3D &&
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
    implicit val defaultOrdering = NLogoModelOrdering
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
