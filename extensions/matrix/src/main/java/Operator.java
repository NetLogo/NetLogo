package org.nlogo.extensions.matrix;

import Jama.Matrix;

import java.util.Iterator;

public abstract class Operator {
  public Object reduce(Iterator<Object> elems) {
    if (!elems.hasNext()) {
      throw new IllegalArgumentException("At least one element is required.");
    }
    Object accumulator = elems.next();
    if (accumulator instanceof Matrix) {
      accumulator = ((Matrix) accumulator).copy();
    }
    return reduce(accumulator, elems);
  }

  public Object reduce(Object accumulator, Iterator<Object> elems) {
    while (elems.hasNext()) {
      accumulator = applyEquals(accumulator, elems.next());
    }
    return accumulator;
  }

  public Object apply(Object accumulator, Object elem) {
    if (accumulator instanceof Matrix) {
      return apply((Matrix) accumulator, elem);
    } else if (accumulator instanceof Double) {
      return apply(((Double) accumulator).doubleValue(), elem);
    } else {
      throw typeViolation(accumulator);
    }
  }

  public Object applyEquals(Object accumulator, Object elem) {
    if (accumulator instanceof Matrix) {
      return applyEquals((Matrix) accumulator, elem);
    } else if (accumulator instanceof Double) {
      return apply(((Double) accumulator).doubleValue(), elem);
    } else {
      throw typeViolation(accumulator);
    }
  }

  public Matrix apply(Matrix arg1, Object arg2) {
    return applyEquals(arg1.copy(), arg2);
  }

  public Matrix applyEquals(Matrix accumulator, Object elem) {
    if (elem instanceof Matrix) {
      return applyEquals(accumulator, (Matrix) elem);
    } else if (elem instanceof Double) {
      return applyEquals(accumulator, ((Double) elem).doubleValue());
    } else {
      throw typeViolation(elem);
    }
  }

  public Object apply(double accumulator, Object elem) {
    if (elem instanceof Matrix) {
      return apply(((Double) accumulator).doubleValue(), (Matrix) elem);
    } else if (elem instanceof Double) {
      return apply(((Double) accumulator).doubleValue(), ((Double) elem).doubleValue());
    } else {
      throw typeViolation(elem);
    }
  }

  abstract public double apply(double accumulator, double elem);

  protected IllegalArgumentException typeViolation(Object arg) {
    return new IllegalArgumentException("Inputs must be matrices or numbers, but got " + arg.getClass());
  }

  public Matrix applyEquals(Matrix accumulator, Matrix elem) {
    int numRows = accumulator.getRowDimension();
    int numCols = accumulator.getColumnDimension();
    int elemRows = elem.getRowDimension();
    int elemCols = elem.getColumnDimension();
    if (numRows != elemRows || numCols != elemCols) {
      throw new IllegalArgumentException("Matrices must have the same number of rows and columns. Needed " +
              "a matrix with " + numRows + " rows and " + numCols + " columns, but found a matrix with " + elemRows +
              " rows and " + elemCols + " columns.");
    }

    double[][] accumulatorArray = accumulator.getArray();
    double[][] elemArray = elem.getArray();
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        accumulatorArray[i][j] = apply(accumulatorArray[i][j], elemArray[i][j]);
      }
    }
    return accumulator;
  }

  public Matrix applyEquals(Matrix accumulator, double elem) {
    for (double[] row : accumulator.getArray()) {
      for (int i=0; i<row.length; i++) {
        row[i] = apply(row[i], elem);
      }
    }
    return accumulator;
  }

  public Matrix apply(double accumulator, Matrix elem) {
    double[][] resultArray = elem.getArrayCopy();
    for (double[] row : resultArray) {
      for (int i=0; i<row.length; i++) {
        row[i] = apply(accumulator, row[i]);
      }
    }
    return new Matrix(resultArray);
  }
}
