#!/bin/sh
exec scala -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

import sys.process.Process

object Main
{
    var path1 = ""
    var path2 = ""
    var model = ""
    var experiment = ""
    var trials = 1

    def main(args: Array[String]): Unit =
    {
        var argsIterator = args.iterator

        while (argsIterator.hasNext)
        {
            argsIterator.next.trim match
            {
                case "-a" => path1 = argsIterator.next.trim
                case "-b" => path2 = argsIterator.next.trim
                case "-m" => model = argsIterator.next.trim
                case "-e" => experiment = argsIterator.next.trim
                case "-t" => trials = argsIterator.next.trim.toInt
                case _ => return println("invalid argument")
            }
        }

        if (path1.isEmpty || path2.isEmpty) return println("not enough arguments")

        time(path1)
        time(path2)
    }

    def time(path: String): Unit =
    {
        println(s"Testing $path...")

        var average = 0f

        for (i <- 0 until trials)
        {
            val start = System.nanoTime

            Process(s"./NetLogo_Console --headless --model '${model}' --experiment '${experiment}'", new java.io.File(path1)).!!

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
        println("-a <path>   path to directory containing old NetLogo_Console")
        println("-b <path>   path to directory containing new NetLogo_Console")
        println("-m <path>   path to model")
        println("-e <string>   experiment name")
        println("-t <number>   number of identical trials to execute")
    }
}
