package org.nlogo.extensions.nw

trait Graph[V, E] {

  def nodes: Iterable[V]
  def links: Iterable[E] = nodes.flatMap(outEdges).toSet

  /**
   * Returns the number of directed arcs, where an undirected link count as
   * two arcs. Thus, this will be equal to `links.size` for directed networks
   * and `links.size * 2` for undirected networks. For mixed networks, this
   * will be somewhere in between.
   * In other words, this is the sum of the elements of the adjacency matrix
   * of the network.
   */
  lazy val arcCount: Int = nodes.view.map(outEdges(_).size).sum
  lazy val totalArcWeight: Double = nodes.view.flatMap(outEdges).map(weight).sum

  def otherEnd(node: V)(link: E): V = {
    val (end1, end2) = ends(link)
    if (end2 == node) end1 else end2
  }

  def ends(link: E): (V, V)

  /**
   * Should return both incoming directed and undirected edges.
   */
  def inEdges(node: V): Seq[E] = nodes.view.flatMap(v => outEdges(v) filter (otherEnd(v)(_) == node)).toSeq
  /**
   * Should return both incoming directed and undirected neighbors.
   */
  def inNeighbors(node: V): Seq[V] = inEdges(node) map otherEnd(node)

  /**
   * Should return both outgoing directed and undirected edges.
   */
  def outEdges(node: V): Seq[E]
  /**
   * Should return both outgoing directed and undirected neighbors.
   */
  def outNeighbors(node: V): Seq[V] = outEdges(node) map otherEnd(node)

  def allEdges(node: V): Seq[E] = (inEdges(node) ++ outEdges(node)).distinct
  def allNeighbors(node: V): Seq[V] = allEdges(node) map otherEnd(node)

  def weight(link: E): Double = 1.0
}

case class MixedMultiGraph[V](override val links: Seq[(V, V, Boolean)]) extends Graph[V, (V,V,Boolean)] {

  override val nodes: Seq[V] = (links.map(_._1) ++ links.map(_._2)).distinct
  val undirLinks: Map[V, Seq[(V,V,Boolean)]] = {
    val undirLinks = links.filterNot(_._3)
    val outUndirLinks = undirLinks.groupBy(_._1)
    val inUndirLinks = undirLinks.groupBy(_._2)
    nodes.map { node => node ->
      (outUndirLinks.getOrElse(node, Seq.empty[(V,V,Boolean)])
        ++ inUndirLinks.getOrElse(node, Seq.empty[(V,V,Boolean)]))
    }.toMap
  }
  val outLinks: Map[V, Seq[(V,V,Boolean)]] =
    links.filter(_._3).groupBy(_._1) withDefaultValue Seq.empty[(V,V,Boolean)]
  lazy val inLinks: Map[V, Seq[(V,V,Boolean)]] =
    links.filter(_._3).groupBy(_._2) withDefaultValue Seq.empty[(V,V,Boolean)]

  override def outEdges(node: V): Seq[(V,V,Boolean)] = undirLinks(node) ++ outLinks(node)
  override def inEdges(node: V): Seq[(V,V,Boolean)] = undirLinks(node) ++ outLinks(node)

  override def ends(link: (V,V,Boolean)) = (link._1, link._2)
}
