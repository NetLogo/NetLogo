// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.nio.file.{ FileSystems, Path, WatchKey, WatchService }
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY

import scala.collection.JavaConverters._

private class FileWatcherThread(paths: List[Path], callback: () => Boolean) extends Thread {
  private val watchService: WatchService = FileSystems.getDefault.newWatchService
  private val parentSet: Set[Path] = paths.map(_.getParent).toSet

  private def f(x: Path): (WatchKey, Path) = x.register(watchService, ENTRY_MODIFY) -> x
  private val keyPathMap: Map[WatchKey, Path] = parentSet.map(f).toMap

  override def run() {
    try {
      var done: Boolean = false

      while (!done && !isInterrupted()) {
        val key: WatchKey = watchService.take

        for (event <- key.pollEvents.asScala;
             dirPath <- keyPathMap.get(key)) {
          val eventPath: Path = event.context.asInstanceOf[Path]
          val fullEventPath: Path = dirPath.resolve(eventPath)

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
