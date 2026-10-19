package bsearch.space;

import org.nlogo.api.MersenneTwisterFast;

/** characterizes a parameter that has a continuous (floating point) range on the interval [A,B) */
public class DoubleContinuousSpec extends ParameterSpec {
    private double dMin;
    private double dMax;


    public DoubleContinuousSpec(String name, double min, double max) {
        super(name);
        this.dMin = min;
        this.dMax = max;
    }

    public double getMin()
    {
        return dMin;
    }
    public double getMax()
    {
        return dMax;
    }

    public double enforceValidRange(double d)
    {
        if (d < dMin)
            return dMin;
        else if (d > dMax)
            return dMax;
        else
            return d;
    }

    @Override
    public Double generateRandomValue(MersenneTwisterFast rng) {
        return rng.nextDouble() * (dMax - dMin) + dMin;
    }

    /**
     * @param obj - Double - parameter value to be mutated
     * @param mutStrength - controls the magnitude of the Gaussian mutation.
     * (not to be confused with the mutation-rate, which controls the likelihood of mutation).
        For example, mutStrength=0.1 corresponds to a stdDev of 10% of the parameter's range
        This means that 68% of the time, the mutation will fall within +/- 10%
        and 95% of the time, the mutation will fall within +/- 20%.
     * @param rng - random number generator
     * @returns the result of adding Gaussian noise to the given parameter value, clamped to this parameter's min/max range.
     */
    @Override
    public Double mutate(Object obj, double mutStrength, MersenneTwisterFast rng) {
        double mutStdDev = (dMax - dMin) * mutStrength;
        return enforceValidRange((Double)obj + mutStdDev * rng.nextGaussian());
    }

    /**
     * Since this is a continuous real-valued representation, the size of the search space
     * becomes nearly infinite.  For now, we use the special value -1 for this case.
     */
    @Override
    public int choiceCount()
    {
        return -1 ;
    }

    @Override
    public String toString()
    {
        return "[ \"" + name + "\" [ " + dMin + " \"C\" " + dMax + " ]]";
    }

    @Override
    public Object getValueFromChoice(long choice, long maxNumChoices) {
        return dMin + (dMax - dMin) * choice / (maxNumChoices - 1);
    }
    @Override
    public long getChoiceIndexFromValue(Object val, long maxNumChoices) {
        if (!(val instanceof Number))
        {
            throw new IllegalStateException("Type mismatch: can't represent a non-number using this double-valued parameter specification.");
        }
        double dVal = ((Number) val).doubleValue();
        return (long) StrictMath.round((dVal - dMin) * (maxNumChoices - 1) / (dMax - dMin));
    }
}
