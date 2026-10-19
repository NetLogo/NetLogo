package bsearch.algorithms;

import java.util.HashMap;
import java.util.List;

import bsearch.util.GeneralUtils;


/**
 * A convenient abstract class to inherit from when making a new SearchMethod.
 *
 */
public abstract class AbstractSearchMethod implements SearchMethod {

    public boolean supportsAdaptiveSampling()
    {
        return false;
    }

    Double validDoubleParam( HashMap<String , String> searchMethodParams, String pName, double min, double max) throws SearchParameterException
    {
        try {
            double d = Double.valueOf(searchMethodParams.get( pName ));
            if (d < min || d > max)
            {
                throw new SearchParameterException(getName() + " error: parameter '" + pName + "' should be a number between " + min + " and " + max + ", NOT '" + searchMethodParams.get( pName ) + "'");
            }
            return d;
        } catch (NumberFormatException ex)
        {
            throw new SearchParameterException(getName() + " error: parameter '" + pName + "' should be a number between " + min + " and " + max + ", NOT '" + searchMethodParams.get( pName ) + "'");
        }
    }

    Integer validIntParam( HashMap<String , String> searchMethodParams, String pName, int min, int max) throws SearchParameterException
    {
        try {
            int n = Integer.valueOf(searchMethodParams.get( pName ));
            if (n < min || n > max)
            {
                throw new SearchParameterException(getName() + " error: parameter '" + pName + "' should be an integer between " + min + " and " + max + ", NOT '" + searchMethodParams.get( pName ) + "'");
            }
            return n;
        } catch (NumberFormatException ex)
        {
            throw new SearchParameterException(getName() + " error: parameter '" + pName + "' should be an integer between " + min + " and " + max + ", NOT '" + searchMethodParams.get( pName ) + "'");
        }
    }

    String validChoiceParam( HashMap<String , String> searchMethodParams, String pName, List<String> choices) throws SearchParameterException
    {
        String s = searchMethodParams.get( pName );
        if (!choices.contains(s))
        {
            String errorMessage = getName() + " error: parameter '" + pName + "' was given an invalid value: " + s + ".  " + pName + " must be one of the following: ";
            errorMessage += "'" + GeneralUtils.stringJoin(choices,"', '") + "'";
            throw new SearchParameterException(errorMessage);
        }
        return s;
    }

    public String getHTMLHelpText()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<HTML><BODY><B>%s:</B> %s", this.getName(), this.getDescription()));
        if (this.getSearchParams().size() > 0)
        {
            sb.append("<UL>");

            for (String pname: this.getSearchParamsHelp().keySet())
            {
                sb.append(String.format("<LI><I>%s</I> - %s", pname, this.getSearchParamsHelp().get(pname)));
            }
            sb.append("</UL></BODY></HTML>");
        }
        return sb.toString();
    }


}
