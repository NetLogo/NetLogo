// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

import java.io.{ File, PrintWriter }

import sbt.{ Def, InputKey }

import scala.collection.mutable.ArrayBuffer
import scala.sys.process.Process

object BSBench {
  private val bsbench = InputKey[Unit]("bsbench", "Benchmark BehaviorSpace")

  lazy val settings = {
    bsbench := {
      bench(Def.spaceDelimited("").parsed.iterator)
    }
  }

  private def bench(args: Iterator[String]): Unit = {
    var oldPath = ""
    var newPath = ""
    var model = ""
    var experiment = ""
    var setupFile = ""
    var spreadsheet = ""
    var table = ""
    var lists = ""
    var stats = ""
    var threads = 0
    var updatePlots = false
    var varyPlots = false
    var trials = 1
    var outputFile = ""

    def time(path: String, updatePlots: Boolean): List[String] = {
      val name = path.split("/").reverse.drop(1).head

      var data = List[String]()

      println(s"Testing $name...")

      for (i <- 0 until trials) {
        val command = new ArrayBuffer[String]()

        command += path
        command += "--headless"
        command += "--model"
        command += model

        if (experiment.isEmpty) {
          command += "--setup-file"
          command += setupFile
        } else {
          command += "--experiment"
          command += experiment
        }

        if (spreadsheet.nonEmpty) {
          command += "--spreadsheet"
          command += spreadsheet
        }

        if (table.nonEmpty) {
          command += "--table"
          command += table
        }

        if (lists.nonEmpty) {
          command += "--lists"
          command += lists
        }

        if (stats.nonEmpty) {
          command += "--stats"
          command += stats
        }

        command += "--threads"
        command += threads.toString

        if (updatePlots)
          command += "--update-plots"

        val start = System.nanoTime

        Process(command).!

        val end = (System.nanoTime - start).toFloat / 60e9f

        data = data :+ end.toString

        if (trials == 1) {
          println(s"Experiment completed in $end minutes.")
        } else {
          println(s"Trial ${i + 1} of $trials completed in $end minutes.")
        }
      }

      if (trials > 1) {
        val floats = data.map(_.toFloat)
        val average = floats.sum / trials

        if (trials > 2) {
          data = average.toString +: math.sqrt(floats.map(f => math.pow(f - average, 2)).sum / trials).toString +: data
        } else {
          data = average.toString +: data
        }
      }

      name +: updatePlots.toString +: threads.toString +: data
    }

    while (args.hasNext) {
      args.next().trim match {
        case "--old" => oldPath = args.next().trim
        case "--new" => newPath = args.next().trim
        case "--model" => model = args.next().trim
        case "--experiment" => experiment = args.next().trim
        case "--setup-file" => setupFile = args.next().trim
        case "--spreadsheet" => spreadsheet = new File(args.next().trim).getAbsolutePath
        case "--table" => table = new File(args.next().trim).getAbsolutePath
        case "--lists" => lists = new File(args.next().trim).getAbsolutePath
        case "--stats" => stats = new File(args.next().trim).getAbsolutePath
        case "--threads" => threads = args.next().trim.toInt
        case "--update-plots" => updatePlots = true
        case "--vary-plots" => varyPlots = true
        case "--trials" => trials = args.next().trim.toInt
        case "--output" => outputFile = args.next().trim
        case _ => return printHelp()
      }
    }

    if (newPath.isEmpty || model.isEmpty || (setupFile.isEmpty && experiment.isEmpty) || threads == 0)
      return printHelp()

    var data = List[List[String]]()

    if (oldPath.nonEmpty) {
      if (varyPlots) {
        data = data :+ time(oldPath, true) :+ time(oldPath, false)
      } else {
        data = data :+ time(oldPath, updatePlots)
      }
    }

    if (varyPlots) {
      data = data :+ time(newPath, true) :+ time(newPath, false)
    } else {
      data = data :+ time(newPath, updatePlots)
    }

    if (outputFile.nonEmpty) {
      val output = new PrintWriter(new File(outputFile))

      output.write("Name,Update Plots,Threads")

      if (trials > 1) {
        output.write(",Average")

        if (trials > 2)
          output.write(",Standard Deviation")

        for (i <- 1 to trials)
          output.write(s",Trial $i")
      } else {
        output.write(",Time")
      }

      output.write("\n")

      data.foreach(d => output.write(d.mkString(",") + "\n"))

      output.close()
    }
  }

  private def printHelp(): Unit = {
    println("""required testing specifications
              |
              |--new <path>             path to new NetLogo_Console
              |--model <path>           path to model
              |--experiment <string>    experiment name (must be specified with model)
              |--threads <number>       number of threads to use
              |
              |optional testing specifications
              |
              |--old <path>             path to old NetLogo_Console
              |--setup-file <path>      path to setup file (alternative way to specify an experiment)
              |--trials <number>        number of identical trials to execute (default 1)
              |--update-plots           allows plots to be updated (default is plots are not updated)
              |--vary-plots             testing will be done both with and without update-plots
              |
              |optional output files
              |
              |--output <path>          path to desired timing output file
              |--spreadsheet <path>     path to desired spreadsheet output
              |--table <path>           path to desired table output
              |--lists <path>           path to desired lists output
              |--stats <path>           path to desired stats output""".stripMargin)
  }
}
