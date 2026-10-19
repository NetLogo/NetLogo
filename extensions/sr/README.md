# NetLogo Simple R Extension

This NetLogo extension allows you to run R code from within NetLogo.

## Building

Run `sbt simpleR/package`.  If compilation succeeds, `sr.jar`
will be created in the `root-simple-r/` folder, and the required
dependencies will be copied there as well.  For testing, copy all the `jar` files and `rext.r` from the repository root to a `sr` directory inside your NetLogo `extensions` directory.  To package for release to the extensions library, run `simpleR/packageZip`.

## Using

To run R code you must install R and have the `R` executable on your `PATH`.
You can download R from [their site](https://www.r-project.org/).

To use this extension, you must first include it at the top of your NetLogo
model code in an `extensions` declaration.

```netlogo
extensions [
  sr
  ; ... your other extensions
]
```

You must then initialize the R environment with `sr:setup`.
This only needs to be done once per session.
Any subsequent calls will reset your R environment.

Here's an example to get you started:

```netlogo
observer> sr:setup
;; sr:runresult evaluates R statements and returns the result back to NetLogo
observer> show sr:runresult "2 + 2"
observer: 4
;; sr:run runs R code
observer> sr:run "cat('hello world')"
hello world
;; any standard output gets forwarded to the command center output
;; sr:set sets R variables to values from NetLogo
observer> ask patch 0 0 [ set pcolor red ]
observer> sr:set "center_patch_color" [pcolor] of patch 0 0
observer> show sr:runresult "center_patch_color"
observer: 15 ;; the NetLogo representation of the color red
```

See the documentation for each of the particular primitives for details on,
for instance, how to multi-line statements and how object type conversions work.

The extension also includes an interactive R console/REPL that is connected to
the same R environment as the main window's NetLogo environment.
It is useful for executing longer blocks of R code or quickly examining or modifying
R values.
This console can be opened via the menu bar under the SimpleR heading,
or by using the `sr:show-console` command.

### Error handling

R errors will be reported in NetLogo as "Extension exceptions". For instance, this code:

```netlogo
sr:run "stop('hi')"
```

will result in the NetLogo error "Extension exception: hi" appearing in an
error  dialog.

## Citing Simple R in Research

If you use the Simple R extension in research, we ask that you cite us,

Hovet, J. Head, B. & Wilensky, U. (2022). “Simple R NetLogo extension”. https://github.com/NetLogo/SimpleR-Extension Evanston, IL: Center for Connected Learning and Computer Based Modeling, Northwestern University.

## Primitives

[`sr:setup`](#srsetup)
[`sr:run`](#srrun)
[`sr:runresult`](#srrunresult)
[`sr:set`](#srset)
[`sr:set-agent`](#srset-agent)
[`sr:set-agent-data-frame`](#srset-agent-data-frame)
[`sr:set-data-frame`](#srset-data-frame)
[`sr:set-list`](#srset-list)
[`sr:set-named-list`](#srset-named-list)
[`sr:set-plot-device`](#srset-plot-device)
[`sr:r-home`](#srr-home)
[`sr:show-console`](#srshow-console)


### `sr:setup`

```NetLogo
sr:setup
```


Create the R session that this extension will use to execute code.
This command *must* be run before running any other R extension primitive.
Running this command again will shutdown the current R environment and start a new one.



### `sr:run`

```NetLogo
sr:run *R-statement*
(sr:run *R-statement* *anything...*)
```



Runs the given R statements in the current session.
To make multi-line R code easier to run, this command will take multiple strings,
each of which will be interpreted as a separate line of R code. This requires
putting the command in parentheses.

For instance:

```NetLogo
(sr:run
  "domain <- seq(-3.14, 3.14, 0.01)"
  "range <- sin(domain)"
  "png('my_file.png')"
  "plot(domain, range, "
  "     pch = 20,"
  "     main = 'y = sin(x)',"
  "     xlab = 'x',"
  "     ylab = 'y')"
  "dev.off()"
)
```

`sr:run` will wait for the statements to finish before continuing.
If you have long-running R code, NetLogo may freeze for a bit while it runs.



### `sr:runresult`

```NetLogo
sr:runresult *R-expression*
```


Evaluates the given R expression and reports the result.
`rs:runresult` attempts to convert from R data types to NetLogo data types.


Numbers, strings, and booleans convert as you would expect, except for outliers
like Infinity and NaN which will be converted into the strings 'Inf' and 'NaN',
respectively.


R vectors and R lists will be converted to NetLogo lists. NA values will be
converted into the string 'NA'.


R matrices will be flattened into one-dimensional lists using column-major order.
If you want to convert a matrix into a list of lists before sending it to NetLogo,
use the R `asplit` command.
To convert into a list of column lists, use `asplit(<matrix>, 1)`;
for a list of row lists, use `asplit(<matrix>, 2)`.


An R DataFrame will be converted into a list of lists, where the first item in
each sublist is the name of the column and the second item is a list containing
all that row data.
For example, the first 6 rows of the `iris` dataset will be converted into NetLogo like so:
```NetLogo
[
  ["Sepal.Length" [5.1 4.9 4.7 4.6 5 5.4]]
  ["Sepal.Width" [3.5 3 3.2 3.1 3.6 3.9]]
  ["Petal.Length" [1.4 1.4 1.3 1.5 1.4 1.7]]
  ["Petal.Width" [0.2 0.2 0.2 0.2 0.2 0.4]]
  ["Species" ["setosa" "setosa" "setosa" "setosa" "setosa" "setosa"]]
]
```

Other objects will be converted to a string representation if possible and and may throw
an error if not.



### `sr:set`

```NetLogo
sr:set *variable-name* *value*
```


Sets a variable in the R session with the given name to the given NetLogo value.
NetLogo objects will be converted to R objects as expected.


Note that lists in NetLogo are converted into lists in R if the elements are of different
types.  If all the elements of a NetLogo list are of the identical number, boolean, or
string type then the data will be automatically converted into a vector in R.

```NetLogo
sr:set "x" 42
sr:run "print(x)" ;; prints `[1] 42` to the command center
sr:set "y" [1 2 3]
sr:run "print(typeof(y))" ;; prints `[1] "double"` to the command center
sr:run "print(typeof(list(y)))" ;; prints `[1] "list"` to the command center
sr:run "print(y)" ;; prints `[1] 1 2 3` to the command center
show sr:runresult "y" ;; reports [1 2 3]
```

Agents are converted into lists with named elements for each agent variable.

Agentsets are converted into a list of the above lists. If you want to convert
agents to a data frame, see `sr:set-agent-data-frame`.  If you want to use `sr:set`
and do the conversion manually, try the following:

```R
my_data_frame <- as.data.frame(do.call(rbind, <agentset-list-of-lists>))
```

For example:
```NetLogo
breed [goats goat]
goats-own [energy ]
create-goats 2 [ set color 75 ]
ask goat 0 [ set energy 42 set xcor 5]
ask goat 1 [ set energy -42 set xcor -5]

sr:set "goat" goat 0
sr:run "print(typeof(goat))" ;; prints `[1] "list"` to the command center
sr:run "print(goat)"
;; Should output:
;; $WHO
;; [1] 0
;;
;; $COLOR
;; [1] 75
;; (etc.)

sr:set "goats_list_of_lists" goats
sr:run "goats_data_frame <- as.data.frame(do.call(rbind, goats_list_of_lists))"
sr:run "print(goats_data_frame)"
;; Should output:
;;   WHO COLOR HEADING XCOR YCOR   SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE
;; 1   0    75      82    5    0 default               9.9 GOATS   FALSE    1
;; 2   1    75     200   -5    0 default               9.9 GOATS   FALSE    1
;;   PEN-SIZE PEN-MODE ENERGY
;; 1        1       up     42
;; 2        1       up    -42
;;
```

Agents with variables containing references to agentsets will have those variables converted into the string representation of that agentset.



### `sr:set-agent`

```NetLogo
sr:set-agent *r-variable-name* *agent-or-agentset* *agent-variable-name*
(sr:set-agent *r-variable-name* *agent-or-agentset* *agent-variable-name1* *agent-variable-name2...*)
```

Creates a new named list in R with the given variable name.  If you want multiple agent variables make sure to surround the command in parenthesis.

```
clear-all
sr:setup
create-turtles 2
ask turtle 0 [ set color red set xcor 5]
ask turtle 1 [ set color blue set xcor -5]
(sr:set-agent "t0" turtle 0 "who" "color" "xcor" "hidden?")
sr:run "print(typeof(t0))"
;; [1] "list"
sr:run "print(t0)"
;; $who
;; [1] 0
;;
;; $color
;; [1] 15
;;
;; $xcor
;; [1] 5
;;
;; $`hidden?`
;; [1] FALSE
```



### `sr:set-agent-data-frame`

```NetLogo
sr:set-agent-data-frame *r-variable-name* *agents* *agent-variable-name*
(sr:set-agent-data-frame *r-variable-name* *agents* *agent-variable-name1* *agent-variable-name2...*)
```

Creates a new data frame in R with the given variable name.
    The columns will have the names of the NetLogo agent variables used and each row will
    be one agent's data.  If you want multiple agent variables make sure to surround
    the command in parenthesis.

```
clear-all
sr:setup
create-turtles 2
ask turtle 0 [ set color red set xcor 5]
ask turtle 1 [ set color blue set xcor -5]
(sr:set-agent-data-frame "turtles_data_frame" turtles "who" "color" "xcor" "hidden?")
sr:run "print(typeof(turtles_data_frame))"
;; [1] "list"
sr:run "print(is.data.frame(turtles_data_frame))"
;; [1] TRUE
sr:run "print(turtles_data_frame)"
;;   who color xcor hidden?
;; 1   0    15    5   FALSE
;; 2   1   105   -5   FALSE
```



### `sr:set-data-frame`

```NetLogo
sr:set-data-frame *r-variable-name* *column-name* *list-or-anything*
(sr:set-data-frame *variable-name* *column-name1* *list-or-anything-1* *column-name2* *list-or-anything-2...*)
```

Creates a new data frame in R with the given variable name.  The columns will have the names given.  If the value for a column is a list, those will be the values for that column.  If the value is a non-list, it will be used as the single item in that column.  You can add additional column names and values by surrounding the command in parenthesis.

```
clear-all
sr:setup
let l1 [10 20 30 40]
let l2 [false true false false]
let l3 ["orange" "green" "blue" "purple"]
(sr:set-data-frame "df1" "score" l1 "enabled" l2 "color" l3)
sr:run "print(typeof(df1))"
;; [1] "list"
sr:run "print(is.data.frame(df1))"
;; [1] TRUE
sr:run "print(df1)"
;;   score enabled  color
;; 1    10   FALSE orange
;; 2    20    TRUE  green
;; 3    30   FALSE   blue
;; 4    40   FALSE purple
```



### `sr:set-list`

```NetLogo
sr:set-list *r-variable-name* *anything*
(sr:set-list *r-variable-name* *anything1* *anything2...*)
```

Creates a new list in R with the given variable name.  You can add additional values by surrounding the command in parenthesis.


### `sr:set-named-list`

```NetLogo
sr:set-named-list *r-variable-name* *column-name* *list-or-anything*
(sr:set-named-list *r-variable-name* *column-name1* *list-or-anything-1* *column-name2* *list-or-anything-2...*)
```

Creates a new named list in R with the given variable name.  The columns will have the names given.  If the value for a column is a list, those will be the values for that column.  If the value is a non-list, it will be used as the single item in that column.  You can add additional column names and values by surrounding the command in parenthesis.


### `sr:set-plot-device`

```NetLogo
sr:set-plot-device
```

Activates the visual plot device for R, popping open a window if one is not already open.


### `sr:r-home`

```NetLogo
sr:r-home
```



Outputs the R home directory which is the top-level directory of the R installation
being run.

```netlogo
observer> sr:setup
observer> show sr:r-home
observer: "/Library/Frameworks/R.framework/Resources"
```



### `sr:show-console`

```NetLogo
sr:show-console
```



Opens the R console. This console can be opened via the menu bar under the SimpleR heading.



## Transitioning from the old R extension

As of version 2.0 of the Simple R extension, most primitives from the old R extension
have a direct equivalent you can switch over to use, with a different name but identical
syntax.  One change in functionality is that when a named list with a single row and
column is returned, in the R extension you'd get simply the value, in the Simple R
extension you will get a list with the column name and the value as elements.
If there is more than 1 element, you will get a list with the column name and a list
of the values.

### Using the Simple R code converter
- Open the code tab of your model.
- Change r to sr in the extensions line .
- Click the "Check" button.
- From the "SimpleR Extension" menu choose "Convert code from R extension".
- The primitives will be updated.
- You may need to add `sr:setup` in your setup procedure (or equivalent) in order too start a new R environment.
- You will need to change any code that returns a named list.


| R Extension Primitive | Simple R Extension Primitive                                       |
| --------------------- | ------------------------------------------------------------------ |
| `r:put`               | `sr:set`                                                           |
| `r:get`               | `sr:runresult`                                                     |
| `r:eval`              | `sr:run`                                                           |
| `r:__evaldirect`      | `sr:run`                                                           |
| `r:putList`           | `sr:set-list`                                                      |
| `r:putNamedList`      | `sr:set-named-list`                                                |
| `r:putDataFrame`      | `sr:set-data-frame`                                                |
| `r:putAgent`          | `sr:set-agent`                                                     |
| `r:putAgentDf`        | `sr:set-agent-data-frame`                                          |
| `r:setPlotDevice`     | `sr:set-plot-device`                                               |
| `r:interactiveShell`  | `sr:show-console`                                                  |
| `r:clear`             | No exact equivalent, but `sr:setup` will start a new R environment |
| `r:clearLocal`        | No exact equivalent, but `sr:setup` will start a new R environment |
| `r:gc`                | No equivalent, functionality is no longer needed                   |
| `r:stop`              | No equivalent, functionality is no longer needed                   |
| `r:jri-path`          | No equivalent, functionality is no longer needed                   |

### Handling a named list
R's statistical functions often return lists.
The R extension stripped out names to give you values.
With the Simple R extension the user must extract the value

```
sr:run "c <- cor.test(turtles$weight, turtles$height, method = 'spearm', alternative = 'g')"

  ;; original code
  ;; let p sr:get "c$p.value"
  ;; let rho sr:get "c$estimate"           # sample value  0.673271955983285
  let estimate  sr:runresult "c$estimate"  # sample value  [[rho -0.121575984990619]]
  let p sr:runresult "c$p.value"
  ;; select result from list
  let rho item 1 (item 0 estimate)

```
