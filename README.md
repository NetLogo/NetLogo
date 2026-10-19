# NetLogo View2.5D Extension

The `view2.5d` extension allows you to visualize 2d models on a 3d surface, using height to represent agent properties.

## Building

Use the netlogo.jar.url environment variable to tell sbt which NetLogo.jar to compile against (defaults to NetLogo 6.0). For example:

    sbt -Dnetlogo.jar.url=file:///path/to/NetLogo/target/NetLogo.jar package

If compilation succeeds, `view2.5d.jar` will be created.

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

## Primitives

[`view2.5d:patch-view`](https://docs.netlogo.org/view2.5d#patch-view)
[`view2.5d:decorate-patch-view`](https://docs.netlogo.org/view2.5d#decorate-patch-view)
[`view2.5d:undecorate-patch-view`](https://docs.netlogo.org/view2.5d#undecorate-patch-view)
[`view2.5d:turtle-view`](https://docs.netlogo.org/view2.5d#turtle-view)
[`view2.5d:update-all-patch-views`](https://docs.netlogo.org/view2.5d#update-all-patch-views)
[`view2.5d:update-patch-view`](https://docs.netlogo.org/view2.5d#update-patch-view)
[`view2.5d:update-turtle-view`](https://docs.netlogo.org/view2.5d#update-turtle-view)
[`view2.5d:get-z-scale`](https://docs.netlogo.org/view2.5d#get-z-scale)
[`view2.5d:set-z-scale`](https://docs.netlogo.org/view2.5d#set-z-scale)
[`view2.5d:set-turtle-stem-thickness`](https://docs.netlogo.org/view2.5d#set-turtle-stem-thickness)
[`view2.5d:set-turtle-stem-color`](https://docs.netlogo.org/view2.5d#set-turtle-stem-color)
[`view2.5d:show-links-xy-plane`](https://docs.netlogo.org/view2.5d#show-links-xy-plane)
[`view2.5d:show-links-xyz`](https://docs.netlogo.org/view2.5d#show-links-xyz)
[`view2.5d:get-observer-angles`](https://docs.netlogo.org/view2.5d#get-observer-angles)
[`view2.5d:set-observer-angles`](https://docs.netlogo.org/view2.5d#set-observer-angles)
[`view2.5d:get-observer-xy-focus`](https://docs.netlogo.org/view2.5d#get-observer-xy-focus)
[`view2.5d:set-observer-xy-focus`](https://docs.netlogo.org/view2.5d#set-observer-xy-focus)
[`view2.5d:get-observer-distance`](https://docs.netlogo.org/view2.5d#get-observer-distance)
[`view2.5d:set-observer-distance`](https://docs.netlogo.org/view2.5d#set-observer-distance)
[`view2.5d:remove-patch-view`](https://docs.netlogo.org/view2.5d#remove-patch-view)
[`view2.5d:remove-turtle-view`](https://docs.netlogo.org/view2.5d#remove-turtle-view)
[`view2.5d:remove-all-patch-views`](https://docs.netlogo.org/view2.5d#remove-all-patch-views)
[`view2.5d:remove-all-turtle-views`](https://docs.netlogo.org/view2.5d#remove-all-turtle-views)
[`view2.5d:count-windows`](https://docs.netlogo.org/view2.5d#count-windows)

### `view2.5d:patch-view`

```NetLogo
view2.5d:patch-view Title Reporter
```

This command must be called from the Observer context. (Attempting to call from another context causes an error)
The Title is a string, which will be used to label the new Window and to call for subsequent updates and modifications.
Specification of the Reporter uses the NetLogo arrow procedure syntax, from the Observer perspective.

Example:

```NetLogo
view2.5d:patch-view "Test"  [ [the-patch] -> [pxcor] of the-patch ]
```

### `view2.5d:decorate-patch-view`

```NetLogo
view2.5d:decorate-patch-view Title
```

This command must be called from the Observer context. (Attempting to call from another context causes an error)
The Title is a string, the label of an existing Patch View Window.
Effect: draws the turtles of the model at their current location, on top of the Patch view display

> NOTE: only has an effect in the "structures" patch view (in the others, the patch value is inclined based on neighbors & gradient)

> NOTE: for negative patch values, the turtle shapes are drawn below (orbit underneath to see them)

Example:

```NetLogo
view2.5d:decorate-patch-view "Test"
```

### `view2.5d:undecorate-patch-view`

```NetLogo
view2.5d:undecorate-patch-view Title
```

This command must be called from the Observer context. (Attempting to call from another context causes an error)

The Title is a string, the label of an existing Patch View Window.
Effect: STOPS drawing the turtles of the model at their current location, on top of the Patch view display

Example:

```NetLogo
view2.5d:undecorate-patch-view "Test"
```

### `view2.5d:turtle-view`

```NetLogo
view2.5d:turtle-view Title Agents Reporter
```

This command must be called from the Observer context. (Attempting to call from another context causes an error)
The Title is a string, which will be used to label the new Window and to call for subsequent updates.
The turtle-set is any selector for turtles.
Reporter is an arrow reporter that should take a turtle as input, and report some number from it.

Example:

```
view2.5d:turtle-view "Test" turtles with [color = red] [ the-turtle -> [energy] of the-turtle]
; This would create a new 2.5d window, plotting the ENERGY value of all turtles that are red.
```

### `view2.5d:update-all-patch-views`

```NetLogo
view2.5d:update-all-patch-views
```

This command must be called from the Observer context.
Updates all existing patch-view windows according to the latest values.

### `view2.5d:update-patch-view`

```NetLogo
view2.5d:update-patch-view Title
```

This command must be called from the Observer context.
Updates only the patch-view window with the specified title (if any).

### `view2.5d:update-turtle-view`

```NetLogo
view2.5d:update-turtle-view Title Agents
```

This command must be called from the Observer context.
Updates only the turtle-view window with the specified title (if any).
The turtle-set selector must be supplied to refresh the set of turtles.

### `view2.5d:get-z-scale`

```NetLogo
view2.5d:get-z-scale title
```

This reporter must be called from the Observer context.
Returns the current z-scale of the turtle-view or patch-view window with the specified title (if any).

### `view2.5d:set-z-scale`

```NetLogo
view2.5d:set-z-scale Title new-z-scale
```

This command must be called from the Observer context.
Updates only the turtle-view or patch-view window with the specified title (if any).
The view is now displayed with the new z-scale.

### `view2.5d:set-turtle-stem-thickness`

```NetLogo
view2.5d:set-turtle-stem-thickness Title thickness
```

This command must be called from the Observer context.
Updates only the turtle-view window with the specified title (if any).
Turtles are now drawn with "pins" or "stems" that have the specified thickness (instead of the hairline default).

### `view2.5d:set-turtle-stem-color`

```NetLogo
view2.5d:set-turtle-stem-color Title colorReporter
```

This command must be called from the Observer context.
Updates only the turtle-view window with the specified title (if any).
colorReporter is an arrow reporter that should take a turtle as input, and report some number representing a color from it.
Turtles are now drawn with "pins" or "stems" that have the specified color (instead of the grey default).

Example:

```
;; setup view with 2 turtles
crt 1 [ set color green  setxy 2 3]
crt 1 [ set color red  setxy 5 4]
view2.5d:turtle-view "Test" turtles [ the-turtle -> [ xcor] of the-turtle ]
view2.5d:set-observer-distance "Test" 40
view2.5d:set-z-scale "Test" 2
view2.5d:set-observer-angles "Test" 25 30

;; increase the stem thickness
view2.5d:set-turtle-stem-thickness "Test" .2

;; change the stem color to  match the turtle-color
view2.5d:set-turtle-stem-color "Test" [ the-turtle -> [ color ] of the-turtle ]

; now make the stems orange
view2.5d:set-turtle-stem-color "Test" [ orange ]
```

### `view2.5d:show-links-xy-plane`

```NetLogo
view2.5d:show-links-xy-plane Title
```

This command must be called from the Observer context.
Updates only the turtle-view or patch-view window with the specified title (if any).
Links are drawn in the xy-plane (instead of the turtle to turtle default).
This option can be selected using the Link Options xy-plane radio Button in the 3D view.

### `view2.5d:show-links-xyz`

```NetLogo
view2.5d:show-links-xyz Title
```

This command must be called from the Observer context.
Updates only the turtle-view or patch-view window with the specified title (if any).
Links are drawn from turtle to turtle (this is the default way links are displayed).
This option can be selected using the Link Options xyz radio Button in the 3D view.

### `view2.5d:get-observer-angles`

```NetLogo
view2.5d:get-observer-angles Title
```

This reporter must be called from the Observer context.
Returns a list reflecting the observer's angular perspective { heading pitch } (the place on an imaginary sphere at the zoom distance is updated to obey heading & pitch given)

### `view2.5d:set-observer-angles`

```NetLogo
view2.5d:set-observer-angles Title heading pitch
```

This command must be called from the Observer context.
Updates only the turtle-view window with the specified title (if any).
Sets the observer's angular perspective (the place on an imaginary sphere at the zoom distance is updated to obey heading & pitch given)

### `view2.5d:get-observer-xy-focus`

```NetLogo
view2.5d:get-observer-xy-focus Title
```

This reporter must be called from the Observer context.
Returns a list reflecting the x and y coordinates the observer is "looking at" in the patch plane.

### `view2.5d:set-observer-xy-focus`

```NetLogo
view2.5d:set-observer-xy-focus Title number ycor
```

This command must be called from the Observer context.
Updates only the turtle-view window with the specified title (if any).
Sets the x and y coordinates the observer that is "looking at" in the patch plane.

### `view2.5d:get-observer-distance`

```NetLogo
view2.5d:get-observer-distance Title
```

This reporter must be called from the Observer context.
Returns the observer's distance from its "focus point"

### `view2.5d:set-observer-distance`

```NetLogo
view2.5d:set-observer-distance Title new-distance
```

This command must be called from the Observer context.
Updates only the turtle-view window with the specified title (if any).
Sets the observer's distance from its "focus point"

### `view2.5d:remove-patch-view`

```NetLogo
view2.5d:remove-patch-view Title
```

This command closes and removes the specified patch view programmatically (equivalent to closing the window manually).

### `view2.5d:remove-turtle-view`

```NetLogo
view2.5d:remove-turtle-view Title
```

This command closes and removes the specified turtle view programmatically (equivalent to closing the window manually).

### `view2.5d:remove-all-patch-views`

```NetLogo
view2.5d:remove-all-patch-views
```

This command closes and removes all patch views programmatically (equivalent to closing the windows manually).

### `view2.5d:remove-all-turtle-views`

```NetLogo
view2.5d:remove-all-turtle-views
```

This command closes and removes all turtle views programmatically (equivalent to closing the windows manually).

### `view2.5d:count-windows`

```NetLogo
view2.5d:count-windows
```

This reporter returns the number of turtle and patch views that are currently active.

## Acknowledgement

For development of the View 2.5D NetLogo Extension, we acknowledge financial support from Joshua M. Epstein's NIH Director's Pioneer Award (DP1).
