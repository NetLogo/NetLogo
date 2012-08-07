package org.nlogo.util;

import java.io.Serializable;

/**
 * <h3>MersenneTwister and MersenneTwisterFast</h3>
 * <p><b>Version 9</b>, based on version MT199937(99/10/29)
 * of the Mersenne Twister algorithm found at
 * <a href="http://www.math.keio.ac.jp/matumoto/emt.html">
 * The Mersenne Twister Home Page</a>, with the initialization
 * improved using the new 2002/1/26 initialization algorithm
 * By Sean Luke, October 2004.
 * <p/>
 * <p><b>MersenneTwister</b> is a drop-in subclass replacement
 * for java.util.Random.  It is properly synchronized and
 * can be used in a multithreaded environment.  On modern VMs such
 * as HotSpot, it is approximately 1/3 slower than java.util.Random.
 * <p/>
 * <p><b>MersenneTwisterFast</b> is not a subclass of java.util.Random.  It has
 * the same public methods as Random does, however, and it is
 * algorithmically identical to MersenneTwister.  MersenneTwisterFast
 * has hard-code inlined all of its methods directly, and made all of them
 * final (well, the ones of consequence anyway).  Further, these
 * methods are <i>not</i> synchronized, so the same MersenneTwisterFast
 * instance cannot be shared by multiple threads.  But all this helps
 * MersenneTwisterFast achieve well over twice the speed of MersenneTwister.
 * java.util.Random is about 1/3 slower than MersenneTwisterFast.
 * <p/>
 * <h3>About the Mersenne Twister</h3>
 * <p>This is a Java version of the C-program for MT19937: Integer version.
 * The MT19937 algorithm was created by Makoto Matsumoto and Takuji Nishimura,
 * who ask: "When you use this, send an email to: matumoto@math.keio.ac.jp
 * with an appropriate reference to your work".  Indicate that this
 * is a translation of their algorithm into Java.
 * <p/>
 * <p><b>Reference. </b>
 * Makato Matsumoto and Takuji Nishimura,
 * "Mersenne Twister: A 623-Dimensionally Equidistributed Uniform
 * Pseudo-Random Number Generator",
 * <i>ACM Transactions on Modeling and Computer Simulation,</i>
 * Vol. 8, No. 1, January 1998, pp 3--30.
 * <p/>
 * <h3>About this Version</h3>
 * <p/>
 * <p><b>Changes Since V8:</b> setSeed(int) was only using the first 28 bits
 * of the seed; it should have been 32 bits.  For small-number seeds the
 * behavior is identical.
 * <p/>
 * <p><b>Changes Since V7:</b> A documentation error in MersenneTwisterFast
 * (but not MersenneTwister) stated that nextDouble selects uniformly from
 * the full-open interval [0,1].  It does not.  nextDouble's contract is
 * identical across MersenneTwisterFast, MersenneTwister, and java.util.Random,
 * namely, selection in the half-open interval [0,1).  That is, 1.0 should
 * not be returned.  A similar contract exists in nextFloat.
 * <p/>
 * <p><b>Changes Since V6:</b> License has changed from LGPL to BSD.
 * New timing information to compare against
 * java.util.Random.  Recent versions of HotSpot have helped Random increase
 * in speed to the point where it is faster than MersenneTwister but slower
 * than MersenneTwisterFast (which should be the case, as it's a less complex
 * algorithm but is synchronized).
 * <p/>
 * <p><b>Changes Since V5:</b> New empty constructor made to work the same
 * as java.util.Random -- namely, it seeds based on the current time in
 * milliseconds.
 * <p/>
 * <p><b>Changes Since V4:</b> New initialization algorithms.  See
 * (see <a href="http://www.math.keio.ac.jp/matumoto/MT2002/emt19937ar.html"</a>
 * http://www.math.keio.ac.jp/matumoto/MT2002/emt19937ar.html</a>)
 * <p/>
 * <p>The MersenneTwister code is based on standard MT19937 C/C++
 * code by Takuji Nishimura,
 * with suggestions from Topher Cooper and Marc Rieffel, July 1997.
 * The code was originally translated into Java by Michael Lecuyer,
 * January 1999, and the original code is Copyright (c) 1999 by Michael Lecuyer.
 * <p/>
 * <h3>Java notes</h3>
 * <p/>
 * <p>This implementation implements the bug fixes made
 * in Java 1.2's version of Random, which means it can be used with
 * earlier versions of Java.  See
 * <a href="http://www.javasoft.com/products/jdk/1.2/docs/api/java/util/Random.html">
 * the JDK 1.2 java.util.Random documentation</a> for further documentation
 * on the random-number generation contracts made.  Additionally, there's
 * an undocumented bug in the JDK java.util.Random.nextBytes() method,
 * which this code fixes.
 * <p/>
 * <p> Just like java.util.Random, this
 * generator accepts a long seed but doesn't use all of it.  java.util.Random
 * uses 48 bits.  The Mersenne Twister instead uses 32 bits (int size).
 * So it's best if your seed does not exceed the int range.
 * <p/>
 * <p>MersenneTwister can be used reliably
 * on JDK version 1.1.5 or above.  Earlier Java versions have serious bugs in
 * java.util.Random; only MersenneTwisterFast (and not MersenneTwister nor
 * java.util.Random) should be used with them.
 * <p/>
 * <h3>License</h3>
 * <p/>
 * Copyright (c) 2003 by Sean Luke. <br>
 * Portions copyright (c) 1993 by Michael Lecuyer. <br>
 * All rights reserved. <br>
 * <p/>
 * <p>Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li> Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <li> Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <li> Neither the name of the copyright owners, their employers, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * </ul>
 * <p>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * @version 8
 */

