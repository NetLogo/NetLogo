// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import java.io.IOException
import java.nio.file.{ attribute, FileVisitor, FileVisitResult, Files, Path },
  attribute.BasicFileAttributes

import scala.collection.mutable.{ Buffer => MutableBuffer }

object PathTools {

  def findChildrenRecursive(path: Path): Seq[Path] = {
    val visitor = new DefaultVisitor()
    Files.walkFileTree(path, visitor)
    visitor.pathsFound
  }

  def findChildrenRecursive(path: Path, filterDirectories: Path => Boolean): Seq[Path] = {
    val visitor = new FilterDirectoriesVisitor(filterDirectories)
    Files.walkFileTree(path, visitor)
    visitor.pathsFound
  }

  abstract class BaseVisitor extends FileVisitor[Path] {
    def fileFound(p: Path): Unit
    def directoryFound(p: Path): Boolean

    def postVisitDirectory(p: Path, e: IOException): FileVisitResult =
      if (e != null)
        throw e
      else
        FileVisitResult.CONTINUE

    def preVisitDirectory(p: Path, attrs: BasicFileAttributes): FileVisitResult =
      if (directoryFound(p))
        FileVisitResult.CONTINUE
      else
        FileVisitResult.SKIP_SUBTREE

    def visitFile(p: Path, attrs: BasicFileAttributes): FileVisitResult = {
      fileFound(p)
      FileVisitResult.CONTINUE
    }
    def visitFileFailed(p: Path, e: IOException): FileVisitResult = { throw e }
  }

  class DefaultVisitor extends BaseVisitor {
    val pathBuffer = MutableBuffer[Path]()
    def pathsFound: Seq[Path] = pathBuffer.toSeq

    def fileFound(p: Path): Unit =
      pathBuffer += p

    def directoryFound(p: Path): Boolean = true
  }

  class FilterDirectoriesVisitor(filter: Path => Boolean) extends DefaultVisitor {
    override def directoryFound(p: Path): Boolean = filter(p)
  }
}
