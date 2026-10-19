// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw

import org.nlogo.extensions.nw.prim.{SaveFileType, LoadFileType}
import org.nlogo.extensions.nw.prim.jung.{SaveGraphML, LoadGraphML}

import org.nlogo.api

class NetworkExtension extends api.DefaultClassManager with GraphContextManager {

  val version = "1.0.0"

  def checkNetLogoVersion(): Unit = {
    try {
      Class.forName("org.nlogo.api.SimpleChangeEventPublisher")
    } catch {
      case e: ClassNotFoundException => throw new api.ExtensionException(
        "Version " + version + " of the NW extension requires NetLogo version 5.0.5 or greater.", e)
    }
  }

  override def clearAll(): Unit = { clearContext() }
  override def unload(em: api.ExtensionManager): Unit = {
    clearAll()
  }

  override def load(primManager: api.PrimitiveManager): Unit = {

    checkNetLogoVersion()

    val add = primManager.addPrimitive

    add("set-context", new prim.SetContext(this))
    add("get-context", new prim.GetContext(this))
    add("with-context", new prim.WithContext(this))

    add("turtles-in-radius", new org.nlogo.extensions.nw.prim.TurtlesInRadius(this))
    add("turtles-in-reverse-radius", new org.nlogo.extensions.nw.prim.TurtlesInReverseRadius(this))

    add("mean-path-length", new prim.MeanPathLength(this))
    add("mean-weighted-path-length", new prim.MeanWeightedPathLength(this))

    add("distance-to", new prim.DistanceTo(this))
    add("weighted-distance-to", new prim.WeightedDistanceTo(this))
    add("path-to", new prim.PathTo(this))
    add("weighted-path-to", new prim.WeightedPathTo(this))
    add("turtles-on-path-to", new prim.TurtlesOnPathTo(this))
    add("turtles-on-weighted-path-to", new prim.TurtlesOnWeightedPathTo(this))

    add("betweenness-centrality", new prim.jung.BetweennessCentrality(this))
    add("eigenvector-centrality", new prim.EigenvectorCentrality(this))
    add("page-rank", new prim.jung.PageRank(this))
    add("closeness-centrality", new prim.jung.ClosenessCentrality(this))

    add("weighted-closeness-centrality", new prim.jung.WeightedClosenessCentrality(this))
    /*
    There are some major oddities with Jung's weighted betweenness centrality. For example, in the network 0--1--2--3--0,
    with 3--0 having weight 10, it gives [0 1.5 1.25 0]. I don't understand what betweenness centrality > 1 is or
    how it could be asymmetric. So for now, I'm going to leave the plumbing in place, but not expose the functionality
    till we understand it. -- BCH 5/14/2014
     */
    //add("weighted-betweenness-centrality", new prim.jung.WeightedBetweennessCentrality(this))

    add("clustering-coefficient", new prim.ClusteringCoefficient(this))
    add("modularity", new prim.Modularity(this))

    add("louvain-communities", new prim.LouvainCommunities(this))

    add("bicomponent-clusters", new prim.jung.BicomponentClusters(this))
    add("weak-component-clusters", new prim.jung.WeakComponentClusters(this))

    add("maximal-cliques", new prim.jgrapht.MaximalCliques(this))
    add("biggest-maximal-cliques", new prim.jgrapht.BiggestMaximalCliques(this))

    add("generate-preferential-attachment", new prim.BarabasiAlbertGenerator)
    add("generate-random", new prim.ErdosRenyiGenerator)
    add("generate-small-world", new prim.jung.KleinbergSmallWorldGenerator)
    add("generate-watts-strogatz", new prim.WattsStrogatzGenerator)
    add("generate-lattice-2d", new prim.jung.Lattice2DGenerator)

    add("generate-ring", new prim.jgrapht.RingGenerator)
    add("generate-star", new prim.jgrapht.StarGenerator)
    add("generate-wheel", new prim.jgrapht.WheelGenerator)
    add("generate-wheel-inward", new prim.jgrapht.WheelGeneratorInward)
    add("generate-wheel-outward", new prim.jgrapht.WheelGeneratorOutward)

    add("save-matrix", new prim.jung.SaveMatrix(this))
    add("load-matrix", new prim.jung.LoadMatrix)

    //add("save-graphml", new SaveFileType(this, ".graphml"))
    add("save-graphml", new SaveGraphML(this))
    //add("load-graphml", new LoadFileTypeDefaultBreeds(".graphml"))
    add("load-graphml", new LoadGraphML())

    add("load", new prim.Load())
    add("load-from-string", new prim.LoadFromString())
    add("load-dl", new LoadFileType(".dl"))
    add("load-gdf", new LoadFileType(".gdf"))
    add("load-gexf", new LoadFileType(".gexf"))
    add("load-gml", new LoadFileType(".gml"))
    add("load-vna", new LoadFileType(".vna"))

    add("save", new prim.Save(this))
    add("save-to-string", new prim.SaveToString(this))
    add("save-dl", new SaveFileType(this, ".dl"))
    add("save-gdf", new SaveFileType(this, ".gdf"))
    add("save-gexf", new SaveFileType(this, ".gexf"))
    add("save-gml", new SaveFileType(this, ".gml"))
    add("save-vna", new SaveFileType(this, ".vna"))
  }
}