// Note: this class is hard-inlined in all of its methods.  This makes some of
// the methods well-nigh unreadable in their complexity.  In fact, the Mersenne
// Twister is fairly easy code to understand: if you're trying to get a handle
// on the code, I strongly suggest looking at MersenneTwister.java first.
// -- Sean

public final strictfp class MersenneTwisterFast
    extends java.util.Random
    implements Serializable, Cloneable {

  // Period parameters
  private static final int N = 624;
  private static final int M = 397;
  private static final int MATRIX_A = 0x9908b0df;   //    private static final * constant vector a
  private static final int UPPER_MASK = 0x80000000; // most significant w-r bits
  private static final int LOWER_MASK = 0x7fffffff; // least significant r bits


  // Tempering parameters
  private static final int TEMPERING_MASK_B = 0x9d2c5680;
  private static final int TEMPERING_MASK_C = 0xefc60000;

  private int __mt[]; // the array for the state vector
  private int mti; // mti==N+1 means mt[N] is not initialized
  private int __mag01[];

  // a good initial seed (of int size, though stored in a long)
  //private static final long GOOD_SEED = 4357;

  private double __nextNextGaussian;
  private boolean __haveNextNextGaussian;

  private static final String IDENTIFIER = "0";

  /**
   * This method added for use by NetLogo's "with-local-randomness"
   * primitive.  It was not in Sean's original code.
   */
  @Override
  public MersenneTwisterFast clone() {
    MersenneTwisterFast result;
    try {
      result = (MersenneTwisterFast) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException(ex);
    }
    result.__mag01 = result.__mag01.clone();
    result.__mt = result.__mt.clone();
    return result;
  }

  /**
   * This method added for use by NetLogo's export-world feature.
   * It was not in Sean's original code.
   */
  public String save() {
    StringBuilder result =
        new StringBuilder(IDENTIFIER + " " +
            __mag01[0] + " " +
            __mag01[1] + " " +
            mti + " " +
            __nextNextGaussian + " " +
            __haveNextNextGaussian);
    for (int i = 0; i < N; i++) {
      result.append(" ");
      result.append(Integer.toString(__mt[i]));
    }
    return result.toString();
  }

  /**
   * This method added for use by NetLogo's import-world feature.
   * It was not in Sean's original code.
   */
  public void load(String s) {
    java.util.StringTokenizer tokenizer =
        new java.util.StringTokenizer(s);
    String identifier = tokenizer.nextToken();
    if (!identifier.equals(IDENTIFIER)) {
      throw new RuntimeException
          ("identifier mismatch: expected \"" + IDENTIFIER +
              "\", got \"" + identifier + "\"");
    }
    __mag01[0] = Integer.parseInt(tokenizer.nextToken());
    __mag01[1] = Integer.parseInt(tokenizer.nextToken());
    mti = Integer.parseInt(tokenizer.nextToken());
    __nextNextGaussian = Double.parseDouble(tokenizer.nextToken());
    String next = tokenizer.nextToken();
    if (next.equals("true")) {
      __haveNextNextGaussian = true;
    } else if (next.equals("false")) {
      __haveNextNextGaussian = false;
    } else {
      throw new RuntimeException
          ("expected true or false, got \"" + next + "\"");
    }
    for (int i = 0; i < N; i++) {
      __mt[i] = Integer.parseInt(tokenizer.nextToken());
    }
    assert (!tokenizer.hasMoreTokens());
  }

  /**
   * Constructor using the default seed.
   */
  public MersenneTwisterFast() {
    this(System.nanoTime());
  }

  /**
   * Constructor using a given seed.  Though you pass this seed in
   * as a long, it's best to make sure it's actually an integer.
   */
  public MersenneTwisterFast(final long seed) {
    setSeed(seed);
  }


  /**
   * Constructor using an array.
   */
  public MersenneTwisterFast(final int[] array) {
    setSeed(array);
  }


  /**
   * Initalize the pseudo random number generator.  Don't
   * pass in a long that's bigger than an int (Mersenne Twister
   * only uses the first 32 bits for its seed).
   */

  @Override
  public synchronized void setSeed(final long seed) {
    // Due to a bug in java.util.Random clear up to 1.2, we're
    // doing our own Gaussian variable.
    __haveNextNextGaussian = false;

    __mt = new int[N];

    __mag01 = new int[2];
    __mag01[0] = 0x0;
    __mag01[1] = MATRIX_A;

    __mt[0] = (int) (seed & 0xffffffff);
    for (mti = 1; mti < N; mti++) {
      __mt[mti] =
          (1812433253 * (__mt[mti - 1] ^ (__mt[mti - 1] >>> 30)) + mti);
      /* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
      /* In the previous versions, MSBs of the seed affect   */
      /* only MSBs of the array mt[].                        */
      /* 2002/01/09 modified by Makoto Matsumoto             */
      __mt[mti] &= 0xffffffff;
      /* for >32 bit machines */
    }
  }


  /**
   * An alternative, more complete, method of seeding the
   * pseudo random number generator.  array must be an
   * array of 624 ints, and they can be any value as long as
   * they're not *all* zero.
   */

  public synchronized void setSeed(final int[] array) {
    int i, j, k;
    setSeed(19650218);
    i = 1;
    j = 0;
    k = (N > array.length ? N : array.length);
    for (; k != 0; k--) {
      __mt[i] = (__mt[i] ^ ((__mt[i - 1] ^ (__mt[i - 1] >>> 30)) * 1664525)) + array[j] + j; /* non linear */
      __mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
      i++;
      j++;
      if (i >= N) {
        __mt[0] = __mt[N - 1];
        i = 1;
      }
      if (j >= array.length) j = 0;
    }
    for (k = N - 1; k != 0; k--) {
      __mt[i] = (__mt[i] ^ ((__mt[i - 1] ^ (__mt[i - 1] >>> 30)) * 1566083941)) - i; /* non linear */
      __mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
      i++;
      if (i >= N) {
        __mt[0] = __mt[N - 1];
        i = 1;
      }
    }
    __mt[0] = 0x80000000; /* MSB is 1; assuring non-zero initial array */
  }


  @Override
  public int nextInt() {
    int y;

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    return y;
  }


  public short nextShort() {
    int y;

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    return (short) (y >>> 16);
  }


  public char nextChar() {
    int y;

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    return (char) (y >>> 16);
  }


  @Override
  public boolean nextBoolean() {
    int y;

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    return (y >>> 31) != 0;
  }


  /**
   * This generates a coin flip with a probability <tt>probability</tt>
   * of returning true, else returning false.  <tt>probability</tt> must
   * be between 0.0 and 1.0, inclusive.   Not as precise a random real
   * event as nextBoolean(double), but twice as fast. To explicitly
   * use this, remember you may need to cast to float first.
   */

  public boolean nextBoolean(final float probability) {
    int y;

    if (probability < 0.0f || probability > 1.0f)
      throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
    if (probability == 0.0f) return false;            // fix half-open issues
    else if (probability == 1.0f) return true;        // fix half-open issues
    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    return (y >>> 8) / ((float) (1 << 24)) < probability;
  }


  /**
   * This generates a coin flip with a probability <tt>probability</tt>
   * of returning true, else returning false.  <tt>probability</tt> must
   * be between 0.0 and 1.0, inclusive.
   */

  public boolean nextBoolean(final double probability) {
    int y;
    int z;

    if (probability < 0.0 || probability > 1.0)
      throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
    if (probability == 0.0) return false;             // fix half-open issues
    else if (probability == 1.0) return true; // fix half-open issues
    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
      }
      for (; kk < N - 1; kk++) {
        z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
      }
      z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

      mti = 0;
    }

    z = __mt[mti++];
    z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
    z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
    z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
    z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)

    /* derived from nextDouble documentation in jdk 1.2 docs, see top */
    return ((((long) (y >>> 6)) << 27) + (z >>> 5)) / (double) (1L << 53) < probability;
  }


  public byte nextByte() {
    int y;

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    return (byte) (y >>> 24);
  }


  @Override
  public void nextBytes(byte[] bytes) {
    int y;

    for (int x = 0; x < bytes.length; x++) {
      if (mti >= N)   // generate N words at one time
      {
        int kk;
        final int[] mt = this.__mt; // locals are slightly faster
        final int[] mag01 = this.__mag01; // locals are slightly faster

        for (kk = 0; kk < N - M; kk++) {
          y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
        }
        for (; kk < N - 1; kk++) {
          y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
        }
        y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
        mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

        mti = 0;
      }

      y = __mt[mti++];
      y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
      y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
      y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
      y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

      bytes[x] = (byte) (y >>> 24);
    }
  }


  @Override
  public long nextLong() {
    int y;
    int z;

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
      }
      for (; kk < N - 1; kk++) {
        z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
      }
      z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

      mti = 0;
    }

    z = __mt[mti++];
    z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
    z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
    z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
    z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)

    return (((long) y) << 32) + z;
  }


  /**
   * Returns a long drawn uniformly from 0 to n-1.  Suffice it to say,
   * n must be > 0, or an IllegalArgumentException is raised.
   */
  public long nextLong(final long n) {
    if (n <= 0)
      throw new IllegalArgumentException("n must be positive");

    long bits, val;
    do {
      int y;
      int z;

      if (mti >= N)   // generate N words at one time
      {
        int kk;
        final int[] mt = this.__mt; // locals are slightly faster
        final int[] mag01 = this.__mag01; // locals are slightly faster

        for (kk = 0; kk < N - M; kk++) {
          y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
        }
        for (; kk < N - 1; kk++) {
          y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
        }
        y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
        mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

        mti = 0;
      }

      y = __mt[mti++];
      y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
      y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
      y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
      y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

      if (mti >= N)   // generate N words at one time
      {
        int kk;
        final int[] mt = this.__mt; // locals are slightly faster
        final int[] mag01 = this.__mag01; // locals are slightly faster

        for (kk = 0; kk < N - M; kk++) {
          z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
        }
        for (; kk < N - 1; kk++) {
          z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
        }
        z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
        mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

        mti = 0;
      }

      z = __mt[mti++];
      z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
      z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
      z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
      z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)

      bits = (((((long) y) << 32) + z) >>> 1);
      val = bits % n;
    } while (bits - val + (n - 1) < 0);
    return val;
  }

  /**
   * Returns a random double in the half-open range from [0.0,1.0).  Thus 0.0 is a valid
   * result but 1.0 is not.
   */
  @Override
  public double nextDouble() {
    int y;
    int z;

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
      }
      for (; kk < N - 1; kk++) {
        z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
      }
      z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

      mti = 0;
    }

    z = __mt[mti++];
    z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
    z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
    z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
    z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)

    /* derived from nextDouble documentation in jdk 1.2 docs, see top */
    return ((((long) (y >>> 6)) << 27) + (z >>> 5)) / (double) (1L << 53);
  }


  @Override
  public double nextGaussian() {
    if (__haveNextNextGaussian) {
      __haveNextNextGaussian = false;
      return __nextNextGaussian;
    } else {
      double v1, v2, s;
      do {
        int y;
        int z;
        int a;
        int b;

        if (mti >= N)   // generate N words at one time
        {
          int kk;
          final int[] mt = this.__mt; // locals are slightly faster
          final int[] mag01 = this.__mag01; // locals are slightly faster

          for (kk = 0; kk < N - M; kk++) {
            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
          }
          for (; kk < N - 1; kk++) {
            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
          }
          y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
          mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

          mti = 0;
        }

        y = __mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

        if (mti >= N)   // generate N words at one time
        {
          int kk;
          final int[] mt = this.__mt; // locals are slightly faster
          final int[] mag01 = this.__mag01; // locals are slightly faster

          for (kk = 0; kk < N - M; kk++) {
            z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + M] ^ (z >>> 1) ^ mag01[z & 0x1];
          }
          for (; kk < N - 1; kk++) {
            z = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + (M - N)] ^ (z >>> 1) ^ mag01[z & 0x1];
          }
          z = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
          mt[N - 1] = mt[M - 1] ^ (z >>> 1) ^ mag01[z & 0x1];

          mti = 0;
        }

        z = __mt[mti++];
        z ^= z >>> 11;                          // TEMPERING_SHIFT_U(z)
        z ^= (z << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(z)
        z ^= (z << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(z)
        z ^= (z >>> 18);                        // TEMPERING_SHIFT_L(z)

        if (mti >= N)   // generate N words at one time
        {
          int kk;
          final int[] mt = this.__mt; // locals are slightly faster
          final int[] mag01 = this.__mag01; // locals are slightly faster

          for (kk = 0; kk < N - M; kk++) {
            a = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + M] ^ (a >>> 1) ^ mag01[a & 0x1];
          }
          for (; kk < N - 1; kk++) {
            a = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + (M - N)] ^ (a >>> 1) ^ mag01[a & 0x1];
          }
          a = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
          mt[N - 1] = mt[M - 1] ^ (a >>> 1) ^ mag01[a & 0x1];

          mti = 0;
        }

        a = __mt[mti++];
        a ^= a >>> 11;                          // TEMPERING_SHIFT_U(a)
        a ^= (a << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(a)
        a ^= (a << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(a)
        a ^= (a >>> 18);                        // TEMPERING_SHIFT_L(a)

        if (mti >= N)   // generate N words at one time
        {
          int kk;
          final int[] mt = this.__mt; // locals are slightly faster
          final int[] mag01 = this.__mag01; // locals are slightly faster

          for (kk = 0; kk < N - M; kk++) {
            b = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + M] ^ (b >>> 1) ^ mag01[b & 0x1];
          }
          for (; kk < N - 1; kk++) {
            b = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + (M - N)] ^ (b >>> 1) ^ mag01[b & 0x1];
          }
          b = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
          mt[N - 1] = mt[M - 1] ^ (b >>> 1) ^ mag01[b & 0x1];

          mti = 0;
        }

        b = __mt[mti++];
        b ^= b >>> 11;                          // TEMPERING_SHIFT_U(b)
        b ^= (b << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(b)
        b ^= (b << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(b)
        b ^= (b >>> 18);                        // TEMPERING_SHIFT_L(b)

        /* derived from nextDouble documentation in jdk 1.2 docs, see top */
        v1 = 2 *
            (((((long) (y >>> 6)) << 27) + (z >>> 5)) / (double) (1L << 53))
            - 1;
        v2 = 2 * (((((long) (a >>> 6)) << 27) + (b >>> 5)) / (double) (1L << 53))
            - 1;
        s = v1 * v1 + v2 * v2;
      } while (s >= 1 || s == 0);
      double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
      __nextNextGaussian = v2 * multiplier;
      __haveNextNextGaussian = true;
      return v1 * multiplier;
    }
  }


  /**
   * Returns a random float in the half-open range from [0.0f,1.0f).  Thus 0.0f is a valid
   * result but 1.0f is not.
   */
  @Override
  public float nextFloat() {
    int y;

    if (mti >= N)   // generate N words at one time
    {
      int kk;
      final int[] mt = this.__mt; // locals are slightly faster
      final int[] mag01 = this.__mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = __mt[mti++];
    y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

    return (y >>> 8) / ((float) (1 << 24));
  }


  /**
   * Returns an integer drawn uniformly from 0 to n-1.  Suffice it to say,
   * n must be > 0, or an IllegalArgumentException is raised.
   */
  @Override
  public int nextInt(final int n) {
    if (n <= 0)
      throw new IllegalArgumentException("n must be positive");

    if ((n & -n) == n)  // i.e., n is a power of 2
    {
      int y;

      if (mti >= N)   // generate N words at one time
      {
        int kk;
        final int[] mt = this.__mt; // locals are slightly faster
        final int[] mag01 = this.__mag01; // locals are slightly faster

        for (kk = 0; kk < N - M; kk++) {
          y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
        }
        for (; kk < N - 1; kk++) {
          y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
        }
        y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
        mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

        mti = 0;
      }

      y = __mt[mti++];
      y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
      y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
      y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
      y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

      return (int) ((n * (long) (y >>> 1)) >> 31);
    }

    int bits, val;
    do {
      int y;

      if (mti >= N)   // generate N words at one time
      {
        int kk;
        final int[] mt = this.__mt; // locals are slightly faster
        final int[] mag01 = this.__mag01; // locals are slightly faster

        for (kk = 0; kk < N - M; kk++) {
          y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
        }
        for (; kk < N - 1; kk++) {
          y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
          mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
        }
        y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
        mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

        mti = 0;
      }

      y = __mt[mti++];
      y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
      y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
      y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
      y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)

      bits = (y >>> 1);
      val = bits % n;
    } while (bits - val + (n - 1) < 0);
    return val;
  }

  @Override
  protected int next(int bits) {
    throw new UnsupportedOperationException();
  }
}
