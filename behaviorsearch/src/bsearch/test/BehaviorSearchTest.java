package bsearch.test;

//TODO: Improve test coverage!  (It's pretty weak right now... :-/)

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.nlogo.api.MersenneTwisterFast;
import org.nlogo.api.Version$;
import org.xml.sax.SAXException;

import bsearch.algorithms.SearchMethod;
import bsearch.algorithms.SearchMethodLoader;
import bsearch.algorithms.SearchParameterException;
import bsearch.app.BehaviorSearch;
import bsearch.app.BehaviorSearchException;
import bsearch.app.SearchProtocol;
import bsearch.app.BehaviorSearch.RunOptions;
import bsearch.nlogolink.ModelRunner;
import bsearch.nlogolink.NetLogoLinkException;
import bsearch.representations.Chromosome;
import bsearch.representations.ChromosomeFactory;
import bsearch.representations.ChromosomeTypeLoader;
import bsearch.representations.GrayBinaryChromosome;
import bsearch.representations.StandardBinaryChromosome;
import bsearch.space.SearchSpace;
import bsearch.util.GeneralUtils;

public class BehaviorSearchTest
{
    // a main method to run it -- for convenience.
    public static void main( String... args )
    {
        org.junit.runner.JUnitCore.main( BehaviorSearchTest.class.getName() ) ;
    }

  public String sampleModelsPath() {
    return "../models/Sample Models/";
  }


    @Test
    public void testModelRunner1() throws Exception
    {
        ModelRunner runner;
        LinkedHashMap<String,Object> params;
        runner = bsearch.nlogolink.ModelRunner.createModelRunnerForTesting(sampleModelsPath() + "Earth Science/Fire.nlogox", true, 100);
        runner.setSetupCommands( "setup" );
        runner.setStepCommands( "go" );
        runner.setStopConditionReporter( "burned-trees > 3000" );
        runner.setMeasureIfReporter("ticks mod 10 = 0 or ticks = 37");
        runner.addResultReporter( "burned-trees" );

        params = new LinkedHashMap<String, Object>();
        params.put("density", 61.0);

        ModelRunner.RunSetup runSetup = new ModelRunner.RunSetup(0, params);

        List<Double> results = runner.doFullRun( runSetup ).getTimeSeriesForMeasure("burned-trees");
        Assert.assertEquals(5, results.size()); // 5 because we only get to tick 37, due to the stop condition.

        Assert.assertEquals(3002.0, results.get(results.size() - 1).doubleValue(), 0.001);
        Assert.assertEquals(37.0, runner.report( "ticks" ));
        runner.command("go");
        Assert.assertEquals(3068.0, runner.measureResults().get("burned-trees"));

        runner.dispose();
    }

    @Test
    public void testModelRunner2() throws Exception
    {
        ModelRunner runner;
        LinkedHashMap<String,Object> params;
        runner = bsearch.nlogolink.ModelRunner.createModelRunnerForTesting(sampleModelsPath() + "Earth Science/Fire.nlogox", false, 100);
        runner.setSetupCommands( "setup" );
        runner.setStepCommands( "go" );
        runner.setStopConditionReporter( "burned-trees > 3000" );
        runner.setMeasureIfReporter("true");
        runner.addResultReporter( "burned-trees" );

        params = new LinkedHashMap<String, Object>();
        params.put("density", 61.0);

        // now do it manually, without using doFullRun()
        runner.setup(0, params);
        int i;
        for (i = 0; i < 100; i++)
        {
            if (runner.go())
                break;
        }
        Assert.assertEquals(3002.0, runner.measureResults().get("burned-trees"));
        Assert.assertEquals(37.0, runner.report("ticks"));
        runner.dispose();
    }
    @Test
    public void testConstraintsTextGeneration() throws BehaviorSearchException, NetLogoLinkException
    {
        Assert.assertEquals(bsearch.nlogolink.Utils.getDefaultConstraintsText(sampleModelsPath() + "/Social Science/Ethnocentrism.nlogox").trim(),
        "[\"immigrants-per-day\" [0 1 100]]\n[\"death-rate\" [0 0.05 1]]\n[\"mutation-rate\" [0 0.001 1]]\n[\"immigrant-chance-cooperate-with-different\" [0 0.01 1]]\n[\"initial-ptr\" [0 0.01 1]]\n[\"cost-of-giving\" [0 0.01 1]]\n[\"gain-of-receiving\" [0 0.01 1]]\n[\"immigrant-chance-cooperate-with-same\" [0 0.01 1]]");
    }


