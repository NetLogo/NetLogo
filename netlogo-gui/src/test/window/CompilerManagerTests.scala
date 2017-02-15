// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.scalatest.FunSuite
import org.nlogo.core.Femto
import org.nlogo.api.{ JobOwner, MersenneTwisterFast, NetLogoLegacyDialect }
import org.nlogo.agent.World
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.workspace.{ AbstractWorkspace, DummyAbstractWorkspace }
import org.nlogo.window.Events.{ CompiledEvent, CompileMoreSourceEvent, InterfaceGlobalEvent, LoadBeginEvent, LoadEndEvent }

class CompilerManagerTests extends FunSuite {
  def loadWidgets(ws: Seq[JobOwner],
    source: String = "globals [ a b c ] to foo fd 1 end")(
      cm: CompilerManager): CompilerManager = {
    cm.proceduresInterface.innerSource = source
    cm.handle(new LoadBeginEvent())
    ws.foreach { w =>
      cm.handle(new CompileMoreSourceEvent(w))
    }
    cm.handle(new LoadEndEvent())
    cm
  }

  def newWorkspace = new DummyAbstractWorkspace {
    override def aggregateManager = null
    override def compiler = Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler", NetLogoLegacyDialect)
  }

  def testCompilerManager(run: (CompilerManager) => Unit)(
      assertions: (AbstractWorkspace, CompilerManager, Seq[Event]) => Unit): Unit = {
        val procedures = new DummyProcedures()
        val workspace = newWorkspace
        var events: Seq[Event] = Seq()
        val compilerManager = new CompilerManager(workspace, procedures,
          ((e, o) => (events = events :+ e)))
        run(compilerManager)
        assertions(workspace, compilerManager, events)
      }

  trait Helper {
    var widgets: Seq[JobOwner] = Seq.empty[JobOwner]
    var source: String = "globals [ a b c ] to foo fd 1 end"
    var events = Seq.empty[Event]
    lazy val workspace = newWorkspace
    lazy val procedures = new DummyProcedures()
    lazy val compilerManager = new CompilerManager(workspace, procedures, ((e, o) => events = events :+ e))
    def loadWidgets(): Unit = {
      compilerManager.proceduresInterface.innerSource = source
      compilerManager.handle(new LoadBeginEvent())
      compilerManager.handle(new LoadBeginEvent())
      widgets.foreach { w =>
        compilerManager.handle(new CompileMoreSourceEvent(w))
      }
      compilerManager.handle(new LoadEndEvent())
    }
  }

  test("compiler manager set its own widgets and world program") { new Helper {
    source = ""
    loadWidgets()
    assert(compilerManager.widgets.isEmpty)
    assert(workspace.world.program != null)
  } }

  test("given no widgets, the compiler manager emits one CompiledEvent for empty widgets, one CompiledEvent for code tab") { new Helper {
    loadWidgets()
    assert(workspace.world.program.userGlobals == Seq("A", "B", "C"))
    assert(workspace.procedures.get("FOO").nonEmpty)
    assert(workspace.procedures.apply("FOO").owner == compilerManager.proceduresInterface)
    assert(events.length == 3)
    assert(events(0).isInstanceOf[Events.RemoveAllJobsEvent])
    assert(events(1).isInstanceOf[Events.CompiledEvent])
    assert(events(2).isInstanceOf[Events.CompiledEvent])
  } }

  test("compiler manager emits a CompiledEvent for each widget with source and one for the code tab") { new Helper {
    widgets = Seq(new DummyWidget("show 5"))
    loadWidgets()
    assert(compilerManager.widgets.size == 1)
    assert(events.length == 3)
    assert(events(0).isInstanceOf[Events.RemoveAllJobsEvent])
    assert(events(1).isInstanceOf[Events.CompiledEvent])
    assert(events(2).isInstanceOf[Events.CompiledEvent])
  } }

  test("compiler manager handles CompileMoreSource after loading ends by recompiling widgets") { new Helper {
    val widget = new DummyWidget("set a 5")
    widgets = Seq(widget)
    loadWidgets()
    widget.sourceCode = "set a 10"
    compilerManager.handle(new CompileMoreSourceEvent(widget))
    assert(events.length == 4)
    assert(events(3).isInstanceOf[Events.CompiledEvent])
  } }

  test("compiler manager clears the world at the end of loading") { new Helper {
    workspace.world.createTurtle(workspace.world.turtles)
    loadWidgets()
    assert(workspace.world.turtles.isEmpty)
  } }

  test("compiler widget compiles the command center source and emits a compiled event") { new Helper {
    loadWidgets()
    val ccWidget = new DummyWidget("", true)
    compilerManager.handle(new CompileMoreSourceEvent(ccWidget))
    assert(events.length == 4)
    assert(events(3).isInstanceOf[Events.CompiledEvent])
  } }

