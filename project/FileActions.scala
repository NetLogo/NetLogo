import java.nio.file.{ attribute, Files, FileVisitResult, FileVisitor, Path, Paths, StandardCopyOption }, attribute.BasicFileAttributes
import java.io.IOException
import java.io.File

object FileActions {
  class ListVisitor extends FileVisitor[Path] {
    val pathBuffer = scala.collection.mutable.Buffer[Path]()
    def pathsFound: Seq[Path] = pathBuffer.toSeq

    def postVisitDirectory(p: Path, e: IOException): FileVisitResult =
      if (e != null)
        throw e
      else {
        pathBuffer += p
        FileVisitResult.CONTINUE
      }

    def preVisitDirectory(p: Path, attrs: BasicFileAttributes): FileVisitResult =
      FileVisitResult.CONTINUE

    def visitFile(p: Path, attrs: BasicFileAttributes): FileVisitResult = {
      pathBuffer += p
      FileVisitResult.CONTINUE
    }
    def visitFileFailed(p: Path, e: IOException): FileVisitResult = { throw e }
  }

  class CopyVisitor(src: Path, dest: Path) extends FileVisitor[Path] {
    def postVisitDirectory(p: Path, e: IOException): FileVisitResult = {
      if (e != null)
        throw e
      else
        FileVisitResult.CONTINUE
    }
    def preVisitDirectory(p: Path, attrs: BasicFileAttributes): FileVisitResult = {
      val relativePath = src.relativize(p)
      FileActions.createDirectory(dest.resolve(relativePath))
      FileVisitResult.CONTINUE
    }
    def visitFile(p: Path, attrs: BasicFileAttributes): FileVisitResult = {
      val relativePath = src.relativize(p)
      Files.copy(p, dest.resolve(relativePath), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)
      FileVisitResult.CONTINUE
    }
    def visitFileFailed(p: Path, e: IOException): FileVisitResult = { throw e }
  }

  def copyAll(copies: Traversable[(File, File)]): Unit = {
    copyAllPaths(copies.map {
      case (s, d) => (s.toPath, d.toPath)
    })
  }

  def createDirectory(f: File): Unit =
    createDirectory(f.toPath)

  def createDirectory(p: Path): Unit = {
    try {
      Files.createDirectory(p)
    } catch {
      case e: java.nio.file.FileAlreadyExistsException => // ignore
    }
  }

  def createDirectories(f: File): Unit = createDirectories(f.toPath)

  def createDirectories(p: Path): Unit = { Files.createDirectories(p) }

  def copyAllPaths(copies: Traversable[(Path, Path)]): Unit = {
    copies.foreach {
      case (src, dest) if Files.isDirectory(dest) => createDirectory(dest)
      case (src, dest) =>
        if (! Files.isDirectory(dest.getParent))
          createDirectories(dest.getParent)
        copyFile(src, dest)
    }
  }

  def copyAny(src: File, dest: File): Unit =
    copyAny(src.toPath, dest.toPath)

  def copyAny(src: Path, dest: Path): Unit =
    src match {
      case f if Files.isDirectory(f) => copyDirectory(src, dest)
      case f => copyFile(src, dest)
    }

  def copyFile(src: File, dest: File): Unit = {
    copyFile(src.toPath, dest.toPath)
  }

  def copyFile(src: Path, dest: Path): Unit = {
    Files.copy(src, dest, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)
  }

  def copyDirectory(src: File, dest: File): Unit = {
    copyDirectory(src.toPath, dest.toPath)
  }

  def copyDirectory(src: Path, dest: Path): Unit = {
    Files.walkFileTree(src, new java.util.HashSet(), Int.MaxValue, new CopyVisitor(src, dest))
  }

  def enumerateFiles(path: Path): Seq[Path] = {
    val listVisitor = new ListVisitor()
    Files.walkFileTree(path, new java.util.HashSet(), Int.MaxValue, listVisitor)
    listVisitor.pathsFound
  }
}
