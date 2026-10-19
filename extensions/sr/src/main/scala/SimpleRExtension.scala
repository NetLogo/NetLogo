package org.nlogo.extensions.simpler

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper

import java.io.File
import java.net.ServerSocket

import org.json4s.JsonDSL._
import org.json4s.jackson.{ JsonMethods, Json4sScalaModule }

import org.nlogo.languagelibrary.{ Logger, Subprocess }
import org.nlogo.languagelibrary.config.Config
import org.nlogo.languagelibrary.prims.{ EnableDebug }
import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.api.{ Argument, Command, Context, DefaultClassManager, ExtensionException, ExtensionManager, FileIO, PrimitiveManager, Reporter, Workspace }
import org.nlogo.core.{ LogoList, Syntax }

object SimpleRExtension {
  private var _isHeadless = true
  def isHeadless: Boolean = _isHeadless

  private var _codeName = "sr"
  def codeName: String = _codeName

  private var _longName = "SimpleR Extension"
  def longName: String = _longName

  private var _extensionClass: Class[?] = classOf[SimpleRExtension]
  def extensionClass: Class[?] = _extensionClass

  // The vars above and this reset business is just for the deprecated R extensions functionality.  Once that old
  // extension stand-in is removed, this can also be removed.  -Jeremy B Octover 2022
  def resetProps(codeName: String, longName: String, extensionClass: Class[?]) = {
    _codeName       = codeName
    _longName       = longName
    _extensionClass = extensionClass
  }

  val extLangBin = "Rscript"

  object MessageIds {
    val SET_NAMED_LIST = 900
    val SET_DATA_FRAME = 901
  }

  var srMenu: SRMenu = new SRMenuHeadless
  def config: Config =
    Config.createForPropertyFile(SimpleRExtension.extensionClass, SimpleRExtension.codeName)

  private var _rProcess: Option[Subprocess] = None

  def isStarted: Boolean =
    _rProcess.map(_ => true).getOrElse(false)

  def rProcess: Subprocess =
    _rProcess.getOrElse(throw new ExtensionException(
      "R process has not been started. Please run sr:setup first before any other SimpleR extension primitive"
    ))

  def rProcess_=(proc: Subprocess): Unit = {
    _rProcess.foreach(_.close())
    _rProcess = Some(proc)
  }

  def startR(workspace: Workspace): Unit = {
    val dummySocket = new ServerSocket(0)
    val port = dummySocket.getLocalPort
    dummySocket.close()

    val rExtensionDirectory = Config.getExtensionRuntimeDirectory(SimpleRExtension.extensionClass, SimpleRExtension.codeName)
    // see docs in `rlibs.R` for what this is about
    val maybeRLibFile     = new File(rExtensionDirectory, "rlibs.R")
    val rLibFile          = if (maybeRLibFile.exists) { maybeRLibFile } else { (new File("rlibs.R")).getCanonicalFile }
    val rLibFilePath      = rLibFile.toString
    val maybeRExtFile     = new File(rExtensionDirectory, "rext.R")
    val rExtFile          = if (maybeRExtFile.exists) { maybeRExtFile } else { (new File("rext.R")).getCanonicalFile }
    val rExtFilePath      = rExtFile.toString
    val maybeRRuntimePath = Config.getRuntimePath(
      SimpleRExtension.extLangBin
    , SimpleRExtension.config.runtimePath.getOrElse("")
    , "--version"
    )
    val rRuntimePath = maybeRRuntimePath.getOrElse(
      throw new ExtensionException(s"We couldn't find an R executable file to run.  Please make sure R is installed on your system.  Then you can tell the ${SimpleRExtension.longName} where it's located by opening the SimplerR Extension menu and selecting Configure to choose the location yourself or putting making sure ${SimpleRExtension.extLangBin} is available on your PATH.\n")
    )
    val rExtUserDirPath = FileIO.perUserExtensionDir(SimpleRExtension.codeName).toString

    try {
      // see docs in `rlibs.R` for what this is about
      import scala.sys.process._
      Seq(rRuntimePath, rLibFilePath, rExtUserDirPath).!

      val mapper = new JsonMethods {
        override def mapper: ObjectMapper =
          JsonMapper.builder.addModule(new Json4sScalaModule).enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS).build()
      }

      SimpleRExtension.rProcess = Subprocess.start(
        workspace
      , Seq(rRuntimePath)
      , Seq(rExtFilePath, port.toString, rExtUserDirPath, Logger.current.isDebugEnabled.toString)
      , SimpleRExtension.codeName
      , SimpleRExtension.longName
      , Some(port)
      , Option(mapper)
      )
      SimpleRExtension.srMenu.setup()
    } catch {
      case e: Exception => {
        println(e)
        throw new ExtensionException(s"""The ${SimpleRExtension.longName} didn't want to start.  Make sure you are using version 4 of R.  You can also try to manually install the rjson package is installed for use by R: `install.packages("rjson", repos = "http://cran.us.r-project.org", quiet = TRUE)`.""", e)
      }
    }
  }

  def killR(): Unit = {
    _rProcess.foreach(_.close())
    _rProcess = None
  }

}

