#!/bin/sh
exec scala -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

import java.io.File

import scala.io.Source
import scala.sys.process.Process

object Main
{
    var passed = 0
    var total = 0

    def main(args: Array[String]): Unit =
    {
        passed = 0
        total = 0

        test("simpletest")

        if (passed == total) println("All tests passed!")
        else println(s"$passed out of $total tests passed.")
    }

    def test(name: String, filter: String = ""): Unit =
    {
        Process(Array(
            "sh ../../translationhelper.scala",
            "-g",
            "-c current.properties",
            "-d old.properties",
            "-t translated.properties",
            s"-f \"$filter\"",
            "-o temp.txt"
        ).mkString(" "), new File(name)).!

        compareLines(name + " generate", s"$name/gen.txt", s"$name/temp.txt")

        Process(Array(
            "sh ../../translationhelper.scala",
            "-m",
            "-c current.properties",
            "-t translated.properties",
            "-n changes.txt",
            "-o temp.txt"
        ).mkString(" "), new File(name)).!

        compareLines(name + " merge", s"$name/merge.properties", s"$name/temp.txt")

        Process("rm -f temp.txt", new File(name).getCanonicalFile).!
    }

    def compareLines(name: String, one: String, two: String): Unit =
    {
        total += 1

        try
        {
            val expected = Source.fromFile(one).getLines
            val actual = Source.fromFile(two).getLines

            var i = 0

            while (expected.hasNext && actual.hasNext)
            {
                val s1 = expected.next.trim
                val s2 = actual.next.trim

                if (s1 != s2)return printIncorrectOutput(name, i, s1, s2)

                i += 1
            }

            if (expected.hasNext) return printIncorrectOutput(name, i, expected.next, "")
            if (actual.hasNext) return printIncorrectOutput(name, i, "", actual.next)
        }

        catch case e: java.io.FileNotFoundException => return printError(name, e.toString)

        passed += 1
    }

    def printError(name: String, message: String): Unit =
    {
        println(s"Test '$name' failed with error: $message")
    }

    def printIncorrectOutput(name: String, index: Int, expected: String, actual: String): Unit =
    {
        println(s"Test '$name' failed at line $index.\nExpected:\n$expected\nActual:\n$actual\n")
    }
}
