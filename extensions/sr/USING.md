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
