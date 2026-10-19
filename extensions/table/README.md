
# NetLogo Table Extension

## Building

Use the netlogo.jar.url environment variable to tell sbt which NetLogo.jar to compile against (defaults to NetLogo 6.0). For example:

    sbt -Dnetlogo.jar.url=file:///path/to/NetLogo/target/NetLogo.jar package

If compilation succeeds, `table.jar` will be created.

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

## Primitives

[`table:clear`](#tableclear)
[`table:counts`](#tablecounts)
[`table:group-agents`](#tablegroup-agents)
[`table:group-items`](#tablegroup-items)
[`table:from-list`](#tablefrom-list)
[`table:from-json`](#tablefrom-json)
[`table:from-json-file`](#tablefrom-json-file)
[`table:get`](#tableget)
[`table:get-or-default`](#tableget-or-default)
[`table:has-key?`](#tablehas-key?)
[`table:keys`](#tablekeys)
[`table:length`](#tablelength)
[`table:make`](#tablemake)
[`table:put`](#tableput)
[`table:remove`](#tableremove)
[`table:to-list`](#tableto-list)
[`table:to-json`](#tableto-json)
[`table:values`](#tablevalues)


### `table:clear`

```NetLogo
table:clear table
```

Removes all key-value pairs from *table*.


### `table:counts`

```NetLogo
table:counts list
```

Counts the occurrences of each element of the given list and reports the counts in a table.


### `table:group-agents`

```NetLogo
table:group-agents agentset arrow reporter
```


Groups the agents in the agentset based on the given reporter.
Agents that report the same thing for reporter will be grouped together.
The results of the reporter will be used as the keys in the resulting table and the groups will be agentsets.

For example:


```
observer> create-turtles 100 [ set color one-of [ red green blue ] ]
observer> show table:group-agents turtles [ color ]
observer: {{table: [[105 (agentset, 38 turtles)] [55 (agentset, 32 turtles)] [15 (agentset, 30 turtles)]]}}
```



### `table:group-items`

```NetLogo
table:group-items list arrow-reporter
```


Groups the items of the list based on the given reporter.
The reporter should take a single argument, which will be the items of the list.
Items that report the same thing when passed to the reporter will be grouped together.
The results of the reporter will be used as the keys in the resulting table and the groups will be lists.

For example:


```
observer> show table:group-items range 10 [ num -> num mod 3 ]
observer: {{table: [[0 [0 3 6 9]] [1 [1 4 7]] [2 [2 5 8]]]}}
```



### `table:from-list`

```NetLogo
table:from-list list
```


Reports a new table with the contents of *list*.
*list* must be a list of two element lists, or pairs.
The first element in the pair is the key and the second element is the value.



### `table:from-json`

```NetLogo
table:from-json string
```


    Reads in the JSON object from the given string argument, converts it to a table, and reports the table.
    The JSON object in the argument must be an object, not an array (i.e. the outmost characters must be `{` and `}`, not `[` and `]`),
    but there can be arrays inside the JSON object which will be converted to NetLogo lists.



### `table:from-json-file`

```NetLogo
table:from-json-file filename
```


    Reads in the JSON object from *filename.json*, converts it to a table and reports the table.
    The JSON object in *filename.json* must be an object, not an array (i.e. the outmost characters of the file must be `{` and `}`, not `[` and `]`),
    but there can be arrays inside the JSON object which will be converted to NetLogo lists.



### `table:get`

```NetLogo
table:get table key
```

Reports the value that *key* is mapped to in the table. Causes an error if there is no entry for the key.


### `table:get-or-default`

```NetLogo
table:get-or-default table key default-value
```

Reports the value that *key* is mapped to in the table. Reports the default-value if there is no entry for the key.


### `table:has-key?`

```NetLogo
table:has-key? table key
```

Reports true if *key* has an entry in *table*.


### `table:keys`

```NetLogo
table:keys table
```

Reports a list of all the keys in *table*, in the same order the keys were inserted.


### `table:length`

```NetLogo
table:length table
```

Reports the number of entries in *table*.


### `table:make`

```NetLogo
table:make
```

Reports a new, empty table.


### `table:put`

```NetLogo
table:put table key value
```

Maps *key* to *value* in *table*. If an entry already exists in the table for the given key, it is replaced.


### `table:remove`

```NetLogo
table:remove table key
```

Removes the mapping in *table* for *key*.


### `table:to-list`

```NetLogo
table:to-list table
```


Reports a list with the content of <i>table</i>. The list will be a
list of two element lists, or pairs. The first element in the pair is
the key and the second element is the value. The keys appear in the
same order they were inserted.


### `table:to-json`

```NetLogo
table:to-json table
```

Reports a string with the contents of the given table as a JSON string.


### `table:values`

```NetLogo
table:values table
```


Reports a list with the entries of <i>table</i>. The entries will appear
in the same order they were inserted, with duplicates included.


## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo table extension is in the public domain.  To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.