class SimpleRExtension extends DefaultClassManager {

  private var componentIDOpt: Option[Long] = None

  def load(manager: PrimitiveManager): Unit = {
    manager.addPrimitive("setup", SetupR)
    manager.addPrimitive("run", Run)
    manager.addPrimitive("runresult", RunResult)
    manager.addPrimitive("set", Set)

    manager.addPrimitive("set-list", SetList)
    manager.addPrimitive("set-named-list", SetNamedList)
    manager.addPrimitive("set-data-frame", SetDataFrame)
    manager.addPrimitive("set-agent", SetAgent)
    manager.addPrimitive("set-agent-data-frame", SetAgentDataFrame)

    manager.addPrimitive("set-plot-device", SetPlotDevice)
    manager.addPrimitive("r-home", RHome)
    manager.addPrimitive("show-console", ShowConsole)

    manager.addPrimitive("__enable-debug", EnableDebug)
  }

  override def runOnce(em: ExtensionManager): Unit = {
    super.runOnce(em)

    SimpleRExtension._isHeadless = !em.workspaceContext.workspaceGUI

    if (!SimpleRExtension.isHeadless) {
      SimpleRExtension.srMenu = new SRMenuGUI
      SimpleRExtension.srMenu.init(em, SimpleRExtension.longName, SimpleRExtension.extLangBin, SimpleRExtension.config)
    }

  }

  override def unload(em: ExtensionManager): Unit = {
    super.unload(em)
    SimpleRExtension.killR()
    SimpleRExtension.srMenu.unload()
    SimpleRExtension.srMenu.teardown()
  }

}

object SetupR extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(right = List())

  override def perform(args: Array[Argument], context: Context): Unit = {
    SimpleRExtension.startR(context.workspace)
  }
}

object RHome extends Reporter {
  override def getSyntax = Syntax.reporterSyntax(right = List(), ret = Syntax.StringType)

  override def report(args: Array[Argument], context: Context): AnyRef =
    SimpleRExtension.rProcess.eval("R.home()")

}

object ShowConsole extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(right = List())

  override def perform(args: Array[Argument], context: Context): Unit = {
    if (!SimpleRExtension.isHeadless) {
      SimpleRExtension.srMenu.showConsole()
    }
  }
}

object Run extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.StringType | Syntax.RepeatableType)
  )

  override def perform(args: Array[Argument], context: Context): Unit =
    SimpleRExtension.rProcess.exec(args.map(_.getString).mkString("\n"))
}

object RunResult extends Reporter {
  override def getSyntax: Syntax = Syntax.reporterSyntax(
    right = List(Syntax.StringType),
    ret = Syntax.WildcardType
  )

  override def report(args: Array[Argument], context: Context): AnyRef =
    SimpleRExtension.rProcess.eval(args.map(_.getString).mkString("\n"))
}

object Set extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(right = List(Syntax.StringType, Subprocess.convertibleTypesSyntax))
  override def perform(args: Array[Argument], context: Context): Unit =
    SimpleRExtension.rProcess.assign(args(0).getString, args(1).get)
}

object SetList extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.StringType, Subprocess.convertibleTypesSyntax | Syntax.RepeatableType)
  , minimumOption = Some(2)
  , defaultOption = Some(2)
  )

  override def perform(args: Array[Argument], context: Context): Unit = {
    val name = args(0).getString
    val list = args.toSeq.drop(1).map(_.get)
    SimpleRExtension.rProcess.assign(name, list)
  }
}

object SetNamedList extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.StringType, Syntax.StringType, Subprocess.convertibleTypesSyntax | Syntax.RepeatableType)
  , minimumOption = Some(3)
  , defaultOption = Some(3)
  )

  override def perform(args: Array[Argument], context: Context): Unit = {
    val varName       = args(0).getString
    val nameValueArgs = args.toSeq.drop(1)
    if (nameValueArgs.length % 2 != 0) {
      throw new ExtensionException("Each value must have a name.")
    }
    val (logoNames, logoValues) = (0 until nameValueArgs.length by 2).map( (i) => {
      val name  = nameValueArgs(i).getString
      val value = nameValueArgs(i + 1).get
      (name, value)
    }).unzip
    val names  = SimpleRExtension.rProcess.convert.toJson(logoNames)
    val values = SimpleRExtension.rProcess.convert.toJson(logoValues)
    val body   = ("varName" -> varName) ~ ("values" -> values) ~ ("names" -> names)
    SimpleRExtension.rProcess.genericJson(SimpleRExtension.MessageIds.SET_NAMED_LIST, body)
  }
}

