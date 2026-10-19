package org.nlogo.extensions.nw

import org.nlogo.extensions.nw.algorithms.Louvain.CommunityStructure.Community
import org.nlogo.extensions.nw.algorithms.Louvain.CommunityStructure
import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.extensions.nw.algorithms.Louvain

class ClusteringTestSuite extends AnyFunSuite {
  test("merged graphs count weights correctly for mixed multi-graph with self-links") {
    val graph = MixedMultiGraph(Seq(
      (0, 1, false),

      (0, 2, true), // community link 0 -> 1

      (2, 3, false),
      (2, 3, true),

      (2, 4, false), // community link 1 <-> 2

      (4, 5, false),
      (4, 5, false),

      (5, 6, true), // community link 2 -> 3
      (5, 6, true),

      (6, 6, false),

      (6, 7, true), // community link 3 -> 4
      (6, 7, false),

      (7, 7, true)
    ))

    val cs = Seq(Seq(0,1), Seq(2,3), Seq(4,5), Seq(6), Seq(7))
    val comStruct = CommunityStructure(graph, cs)

    val mGraph = Louvain.MergedGraph(graph, comStruct)


    def weight(v1: Community[Int], v2: Community[Int]) = mGraph.links.filter {
      l => l.end1 == v1 && l.end2 == v2
    }.head.weight

    assert(mGraph.links.size === 11)

    // self-links
    assert(weight(0, 0) === 2)
    assert(weight(1, 1) === 3)
    assert(weight(2, 2) === 4)
    assert(weight(3, 3) === 2)
    assert(weight(4, 4) === 1)

    // between-community links
    assert(weight(0, 1) === 1)
    assert(weight(1, 2) === 1)
    assert(weight(2, 1) === 1)
    assert(weight(2, 3) === 2)
    assert(weight(3, 4) === 2)
    assert(weight(4, 3) === 1)
  }
}
