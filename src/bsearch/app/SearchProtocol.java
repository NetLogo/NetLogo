package bsearch.app ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import bsearch.util.GeneralUtils;



public class SearchProtocol
{
    //TODO: Add a "documentation" field to store text/comments about the search protocol.
    public final double bsearchVersionNumber;
    public final String modelFile ;
    public final String modelStepCommands ;
    public final String modelSetupCommands ;
    public final String modelStopCondition ;
    public final int modelStepLimit ;
    public final String modelMetricReporter ;
    public final String modelMeasureIf;

    public final List<String> paramSpecStrings;

    public final boolean fitnessMinimized ;
    public final int fitnessSamplingReplications ; // if == 0, use adaptive sampling.
    public final FITNESS_COLLECTING fitnessCollecting;
    public final FITNESS_COMBINE_REPLICATIONS fitnessCombineReplications;
    public final String fitnessDerivativeParameter;
    public final double fitnessDerivativeDelta;
    public final boolean fitnessDerivativeUseAbs;

    public String searchMethodType ;
    public final HashMap<String,String> searchMethodParams;

    public final String chromosomeType;
    public final boolean caching;
    public final int evaluationLimit;

    public final int bestCheckingNumReplications; // if == 0, no best checking is done


    /**
     * A SearchProtocol stores the information about what is being searched, and how it should be done.
     * These fields map to the BehaviorSearch GUI for designing an experiment/protocol in a
     * relatively straightforward fashion.
     */
    public SearchProtocol(String modelFile, List<String> paramSpecStrings,
            String modelStepCommands, String modelSetupCommands, String modelStopCondition,
            int modelStepLimit, String modelMetricReporter, String modelMeasureIf,
            boolean fitnessMinimized,
            int fitnessSamplingRepetitions,
            FITNESS_COLLECTING fitnessCollecting,
            FITNESS_COMBINE_REPLICATIONS fitnessCombineReplications,
            String fitnessDerivativeParameter,
            double fitnessDerivativeDelta,
            boolean fitnessDerivativeUseAbs,
            String searchMethodType,
            HashMap<String, String> searchMethodParams,
            String chromosomeType,
            boolean caching,
            int evaluationLimit,
            int bestCheckingNumReplications) {
        super();
        this.bsearchVersionNumber = GeneralUtils.getVersionNumber();
        this.modelFile = modelFile;
        this.paramSpecStrings = paramSpecStrings;
        this.modelStepCommands = modelStepCommands;
        this.modelSetupCommands = modelSetupCommands;
        this.modelStopCondition = modelStopCondition;
        this.modelStepLimit = modelStepLimit;
        this.modelMetricReporter = modelMetricReporter;
        this.modelMeasureIf = modelMeasureIf;
        this.fitnessMinimized = fitnessMinimized;
        this.fitnessSamplingReplications = fitnessSamplingRepetitions;
        this.fitnessCollecting = fitnessCollecting;
        this.fitnessCombineReplications = fitnessCombineReplications;
        this.fitnessDerivativeParameter = fitnessDerivativeParameter;
        this.fitnessDerivativeDelta = fitnessDerivativeDelta;
        this.fitnessDerivativeUseAbs = fitnessDerivativeUseAbs;
        this.searchMethodType = searchMethodType;
        this.searchMethodParams = searchMethodParams;
        this.chromosomeType = chromosomeType;
        this.caching = caching;
        this.evaluationLimit = evaluationLimit;
        this.bestCheckingNumReplications = bestCheckingNumReplications;
    }
    private static String loadOrGetDefault(XPath xpath, Document xmlDoc, String path, String defaultVal)
    {
        try {
            String s = (String) xpath.evaluate(path , xmlDoc , XPathConstants.STRING );
            if (s.equals(""))
            {
                return defaultVal;
            }
            return s;
        } catch (XPathExpressionException e) {
            return defaultVal;
        }
    }
    private static int loadOrGetDefaultInt(XPath xpath, Document xmlDoc, String path, int defaultVal)
    {
        try {
            String s = (String) xpath.evaluate(path , xmlDoc , XPathConstants.STRING );
            if (s.equals(""))
            {
                return defaultVal;
            }
            return Integer.valueOf(s.trim());
        } catch (XPathExpressionException e) {
            return defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
    private static double loadOrGetDefaultDouble(XPath xpath, Document xmlDoc, String path, double defaultVal)
    {
        try {
            String s = (String) xpath.evaluate(path , xmlDoc , XPathConstants.STRING );
            if (s.equals(""))
            {
                return defaultVal;
            }
            return Double.valueOf(s.trim());
        } catch (XPathExpressionException e) {
            return defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private SearchProtocol(Document xmlDoc) throws XPathExpressionException
    {
        // Admittedly, XPath isn't the most efficient approach, but this code isn't performance critical,
        // and I find XPath nice and readable. ~Forrest (10/28/2008)
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        bsearchVersionNumber = loadOrGetDefaultDouble(xpath,  xmlDoc, "/search/bsearchVersionNumber/text()" , 0.71); // 0.71 since that was the version before we added this XML tag
        modelFile = loadOrGetDefault(xpath,  xmlDoc, "/search/modelInfo/modelFile/text()" , "");
        modelSetupCommands = loadOrGetDefault(xpath,  xmlDoc, "/search/modelInfo/modelSetupCommands/text()", "");
        modelStepCommands = loadOrGetDefault(xpath,  xmlDoc, "/search/modelInfo/modelStepCommands/text()" , "" );
        modelStopCondition = loadOrGetDefault(xpath,  xmlDoc, "/search/modelInfo/modelStopCondition/text()", "");
        modelStepLimit = loadOrGetDefaultInt(xpath,  xmlDoc, "/search/modelInfo/modelStepLimit/text()", 100);
        modelMetricReporter = loadOrGetDefault(xpath,  xmlDoc, "/search/modelInfo/modelMetricReporter/text()" , "");
        modelMeasureIf = loadOrGetDefault(xpath,  xmlDoc, "/search/modelInfo/modelMeasureIf/text()" , "true");
        fitnessMinimized = Boolean.valueOf(loadOrGetDefault(xpath,  xmlDoc, "/search/fitnessInfo/fitnessMinimized/text()" , "false").trim());
        String fcollect = loadOrGetDefault(xpath,  xmlDoc, "/search/fitnessInfo/fitnessCollecting/text()" , FITNESS_COLLECTING.AT_FINAL_STEP.toString() );
        fitnessCollecting = FITNESS_COLLECTING.valueOf( fcollect.trim() );
        fitnessSamplingReplications = loadOrGetDefaultInt(xpath,  xmlDoc, "/search/fitnessInfo/fitnessSamplingReplications/text()" , 10);
        String fcombine = loadOrGetDefault(xpath, xmlDoc,"/search/fitnessInfo/fitnessCombineReplications/text()" , FITNESS_COMBINE_REPLICATIONS.MEAN.toString());
        fitnessCombineReplications = FITNESS_COMBINE_REPLICATIONS.valueOf( fcombine.trim() );
        fitnessDerivativeParameter = loadOrGetDefault(xpath, xmlDoc,"/search/fitnessInfo/fitnessDerivative/@parameter" , "");
        fitnessDerivativeDelta = loadOrGetDefaultDouble(xpath, xmlDoc,"/search/fitnessInfo/fitnessDerivative/@delta" , 0.0);
        fitnessDerivativeUseAbs = Boolean.valueOf(loadOrGetDefault(xpath,  xmlDoc, "/search/fitnessInfo/fitnessDerivative/@useabs" , "false").trim());

        NodeList paramSpecNodes = (NodeList) xpath.evaluate("/search/searchSpace/paramSpec/text()" , xmlDoc , XPathConstants.NODESET );
        paramSpecStrings = new LinkedList<String>();
        for (int i = 0; i < paramSpecNodes.getLength(); i++)
        {
            Node n = paramSpecNodes.item( i );
            paramSpecStrings.add( n.getNodeValue().trim() );
        }

        searchMethodType = loadOrGetDefault(xpath,  xmlDoc, "/search/searchMethod/@type" , "RandomSearch" );

        NodeList methodParamNodes = (NodeList) xpath.evaluate("/search/searchMethod/searchMethodParameter" , xmlDoc , XPathConstants.NODESET );
        searchMethodParams = new LinkedHashMap<String,String>();
        for (int i = 0; i < methodParamNodes.getLength(); i++)
        {
            Element n = (Element) methodParamNodes.item( i );
            searchMethodParams.put( n.getAttribute( "name" ) , n.getAttribute("value") );
        }
        chromosomeType = loadOrGetDefault(xpath,  xmlDoc, "/search/chromosomeRepresentation/@type" , "MixedTypeChromosome" );
        caching = Boolean.valueOf(loadOrGetDefault(xpath,  xmlDoc, "/search/caching/text()" , "true"));
        evaluationLimit = loadOrGetDefaultInt(xpath,  xmlDoc, "/search/evaluationLimit/text()" , 300);
        bestCheckingNumReplications = loadOrGetDefaultInt(xpath,  xmlDoc, "/search/bestCheckingNumReplications/text()" , 0);

        updateForVersionChanges();
    }
    /*
     * Do a bit of processing so that old protocol files can be loaded...
     */
    private void updateForVersionChanges()
    {
        if (bsearchVersionNumber < 0.72)
        {
            if (searchMethodType.equals("GenerationalGA"))
            {
                searchMethodType = "StandardGA";
            }
        }
    }

    // convenience method...
    private static void xmlElementNoAtts(TransformerHandler hd, String elementName, String cData) throws SAXException
    {
        xmlElementWithAtts(hd,elementName,new AttributesImpl(), cData);
    }
    private static void xmlElementWithAtts(TransformerHandler hd, String elementName, AttributesImpl atts) throws SAXException
    {
        xmlElementWithAtts(hd,elementName,atts, "");
    }
    // another convenience method...
    private static void xmlElementWithAtts(TransformerHandler hd, String elementName, AttributesImpl atts, String cData) throws SAXException
    {
        hd.startElement( "" , "" , elementName , atts);
        if (cData.length() > 0) {
            hd.characters( cData.toCharArray() , 0 , cData.length() );
        }
        hd.endElement( "" , "" , elementName );
    }

    /** Note: This method was adapted from
     *     http://www.javazoom.net/services/newsletter/xmlgeneration.html
     *
     * @param out PrintWriter to send the xml data to.
     */
    public void save(Writer out)
    {
        try
        {
            StreamResult streamResult = new StreamResult( out ) ;
            SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance() ;
            // SAX2.0 ContentHandler.
            TransformerHandler hd = tf.newTransformerHandler() ;
            Transformer serializer = hd.getTransformer() ;
            serializer.setOutputProperty( OutputKeys.ENCODING , "us-ascii" ) ;
            serializer.setOutputProperty( OutputKeys.DOCTYPE_SYSTEM ,
                    "behaviorsearch.dtd" ) ;
            serializer.setOutputProperty( OutputKeys.INDENT , "yes" ) ;
            hd.setResult( streamResult ) ;
            hd.startDocument() ;
            AttributesImpl noAtts = new AttributesImpl() ;
            AttributesImpl atts = new AttributesImpl() ;

            hd.startElement( "" , "" , "search" , noAtts ) ;
                xmlElementNoAtts(hd, "bsearchVersionNumber", String.format(Locale.US, "%.2f", GeneralUtils.getVersionNumber()));
                hd.startElement( "" , "" , "modelInfo" , noAtts );
                    xmlElementNoAtts(hd, "modelFile", modelFile ) ;
                    xmlElementNoAtts(hd, "modelSetupCommands", modelSetupCommands ) ;
                    xmlElementNoAtts(hd, "modelStepCommands", modelStepCommands ) ;
                    xmlElementNoAtts(hd, "modelStopCondition", modelStopCondition ) ;
                    xmlElementNoAtts(hd, "modelStepLimit", Integer.toString( modelStepLimit )) ;
                    xmlElementNoAtts(hd, "modelMetricReporter", modelMetricReporter) ;
                    xmlElementNoAtts(hd, "modelMeasureIf", modelMeasureIf) ;
                hd.endElement( "" , "" , "modelInfo" );
                hd.startElement( "" , "" , "fitnessInfo" , noAtts );
                    xmlElementNoAtts(hd, "fitnessMinimized", Boolean.toString(fitnessMinimized)) ;
                    xmlElementNoAtts(hd, "fitnessCollecting", fitnessCollecting.toString()) ;
                    xmlElementNoAtts(hd, "fitnessSamplingReplications", Integer.toString( fitnessSamplingReplications )) ;
                    xmlElementNoAtts(hd, "fitnessCombineReplications", fitnessCombineReplications.toString()) ;
                    if (fitnessDerivativeParameter.length() > 0)
                    {
                        atts.clear();
                        atts.addAttribute( "" , "" , "parameter" , "CDATA" , fitnessDerivativeParameter);
                        atts.addAttribute( "" , "" , "delta" , "CDATA" , Double.toString(fitnessDerivativeDelta));
                        atts.addAttribute( "" , "" , "useabs" , "CDATA" , Boolean.toString(fitnessDerivativeUseAbs));
                        xmlElementWithAtts(hd, "fitnessDerivative", atts);
                    }
                hd.endElement( "" , "" , "fitnessInfo" );
                hd.startElement( "" , "" , "searchSpace" , noAtts );
                    for (String ps: paramSpecStrings)
                    {
                        xmlElementNoAtts( hd , "paramSpec" , ps );
                    }
                hd.endElement( "" , "" , "searchSpace");
                atts.clear();
                atts.addAttribute( "" , "" , "type" , "CDATA" , searchMethodType);
                hd.startElement( "" , "" , "searchMethod" , atts );
                    for (String pName : searchMethodParams.keySet())
                    {
                        atts.clear();
                        atts.addAttribute( "" , "" , "name" , "CDATA" , pName );
                        atts.addAttribute( "" , "" , "value" , "CDATA" , searchMethodParams.get( pName ) );
                        hd.startElement( "" , "" , "searchMethodParameter" , atts );
                        hd.endElement( "" , "" , "searchMethodParameter" );
                    }
                hd.endElement( "" , "" , "searchMethod" );
                atts.clear();
                atts.addAttribute( "" , "" , "type" , "CDATA" ,chromosomeType);
                xmlElementWithAtts(hd, "chromosomeRepresentation", atts);

                xmlElementNoAtts(hd, "caching", Boolean.toString(caching));
                xmlElementNoAtts(hd, "evaluationLimit", Integer.toString(evaluationLimit));
                xmlElementNoAtts(hd, "bestCheckingNumReplications", Integer.toString(bestCheckingNumReplications));

            hd.endElement( "" , "" , "search" ) ;
            hd.endDocument() ;
        }
        catch( SAXException e )
        {
            e.printStackTrace() ;
        }
        catch( TransformerConfigurationException e )
        {
            e.printStackTrace() ;
        }

    }

    public String toXMLString()
    {
        java.io.StringWriter sw = new java.io.StringWriter();
        save(sw);
        sw.flush();
        return sw.toString();
    }

    public static SearchProtocol load(String xmlStr)
        throws java.io.IOException , SAXException
    {
        InputSource inputSource = new InputSource(
                new java.io.StringReader(//"<!DOCTYPE search SYSTEM \"behaviorsearch.dtd\">\n" +
                        xmlStr));
        return load(inputSource);
    }
    public static SearchProtocol load(InputSource inputSource)
        throws java.io.IOException , SAXException
    {
        File resourcesDir = new File( GeneralUtils.attemptResolvePathFromBSearchRoot("resources/" ));
        inputSource.setSystemId(resourcesDir.toURI().toString() ) ;

        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance() ;
        Document doc = null ;
        try
        {
            // To validate, we may need to help it find the DTD file...
            // inputSource.setSystemId( getClass().getResource( "/" ).toString() ) ;
            fac = DocumentBuilderFactory.newInstance() ;
            //TODO: Perhaps turn xml DTD validation back on?  Or is that too strict?  Is the DTD up to date?
            //fac.setValidating( true ) ;
            DocumentBuilder builder = fac.newDocumentBuilder() ;
            // Choke on any error or warning during parsing, not just fatal errors.
            builder.setErrorHandler( new ErrorHandler()
            {
                public void error( SAXParseException ex ) throws SAXException
                {   throw ex ;  }

                public void fatalError( SAXParseException ex ) throws SAXException
                {   throw ex ;  }

                public void warning( SAXParseException ex ) throws SAXException
                {   throw ex ;  }
            } ) ;
            doc = builder.parse( inputSource ) ;
            return new SearchProtocol(doc);
        }
        catch( ParserConfigurationException ex )
        {
            ex.printStackTrace() ;
            System.exit( 1 ) ;
        }
        catch( XPathExpressionException e )
        {
            e.printStackTrace();
            System.exit( 1 ) ;
        }
        throw new IllegalStateException ("Unreachable code.");
    }

    public static SearchProtocol loadFile(String fullPathFilename) throws IOException, SAXException
    {
        return loadFile(new java.io.File(fullPathFilename));
    }
    public static SearchProtocol loadFile(java.io.File file) throws IOException, SAXException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try { return load(new InputSource(reader)); }
        finally { reader.close(); }
    }



    public static enum FITNESS_COLLECTING {
        AT_FINAL_STEP {
            @Override
            public double collectFrom(List<Double> results) {   return results.get(results.size() - 1);     }
            @Override
            public boolean requiresAllSteps() { return false; }
            },
        MEAN_ACROSS_STEPS {
            @Override
            public double collectFrom(List<Double> results) {
                return bsearch.util.Stats.mean(results);
            }},
        MEDIAN_ACROSS_STEPS {
            @Override
            public double collectFrom(List<Double> results) {
                return bsearch.util.Stats.median(results);
            }},
        MIN_ACROSS_STEPS {
            @Override
            public double collectFrom(List<Double> results) {
                return Collections.min(results);
            }},
        MAX_ACROSS_STEPS {
            @Override
            public double collectFrom(List<Double> results) {
                return Collections.max(results);
            }},
        VARIANCE_ACROSS_STEPS {
            @Override
            public double collectFrom(List<Double> results) {
                return bsearch.util.Stats.variance(results);
            }},
        SUM_ACROSS_STEPS {
            @Override
            public double collectFrom(List<Double> results) {
                return bsearch.util.Stats.sum(results);
            }}
        ;
        public abstract double collectFrom(List<Double> stepResults);
        public boolean requiresAllSteps() { return true; }
    }

    public static enum FITNESS_COMBINE_REPLICATIONS {
        MEAN {
            @Override
            public double combine(List<Double> results) {
                return bsearch.util.Stats.mean(results);
            }},
        MEDIAN {
            @Override
            public double combine(List<Double> results) {
                return bsearch.util.Stats.median(results);
            }},
        MIN {
            @Override
            public double combine(List<Double> results) {
                return Collections.min(results);
            }},
        MAX {
            @Override
            public double combine(List<Double> results) {
                return Collections.max(results);
            }},
        VARIANCE {
            @Override
            public double combine(List<Double> results) {
                return bsearch.util.Stats.variance(results);
            }},
        STDEV {
            @Override
            public double combine(List<Double> results) {
                return bsearch.util.Stats.stdev(results);
            }}
        ;
        public abstract double combine(List<Double> replicationResults);
    }

    public boolean useBestChecking() {
        return bestCheckingNumReplications > 0;
    }

}
