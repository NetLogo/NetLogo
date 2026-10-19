## Using

The matrix extension adds a new matrix data structure to NetLogo.
A matrix is a mutable 2-dimensional array containing only numbers.

### When to Use

Although matrices store numbers, much like a list of lists, or an
array of arrays, the primary reason to use the matrix data type is to
take advantage of special mathematical operations associated with
matrices. For instance, matrix multiplication is a convenient way to
perform geometric transformations, and the repeated application of
matrix multiplication can also be used to simulate other dynamic
processes (for instance, processes on graph/network structures).

If you'd like to know more about matrices and how they can be
used, you might consider a course on linear algebra, or search the
web for tutorials. The matrix extension also allows you to solve
linear algebraic equations (specified in a matrix format), and even
to identify trends in your data and perform linear (ordinary least
squares) regressions on data sets with multiple explanatory
variables.

### How to Use

The matrix extension comes preinstalled.

To use the matrix extension in your model, add a line to the top of your Code tab:

```NetLogo
extensions [matrix]
```

If your model already uses other extensions, then it already has an
`extensions` line in it, so just add `matrix` to the list.

### Example
{{! escape mustache tags}}{{={| |}=}}
```NetLogo
let m matrix:from-row-list [[1 2 3] [4 5 6]]
print m
=> {{matrix:  [ [ 1 2 3 ][ 4 5 6 ] ]}}
print matrix:pretty-print-text m
=>
[[ 1  2  3 ]
 [ 4  5  6 ]]

print matrix:dimensions m
=> [2 3]
;;(NOTE: row & column indexing starts at 0, not 1)
print matrix:get m 1 2 ;; what number is in row 1, column 2?
=> 6
matrix:set m 1 2 10 ;; change the 6 to a 10
print m
=> {{matrix:  [ [ 1 2 3 ][ 4 5 10 ] ]}}

let m2 matrix:make-identity 3
print m2
=> {{matrix:  [ [ 1 0 0 ][ 0 1 0 ][ 0 0 1 ] ]}}
print matrix:times m m2 ;; multiplying by the identity changes nothing
=> {{matrix:  [ [ 1 2 3 ][ 4 5 10 ] ]}}

;; make a new matrix with the middle 1 changed to -1
let m3 (matrix:set-and-report m2 1 1 -1)
print m3
=> {{matrix:  [ [ 1 0 0 ][ 0 -1 0 ][ 0 0 1 ] ]}}
print matrix:times m m3
=> {{matrix:  [ [ 1 -2 3 ][ 4 -5 10 ] ]}}

print matrix:to-row-list (matrix:plus m2 m3)
=> [[2 0 0] [0 0 0] [0 0 2]]
```
