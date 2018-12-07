package org.nlogo.fileformat

import org.nlogo.core.{ Model }
import org.nlogo.core.{ Pen, Plot, View }
import org.scalatest.{ FunSuite, DiagrammedAssertions }

class PlotConverterTests extends FunSuite with ConversionHelper with DiagrammedAssertions {

  /*************
   * Plot Name *
   *************/

  test("if the plot names are not initialized"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = None, pens = List(Pen(display = "dep", setupCode = "plot e 10")))
                                   , Plot(display = None, pens = List(Pen(display = "DeP", setupCode = "plot e 10")))
                                   , Plot(display = None, pens = List(Pen(display = "Dep", setupCode = "plot e 10")))))

    val result = PlotConverter.allPlotNames(model)
    assertResult(List())(result)
  }

  test("if the plot names are initialized"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "dep", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DeP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Test"), pens = List(Pen(display = "Dep", setupCode = "plot e 10")))))

    val result = PlotConverter.allPlotNames(model)
    assertResult(List("MegaLazer", "Teser", "Test"))(result)
  }

   /*********************
   * All Key Pens Names *
   **********************/

  test("if the plot pen names are renamed from a list with no similarities"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "dep", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DeP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Test"), pens = List(Pen(display = "Dep", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq())(result)
  }

  test("if the plot pen names are similar and require rename of a similar type"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "dep", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DeP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Test"), pens = List(Pen(display = "Dep", setupCode = "plot e 10"),
                                                                              Pen(display = "DEP", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq(("Test",Seq(("Dep","Dep"), ("DEP","DEP_1")))))(result)
  }

  test("if the global plot pen names are renamed from a list with similarities and a duplicate pen name"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DeP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Test"), pens = List(Pen(display = "Dep", setupCode = "plot e 10"),
                                                                              Pen(display = "DEP", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq(("Test", Seq(("Dep","Dep"), ("DEP","DEP_1")))))(result)
  }

  test("if all plots have duplicate pen name"){
    val model = Model(widgets = Seq( View()
                  , Plot(display = Some("Tesser"), pens = List(Pen(display = "DEP", setupCode = "plot e 10"),
                                                               Pen(display = "DEp", setupCode = "plot e 10")))
                  , Plot(display = Some("Teser"), pens = List(Pen(display = "DEP", setupCode = "plot e 10"),
                                                               Pen(display = "DEp", setupCode = "plot e 10")))
                  , Plot(display = Some("Test"), pens = List(Pen(display = "DEP", setupCode = "plot e 10"),
                                                             Pen(display = "DEp", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(List(("Tesser", Seq(("DEp","DEp"), ("DEP","DEP_1"))), ("Teser", Seq(("DEp","DEp"), ("DEP","DEP_1"))), ("Test", Seq(("DEp","DEp"), ("DEP","DEP_1")))))(result)
  }

  test("if all plots have duplicate pen name with one field"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Test"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq())(result)
  }

  test("if only one plot doesn't have duplicate (same capitalization) pen name"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DeP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Test"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq())(result)
  }

  test("if there are duplicate capitalization with names, but in different plots, the converter should be empty"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DeP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Test"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq())(result)
  }

  test("if only two plots contain the same names and necessary renames"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Teser"), pens = List(Pen(display = "DEP", setupCode = "plot e 10"), Pen(display = "DeP", setupCode = "plot e 10")))
                                   , Plot(display = Some("Tesuer"), pens = List(Pen(display = "DEP", setupCode = "plot e 10"), Pen(display = "DEp", setupCode = "plot e 10")))
                                   , Plot(display = Some("Test"), pens = List(Pen(display = "DEP", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq(("Teser", Seq(("DeP", "DeP"), ("DEP", "DEP_1")))
                    ,("Tesuer", Seq(("DEp", "DEp"), ("DEP", "DEP_1")))))(result)
  }

  test("Should rename SPORKS to SPORKS_1 in the first plot and SPORKS in the other plot should not be renamed"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "SPORKS", setupCode = "plot e 10"), Pen(display = "sPORKS", setupCode = "plot e 10")))
                                   , Plot(display = Some("Tseser"), pens = List(Pen(display = "SPORKS", setupCode = "plot e 10"), Pen(display = "SPORKS_1", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq(("MegaLazer", Seq(("sPORKS", "sPORKS"), ("SPORKS", "SPORKS_1")))))(result)
  }

  test("Should rename SPORKS to SPORKS_1 in the first plot and SPORKS in the other plot should be renamed to SPORKS_3 thanks to possible duplicates"){
    val model = Model(widgets = Seq( View()
                                   , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "SPORKS", setupCode = "plot e 10"), Pen(display = "sPORKS", setupCode = "plot e 10")))
      , Plot(display = Some("Tseser"), pens = List( Pen(display = "sPORKS", setupCode = "plot e 10")
                                                  , Pen(display = "SPORKS", setupCode = "plot e 10")
                                                  , Pen(display = "SPORKS_2", setupCode = "plot e 10"), Pen(display = "SPORKS_1", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq(("MegaLazer", Seq(("sPORKS", "sPORKS"), ("SPORKS", "SPORKS_1")))
                    ,("Tseser",    Seq(("sPORKS", "sPORKS"), ("SPORKS", "SPORKS_3"))) ))(result)
  }

  test("Should rename SPORKS to SPORKS_2 in the first plot and SPORKS in the other plot should be renamed to SPORKS_4 thanks to possible duplicates"){
    val model = Model(widgets = Seq( View()
      , Plot(display = Some("MegaLazer"), pens = List(Pen(display = "SPORKS", setupCode = "plot e 10")
                                                     , Pen(display = "sPORKS_1", setupCode = "plot e 10")
                                                     , Pen(display = "sPORKS", setupCode = "plot e 10")))
      , Plot(display = Some("Tseser"), pens = List( Pen(display = "sPORKS", setupCode = "plot e 10")
                                                  , Pen(display = "SPORKS", setupCode = "plot e 10")
                                                  , Pen(display = "SPORKS_2", setupCode = "plot e 10")
                                                  , Pen(display = "SPORKS_3", setupCode = "plot e 10")
                                                  , Pen(display = "SPORKS_5", setupCode = "plot e 10")
                                                  , Pen(display = "SPORKS_1", setupCode = "plot e 10")))))

    val result = PlotConverter.determineMapRenames(PlotConverter.allKeyedPenNames(model))
    assertResult(Seq(("MegaLazer", Seq(("sPORKS", "sPORKS"), ("SPORKS", "SPORKS_2")))
                    ,("Tseser",    Seq(("sPORKS", "sPORKS"), ("SPORKS", "SPORKS_4"))) ))(result)
  }
}
