package org.nlogo.api;

/*
 *  This code  was derived from Sun source, with some modification and extensions.
 *
 *   Original License follows:
 * @(#)Matrix3D.java    1.2 96/12/06 *
 * Copyright (c) 1994-1996 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

/**
 * A fairly conventional 3D matrix object that can transform sets of
 * 3D points and perform a variety of manipulations on the transform
 */
public strictfp class Matrix3D {
  double xx, xy, xz, xo;
  double yx, yy, yz, yo;
  double zx, zy, zz, zo;
  static final double pi = StrictMath.PI;

  /**
   * Create a new unit matrix
   */
  public Matrix3D() {
    xx = 1.0d;
    yy = 1.0d;
    zz = 1.0d;
  }

  /**
   * Create a new matrix with transform specified in row-major order
   */
  public Matrix3D(double[] xs, double[] ys, double[] zs) {
    xx = xs[0];
    xy = xs[1];
    xz = xs[2];
    yx = ys[0];
    yy = ys[1];
    yz = ys[2];
    zx = zs[0];
    zy = zs[1];
    zz = zs[2];
  }

  /**
   * Scale by f in all dimensions
   */
  public void scale(double f) {
    xx *= f;
    xy *= f;
    xz *= f;
    xo *= f;
    yx *= f;
    yy *= f;
    yz *= f;
    yo *= f;
    zx *= f;
    zy *= f;
    zz *= f;
    zo *= f;
  }

  /**
   * Scale along each axis independently
   */
  public void scale(double xf, double yf, double zf) {
    xx *= xf;
    xy *= xf;
    xz *= xf;
    xo *= xf;
    yx *= yf;
    yy *= yf;
    yz *= yf;
    yo *= yf;
    zx *= zf;
    zy *= zf;
    zz *= zf;
    zo *= zf;
  }

  /**
   * Translate the origin
   */
  public void translate(double x, double y, double z) {
    xo += x;
    yo += y;
    zo += z;
  }

  /**
   * rotate theta degrees about the y axis
   */
  public void yrot(double theta) {
    theta *= (pi / 180);
    double ct = StrictMath.cos(theta);
    double st = StrictMath.sin(theta);

    double Nxx = (xx * ct + zx * st);
    double Nxy = (xy * ct + zy * st);
    double Nxz = (xz * ct + zz * st);
    double Nxo = (xo * ct + zo * st);

    double Nzx = (zx * ct - xx * st);
    double Nzy = (zy * ct - xy * st);
    double Nzz = (zz * ct - xz * st);
    double Nzo = (zo * ct - xo * st);

    xo = Nxo;
    xx = Nxx;
    xy = Nxy;
    xz = Nxz;
    zo = Nzo;
    zx = Nzx;
    zy = Nzy;
    zz = Nzz;
  }

  /**
   * rotate theta degrees about the x axis
   */
  public void xrot(double theta) {
    theta *= (pi / 180);
    double ct = StrictMath.cos(theta);
    double st = StrictMath.sin(theta);

    double Nyx = (yx * ct + zx * st);
    double Nyy = (yy * ct + zy * st);
    double Nyz = (yz * ct + zz * st);
    double Nyo = (yo * ct + zo * st);

    double Nzx = (zx * ct - yx * st);
    double Nzy = (zy * ct - yy * st);
    double Nzz = (zz * ct - yz * st);
    double Nzo = (zo * ct - yo * st);

    yo = Nyo;
    yx = Nyx;
    yy = Nyy;
    yz = Nyz;
    zo = Nzo;
    zx = Nzx;
    zy = Nzy;
    zz = Nzz;
  }

  /**
   * rotate theta degrees about the z axis
   */
  public void zrot(double theta) {
    theta *= (pi / 180);
    double ct = StrictMath.cos(theta);
    double st = StrictMath.sin(theta);

    double Nyx = (yx * ct + xx * st);
    double Nyy = (yy * ct + xy * st);
    double Nyz = (yz * ct + xz * st);
    double Nyo = (yo * ct + xo * st);

    double Nxx = (xx * ct - yx * st);
    double Nxy = (xy * ct - yy * st);
    double Nxz = (xz * ct - yz * st);
    double Nxo = (xo * ct - yo * st);

    yo = Nyo;
    yx = Nyx;
    yy = Nyy;
    yz = Nyz;
    xo = Nxo;
    xx = Nxx;
    xy = Nxy;
    xz = Nxz;
  }

  public void vrot(double x, double y, double z, double u, double v, double w, double theta) {

    // Set some intermediate values.
    Matrix3D rot = new Matrix3D();
    double us = u * u;
    double vs = v * v;
    double ws = w * w;
    double costheta = StrictMath.cos(theta);
    double sintheta = StrictMath.sin(theta);
    double l2 = us + vs + ws;
    double l = StrictMath.sqrt(l2);

    // Build the matrix entries element by element.
    rot.xx = (us + (vs + ws) * costheta) / l2;
    rot.xy = (u * v * (1 - costheta) - w * l * sintheta) / l2;
    rot.xz = (u * w * (1 - costheta) + v * l * sintheta) / l2;
    rot.xo = (x * (vs + ws) - u * (y * v + z * w)
        + (u * (y * v + z * w) - x * (vs + ws)) * costheta + (y * w - z * v) * l * sintheta) / l2;

    rot.yx = (u * v * (1 - costheta) + w * l * sintheta) / l2;
    rot.yy = (vs + (us + ws) * costheta) / l2;
    rot.yz = (v * w * (1 - costheta) - u * l * sintheta) / l2;
    rot.yo = (y * (us + ws) - v * (x * u + z * w)
        + (v * (x * u + z * w) - y * (us + ws)) * costheta + (z * u - x * w) * l * sintheta) / l2;

    rot.zx = (u * w * (1 - costheta) - v * l * sintheta) / l2;
    rot.zy = (v * w * (1 - costheta) + u * l * sintheta) / l2;
    rot.zz = (ws + (us + vs) * costheta) / l2;
    rot.zo = (z * (us + vs) - w * (x * u + y * v)
        + (w * (x * u + y * v) - z * (us + vs)) * costheta + (x * v - y * u) * l * sintheta) / l2;

    mult(rot);
  }

  /**
   * Multiply this matrix by a second: M = M*R
   */
  public void mult(Matrix3D rhs) {
    double lxx = xx * rhs.xx + yx * rhs.xy + zx * rhs.xz;
    double lxy = xy * rhs.xx + yy * rhs.xy + zy * rhs.xz;
    double lxz = xz * rhs.xx + yz * rhs.xy + zz * rhs.xz;
    double lxo = xo * rhs.xx + yo * rhs.xy + zo * rhs.xz + rhs.xo;

    double lyx = xx * rhs.yx + yx * rhs.yy + zx * rhs.yz;
    double lyy = xy * rhs.yx + yy * rhs.yy + zy * rhs.yz;
    double lyz = xz * rhs.yx + yz * rhs.yy + zz * rhs.yz;
    double lyo = xo * rhs.yx + yo * rhs.yy + zo * rhs.yz + rhs.yo;

    double lzx = xx * rhs.zx + yx * rhs.zy + zx * rhs.zz;
    double lzy = xy * rhs.zx + yy * rhs.zy + zy * rhs.zz;
    double lzz = xz * rhs.zx + yz * rhs.zy + zz * rhs.zz;
    double lzo = xo * rhs.zx + yo * rhs.zy + zo * rhs.zz + rhs.zo;

    xx = lxx;
    xy = lxy;
    xz = lxz;
    xo = lxo;

    yx = lyx;
    yy = lyy;
    yz = lyz;
    yo = lyo;

    zx = lzx;
    zy = lzy;
    zz = lzz;
    zo = lzo;
  }

  /**
   * Reinitialize to the unit matrix
   */
  public void unit() {
    xo = 0;
    xx = 1;
    xy = 0;
    xz = 0;
    yo = 0;
    yx = 0;
    yy = 1;
    yz = 0;
    zo = 0;
    zx = 0;
    zy = 0;
    zz = 1;
  }

  /**
   * Transform nvert points from v into tv.  v contains the input
   * coordinates in doubleing point.  Three successive entries in
   * the array constitute a point.  tv ends up holding the transformed
   * points as integers; three successive entries per point
   */
  public void transform(double v[], double tv[], int nvert) {
    double lxx = xx, lxy = xy, lxz = xz, lxo = xo;
    double lyx = yx, lyy = yy, lyz = yz, lyo = yo;
    double lzx = zx, lzy = zy, lzz = zz, lzo = zo;
    for (int i = nvert * 3; (i -= 3) >= 0;) {
      double x = v[i];
      double y = v[i + 1];
      double z = v[i + 2];
      tv[i] = (x * lxx + y * lxy + z * lxz + lxo);
      tv[i + 1] = (x * lyx + y * lyy + z * lyz + lyo);
      tv[i + 2] = (x * lzx + y * lzy + z * lzz + lzo);
    }
  }

  @Override
  public String toString() {
    return ("[" + xo + "," + xx + "," + xy + "," + xz + ";"
        + yo + "," + yx + "," + yy + "," + yz + ";"
        + zo + "," + zx + "," + zy + "," + zz + "]");
  }
}
