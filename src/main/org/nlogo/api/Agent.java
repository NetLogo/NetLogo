package org.nlogo.api;

/**
 * Interface provides access to NetLogo agents.
 * NetLogo agents include turtles, patches, links and the observer. 
 **/
public interface Agent
{
	/**
	 * Returns a displayable name of this type of agent (Turtle, Link, Patch, Observer)
	 */
	String classDisplayName() ;
	/**
	 * Returns the world object associated with this agent
	 */
	World world() ;
	/**
	 * Returns the id number of this agent.  The who number in the case of a turtle, index into the array in the case of patches
	 */
	long id() ;
	/**
	 * Returns the name of the current shape, empty string in the case of patches.
	 */
	String shape() ;
	/**
	 * Returns the size of this agent
	 */
	double size() ;
	/**
	 * Sets the variable in the position vn of the agent variable array to value
	 * @param vn the index into the agent variable array
	 * @param value the new value for the variable
	 * @throws LogoException
	 * @throws AgentException If value is the wrong type for the given variable or if you try to change variables that cannot be changed
	 */
	void setVariable( int vn , Object value ) throws LogoException , AgentException ;
	/**
	 * Returns the value of the variable in the given position of the agent variable array
	 * @param vn the index into the agent variable array
	 */
	Object getVariable( int vn ) ;	
}
