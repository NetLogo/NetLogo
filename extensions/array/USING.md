## Using

The array extension is pre-installed in NetLogo.

To use the array extension in your model, add a line to the top of your Code tab:

```
extensions [array]
```

If your model already uses other extensions, then it already has an
`extensions` line in it, so just add `array` to the list.

For more information on using NetLogo extensions,
see the [Extensions Guide](https://docs.netlogo.org/extensions.html)

### When to Use

In general, anything you can do with an array in NetLogo, you could
also just use a list for. But you may want to consider using an array
instead for speed reasons. Lists and arrays have different performance
 characteristics, so you may be able to make your model run faster by
selecting the appropriate data structure.

Arrays are useful when you need a collection of values whose size is
fixed. You can quickly access or alter any item in an array if you
know its position.

Unlike NetLogo's lists and strings, arrays are "mutable".
That means that you can actually modify them directly,
rather than constructing an altered copy as with lists. If
the array is used in more than one place in your code, any
changes you make will show up everywhere. It's tricky to write
code involving mutable structures and it's easy to make subtle
errors or get surprising results, so we suggest sticking with lists
and strings unless you're certain you want and need mutability.

### Example use of Array Extension
{{! prevent mustache escaping}}{{={| |}=}}
```NetLogo
let a array:from-list n-values 5 [0]
print a
=> {{array: 0 0 0 0 0}}
print array:length a
=> 5
foreach n-values 5 [ i -> i ] [ i -> array:set a i i * i ]
print a
=> {{array: 0 1 4 9 16}}
print array:item a 0
=> 0
print array:item a 3
=> 9
array:set a 3 50
print a
=> {{array: 0 1 4 50 16}}
```
