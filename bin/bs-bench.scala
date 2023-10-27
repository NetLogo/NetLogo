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
    var threads = 1
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
                case "--update-plots" => updatePlots = if (argsIterator.next().trim == "true") true else false
                case "--vary-plots" => varyPlots = if (argsIterator.next().trim == "true") true else false
                case "--trials" => trials = argsIterator.next().trim.toInt
                case "--output" => outputFile = argsIterator.next().trim
                case _ => return printHelp()
            }
        }

        if (newPath.isEmpty) return printHelp()
        if (setupFile.isEmpty && (model.isEmpty || experiment.isEmpty)) return printHelp()

        var data = List[List[String]]()

        if (!oldPath.isEmpty)
        {
            if (varyPlots)
            {
                data = data :+ time(oldPath, true)
                data = data :+ time(oldPath, false)
            }

            else data = data :+ time(oldPath, updatePlots)
        }

        if (varyPlots)
        {
            data = data :+ time(newPath, true)
            data = data :+ time(newPath, false)
        }

        else data = data :+ time(newPath, updatePlots)

        if (!outputFile.isEmpty)
        {
            val output = new java.io.PrintWriter(new java.io.File(outputFile))

            output.write("Name,Update Plots")

            for (i <- 1 to trials) output.write(s",Trial $i")

            output.write(",Average\n")

            for (line <- data) output.write(line.mkString(",") + "\n")

            output.close()
        }
    }

    def time(path: String, updatePlots: Boolean): List[String] =
    {
        var data = List[String]()

        println(s"Testing $path...")

        data = data :+ path
        data = data :+ updatePlots.toString

        var average = 0f

        for (i <- 0 until trials)
        {
            var command = s"./NetLogo_Console --headless"

            if (model.isEmpty) command += s" --setup-file '$setupFile'"
            else command += s" --model '$model' --experiment '$experiment'"

            if (!spreadsheet.isEmpty) command += s" --spreadsheet '$spreadsheet'"
            if (!table.isEmpty) command += s" --table '$table'"
            if (!lists.isEmpty) command += s" --lists '$lists'"
            if (!stats.isEmpty) command += s" --stats '$stats'"

            command += s" --threads $threads"
            
            if (updatePlots) command += s" --update-plots true"

            val start = System.nanoTime

            sys.process.Process(command, new java.io.File(path)).!!

            val end = (System.nanoTime - start).toFloat / 60e9f

            average += end

            data = data :+ end.toString

            println(s"Trial ${i + 1} of $trials completed in $end minutes.")
        }

        data = data :+ average.toString

        println(s"Average time: ${average / trials} minutes.")

        return data
    }

    def printHelp(): Unit =
    {
        println("general")
        println()
        println("-h   display help information")
        println()
        println("options")
        println()
        println("--old <path>   path to directory containing old NetLogo_Console")
        println("--new <path>   path to directory containing new NetLogo_Console")
        println("--model <path>   path to model")
        println("--experiment <string>   experiment name (must be specified with model)")
        println("--setup-file <path>   path to setup file (alternative to model + experiment)")
        println("--spreadsheet <path>   path to desired spreadsheet output")
        println("--table <path>   path to desired table output")
        println("--lists <path>   path to desired lists output")
        println("--stats <path>   path to desired stats output")
        println("--threads <number>   number of threads to use")
        println("--update-plots <true|false>   whether plots should be updated (default false)")
        println("--vary-plots <true|false>   whether to test with both values of update-plots (default false)")
        println("--trials <number>   number of identical trials to execute (default 1)")
        println("--output <path>   path to desired output file")
    }
}
