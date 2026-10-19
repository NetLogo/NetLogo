package bsearch.space;

import org.nlogo.core.LogoList;
import org.nlogo.api.MersenneTwisterFast;

/**
 *  Note: the Objects need to be types that NetLogo understands
 *   (e.g. Double, String, LogoList, Boolean...)
 */
public abstract class ParameterSpec {
    String name;

    public ParameterSpec(String name)
    {
        this.name = name;
    }
    public String getParameterName()
    {
        return name;
    }
    public abstract Object generateRandomValue(MersenneTwisterFast rng);
    public abstract Object mutate(Object obj, double mutStrength, MersenneTwisterFast rng);

    public abstract Object getValueFromChoice(long choice, long maxNumChoices);
    public abstract long getChoiceIndexFromValue(Object val, long maxNumChoices);
    /**
     * The special value -1 indicates a continuous parameter.
     */
    public abstract int choiceCount();
    //  public abstract Object enforceValid(Object obj1);


    //TODO: Hmm, for BITSTRING type, we could do crossover at the gene level too.
    //public abstract T crossover(T obj1, T obj2, MersenneTwisterFast rng);


    /**
     * Takes a textual parameter specification, and gives back an appropriate ParameterSpec object
     * @param paramString
     */
    public static ParameterSpec fromString(String paramString, Boolean is3d)
    {
        try {
            Object obj = bsearch.nlogolink.Utils.evaluateNetLogoReporterInEmptyWorkspace(paramString, is3d);
            if (obj instanceof LogoList)
            {
                LogoList lst = (LogoList) obj;
                String name = (String) lst.get( 0 );
                obj = lst.get(1);
                if (obj instanceof LogoList) // ranged specification
                {
                    LogoList innerLst = (LogoList) obj;
                    if (innerLst.size() != 3)
                    {
                        throw new IllegalArgumentException("Invalid parameter range spec: " + paramString);
                    }
                    Double dMin = (Double) innerLst.get( 0 );
                    Object incr = innerLst.get( 1 );
                    if (incr instanceof Double)  // discrete step size
                    {
                        Double dStep = (Double) incr;
                        Double dMax = (Double) innerLst.get( 2 );
                        return new DoubleDiscreteSpec(name, dMin, dStep, dMax) ;
                    }
                    else if (incr.toString().equals( "C" )) // continuous step size
                    {
                        Double dMax = (Double) innerLst.get( 2 );
                        return new DoubleContinuousSpec(name, dMin, dMax) ;
                    }
                    else
                    {
                        throw new IllegalArgumentException("Invalid ranged parameter spec, increment must be either \"C\" or a number. " + paramString);
                    }
                }
                else // discrete list of items
                {
                    if (lst.size() > 2)
                    {
                        return new CategoricalSpec(name, lst.butFirst().toJava());
                    }
                    else
                    {
                        return new ConstantSpec(name, lst.get(1));
                    }
                }
            }
            else
            {
                throw new IllegalArgumentException("Invalid parameter spec: " + paramString + " | Object=" + obj );
            }
        } catch (Exception ex)
        {
            if (ex instanceof IllegalArgumentException)
            {
                throw (IllegalArgumentException) ex;
            }
            //TODO: Better error messages?
            throw new IllegalArgumentException("Invalid parameter range spec: " + paramString + ".  Error was " + ex.getMessage(), ex);
        }
    }

}
