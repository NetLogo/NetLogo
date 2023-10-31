#!/bin/sh
exec scala -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

object Main
{
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

    def main(args: Array[String]): Unit =
    {
        var argsIterator = args.iterator

        while (argsIterator.hasNext)
        {
            argsIterator.next().trim match
            {
                case "--old" => oldPath = argsIterator.next().trim
                case "--new" => newPath = argsIterator.next().trim
                case "--model" => model = argsIterator.next().trim
                case "--experiment" => experiment = argsIterator.next().trim
                case "--setup-file" => setupFile = argsIterator.next().trim
                case "--spreadsheet" => spreadsheet = argsIterator.next().trim
                case "--table" => table = argsIterator.next().trim
                case "--lists" => lists = argsIterator.next().trim
                case "--stats" => stats = argsIterator.next().trim
                case "--threads" => threads = argsIterator.next().trim.toInt
                case "--update-plots" => updatePlots = true
                case "--vary-plots" => varyPlots = true
                case "--trials" => trials = argsIterator.next().trim.toInt
                case "--output" => outputFile = argsIterator.next().trim
                case _ => return printHelp()
            }
        }

        if (newPath.isEmpty || model.isEmpty) return printHelp()
        if (setupFile.isEmpty && experiment.isEmpty) return printHelp()
        if (threads == 0) return printHelp()

        var data = List[List[String]]()

        if (!oldPath.isEmpty)
        {
            if (varyPlots)
            {
                try { data = data :+ time(oldPath, true) } catch { case _ : Throwable => {} }
                try { data = data :+ time(oldPath, false) } catch { case _ : Throwable => {} }
            }

            else try { data = data :+ time(oldPath, updatePlots) } catch { case _ : Throwable => {} }
        }

        if (varyPlots)
        {
            try { data = data :+ time(newPath, true) } catch { case _ : Throwable=> {} }
            try { data = data :+ time(newPath, false) } catch { case _ : Throwable=> {} }
        }

        else try { data = data :+ time(newPath, updatePlots) } catch { case _: Throwable => {} }

        if (!outputFile.isEmpty)
        {
            val output = new java.io.PrintWriter(new java.io.File(outputFile))

            output.write("Name,Update Plots,Threads")

            if (trials > 1)
            {
                for (i <- 1 to trials) output.write(s",Trial $i")

                output.write(",Average")

                if (trials > 2) output.write(",Standard Deviation")
            }

            else output.write(",Time")

            output.write("\n")

            for (line <- data) output.write(line.mkString(",") + "\n")

            output.close()
        }
    }

    def time(path: String, updatePlots: Boolean): List[String] =
    {
        val seps = path.split("/")
        val name = if (seps.length == 1) seps(0) else seps(seps.length - 1)

        var data = List[String]()

        println(s"Testing $name...")

        for (i <- 0 until trials)
        {
            var command = new scala.collection.mutable.ArrayBuffer[String]()
            
            command += "./NetLogo_Console"
            command += "--headless"
            command += "--model"
            command += model

            if (experiment.isEmpty)
            {
                command += "--setup-file"
                command += setupFile
            }
            
            else
            {
                command += "--experiment"
                command += experiment
            }

            if (!spreadsheet.isEmpty)
            {
                command += "--spreadsheet"
                command += spreadsheet
            }

            if (!table.isEmpty)
            {
                command += "--table"
                command += table
            }

            if (!lists.isEmpty)
            {
                command += "--lists"
                command += lists
            }

            if (!stats.isEmpty)
            {
                command += "--stats"
                command += stats
            }

            command += "--threads"
            command += threads.toString

            if (updatePlots) command += "--update-plots"

            val start = System.nanoTime

            sys.process.Process(command, new java.io.File(path)).!!

            val end = (System.nanoTime - start).toFloat / 60e9f

            data = data :+ end.toString

            if (trials == 1) println(s"Experiment completed in $end minutes.")
            else println(s"Trial ${i + 1} of $trials completed in $end minutes.")
        }

        if (trials > 1)
        {
            var average = 0f

            for (i <- data) average = average + i.toFloat

            average = average / trials

            if (trials > 2)
            {
                var std = 0f

                for (i <- data) std = std + math.pow(i.toFloat - average, 2).toFloat

                data = average.toString +: math.sqrt(std / trials).toString.toString +: data
            }

            else data = average.toString +: data
        }

        return name +: updatePlots.toString +: threads.toString +: data
    }

    def printHelp(): Unit =
    {
        println("-h   display help information")
        println()
        println("required testing specifications")
        println()
        println("--new <path>             path to directory containing new NetLogo_Console")
        println("--model <path>           path to model")
        println("--experiment <string>    experiment name (must be specified with model)")
        println("--threads <number>       number of threads to use")
        println()
        println("optional testing specifications")
        println()
        println("--old <path>             path to directory containing old NetLogo_Console")
        println("--setup-file <path>      path to setup file (alternative way to specify an experiment)")
        println("--trials <number>        number of identical trials to execute (default 1)")
        println("--update-plots           allows plots to be updated (default is plots are not updated)")
        println("--vary-plots             testing will be done both with and without update-plots")

        println()
        println("optional output files")
        println()
        println("--output <path>          path to desired timing output file")
        println("--spreadsheet <path>     path to desired spreadsheet output")
        println("--table <path>           path to desired table output")
        println("--lists <path>           path to desired lists output")
        println("--stats <path>           path to desired stats output")
    }
}
