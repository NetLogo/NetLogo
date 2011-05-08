package org.nlogo.api ;

public interface Link3D extends Link
{
	/**
	 * Returns the z-coordinate of end1
	 */
	double z1() ;
	/**
	 * Returns the z-coordinate of end2 this coordinate is "unwrapped" so
	 * it may actually be outside the bounds of the world
	 */
	double z2() ;
	/**
	 * Returns the pitch towards end2 from end1
	 */
	double pitch() ;
}
