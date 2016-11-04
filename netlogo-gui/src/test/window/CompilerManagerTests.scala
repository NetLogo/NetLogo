// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.scalatest.FunSuite
import org.nlogo.core.Femto
import org.nlogo.api.{ JobOwner, MersenneTwisterFast, NetLogoLegacyDialect }
import org.nlogo.agent.World
import org.nlogo.nvm.CompilerInterface
import org.nlogo.workspace.{ AbstractWorkspace, DummyAbstractWorkspace }
import org.nlogo.window.Events.{ CompileMoreSourceEvent, InterfaceGlobalEvent, LoadBeginEvent, LoadEndEvent }

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
    override def compiler = Femto.get[CompilerInterface]("org.nlogo.compile.Compiler", NetLogoLegacyDialect)
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

  // this test can probably get deleted
  test("compiler manager sets up with the LoadBeginEvent") {
    testCompilerManager(run = loadWidgets(Seq(), source = "")) {
      (workspace, compilerManager, events) =>
        assert(compilerManager.widgets.isEmpty)
        assert(workspace.world.program != null)
    }
  }

  test("given no widgets, the compiler manager emits one CompiledEvent for empty widgets, one CompiledEvent for code tab") {
    testCompilerManager(run = loadWidgets(Seq())) { (workspace, compilerManager, events) =>
        assert(workspace.world.program.userGlobals == Seq("A", "B", "C"))
        assert(workspace.procedures.get("FOO").nonEmpty)
        assert(workspace.procedures.apply("FOO").owner == compilerManager.proceduresInterface)
        assert(events.length == 3)
        assert(events(0).isInstanceOf[Events.RemoveAllJobsEvent])
        assert(events(1).isInstanceOf[Events.CompiledEvent])
        assert(events(2).isInstanceOf[Events.CompiledEvent])
      }
  }

  test("compiler manager emits a CompiledEvent for each widget and one for the code tab") {
    testCompilerManager(run = loadWidgets(Seq(new DummyWidget("")))) { (workspace, compilerManager, events) =>
        assert(compilerManager.widgets.size == 1)
        assert(events.length == 3)
        assert(events(0).isInstanceOf[Events.RemoveAllJobsEvent])
        assert(events(1).isInstanceOf[Events.CompiledEvent])
        assert(events(2).isInstanceOf[Events.CompiledEvent])
      }
  }

  test("compiler manager handles CompileMoreSource after loading ends by recompiling widgets") {
    val widget = new DummyWidget("")
    testCompilerManager(run = loadWidgets(Seq(widget)) _ andThen { cm =>
      widget.sourceCode = "set a 10"
      cm.handle(new CompileMoreSourceEvent(widget))
      }) { (workspace, compilerManager, events) =>
        assert(events.length == 4)
        assert(events(3).isInstanceOf[Events.CompiledEvent])
      }
  }

  test("compiler widget compiles the command center source and emits a compiled event") {
    val ccWidget = new DummyWidget("", true)
    testCompilerManager(run = loadWidgets(Seq()) _ andThen
    { cm => cm.handle(new CompileMoreSourceEvent(ccWidget)) }) { (workspace, compilerManager, events) =>
      assert(events.length == 4)
      assert(events(3).isInstanceOf[Events.CompiledEvent])
    }
  }

  test("handles interface global event where widget name changed by compiling everything") {
    val widget = new DummyIGWidget("")
    val nameChanged = new InterfaceGlobalEvent(widget, true, false, false, false)
    testCompilerManager(
      run = loadWidgets(Seq(widget)) _ andThen (_.handle(nameChanged))) {
        (workspace, compilerManager, events) =>
          assert(events.length == 6)
          assert(events(3).isInstanceOf[Events.RemoveAllJobsEvent])
          assert(events(4).isInstanceOf[Events.CompiledEvent])
          assert(events(5).isInstanceOf[Events.CompiledEvent])
      }
  }

  test("sets an updating interface global widget to the value of the same-named global") {
    val widget = new DummyIGWidget("")
    val updateIGValue = new InterfaceGlobalEvent(widget, false, true, false, false)
    testCompilerManager(
      run = loadWidgets(Seq(widget), source = "globals [ig]") _ andThen { cm =>
        cm.workspace.world.setObserverVariableByName("ig", Double.box(10))
        cm.handle(updateIGValue)
      }) {
        (workspace, compilerManager, events) =>
          assert(widget.value == Double.box(10))
      }
  }

  test("handles an interface global event where the value has changed by setting the global to the value of the widget") {
    val widget = new DummyIGWidget("", Double.box(10))
    val igValueChange = new InterfaceGlobalEvent(widget, false, false, false, false)
    testCompilerManager( run =
      loadWidgets(Seq(widget), source = "globals [ig]") _ andThen
      (_.handle(igValueChange))) {
        (workspace, compilerManager, events) =>
          assert(workspace.world.getObserverVariableByName("IG") == Double.box(10))
      }
  }

  test("adds all loaded widgets to it's widget set") {
    val igWidget = new DummyIGWidget("", Double.box(10))
    val jobWidget = new DummyJobWidget("", null)
    testCompilerManager(run = loadWidgets(Seq(igWidget, jobWidget))) {
      (workspace, compilerManager, events) =>
        assert(compilerManager.widgets.contains(jobWidget))
        assert(compilerManager.widgets.contains(igWidget))
    }
  }

  test("adds interface global widgets to it's globalWidget set") {
    val igWidget = new DummyIGWidget("", Double.box(10))
    val igValueChange = new InterfaceGlobalEvent(igWidget, false, false, false, false)
    testCompilerManager(run = loadWidgets(Seq(igWidget)) _ andThen
      (_.handle(igValueChange))) {
      (workspace, compilerManager, events) =>
        assert(compilerManager.globalWidgets.contains(igWidget))
    }
  }

  test("compiler manager clears the old program completely when loading begins") {
    testCompilerManager(run =
      loadWidgets(Seq(), source = "breed [ as a ]") _ andThen
      loadWidgets(Seq(), source = "breed [ bs b ]") _) {
        (workspace, compilerManager, events) =>
          assert(workspace.world.program.breeds.get("BS").isDefined)
          assert(workspace.world.getBreed("BS") != null)
          assert(workspace.world.getBreed("BS").isInstanceOf[org.nlogo.api.AgentSet])
          assert(workspace.world.program.breeds.get("AS").isEmpty)
          assert(workspace.world.getBreed("AS") == null)
      }
  }
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
