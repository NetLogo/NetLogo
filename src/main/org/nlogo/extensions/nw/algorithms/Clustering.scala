package org.nlogo.extensions.nw.algorithms

import org.nlogo.extensions.nw.Graph
import org.nlogo.extensions.nw.algorithms.Louvain.CommunityStructure.Community

import scala.util.Random

object ClusteringMetrics {
  def clusteringCoefficient[V,E](graph: Graph[V,E], node: V): Double = {
    val neighbors = graph.outNeighbors(node)
    val neighborSet = neighbors.toSet
    if (neighbors.size < 2) {
      0
    } else {
      val neighborLinkCounts = neighbors map {
        (t: V) => graph.outNeighbors(t).count(neighborSet.contains)
      }

      neighborLinkCounts.sum.toDouble / (neighbors.size * (neighbors.size - 1))
    }
  }

  def modularity[V,E](graph: Graph[V, E], communities: Iterable[Set[V]]): Double = {
    communities.map(c => communityModularity(graph, c)).sum
  }

  /**
   * A single community's contribution to modularity. A `totalArcWeight` of 0 leaves this undefined as
   * 0/0, so it contributes 0 instead. That happens for a network with no links, but also for a weighted
   * network whose link weights sum to 0, so the total is what we check rather than the link count.
   */
  private[algorithms] def modularityContribution(
    internal: Double, totalIn: Double, totalOut: Double, totalArcWeight: Double): Double =
    if (totalArcWeight == 0) 0
    else (internal - totalIn * totalOut / totalArcWeight) / totalArcWeight

  def communityModularity[V,E](graph: Graph[V,E], community: Set[V]): Double = {
    var totalIn: Double = 0
    var totalOut: Double = 0
    var internal: Double = 0
    community.foreach { node =>
      graph.outEdges(node).foreach { edge =>
        val weight = graph.weight(edge)
        if (community contains graph.otherEnd(node)(edge)) internal += weight
        totalOut += weight
      }
      graph.inEdges(node).foreach { edge => totalIn += graph.weight(edge) }
    }
    modularityContribution(internal, totalIn, totalOut, graph.totalArcWeight)
  }

}

object Louvain {
  def cluster[V,E](graph: Graph[V,E], rng: Random): Seq[Seq[V]] = {
    val initComms = clusterLocally(graph, rng)
    if (initComms.communities.size < graph.nodes.size) {
      val mGraph = MergedGraph(graph, initComms)
      val metaComms: Seq[Seq[Community[V]]] = cluster(mGraph, rng)
      metaComms.map(_.flatMap(initComms.members))
    } else {
      initComms.communities.map(initComms.members)
    }
  }


  def clusterLocally[V,E](graph: Graph[V,E], rng: Random): CommunityStructure[V,E] = {
    var comStruct = CommunityStructure(graph)

    var switchOccurred = true
    // Note that GraphContext.nodes is a SortedSet, so we need to convert to a Seq to get a deterministic shuffle order.
    val nodesSeq = graph.nodes.toSeq
    while (switchOccurred) {
      switchOccurred  = false
      rng.shuffle(nodesSeq).foreach { v =>
        // Note that the original community is almost certainly in the connected communities, so we remove it
        val originalCommunity: Community[V] = comStruct.community(v)
        val connectedCommunities: Seq[Community[V]] = rng.shuffle(
          graph.outNeighbors(v).map(comStruct.community).filterNot(_ == originalCommunity).distinct
        )
        if (connectedCommunities.nonEmpty) {
          val newComStruct = connectedCommunities.map(comStruct.move(v, _)).maxBy(_.modularity)
          // Require a strictly better score to switch.
          if (newComStruct.modularity > comStruct.modularity) {
            comStruct = newComStruct
            switchOccurred = true
          }
        }
      }
    }
    comStruct
  }

  object CommunityStructure {
    type Community[V] = Int

    def apply[V,E](graph: Graph[V,E]): CommunityStructure[V,E] = {
      val comMap: Map[V, Community[V]] = graph.nodes.zipWithIndex.toMap
      val internal = Array.fill(graph.nodes.size)(0.0)
      val totalIn = Array.fill(graph.nodes.size)(0.0)
      val totalOut = Array.fill(graph.nodes.size)(0.0)
      graph.nodes.foreach { node =>
        val com = comMap(node)
        graph.outEdges(node).foreach { edge =>
          val weight = graph.weight(edge)
          val other = graph.otherEnd(node)(edge)
          if (com == comMap(other)) internal(com) += weight
          totalOut(com) += weight
          totalIn(comMap(other)) += weight
        }
      }
      val mod = internal.lazyZip(totalIn).lazyZip(totalOut).map { case (intern: Double, in: Double, out: Double) =>
        ClusteringMetrics.modularityContribution(intern, in, out, graph.totalArcWeight)
      }.sum
      new CommunityStructure[V,E](graph, comMap, internal.toVector, totalIn.toVector, totalOut.toVector, mod)
    }

