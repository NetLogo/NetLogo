// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.io.File

import com.typesafe.config.{ Config, ConfigException, ConfigFactory, ConfigParseOptions, ConfigSyntax }

import org.nlogo.workspace.{ ModelsLibrary}, ModelsLibrary.{ Leaf, Node, Tree }

import scala.annotation.tailrec
import scala.util.{ Try, Failure }

object ModelCrossReferencer {
  val SingleModelsKey = "org.nlogo.models.crossReference.singleModels"
  val DirectoryKey = "org.nlogo.models.crossReference.directories"
  val DefaultPath = ModelsLibrary.modelsRoot + File.separator + "crossReference.conf"

  def addReference(t: Tree, source: String, dest: String): Tree = {
    findTargetNode(t, source.split("/").toIndexedSeq)
      .map(n => insertNode(t, n, dest.split("/").toIndexedSeq))
      .getOrElse(t)
  }

  // NOTE: This method is O(n log n) where n is the number of elements in the source
  // Node. I don't consider this to be speed-critical, but it could easily be optimized to be
  // something like O(n)
  def addDirectoryReference(t: Tree, source: String, dest: String): Tree = {
    findTargetNode(t, source.split("/").toIndexedSeq)
      .flatMap {
        case Tree(_, _, children) =>
          Some(children.foldLeft(t) {
            case (t, child: Leaf) => insertNode(t, child, dest.split("/").toIndexedSeq)
            case (t, _) => t
          })
        case _ => None
      }.getOrElse(t)
  }

  def addDirectoryReferenceRecursive(t: Tree, source: String, dest: String): Tree = {
    findTargetNode(t, source.split("/").toIndexedSeq)
      .flatMap {
        case Tree(_, _, children) =>
          Some(children.foldLeft(t) {
            case (t, child: Tree) =>
              val xRefAdded = insertNode(t, Tree(child.name, "xref", Seq()), dest.split("/").toIndexedSeq)
              addDirectoryReferenceRecursive(xRefAdded, source + "/" + child.name, dest + "/" + child.name)
            case (t, child: Leaf) => insertNode(t, child, dest.split("/").toIndexedSeq)
          })
        case _ => None
      }.getOrElse(t)
  }

  def applyConfig(original: Tree, config: Config): Tree = {
    import scala.jdk.CollectionConverters.ListHasAsScala

    implicit class RichConfig(c: Config) {
      def tryString(key: String): Try[String] =
        Try(c.getString(key))
      def tryBoolean(key: String): Try[Boolean] =
        Try(c.getBoolean(key))
    }

    def warnOnMissing[A](typeExpected: String, t: Try[A]): Try[A] =
      t.recoverWith {
        case missing: ConfigException.Missing =>
          System.err.println(s"Invalid ${typeExpected} on line ${missing.origin.lineNumber}, continuing without")
          Failure(missing)
      }

    def warnNoReference(config: Config, key: String): Option[List[_ <: Config]] =
      try {
        Some(config.getConfigList(key).asScala.toList)
      } catch {
        case missing: ConfigException.Missing =>
          System.err.println(s"No config found for $key, some model cross references may be missing")
          None
      }

    def getModelRef(c: Config): Option[(String, String)] =
      warnOnMissing("single-model crossReference",
        for {
          source <- c.tryString("source")
          dest   <- c.tryString("referenceIn")
        } yield (source, dest)).toOption

    def getDirectoryRef(c: Config): Option[(String, String, Boolean)] =
      warnOnMissing("directory crossReference",
        for {
          source <- c.tryString("sourceDir")
          dest   <- c.tryString("referenceIn")
          rec    <- c.tryBoolean("recursive")
        } yield (source, dest, rec)).toOption

    val singleModelChanges =
      warnNoReference(config, SingleModelsKey).map(_.flatMap(getModelRef)).getOrElse(Seq[(String, String)]())

    val directoryChanges =
      warnNoReference(config, DirectoryKey).map(_.flatMap(getDirectoryRef)).getOrElse(Seq[(String, String, Boolean)]())

    val t2 = singleModelChanges.foldLeft(original) {
      case (t, (source, dest)) => addReference(t, source, dest)
    }

    directoryChanges.foldLeft(t2) {
      case (t, (source, dest, rec)) =>
        if (rec) addDirectoryReferenceRecursive(t, source, dest)
        else addDirectoryReference(t, source, dest)
    }
  }

  def loadConfig(path: String = DefaultPath): Option[Config] = {
    try {
      val parsingConfiguration = ConfigParseOptions.defaults.setSyntax(ConfigSyntax.CONF)
      Some(ConfigFactory.parseFile(new File(path), parsingConfiguration))
    } catch {
      case ex: ConfigException =>
        System.err.println(s"Error loading crossReference config at: $path - ${ex.getMessage}")
        None
    }
  }

  @tailrec
  private def findTargetNode(n: Node, names: Seq[String]): Option[Node] = {
    if (names.isEmpty) Some(n)
    else n match {
      case Tree(_, _, children) =>
        val matchingChild = children.find(_.name == names.head)
        if (matchingChild.isEmpty) None
        else findTargetNode(matchingChild.get, names.tail)
      case _ => None
    }
  }

  private def insertNode(original: Tree, inserting: Node, atPath: Seq[String]): Tree = {
    if (atPath.isEmpty) original.updateChildren(children => children :+ inserting)
    else {
      val thisName = atPath.head
      val existingDir = original.children.find(n => n.name == thisName).collect { case t: Tree => t }
      existingDir match {
        case Some(t) =>
          original.updateChildren(children => children.filterNot(_.name == thisName) :+ insertNode(t, inserting, atPath.tail))
        case None =>
          original.updateChildren(children => children :+ insertNode(Tree(thisName, "xref", Seq()), inserting, atPath.tail))
      }
    }
  }
}
