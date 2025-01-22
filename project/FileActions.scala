import java.nio.file.{ attribute, Files, FileVisitResult, FileVisitor, Path, Paths, StandardCopyOption }, attribute.BasicFileAttributes
import java.io.{ File, IOException }
import java.net.URL
import sbt.IO
import org.apache.commons.io.FileUtils

import scala.collection.JavaConverters._

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

  class CopyFilterVisitor(src: Path, dest: Path, filter: Path => Boolean) extends CopyVisitor(src, dest) {
    override def visitFile(p: Path, attrs: BasicFileAttributes): FileVisitResult = {
      if (filter(p))
        super.visitFile(p, attrs)
      else if (Files.isDirectory(p))
        FileVisitResult.SKIP_SUBTREE
      else
        FileVisitResult.CONTINUE
    }

    override def preVisitDirectory(p: Path, attrs: BasicFileAttributes): FileVisitResult = {
      if (filter(p)) super.visitFile(p, attrs)
      else FileVisitResult.SKIP_SUBTREE
    }
  }

  def copyAll(copies: Traversable[(File, File)]): Unit = {
    copyAllPaths(copies.map {
      case (s, d) => (s.toPath, d.toPath)
    })
  }

  // Creates a new directory specified as a file. The createDirectories method should be
  // used if it is necessaryto create all nonexistent parent directories first.  No error
  // if the directory already exists
  def createDirectory(f: File): Unit =
    createDirectory(f.toPath)

  // Creates a new directory specified as a path. The createDirectories method should be
  // used if it is necessary to create all nonexistent parent directories first.  No error
  // if the directory already exists
  def createDirectory(p: Path): Unit = {
    try {
      Files.createDirectory(p)
    } catch {
      case e: java.nio.file.FileAlreadyExistsException => // ignore
    }
  }

  // Creates a directory by creating all nonexistent parent directories first.  No error
  // if the directory already exists
  def createDirectories(f: File): Unit = createDirectories(f.toPath)

  // Creates a directory by creating all nonexistent parent directories first.  No error
  // if the directory already exists
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

  // Copies a file or directory to a destination. If directory copies all contents
  // overwrites any target files that exist
  def copyAny(src: File, dest: File): Unit =
    copyAny(src.toPath, dest.toPath)

  // Copies a file or directory (as path) to a destination (as path). If directory copies
  // all contents.  Overwrites any target files that exist.
  def copyAny(src: Path, dest: Path): Unit =
    src match {
      case f if Files.isDirectory(f) => copyDirectory(src, dest)
      case f => copyFile(src, dest)
    }

  // Copies a file to a destination, overwriting the target file if it exists.
  def copyFile(src: File, dest: File): Unit = {
    copyFile(src.toPath, dest.toPath)
  }

  // Copies a path to a destination, overwriting the target path if it exists. If file is a
  // directory, does not copy contents.
  def copyFile(src: Path, dest: Path): Unit = {
    Files.copy(src, dest, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)
  }

  // Copies a directory (as file) and its contents to a destination, overwriting any
  // target files that exist.
  def copyDirectory(src: File, dest: File): Unit = {
    copyDirectory(src.toPath, dest.toPath)
  }

  // Copies a directory (as path) and its contents to a destination, overwriting any
  // target files that exist.
  def copyDirectory(src: Path, dest: Path): Unit = {
    Files.walkFileTree(src, new java.util.HashSet(), Int.MaxValue, new CopyVisitor(src, dest))
  }

  // Copies a directory (as path) and its contents that satisfy a filter to a destination,
  // overwriting any target files that exists
  def copyDirectory(src: Path, dest: Path, filter: Path => Boolean): Unit = {
    Files.walkFileTree(src, new java.util.HashSet(), Int.MaxValue, new CopyFilterVisitor(src, dest, filter))
  }

  // Moves the file to the target file, failing if the target file exists. Moving a
  // directory will fail if it requires moving sub files.
  def moveFile(src: File, dest: File): Unit = {
    Files.move(src.toPath, dest.toPath)
  }

  // Finds files at the specified path (non-recursive)
  def listDirectory(path: Path): Seq[Path] = {
    Files.walk(path, 1).iterator.asScala.toSeq
  }

  // Finds files at the specified path and below (recursive)
  def enumeratePaths(path: Path): Seq[Path] = {
    val listVisitor = new ListVisitor()
    Files.walkFileTree(path, new java.util.HashSet(), Int.MaxValue, listVisitor)
    listVisitor.pathsFound
  }

  // Copies bytes from the URL source to a file destination. The directories up to
  // destination will be created if they don't already exist. The destination will be
  // overwritten if it already exists.
  def download(url: URL, file: File): Unit = {
    FileUtils.copyURLToFile(url, file)
  }

  // Deletes file, recursively if it is a directory. No error if file does not exist
  def remove(f: File): Unit = {
    IO.delete(f)
  }

  // Creates a relative soft symbolic link to a specified target. Input: link - a File for the
  // symbolic link to create, target - a File for the target of the symbolic link. If the
  // link already exists it will be overwritten.
  def createRelativeSoftLink(link: File, target: File): Unit = {
    createRelativeSoftLink(link.toPath, target.toPath)
  }

  // Creates a relative soft symbolic link to a specified target.  Input: link - the absolute
  // path of the symbolic link to create, target - the absolute path of the target of the
  // symbolic link. If the link already exists it will be overwritten.
  def createRelativeSoftLink(link: Path, target: Path): Unit = {
    val relativeTarget = link.getParent().relativize(target)
    if (Files.exists(link)) {
      Files.delete(link)
    }
    // create the directory hierarchy if any folder is missing
    link.getParent().toFile().mkdirs()
    Files.createSymbolicLink(link, relativeTarget)
  }
}