    // Very slow; only used for testing. Could speed it up by calculating the initial values for each of the vectors
    // but am too lazy for test code.
    def apply[V,E](graph: Graph[V,E], groups: Seq[Seq[V]]): CommunityStructure[V,E] =
      groups.zipWithIndex.flatMap { case (vs, i) => vs.map(_ -> i) }.foldLeft(CommunityStructure(graph)) {
        (cs: CommunityStructure[V,E], pair: (V, Community[V])) => cs.move(pair._1, pair._2)
      }
  }

  class CommunityStructure[V,E](graph: Graph[V,E],
                                comMap: Map[V, Community[V]],
                                internal: Vector[Double],
                                totalIn: Vector[Double],
                                totalOut: Vector[Double],
                                val modularity: Double) {

    def community(node: V): Community[V] = comMap(node)

    def move(node: V, newCommunity: Community[V]): CommunityStructure[V,E] = {
      val originalCommunity = community(node)
      val otherEnd = graph.otherEnd(node)
      var inDegree = 0.0
      var outDegree = 0.0
      var internalOriginal = 0.0
      var internalNew = 0.0
      graph.outEdges(node).foreach { link =>
        val other = otherEnd(link)
        val comOther = community(other)
        val weight = graph.weight(link)
        outDegree += weight
        if (other == node) {
          internalOriginal += weight
          internalNew += weight
        } else if (comOther == originalCommunity) {
          internalOriginal += weight
        } else if (comOther == newCommunity) {
          internalNew += weight
        }
      }
      graph.inEdges(node).foreach { link =>
        val other = otherEnd(link)
        val comOther = community(other)
        val weight = graph.weight(link)
        inDegree += weight
        if (other == node) {
          internalOriginal += weight
          internalNew += weight
        } else if (comOther == originalCommunity) {
          internalOriginal += weight
        } else if (comOther == newCommunity) {
          internalNew += weight
        }
      }

      val newTotalIn = totalIn
        .updated(originalCommunity, totalIn(originalCommunity) - inDegree)
        .updated(newCommunity, totalIn(newCommunity) + inDegree)
      val newTotalOut = totalOut
        .updated(originalCommunity, totalOut(originalCommunity) - outDegree)
        .updated(newCommunity, totalOut(newCommunity) + outDegree)
      val newInternal = internal
        .updated(originalCommunity, internal(originalCommunity) - internalOriginal)
        .updated(newCommunity, internal(newCommunity) + internalNew)
      val contrib = (com: Int, intern: Vector[Double], in: Vector[Double], out: Vector[Double]) =>
        ClusteringMetrics.modularityContribution(intern(com), in(com), out(com), graph.totalArcWeight)

      val deltaOriginal =
        contrib(originalCommunity, newInternal, newTotalIn, newTotalOut) -
        contrib(originalCommunity, internal, totalIn, totalOut)
      val deltaNew =
        contrib(newCommunity, newInternal, newTotalIn, newTotalOut) -
        contrib(newCommunity, internal, totalIn, totalOut)
      new CommunityStructure[V,E](graph,
        comMap.updated(node, newCommunity),
        newInternal, newTotalIn, newTotalOut, modularity + deltaOriginal + deltaNew)
    }

    lazy val communities: Seq[Community[V]] = comMap.values.toSeq.distinct.sorted

    private lazy val _members: Map[Community[V], Seq[V]] = graph.nodes.toSeq.groupBy(comMap)
    def members(community: Community[V]): Seq[V] = _members(community)

  }

  case class WeightedLink[V](end1:V, end2: V, weight: Double)

  case class MergedGraph[V,E](graph: Graph[V, E], communityStructure: CommunityStructure[V,E])
    extends Graph[Community[V], WeightedLink[Community[V]]] {

    override val nodes: Seq[Community[V]] = communityStructure.communities

    val edges: Seq[WeightedLink[Community[V]]] = communityStructure.communities.flatMap { (sourceCom: Community[V]) =>
      communityStructure.members(sourceCom).flatMap { source =>
        graph.outEdges(source).map { e =>
          communityStructure.community(graph.otherEnd(source)(e)) -> graph.weight(e)
        }
      }.groupBy(_._1).map { case (targetCom, ws) =>
        WeightedLink(sourceCom, targetCom, ws.map(_._2).sum)
      }
    }

    val outEdgeMap: Map[Community[V], Seq[WeightedLink[Community[V]]]] =
      edges.groupBy(_.end1).withDefaultValue(Seq.empty[WeightedLink[Community[V]]])
    val inEdgeMap: Map[Community[V], Seq[WeightedLink[Community[V]]]] =
      edges.groupBy(_.end2).withDefaultValue(Seq.empty[WeightedLink[Community[V]]])

    override def inEdges(node: Community[V]): Seq[WeightedLink[Community[V]]] = inEdgeMap(node)
    override def outEdges(node: Community[V]): Seq[WeightedLink[Community[V]]] = outEdgeMap(node)
    override def weight(link: WeightedLink[Community[V]]): Double = link.weight

    override def ends(link: WeightedLink[Community[V]]): (Community[V], Community[V]) = link.end1 -> link.end2
  }
}

