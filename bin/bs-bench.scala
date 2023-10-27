#!/bin/sh
exec scala -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

object Main
{
    var oldPath = ""
    var newPath = ""
    var model = ""
    var experiment = ""
    var spreadsheet = ""
    var table = ""
    var lists = ""
    var stats = ""
    var trials = 1

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
                case "--spreadsheet" => spreadsheet = argsIterator.next().trim
                case "--table" => table = argsIterator.next().trim
                case "--lists" => lists = argsIterator.next().trim
                case "--stats" => stats = argsIterator.next().trim
                case "--trials" => trials = argsIterator.next().trim.toInt
                case _ => return printHelp()
            }
        }

        if (oldPath.isEmpty || newPath.isEmpty || model.isEmpty || experiment.isEmpty) return printHelp()

        time(oldPath)
        time(newPath)
    }

    def time(path: String): Unit =
    {
        println(s"Testing $path...")

        var average = 0f

        for (i <- 0 until trials)
        {
            var command = s"./NetLogo_Console --headless --model '${model}' --experiment '${experiment}'"

            if (!spreadsheet.isEmpty) command += s" --spreadsheet '${spreadsheet}'"
            if (!table.isEmpty) command += s" --table '${table}'"
            if (!lists.isEmpty) command += s" --lists '${lists}'"
            if (!stats.isEmpty) command += s" --stats '${stats}'"

            val start = System.nanoTime

            sys.process.Process(command, new java.io.File(path)).!!

            average += System.nanoTime - start

            println(s"Trial ${i + 1} of $trials completed.")
        }

        println(s"Average time: ${(average / trials) / 60e9} minutes.")
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
        println("--experiment <string>   experiment name")
        println("--spreadsheet <path>   path to desired spreadsheet output")
        println("--table <path>   path to desired table output")
        println("--lists <path>   path to desired lists output")
        println("--stats <path>   path to desired stats output")
        println("--trials <number>   number of identical trials to execute")
    }
}
