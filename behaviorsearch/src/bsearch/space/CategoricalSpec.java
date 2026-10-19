package bsearch.space;



import java.util.ArrayList;
import java.util.List;

import org.nlogo.api.MersenneTwisterFast;

/** For any parameter that is fixed for the search.
 */
public class CategoricalSpec extends ParameterSpec {
    private List<Object> choices;

    public CategoricalSpec(String name, List<Object> choices) {
        super(name);
        this.choices = new ArrayList<Object>(choices);
    }
    public CategoricalSpec(String name, Object... choiceArray) {
        super(name);
        this.choices = new ArrayList<Object>();
        for (Object obj : choiceArray)
        {
            this.choices.add( obj );
        }
    }

    @Override
    public Object generateRandomValue(MersenneTwisterFast rng) {
        return choices.get( rng.nextInt( choices.size() ) );
    }

    /**
     *  Since there is no explicit relationship between the alleles for this gene,
     *  mutation is done by randomly choosing a *different* one of them at random.
     *  the mutStrength parameter is ignored.
     */
    @Override
    public Object mutate(Object obj, double mutStrength, MersenneTwisterFast rng) {
        int index = choices.indexOf( obj );
        int newchoice = rng.nextInt( choices.size() - 1);
        if (newchoice >= index)
        {
            newchoice++;
        }
        return choices.get( newchoice );
    }

    @Override
    public int choiceCount()
    {
        return choices.size() ;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "[ ");
        sb.append('"');
        sb.append(name);
        sb.append('"');
        for (Object obj: choices)
        {
            sb.append(" ");
            sb.append(obj);
        }
        sb.append(" ]");
        return sb.toString();
    }
    @Override
    public Object getValueFromChoice(long choice, long maxNumChoices) {
        return choices.get((int)(choice % choices.size()));
    }
    @Override
    public long getChoiceIndexFromValue(Object val, long maxNumChoices) {
        return choices.indexOf(val);
    }
}
