// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.nio.file.{ FileSystems, Path, WatchKey, WatchService, WatchEvent }
import java.nio.file.StandardWatchEventKinds.{ ENTRY_CREATE, ENTRY_MODIFY }

import scala.jdk.CollectionConverters.ListHasAsScala

private class FileWatcherThread(paths: List[Path], callback: () => Boolean) extends Thread {
  private val watchService: WatchService = FileSystems.getDefault.newWatchService
  private val parentSet: Set[Path] = paths.map(_.getParent).toSet

  private def f(x: Path): (WatchKey, Path) = x.register(watchService, ENTRY_CREATE, ENTRY_MODIFY) -> x
  private val keyPathMap: Map[WatchKey, Path] = parentSet.map(f).toMap

  override def run(): Unit = {
    try {
      var done: Boolean = false

      while (!done && !isInterrupted()) {
        val key: WatchKey = watchService.take
        val events: Iterator[WatchEvent[?]] = key.pollEvents.asScala.iterator
        val maybeDirPath: Option[Path] = keyPathMap.get(key)

        while (!done && maybeDirPath.isDefined && events.hasNext) {
          val eventPath: Path = events.next.context.asInstanceOf[Path]
          val fullEventPath: Path = maybeDirPath.get.resolve(eventPath)

          if (paths.contains(fullEventPath)) {
            done = callback()
          }
        }

        key.reset
      }
    } catch {
      // Do nothing for InterruptedException. We just need to exit the loop.
      case e: InterruptedException => ()

      // Other exceptions are unexpected, so we allow them to propagate.
    }
  }
}