object SetDataFrame extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.StringType, Syntax.StringType, Subprocess.convertibleTypesSyntax | Syntax.RepeatableType)
  , minimumOption = Some(3)
  , defaultOption = Some(3)
  )

  override def perform(args: Array[Argument], context: Context): Unit = {
    val varName = args(0).getString
    val nameValueArgs = args.toSeq.drop(1)
    if (nameValueArgs.length % 2 != 0) {
      throw new ExtensionException("Each value must have a name.")
    }
    val (logoNames, logoColumns) = (0 until nameValueArgs.length by 2).map( (i) => {
      val name  = nameValueArgs(i).getString
      val value = nameValueArgs(i + 1).get match {
        case l: LogoList => l
        case v           => LogoList(v)
      }
      (name, value)
    }).unzip
    val names   = SimpleRExtension.rProcess.convert.toJson(logoNames)
    val columns = SimpleRExtension.rProcess.convert.toJson(logoColumns)
    val body    = ("varName" -> varName) ~ ("columns" -> columns) ~ ("names" -> names)
    SimpleRExtension.rProcess.genericJson(SimpleRExtension.MessageIds.SET_DATA_FRAME, body)
  }
}

object SetAgent extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.StringType, Syntax.AgentType | Syntax.AgentsetType, Syntax.StringType | Syntax.RepeatableType)
  , minimumOption = Some(3)
  , defaultOption = Some(3)
  )

  def agentToValues(agent: Agent, variableIndices: Seq[Int]): Seq[AnyRef] = {
    variableIndices.map( (i) => {
      // this logic mirrors what the Language-Library does with `agentToJson()` -Jeremy B October 2022
      agent.getVariable(i) match {
        case set: AgentSet => set.printName // <plural agentset name>
        case agent: Agent  => agent.toString // <singular agent name> <id>
        case other: AnyRef => other
      }
    })
  }

  override def perform(args: Array[Argument], context: Context): Unit = {
    val varName  = args(0).getString
    val agentArg = args(1).get
    val names    = args.toSeq.drop(2).map(_.getString)
    val agents   = agentArg match {
      case agent: Agent  =>
        val variableIndices = names.map( (varName) => agent.world.indexOfVariable(agent, varName.toUpperCase) )
        val values          = SetAgent.agentToValues(agent, variableIndices)
        values

      case agentset: AgentSet =>
        import scala.jdk.CollectionConverters.IterableHasAsScala
        val sampleAgent     = agentset.agents.iterator.next.asInstanceOf[Agent]
        val variableIndices = names.map( (varName) => sampleAgent.world.indexOfVariable(sampleAgent, varName.toUpperCase) )
        val agentsAsRows    = agentset.agents.asScala.map( (agent) => SetAgent.agentToValues(agent.asInstanceOf[Agent], variableIndices) )
        agentsAsRows.transpose
    }
    val values = SimpleRExtension.rProcess.convert.toJson(agents)
    val body   = ("varName" -> varName) ~ ("values" -> values) ~ ("names" -> names)
    SimpleRExtension.rProcess.genericJson(SimpleRExtension.MessageIds.SET_NAMED_LIST, body)
  }
}

object SetAgentDataFrame extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.StringType, Syntax.AgentType | Syntax.AgentsetType, Syntax.StringType | Syntax.RepeatableType)
  , minimumOption = Some(3)
  , defaultOption = Some(3)
  )

  override def perform(args: Array[Argument], context: Context): Unit = {
    val varName   = args(0).getString
    val agentArg  = args(1).get
    val names     = args.toSeq.drop(2).map(_.getString)
    val agentRows = agentArg match {
      case agent: Agent  =>
        val variableIndices = names.map( (varName) => agent.world.indexOfVariable(agent, varName.toUpperCase) )
        Seq(SetAgent.agentToValues(agent, variableIndices))

      case agentset: AgentSet =>
        import scala.jdk.CollectionConverters.IterableHasAsScala
        val sampleAgent     = agentset.agents.iterator.next.asInstanceOf[Agent]
        val variableIndices = names.map( (varName) => sampleAgent.world.indexOfVariable(sampleAgent, varName.toUpperCase) )
        agentset.agents.asScala.map( (agent) => SetAgent.agentToValues(agent.asInstanceOf[Agent], variableIndices) )
    }
    val logoColumns = agentRows.transpose
    val columns     = SimpleRExtension.rProcess.convert.toJson(logoColumns)
    val body        = ("varName" -> varName) ~ ("columns" -> columns) ~ ("names" -> names)
    SimpleRExtension.rProcess.genericJson(SimpleRExtension.MessageIds.SET_DATA_FRAME, body)
  }
}

// Not to be confused with `EnableDeusExMachina`, `AddNewMacGuffin`, or `ShowRedHerring`
object SetPlotDevice extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(right = List())

  override def perform(args: Array[Argument], context: Context): Unit = {
    if (!SimpleRExtension.isHeadless) {
      val osName = System.getProperty("os.name").toLowerCase

      val plotDeviceCommand = osName.substring(0, 3) match {
        case "win" => "windows()"
        case "mac" => "quartz()"
        case _     => "x11()"
      }

      SimpleRExtension.rProcess.exec(plotDeviceCommand)
    }
  }
}
