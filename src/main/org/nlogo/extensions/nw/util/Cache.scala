package org.nlogo.extensions.nw.util

import scala.collection.mutable
import scala.ref.WeakReference
import org.nlogo.agent.World.VariableWatcher
import org.nlogo.agent.{World, Agent}

class Cache[A,B](default: A=>B) extends (A=>B) {
  val cachedValues = mutable.Map.empty[A,B]
  def apply(key: A): B = cachedValues.getOrElseUpdate(key, default(key))
  def get(key: A): Option[B] = cachedValues.get(key)
  def update(key: A, value: B) = cachedValues(key) = value
}

class CacheManager[A,B](world: World, default: (Option[String])=>A=>B) {
  val caches = mutable.Map.empty[Option[String], Cache[A,B]]
  def apply(variable: Option[String] = None): Cache[A,B] = caches.getOrElseUpdate(variable, {
    variable map { varName =>
      world.addWatcher(varName, new CacheClearingWatcher(new WeakReference[mutable.Map[Option[String], ?]](caches)))
    }
    new Cache[A,B](default(variable))
  })
}

object CacheManager {
  def apply[A,B](world: World): CacheManager[A,B] =
    CacheManager(world, (v: Option[String]) => (x: A) => throw new java.util.NoSuchElementException("key not found: " + x.toString))
  def apply[A,B](world: World, default: (Option[String])=>(A)=>B): CacheManager[A,B] =
    new CacheManager[A,B](world, default)
}

/*
The reference to the caches must be weak since the watcher may outlive the graph context (e.g. a clear-all will cause
this). - BCH 5/2/2014
 */
private class CacheClearingWatcher(caches: WeakReference[mutable.Map[Option[String], ?]]) extends VariableWatcher {
  def update(agent: Agent, variableName: String, value: AnyRef) = {
    caches.get.foreach { _.remove(Some(variableName)) }
    agent.world.deleteWatcher(variableName, this)
  }
}
