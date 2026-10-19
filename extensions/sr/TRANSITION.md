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