    @Test
    public void testSearchProtocol() throws IOException , SAXException
    {
        String FILENAME = GeneralUtils.attemptResolvePathFromBSearchRoot("test/TestProtocol.xml") ;
        SearchProtocol sp = SearchProtocol.loadFile( FILENAME ) ;
        java.io.StringWriter sw = new java.io.StringWriter() ;
        sp.save( new PrintWriter( sw ) ) ;
        // is the result the same as the original, ignoring all whitespace?
        String result = sw.getBuffer().toString().replaceAll( "\\s" , "" ) ;
        String original  = bsearch.util.GeneralUtils.stringContentsOfFile( new java.io.File(FILENAME) ).replaceAll( "\\s" , "" ) ;
        //System.out.println( result ) ;
        //System.out.println( original ) ;
        //System.out.println( result.equals( original ) ) ;
        Assert.assertEquals(result, original);
    }

    @Test
    public void testSearchMethodLoader() throws BehaviorSearchException
    {
        // just make sure there are no errors...
        for (String s: SearchMethodLoader.getAllSearchMethodNames())
        {
            SearchMethod searcher = SearchMethodLoader.createFromName(s);
            searcher.getSearchParams().toString();
        }
    }

    @Test
    public void testChromosomes() throws BehaviorSearchException
    {
        SearchSpace ss = new SearchSpace(Arrays.asList("[\"discrete1to4\" [1 1 4]]",
                                                        "[\"continuous0to1.5\" [0.0 \"C\" 1.5]]",
                                                        "[\"categorical\" \"apple\" \"banana\" \"cherry\"]",
                                                        "[\"const\" 25]",
                                                        "[\"discretedecimal\" [-1 0.17 2]]"), false);
        MersenneTwisterFast rng = new MersenneTwisterFast();

        for (String chromoType: ChromosomeTypeLoader.getAllChromosomeTypes())
        {
            ChromosomeFactory factory = ChromosomeTypeLoader.createFromName(chromoType);
            Chromosome cs[] = new Chromosome[] {factory.createChromosome(ss, rng), factory.createChromosome(ss, rng)};
            for (int i = 0; i < 1000; i++)
            {
                cs[0] = cs[0].mutate(0.20, rng);
                cs[1] = cs[1].crossoverWith(cs[0], rng)[0];

                for (Chromosome c: cs )
                {
                    LinkedHashMap<String, Object> params = c.getParamSettings();
                    double val1 = (Double)params.get("discrete1to4");
                    double val2 = (Double)params.get("continuous0to1.5");
          // legitimate bug here - we have a logolist of strings when we're expecting a
          // single string, unclear what broke
                    String val3 = (String) params.get("categorical");
                    Object val4 = params.get("const");
                    double val5 = (Double)params.get("discretedecimal");
                    Assert.assertTrue(chromoType + " check val1 >= 1", val1 >= 1 );
                    Assert.assertTrue(chromoType + " check val1 <= 4", val1 <= 4 );
                    Assert.assertTrue(chromoType + " check val2 >= 0.0", val2 >= 0.0 );
                    Assert.assertTrue(chromoType + " check val2 <= 1.5", val2 <= 1.5 );
                    Assert.assertTrue(chromoType + " check val3 okay", val3.equals("apple") || val3.equals("banana") || val3.equals("cherry"));
                    Assert.assertTrue(chromoType + " check val4 okay", val4.equals(Double.valueOf(25)));
                    Assert.assertTrue(chromoType + " check val5 >= -1", val5 >= -1 );
                    Assert.assertTrue(chromoType + " check val5 <= 2", val5 <= 2 );
                }
            }
            for (int i = 0; i < 10000; i++)
            {
                Chromosome c = factory.createChromosome(ss, rng);
                //System.out.println(chromoType + " i=" + i + " : " + c);
                Chromosome c2 = factory.createChromosome(ss, c.getParamSettings());
                Assert.assertEquals(chromoType + " check recreate Chromosomes from values:", c.getParamSettings(), c2.getParamSettings());
            }
        }
    }

