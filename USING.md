## Using

The table extension is pre-installed in NetLogo.

To use the table extension in your model, add a line to the top of your Code tab:

```
extensions [table]
```

If your model already uses other extensions, then it already has an
`extensions` line in it, so just add `table` to the list.

For more information on using NetLogo extensions,
see the [Extensions Guide](http://ccl.northwestern.edu/netlogo/docs/extensions.html)

### When to Use

In general, anything you can do with an table in NetLogo, you could
also just use a list for. But you may want to consider using an table
instead for speed reasons. Lists and tables have different performance
 characteristics, so you may be able to make your model run faster by
selecting the appropriate data structure.

Tables are useful when you need to do associate values with other
values. For example, you might make a table of words and their
definitions. Then you can look up the definition of any word. Here,
the words are the &quot;keys&quot;. You can easily retrieve the value
for any key in the table, but not vice versa.

Unlike NetLogo's lists and strings, tables are
"mutable". That means that you can actually modify them
directly, rather than constructing an altered copy as with lists. If
the table is used in more than one place in your code, any
changes you make will show up everywhere. It's tricky to write
code involving mutable structures and it's easy to make subtle
errors or get surprising results, so we suggest sticking with lists
and strings unless you're certain you want and need mutability.

### Example

{{! don't turn extension object into template variable }}{{= | | =}}
```NetLogo
let dict table:make
table:put dict "turtle" "cute"
table:put dict "bunny" "cutest"
print dict
=> {{table: "turtle" -> "cute", "bunny" -> "cutest" }}
print table:length dict
=> 2
print table:get dict "turtle"
=> "cute"
print table:get dict "leopard"
=> (error)
print table:keys dict
=> ["turtle" "bunny"]
```

### Manipulating Tables

If the same key is used with `table:put` more than once for the same table, the value provided to *last* call of `table:put` will be the value shown when `table:get` is used.
Here is an example:

```NetLogo
let dict table:make
table:put dict "a" 5
table:put dict "a" 3
print table:get dict "a"
=> 3
```

Because tables are mutable, manipulating existing values should be done by calling `table:get` or `table:get-or-default` on a key, transforming the returned value, and then calling `table:put` to update the transformed value in the table.
Here is an example procedure which increments a value in a table at a given key.
If the key doesn't exist, it puts a 1 at that key instead.

```NetLogo
to increment-table-value [ dict key ]
  let i table:get-or-default dict key 0
  table:put dict key i + 1
end
```

As `increment-table-value` shows, when a table is given as an input for a procedure, modifications made to it with `table:put`, `table:remove`, or `table:clear` are reflected in the original value outside of the procedure.  This is different behavior than with list values, which are immutable and so cannot be changed when given as inputs.  Caution needs to be exercised when using `let` or `set`, as they can give a different variable name to the same table.  If you change the value for a key in the table using one variable, any other variables assigned that same table will also reflect the change.

```NetLogo
let dict table:make
table:put dict "a" 5
let alt dict
table:put alt "a" 3
print table:get dict "a"
=> 3 ; changed because `dict` and `alt` refer to the same table
print table:get alt "a"
=> 3
```

If you want to create a copy of a table instead of assigning the same table to multiple variable names, here is a simple reporter for creating a duplicate table from an existing one:

```NetLogo
to-report copy-table [ orig ]
  let copy table:make
  foreach ( table:keys orig ) [
    [key] -> table:put copy key ( table:get orig key )
  ]
  report copy
end
```

And here is a sample usage of the `copy-table` reporter:

```NetLogo
let dict table:make
table:put dict "a" 5
let alt copy-table dict
table:put alt "a" 3
print table:get dict "a"
=> 5 ; `dict` is not changed because we created a new table copy for `alt`
print table:get alt "a"
=> 3
```

### Key Restrictions

Table keys are limited to the following NetLogo types:

* Numbers
* Strings
* Booleans
* Lists containing only elements which are themselves valid keys

If you attempt to use an illegal value, the table extension will raise an exception, as shown in the following example.

```NetLogo
crt 1
let dict table:make
table:put dict (one-of turtles) 10
;; Errors with the following message:
;; (turtle 0) is not a valid table key (a table key may only be a number, a string, true or false, or a list whose items are valid keys)
```
