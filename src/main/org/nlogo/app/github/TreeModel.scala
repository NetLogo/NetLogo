package org.nlogo.app.github

import java.util.regex.Pattern

import scala.annotation.tailrec
import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Success

import org.nlogo.api.ModelReader.SEPARATOR
import org.nlogo.app.InfoFormatter
import org.nlogo.app.github.ApiClient.RichWSResponse
import org.nlogo.swing.Implicits.swingExecutionContext

import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsArray
import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.ws.WSResponse

object TreeModel {
  val fileExtensions = Seq(".nlogo", ".nlogo3d")
}

class TreeModel(val client: ApiClient) extends DefaultTreeModel(new DefaultMutableTreeNode) {
  private val _root = new Root(this)
  setRoot(_root)
  _root.addChildren // trigger the lazy future
}

trait CanAddChildren {
  self: MutableTreeNode =>
  val treeModel: TreeModel
  protected def _addChildren: Future[Unit]
  lazy val addChildren = Promise[Unit].completeWith(_addChildren)
  def insertSorted(node: MutableTreeNode, f: (TreeNode) => String): Unit = {
    val index = (0 until getChildCount)
      .find(i => f(getChildAt(i)).toUpperCase > f(node).toUpperCase)
      .getOrElse(getChildCount)
    treeModel.insertNodeInto(node, this, index)
  }
}

trait Node extends DefaultMutableTreeNode {
  val treeModel: TreeModel
  val path: String
  def parentPath = path.split("/").init.mkString("/")
  def info: String
}

class Root(override val treeModel: TreeModel) extends DefaultMutableTreeNode("") with CanAddChildren {
  override def isLeaf = false
  implicit val repoReads: Reads[RepoNode] = (
    (JsPath \ "full_name").read[String] and
    (JsPath \ "description").readNullable[String] and
    (JsPath \ "url").read[String] and
    (JsPath \ "default_branch").read[String]
  )(new RepoNode(treeModel, _, _, _, _))
  override def _addChildren = {
    def process(r: WSResponse): Future[Unit] = {
      val processNextPage: Option[Future[Unit]] =
        r.nextPageUrl.map { nextUrl =>
          treeModel.client.get(nextUrl).flatMap(process)
        }
      for {
        item <- (r.json \ "items").as[JsArray].value
        repo <- item.validate[RepoNode].asOpt
      } insertSorted(repo, _.asInstanceOf[RepoNode].fullName)
      processNextPage.getOrElse(Future(()))
    }
    treeModel.client.get(
      "https://api.github.com/search/repositories",
      "q" -> "language:NetLogo",
      "per_page" -> "100"
    ).flatMap(process)
  }
}

class RepoNode(
  override val treeModel: TreeModel,
  val fullName: String,
  val description: Option[String],
  val url: String,
  val defaultBranch: String)
  extends Node with CanAddChildren {
  val path = "\\"
  lazy val readme: Future[String] =
    treeModel.client.get(url + "/readme").map(_.content)
  override def info: String =
    description.map("<p>" + _ + "</p>").getOrElse("") +
      (readme.value match {
        case Some(Success(readme)) => InfoFormatter(readme)
        case _ => ""
      })
  override def toString = fullName
  override def isLeaf = false
  implicit val folderReads: Reads[FolderNode] = (
    (JsPath \ "path").read[String] and
    (JsPath \ "sha").read[String] and
    (JsPath \ "url").read[String]
  )(new FolderNode(treeModel, _, _, _))
  implicit val modelReads: Reads[ModelNode] = (
    (JsPath \ "path").read[String] and
    (JsPath \ "sha").read[String] and
    (JsPath \ "url").read[String]
  )(new ModelNode(treeModel, _, _, _))

  override def _addChildren = {
    treeModel.client.get(s"${url}/branches/${defaultBranch}").flatMap { r =>
      val treeUrl = (r.json \ "commit" \ "commit" \ "tree" \ "url").as[String]
      val map = mutable.Map[String, Node]()
      def parent(n: Node) = map.getOrElse(n.parentPath, this)
      treeModel.client.get(treeUrl, "recursive" -> "1").map { r =>
        for {
          v <- (r.json \ "tree").as[JsArray].value
          n <- (v \ "type").as[String] match {
            case "tree" => v.validate[FolderNode].asOpt
            case "blob" if (
              TreeModel.fileExtensions.exists(((v \ "path").as[String]).endsWith)
            ) => v.validate[ModelNode].asOpt
            case _ => None
          }
        } {
          parent(n).add(n)
          map += n.path -> n
        }
        @tailrec def prune(): Unit = {
          val nodesToPrune = depthFirstEnumeration.asScala
            .collect { case f: FolderNode if f.getChildCount == 0 => f }
          if (nodesToPrune.nonEmpty) {
            nodesToPrune.foreach { n => parent(n).remove(n) }
            prune()
          }
        }
        prune()
        treeModel.nodesWereInserted(this, (0 until getChildCount).toArray)
      }
    }
  }

}

class ModelNode(
  override val treeModel: TreeModel,
  val path: String,
  val sha: String,
  val url: String)
  extends Node {
  override val toString =
    TreeModel.fileExtensions.foldLeft(path.split("/").last)(_.stripSuffix(_))
  override def isLeaf = true
  lazy val source: Future[String] = treeModel.client.get(url).map(_.content)
  override def info: String = source.value match {
    case Some(Success(source)) =>
      val info = source.split(Pattern.quote(SEPARATOR))(2)
      InfoFormatter(info)
    case _ => "Loading..."
  }
}

class FolderNode(
  override val treeModel: TreeModel,
  val path: String,
  val sha: String,
  val url: String)
  extends Node {
  override val toString = path.split("/").last
  override def isLeaf = false
  override def info = path
}
