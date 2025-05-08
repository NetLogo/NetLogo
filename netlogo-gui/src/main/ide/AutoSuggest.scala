// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.{ HashMap, ListBuffer }

import org.nlogo.core.Dialect
import org.nlogo.nvm.ExtensionManager

object AutoSuggest {
  def apply(dialect: Dialect, extensionManager: Option[ExtensionManager]): AutoSuggest = {
    val commandNames = dialect.tokenMapper.allCommandNames
    val reporterNames = dialect.tokenMapper.allReporterNames
    val extensionPrimNames =
      extensionManager.map { ex =>
      { () => (ex.extensionCommandNames ++ ex.extensionReporterNames).map(_.toLowerCase) }
      }.getOrElse({() => Set.empty[String]})
    new AutoSuggest(commandNames ++ reporterNames, extensionPrimNames)
  }
}

/**
 * Builds the trie from commands and reporters and provides fuctions to get suggestions.
 * Special care should be taken as everything is inserted and retrieved in lower case.
 */
class AutoSuggest(val primitiveNames: Set[String], extensionPrimNames: () => Set[String]) {
  val EDIT_WEIGHT_ADD = 1
  val EDIT_WEIGHT_REMOVE = 1
  val EDIT_WEIGHT_REPLACE = 1

  val trie = new TrieNode()

  var extensionPrims = Set.empty[String]

  for(name <- primitiveNames) {
    trie.append(name.toLowerCase)
  }

  def editDistance(s1: String, s2: String): Int = {
    val memo = HashMap[(List[Char],List[Char]),Int]()
    def min(a:Int, b:Int, c:Int) = a min b min c
    def stringDistance(s1: List[Char], s2: List[Char]): Int = {
      if (memo.contains((s1,s2)) == false)
        memo((s1,s2)) = (s1, s2) match {
          case (_, Nil) => s1.length
          case (Nil, _) => s2.length
          case (c1::t1, c2::t2)  => min(stringDistance(t1,s2) + EDIT_WEIGHT_ADD, stringDistance(s1,t2) + EDIT_WEIGHT_REMOVE,
            stringDistance(t1,t2) + (if (c1==c2) 0 else EDIT_WEIGHT_REPLACE))
        }
      memo((s1,s2))
    }
    stringDistance(s1.toLowerCase.toList, s2.toLowerCase.toList)
  }

  // reloads extension primitives
  def refresh(): Unit = {
    extensionPrims.foreach(trie.remove)
    extensionPrims = extensionPrimNames()
    extensionPrims.foreach(trie.append)
  }

  def getSuggestions(word: String): Seq[String] = {
    var suggestions = trie.findByPrefix(word)
    if(word.length > 1) {
      trie.findByAcronym(word).foreach(suggestion => suggestions ++= trie.findByPrefix(suggestion))
    }
    suggestions.toSeq
  }

  /**
    * Code below contains the Trie implementation for suggestion retrieval
    */
  object Trie {
    def apply() : Trie = new TrieNode()
  }

  sealed trait Trie extends Iterable[String] {

    def append(key : String): Unit
    def findByPrefix(prefix: String): scala.collection.Seq[String]
    def contains(word: String): Boolean
    def remove(word : String) : Boolean

  }

  class TrieNode(val char : Option[Char] = None, var word: Option[String] = None) extends Trie {

    val children: mutable.Map[Char, TrieNode] = {
        import scala.jdk.CollectionConverters.MapHasAsScala
        new java.util.TreeMap[Char, TrieNode]().asScala
    }

    override def append(key: String) = {

      @tailrec
      def appendHelper(node: TrieNode, currentIndex: Int): Unit = {
        if (currentIndex == key.length) {
          node.word = Some(key)
        } else {
          val char = key.charAt(currentIndex).toLower
          val result = node.children.getOrElseUpdate(char, {
            new TrieNode(Some(char))
          })

          appendHelper(result, currentIndex + 1)
        }
      }

      appendHelper(this, 0)
    }

    override def foreach[U](f: String => U): Unit = {

      @tailrec
      def foreachHelper(nodes: TrieNode*): Unit = {
        if (nodes.size != 0) {
          nodes.foreach(node => node.word.foreach(f))
          foreachHelper(nodes.flatMap(node => node.children.values)*)
        }
      }

      foreachHelper(this)
    }

    override def findByPrefix(prefix: String): scala.collection.Seq[String] = {

      @tailrec
      def helper(currentIndex: Int, node: TrieNode, items: ListBuffer[String]): ListBuffer[String] = {
        if (currentIndex == prefix.length) {
          items ++ node
        } else {
          node.children.get(prefix.charAt(currentIndex).toLower) match {
            case Some(child) => helper(currentIndex + 1, child, items)
            case None => items
          }
        }
      }

      helper(0, this, new ListBuffer[String]())
    }

    def findByAcronym(acronym: String): scala.collection.Seq[String] = {
      def helper(currentIndex: Int, node: TrieNode, items: ListBuffer[String], previousDash: Boolean, word: String): ListBuffer[String] = {
        if(currentIndex == acronym.length){
          items += word
        } else {
          node.children.foreach {
            case child =>
              if (previousDash) {
                if (child._1.toLower == acronym(currentIndex).toLower) {
                  helper(currentIndex + 1, child._2, items, child._1 == '-', word + child._1)
                }
              } else {
                helper(currentIndex, child._2, items, child._1 == '-', word + child._1)
              }
          }
          items
        }
      }
      helper(0, this, new ListBuffer[String](), true, "")
    }

    def findByLength(prefix: String, length: Int): scala.collection.Seq[String] = {

      def helper(currentIndex: Int, node: TrieNode, items: ListBuffer[String], word: String): ListBuffer[String] = {
        if(currentIndex == length){
         items += word
        } else {
          for(child <- node.children){
            helper(currentIndex + 1, child._2, items, word + child._1)
          }
          items
        }
      }

      helper(0, this, new ListBuffer[String](), "")
    }

    override def contains(word: String): Boolean = {

      @tailrec
      def helper(currentIndex: Int, node: TrieNode): Boolean = {
        if (currentIndex == word.length) {
          node.word.isDefined
        } else {
          node.children.get(word.charAt(currentIndex).toLower) match {
            case Some(child) => helper(currentIndex + 1, child)
            case None => false
          }
        }
      }

      helper(0, this)
    }

    override def remove(word : String) : Boolean = {

      pathTo(word) match {
        case Some(path) => {
          var index = path.length - 1
          var continue = true

          path(index).word = None

          while ( index > 0 && continue ) {
            val current = path(index)

            if (current.word.isDefined) {
              continue = false
            } else {
              val parent = path(index - 1)

              if (current.children.isEmpty) {
                parent.children.remove(word.charAt(index - 1).toLower)
              }

              index -= 1
            }
          }

          true
        }
        case None => false
      }

    }

    def pathTo( word : String ) : Option[ListBuffer[TrieNode]] = {

      def helper(buffer : ListBuffer[TrieNode], currentIndex : Int, node : TrieNode) : Option[ListBuffer[TrieNode]] = {
        if ( currentIndex == word.length) {
          node.word.map( word => buffer += node )
        } else {
          node.children.get(word.charAt(currentIndex).toLower) match {
            case Some(found) => {
              buffer += node
              helper(buffer, currentIndex + 1, found)
            }
            case None => None
          }
        }
      }

      helper(new ListBuffer[TrieNode](), 0, this)
    }

    override def iterator: Iterator[String] =
       Iterator(word).flatten ++ children.values.flatMap(_.iterator)

    override def toString() : String = s"Trie(char=${char},word=${word})"

  }

}
