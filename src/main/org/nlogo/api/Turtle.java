package org.nlogo.api;

/**
 * Interface provides access to NetLogo turtles. 
 **/
public interface Turtle extends Agent
{
	/**
	 * Returns the value of the <code>xcor</code> variable
	 */
	double xcor() ;
	/**
	 * Returns the value of the <code>ycor</code> variable
	 */
	double ycor() ;
	/**
	 * Returns the value of the <code>heading</code> variable
	 */
	double heading() ;
	/**
	 * Sets the value of the <code>heading</code> variable
	 * @param d the new heading
	 */
	void heading( double d ) ;
	/**
	 * Returns the value of the <code>hidden?</code> variable
	 */
	boolean hidden() ;
	/**
	 * Returns the <code>line-thickness</code>
	 */
	double lineThickness() ;
	/**
	 * Returns true if the <code>label</code> variable has a non-empty string value
	 */
	boolean hasLabel() ;
	/**
	 * Returns the value of the <code>color</code> variable
	 */
	Object color() ;
	/**
	 * Returns the value of the <code>label</code> variable
	 */
	String labelString() ;
	/**
	 * Returns the value of the <code>label-color</code> variable
	 */
	Object labelColor() ;
	/**
	 * Returns the breed AgentSet of this turtle, all turtles if the turtle is unbreeded
	 */
	AgentSet getBreed() ;
	/**
	 * Returns the index of this turtle's breed
	 */
	int getBreedIndex() ;
	/**
	 * Returns the patch that this turtle is on
	 */
	Patch getPatchHere() ;
	/**
	 * Moves the turtle forward distance as if the command <code>jump</code> were used
	 * @param distance this amount to jump
	 */
	void jump( double distance ) throws AgentException ;
}