    @Test
    public void testBinaryConversion()
    {
        int numBits = 8;
        boolean[] bits = new boolean[numBits + 12];

        SearchSpace ss = new SearchSpace(Arrays.asList("[\"moo\" [1 1 4]]"), false);
        GrayBinaryChromosome bcgray = new GrayBinaryChromosome(ss, new MersenneTwisterFast() );
        StandardBinaryChromosome bcstd = new StandardBinaryChromosome(ss, new MersenneTwisterFast() );

        for (long i = 0; i < (1L << numBits); i ++)
        {
            bcgray.binaryEncode(i, bits, 5, numBits);
            long dec = bcgray.binaryDecode(bits, 5, numBits);
            Assert.assertEquals(i, dec);
            bcstd.binaryEncode(i, bits, 5, numBits);
            dec = bcstd.binaryDecode(bits, 5, numBits);
//          if (i != dec)
//          {
//              System.out.println("i = " + i + ":  " + BinaryChromosome.toBinaryString(bits) + "   decode=" + dec);
//          }
            Assert.assertEquals(i, dec);
        }
        numBits = 62 ;
        bits= new boolean[numBits + 3];
        for (long i = 0; i < (1L << numBits); i += 15037093017331L)
        {
            bcgray.binaryEncode(i, bits, 2, numBits);
            long dec = bcgray.binaryDecode(bits, 2, numBits);
            Assert.assertEquals(i, dec);
            bcstd.binaryEncode(i, bits, 2, numBits);
            dec = bcstd.binaryDecode(bits, 2, numBits);
            Assert.assertEquals(i, dec);
        }
    }


    public static <T> String join(final Iterable<T> objs, final String delimiter) {
        java.util.Iterator<T> iter = objs.iterator();
        if (!iter.hasNext())
            return "";
        StringBuffer buffer = new StringBuffer(String.valueOf(iter.next()));
        while (iter.hasNext())
            buffer.append(delimiter).append(String.valueOf(iter.next()));
        return buffer.toString();
    }

    @Test
    public void testConsistentOutputResults() throws IOException , SAXException, SearchParameterException, BehaviorSearchException, InterruptedException, CmdLineException
    {
    Path tmpDirectory = Path.of("test/tmp");
    Files.createDirectories(tmpDirectory);
        LinkedHashMap<String,String> scenarios = new LinkedHashMap<String,String>();
        scenarios.put("TesterSuperRandom","-p test/TesterSuperRandom.bsearch -o test/tmp/TesterSuperRandom -t 7 -n 2 --randomseed 123 --quiet");
        scenarios.put("Tester1","-p test/Tester.bsearch -o test/tmp/Tester1 -t 1 -n 2 -f 3 --randomseed 1234 --quiet");
        scenarios.put("Tester2","-p test/Tester.bsearch -o test/tmp/Tester2 -t 2 -n 3 -f 10 --randomseed 99 --quiet");
        scenarios.put("TesterNoisy","-p test/TesterNoisy.bsearch -o test/tmp/TesterNoisy -t 2 -n 2 --randomseed 123 --quiet");
        scenarios.put("TesterGANoCache","-p test/TesterGANoCache.bsearch -o test/tmp/TesterGANoCache -n 1 -t 2 --randomseed 99 --quiet");
        scenarios.put("TesterSA_Deriv","-p test/TesterSA_Deriv.bsearch -o test/tmp/TesterSA_Deriv -n 2 -t 1 --randomseed 67 --quiet");
        scenarios.put("TesterNoisy_RS","-p test/TesterNoisy_RS.bsearch -o test/tmp/TesterNoisy_RS -t 1 -n 1 --randomseed 123 --quiet");
        scenarios.put("TesterNoisy_RS","-p test/TesterNoisy_RS.bsearch -o test/tmp/TesterNoisy_RS -t 5 -n 1 --randomseed 123 --quiet");

        List<String> outputExtensions = Arrays.asList(".searchConfig.xml", ".modelRunHistory.csv",
                ".objectiveFunctionHistory.csv",  ".bestHistory.csv", ".finalBests.csv", ".finalCheckedBests.csv");

        List<String> failedList = new ArrayList<String>();
        for (String key: scenarios.keySet())
        {
            //System.out.println("key: + " + key);
            RunOptions clOptions = new RunOptions();
            CmdLineParser parser = new CmdLineParser(clOptions);
            parser.parseArgument(scenarios.get(key).split("\\s"));
            BehaviorSearch.runWithOptions(clOptions);

            for (String extension: outputExtensions)
            {
                String checkFileName = GeneralUtils.attemptResolvePathFromBSearchRoot("test/checks/" + key + extension);
                String testFileName = GeneralUtils.attemptResolvePathFromBSearchRoot("test/tmp/" + key + extension);
                File checkFile = new File(checkFileName);
                File testFile = new File(testFileName);
                String expected = checkFile.exists()?GeneralUtils.stringContentsOfFile(checkFile):"*no_file*";
                String result = testFile.exists()?GeneralUtils.stringContentsOfFile(testFile):"*no_file*";
                if (!result.equals(expected))
                {
                    failedList.add(key + extension);
                }
            }
        }
        if (!failedList.isEmpty())
        {
            Assert.fail("Output files that differ: " + join(failedList, ", "));
        }
    }

}
