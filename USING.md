The View2.5D extension offers visualization for Patch and Turtle reporters, in real time, in a simulation's context.

## How to Use

The view2.5d extension is pre-installed in NetLogo.

To use the view2.5d extension in your model, add a line to the top of your Code tab:

```
extensions [view2.5d]
```

If your model already uses other extensions, then it already has an
`extensions` line in it, so just add `view2.5d` to the list.

For more information on using NetLogo extensions,
see the [Extensions Guide](http://ccl.northwestern.edu/netlogo/docs/extensions.html)

### Incorporating Into Models

open a window using either the `view2.5d:patch-view` or `view2.5d:turtle-view` commands
(it can be a good idea to put these in your 'SETUP' procedure or a separate button).

update your window's view using one of the update commands (put these in your 'GO' procedure).

See the View2.5d Code Examples in the NetLogo models library.

### Feedback

Send comments, bugs, or other feedback to [CCL Feedback](mailto:ccl-feedback@ccl.northwestern.edu).
