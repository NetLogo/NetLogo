package org.nlogo.nvm ;

import org.nlogo.api.Dump;

public strictfp class ArgumentTypeException
	extends EngineException
{

	private final int wantedType;
	private final Object badValue;
	
	@SuppressWarnings("unused")  // Eclipse complains too
	private final int badArgIndex; // NOPMD pmd complains because the code that uses this is commented out below - ST 3/10/08

	public ArgumentTypeException( Context context , 
								  Instruction problemInstr ,
								  int badArgIndex,
								  int wantedType , Object badValue )
	{
		super( context , problemInstr , "message will be built later." ) ;
		this.badArgIndex = badArgIndex;
		this.wantedType = wantedType;
		this.badValue = badValue;	
	}

	/* this method should really only be called after
	 * the resolveErrorInstruction() method has been called, otherwise
	 * it may give faulty results.  ~Forrest (10/24/2006)
	 */
	@Override
	public String getMessage()
	{
		String result = "";
		String positionPhrase = "input";
		
		if (instruction != null)
		{
			result += instruction.displayName() ;
		}
		result += " expected " + positionPhrase 
					+ " to be " + Syntax.aTypeName( wantedType ); 

		// if badValue is a Class object, then it's not REALLY
		// a value at all -- it's just something to tell us what
		// kind of bad value was returned.
		if (badValue instanceof Class<?>)
		{
			result += " but got " + Syntax.aTypeName( Syntax.getTypeConstant( (Class<?>) badValue) ) + " instead.";
		}
		else if (badValue != null)
		{
			String badValueStr = Dump.logoObject( badValue , true , false);
		
			result += " but got " + ( badValue instanceof org.nlogo.api.Nobody
					? "NOBODY"
					: "the " + Syntax.typeName(badValue) + " " + badValueStr ) 
					+ " instead." ;
		}
		else
		{
			result += ".";
		}

		return result ;
		
	}
		
}
