## Common use cases and examples

### Read a file all at once

Just use `csv:from-file "/path/to/myfile.csv"`! See [from-file](#csvfrom-file) for more information.

### Read a file one line at a time

For really big files, you may not want to store the entire file in memory, but rather just process it a line at a time. For instance, if you want to sum each of the columns of a numeric CSV file, you can do:

```NetLogo
to-report sum-columns [ file ]
  file-open file
  set result csv:from-row file-read-line
  while [ not file-at-end? ] [
    let row csv:from-row file-read-line
    set result (map [ [ [col-total new-val] -> col-total + new-val] result row)
  ]
  file-close
  report result
end
```

You can also use this technique to...

### Read a file one line per tick

Here's an example model that reads in a file one line per tick:

```NetLogo
globals [ data ]

to setup
  clear-all
  file-close-all % Close any files open from last run
  file-open "data.csv"
  % other setup goes here
  reset-ticks
end

to go
  if file-at-end? [ stop ]
  set data csv:from-row file-read-line
  % model update goes here
  tick
end
```

### Write a file

Just use `csv:to-file "/path/to/myfile.csv" my-data`! See [to-file](#csvto-file) for more information.
