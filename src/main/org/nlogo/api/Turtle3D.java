package org.nlogo.api ;

public interface Turtle3D extends Turtle
{
	/**
	 * Returns the value of the <code>pitch</code> variable
	 */
	double pitch() ;
	/**
	 * Returns the value of the <code>roll</code> variable
	 */
	double roll() ;
	/**
	 * Returns the value of the <code>zcor</code> variable
	 */
	double zcor() ;
	/**
	 * Returns the x component of the forward vector
	 */
	double dx() ;
	/**
	 * Returns the y component of the forward vector
	 */
	double dy() ;
	/**
	 * Returns the z component of the forward vector
	 */
	double dz() ;
}