  test("handles interface global event where widget name changed by compiling everything") { new Helper {
    val widget = new DummyIGWidget("bar")
    widgets = Seq(widget)
    loadWidgets()
    val nameChanged = new InterfaceGlobalEvent(widget, true, false, false, false)
    compilerManager.handle(nameChanged)
    assert(events.length == 6)
    assert(events(3).isInstanceOf[Events.RemoveAllJobsEvent])
    assert(events(4).isInstanceOf[Events.CompiledEvent])
    assert(events(5).isInstanceOf[Events.CompiledEvent])
  } }

  test("sets an updating interface global widget to the value of the same-named global") { new Helper {
    val widget = new DummyIGWidget("")
    widgets = Seq(widget)
    source = "globals [ig]"
    loadWidgets()
    workspace.world.setObserverVariableByName("ig", Double.box(10))
    val updateIGValue = new InterfaceGlobalEvent(widget, false, true, false, false)
    compilerManager.handle(updateIGValue)
    assert(widget.value == Double.box(10))
  } }

  test("handles an interface global event where the value has changed by setting the global to the value of the widget") { new Helper {
    val widget = new DummyIGWidget("", Double.box(10))
    widgets = Seq(widget)
    source = "globals [ig]"
    loadWidgets()
    val igValueChange = new InterfaceGlobalEvent(widget, false, false, false, false)
    compilerManager.handle(igValueChange)
    assert(workspace.world.getObserverVariableByName("IG") == Double.box(10))
  } }

  test("adds all loaded widgets to its widget set") { new Helper {
    val igWidget = new DummyIGWidget("", Double.box(10))
    val jobWidget = new DummyJobWidget("", null)
    widgets = Seq(igWidget, jobWidget)
    loadWidgets()
    assert(compilerManager.widgets.contains(jobWidget))
    assert(compilerManager.widgets.contains(igWidget))
  } }

  test("adds interface global widgets to it's globalWidget set") { new Helper {
    val igWidget = new DummyIGWidget("", Double.box(10))
    widgets = Seq(igWidget)
    loadWidgets()
    compilerManager.handle(new InterfaceGlobalEvent(igWidget, false, false, false, false))
    assert(compilerManager.globalWidgets.contains(igWidget))
  } }

  test("compiler manager clears the old program completely when loading begins") { new Helper {
    source = "breed [ as a ]"
    loadWidgets()
    source = "breed [ bs b ]"
    loadWidgets()
    assert(workspace.world.program.breeds.get("BS").isDefined)
    assert(workspace.world.getBreed("BS") != null)
    assert(workspace.world.getBreed("BS").isInstanceOf[org.nlogo.api.AgentSet])
    assert(workspace.world.program.breeds.get("AS").isEmpty)
    assert(workspace.world.getBreed("AS") == null)
  } }

  test("compiler manager does not emit an event for a widget when recompiling all source if that widget has no source") { new Helper {
    val widget = new DummyWidget("")
    widgets = Seq(widget)
    loadWidgets()
    val compiledEvents = events.collect {
      case e: CompiledEvent => e
    }
    assert(compiledEvents.forall(_.sourceOwner ne widget))
  } }
}

class DummyProcedures extends ProceduresInterface {
  var codeTabSource: String = ""
  def classDisplayName: String = ???
  def headerSource: String = ???
  def innerSource_=(s: String): Unit = { codeTabSource = s }
  def innerSource: String = codeTabSource
  def kind: org.nlogo.core.AgentKind = org.nlogo.core.AgentKind.Observer
  def source: String = codeTabSource
}

class DummyWidget(var sourceCode: String, val isCommandCenter: Boolean = false) extends JobOwner {
  def displayName: String = "test job owner"
  def isButton: Boolean = ???
  def isLinkForeverButton: Boolean = ???
  def isTurtleForeverButton: Boolean = ???
  def ownsPrimaryJobs: Boolean = ???
  def random: MersenneTwisterFast = ???

  // Members declared in org.nlogo.api.SourceOwner
  def classDisplayName: String = "test job owner"
  def headerSource: String = ???
  def innerSource_=(s: String): Unit = ???
  def innerSource: String = ???
  def kind: org.nlogo.core.AgentKind = ???
  def source: String = sourceCode
}

class DummyJobWidget(source: String, rand: MersenneTwisterFast) extends JobWidget(rand) {
  type WidgetModel = org.nlogo.core.Button
  def load(widget: WidgetModel): AnyRef = {
    null
  }
  def model: org.nlogo.window.DummyJobWidget#WidgetModel = {
    null
  }
}

class DummyIGWidget(source: String, var value: AnyRef = Double.box(0)) extends DummyWidget(source) with InterfaceGlobalWidget {
  def name: String = "ig"
  def updateConstraints(): Unit = { }
  def valueObject(x: AnyRef): Unit = x match {
    case a: AnyRef => value = a
  }
  def valueObject: AnyRef = value
}
