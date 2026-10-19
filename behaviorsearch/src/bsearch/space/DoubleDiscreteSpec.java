package bsearch.space;

import org.nlogo.api.MersenneTwisterFast;

public class DoubleDiscreteSpec extends ParameterSpec {
    private double dMin;
    private double dStep;
    private double dMax;

    public DoubleDiscreteSpec(String name, double min, double step, double max) {
        super(name);
        dMin = min;
        dStep = step;
        // change dMax to be the actual largest value allowed, given the step size
        dMax = min + step * StrictMath.floor((max-min)/step);
    }

    private double enforceValidRange(double d)
    {
        if (d < dMin)
            d = dMin;
        else if (d > dMax)
            d = dMax;
        return  dMin + StrictMath.round((d - dMin) / dStep) * dStep;
    }

    @Override
    public Double generateRandomValue(MersenneTwisterFast rng) {
        return rng.nextInt(1 + (int)StrictMath.floor((dMax - dMin)/dStep)) * dStep + dMin;
    }

    /**
     * @param obj - Double - parameter value to be mutated
     * @param mutStrength - controls the magnitude of the Gaussian mutation.
     * (not to be confused with the mutation-rate, which controls the likelihood of mutation).
        For example, mutStrength=0.1 corresponds to a stdDev of 10% of the parameter's range
        This means that 68% of the time, the mutation will fall within +/- 10%
        and 95% of the time, the mutation will fall within +/- 20%.
     * @param rng - random number generator
     * @returns the result of adding Gaussian noise to the given parameter value,
     *     clamped to be one of the possible discrete double values in this parameter's range.
     */
    @Override
    public Double mutate(Object obj, double mutStrength, MersenneTwisterFast rng) {
        double mutStdDev = (dMax - dMin) * mutStrength;
        return enforceValidRange((Double)obj + mutStdDev * rng.nextGaussian());
    }

    @Override
    public int choiceCount()
    {
        return 1 + (int)StrictMath.floor((dMax - dMin)/dStep) ;
    }

    @Override
    public String toString()
    {
        return "[ \"" + name + "\" [ " + dMin + " " + dStep + " " + dMax + " ]]";
    }

    @Override
    public Object getValueFromChoice( long choice , long maxNumChoices)
    {
        if (choice < choiceCount())
        {
            return Double.valueOf(dMin + dStep * choice);
        }
        else
        {
            // If the choice is greater than the choices, we assume this is because with bitstring representations,
            // the maxNumChoices is the next power of 2 above choiceCount().
            // In this case, we try to spread out these "extra" choice settings across the whole range, since
            // this seems more fair than choosing all low settings.
            // (Of course, some numbers are still more likely to be chosen than others, but that's unavoidable.)
            long wrappedChoice = (long) StrictMath.round((double) (choice % choiceCount()) / (maxNumChoices - choiceCount()) * choiceCount());
            return Double.valueOf(dMin + dStep * wrappedChoice);
        }
    }
    @Override
    public long getChoiceIndexFromValue(Object val, long maxNumChoices) {
        if (!(val instanceof Number))
        {
            System.out.println(val);
            throw new IllegalStateException("Type mismatch: can't represent a non-number using this double-valued parameter specification.");
        }
        double dVal = ((Number) val).doubleValue();

        return (long) StrictMath.round((dVal - dMin) / dStep);
    }


}
