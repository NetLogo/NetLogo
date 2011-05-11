package org.nlogo.nvm;

/**
 * Reporters should implement the Constant interface
 * (which extends Pure) if they are truly constant,
 * i.e., _constdouble, _conststring.
 * <p/>
 * They should implement the Pure interface if they are
 * constant-preserving
 * (i.e. the result is constant when all of their args are constant.)
 * <p/>
 * The main point here is that this distinction allows us to compute
 * some values at compile-time, rather than run-time.
 * <p/>
 * Specifically, PureConstantOptimizer looks for reporters
 * that are entirely pure.  A reporter is "entirely pure" if it implements Pure,
 * and all of it's children are "entirely pure".  Entirely pure reporters get
 * evaluated at compile time, and replaced with the appropriate constant reporter.
 * <p/>
 * "Pure" reporters *must* not depend on "context", "workspace", "world", etc
 * <p/>
 * ~Forrest (4/28/2006)
 * - ST 6/1/06
 */
public interface Pure {

}
