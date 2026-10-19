import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.Reporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

//TODO: Check cache handling

public class SchemeColors implements Reporter
{

    public Syntax getSyntax()
    {
        int[] right = {
                Syntax.StringType() ,
                Syntax.StringType() ,
                Syntax.NumberType() ,
        } ;
        int ret = Syntax.ListType() ;
        return SyntaxJ.reporterSyntax( right , ret ) ;
    }

    public Object report( Argument args[] , Context context )
            throws ExtensionException
    {
        String schemename;
        String legendname;
        int size;
        try
        {
            schemename = args[ 0 ].getString();
            legendname = args[ 1 ].getString();
            size = args[ 2 ].getIntValue();
        }
        catch( LogoException e )
        {
            throw new ExtensionException( e.getMessage() ) ;
        }
        int index = 0;
        int [][] legend;

        legend = ColorSchemes.getRGBArray(schemename, legendname, size);

        LogoListBuilder list = new LogoListBuilder();
        for (int i = 0 ; i < legend.length; i++)
        {
          LogoListBuilder rgblist = new LogoListBuilder() ;
            rgblist.add(Double.valueOf (legend[i][0])) ;
            rgblist.add(Double.valueOf (legend[i][1])) ;
            rgblist.add(Double.valueOf (legend[i][2])) ;
            list.add(rgblist.toLogoList());
        }
        return list.toLogoList() ;
    }
}
