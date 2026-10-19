
# NetLogo Python extension

This NetLogo extension allows you to run Python code from NetLogo. It works with both Python 2 and 3, and should work with almost all Python libraries.

## Building

Make sure your sbt is at least at version 0.13.6

Run `sbt package`.

If compilation succeeds, `py.jar` will be created and the required dependencies will be copied to the root of the repository.  Copy all the `jar` files and `pyext.py` from the repository root to a `py` directory inside your NetLogo `extensions` directory.

Run `sbt test` to run the NetLogo language test code from `tests.txt`.

## Using

As with all NetLogo extensions, you must declare that you're using this extension in your NetLogo code with:

```netlogo
extensions [
  py
  ; ... your other extensions
]
```

The general workflow of this extension is to run `py:setup py:python` to initialize the Python session that NetLogo will talk to, and then use `py:run`, `py:runresult`, and `py:set` to interact with that Python session.
By default, `py:python` will report the latest version of Python that the extension finds on your system.
You can also use `py:python3` or `py:python2` to use Python 3 or 2 specifically.
See the [Configuring](#configuring) section below to specify exactly which Python installations to use.

Here's an example to get you started:

```netlogo
observer> py:setup py:python
observer> show py:runresult "1 + 1"
observer: 2
observer> py:run "print('hi')"
hi
observer> py:run "import math"
observer> show py:runresult "[math.factorial(i) for i in range(10)]"
observer: [1 1 2 6 24 120 720 5040 40320 362880]
observer> py:set "patch_xs" [ pxcor ] of patches
observer> show py:runresult "max(patch_xs)"
observer: 16
observer> py:run "print(min(patch_xs))"
-16
```

See the documentation for each of the particular primitives for details on, for instance, how to multi-line statements and how object type conversions work.
See the demo models included in the `demo` folder for some examples of using libraries such as `numpy` and `tensorflow`.

### Error handling

Python errors will be reported in NetLogo as "Extension exceptions". For instance, this code:

```netlogo
py:run "raise Exception('hi')"
```

will result in the NetLogo error "Extension exception: hi".
To see the Python stack trace of the exception, click "Show internal details".
If you then scroll down, you will find the Python stack trace in the middle of the Java stack trace.

## Configuring

By default, the `py:python2`, `py:python3`, and `py:python` commands will attempt to find a Python executable of the appropriate version.
If you'd like to change which Python executable they use, or they can't find a Python executable, you should configure which Python executables to use.
You can do this by either:

- Using the configuration menu under the Python toolbar menu that appears when you use a model that uses the Python extension.
- Editing the `python.properties` file that appears in the Python extension installation folder as follows:

```
python3=/path/to/python3
python2=/path/to/python2
```


## Primitives


### `py:setup`

```NetLogo
py:setup python-executable
```


Create the Python session that this extension will use to execute code. The session will be started with the given Python executable. This command *must* be run before running any other Python extension primitive. Running this command again will shutdown the current Python environment and start a new one.

 The executable may be specified as a relative path, absolute path, or just the executable name if it is on your PATH.
 Furthermore, this extension offers a few helper primitives for getting particular versions of Python in system
 independent ways.

 In general, unless working with a virtual environment or a specific system setup, you should do:

 ```NetLogo
 py:setup py:python  ; if your code works with either Python 2 or 3
 py:setup py:python3 ; for Python 3
 py:setup py:python2 ; for Python 2
 ```

`py:setup` may be invoked by directly referring to different Pythons as well. For instance:

```NetLogo
py:setup "python3" ; if `python3` is on your PATH
py:setup "python"  ; if `python` is on your PATH
```

If you use virtualenv or Conda, simply specify the path of the Python executable in the environment you wish to use:

```NetLogo
py:setup "/path/to/myenv/bin/python"
```

The path may be relative or absolute. So, if you have a virtual environment in the same folder as your model, you can do:

```NetLogo
py:setup "myenv/bin/python"
```



### `py:python`

```NetLogo
py:python
```


Reports either the path to the latest version of Python configured in the `python.properties` file or, if that is blank, looks for a Python executable on your system's PATH.
For Windows, there is an installation option for including Python on your PATH.
For MacOS and Linux, it will likely already be on your PATH.
The output of this reporter is meant to be used with `py:setup`, but you may also use it to see which Python installation this extension will use by default.

For example, on MacOS with Homebrew installed Python 3:
```NetLogo
observer> show py:python
observer: "/usr/local/bin/python3"
```



### `py:python2`

```NetLogo
py:python2
```


Reports either the path to Python 2 configured in the `python.properties` file or, if that is blank, looks for a Python 2 executable on your system's PATH.
For Windows, there is an installation option for including Python on your PATH.
For MacOS and Linux, it will likely already be on your PATH.
The output of this reporter is meant to be used with `py:setup`, but you may also use it to see which Python 2 installation this extension will use by default.

For example, on MacOS with Homebrew installed Python 2:
```NetLogo
observer> show py:python2
observer: "/usr/local/bin/python2"
```



### `py:python3`

```NetLogo
py:python3
```


Reports either the path to Python 3 configured in the `python.properties` file or, if that is blank, looks for a Python 3 executable on your system's PATH.
For Windows, there is an installation option for including Python on your PATH.
For MacOS and Linux, it will likely already be on your PATH.
The output of this reporter is meant to be used with `py:setup`, but you may also use it to see which Python 3 installation this extension will use by default.

For example, on MacOS with Homebrew installed Python 3:
```NetLogo
observer> show py:python3
observer: "/usr/local/bin/python3"
```



### `py:run`

```NetLogo
py:run python-statement
```


Runs the given Python statements in the current Python session. To make multi-line Python code easier to run, this command will take multiple strings, each of which will be interpreted as a separate line of Python code. For instance:

```NetLogo
(py:run
  "import matplotlib"
  "matplotlib.use('TkAgg')"
  "import numpy as np"
  "import matplotlib.pyplot as plt"
  "for i in range(10):"
  "    plt.plot([ x ** i for x in np.arange(-1, 1, 0.1) ])"
  "plt.show()"
)
```

`py:run` will wait for the statements to finish running before continuing. Thus, if you have long running Python code, NetLogo will pause while it runs.



### `py:runresult`

```NetLogo
py:runresult python-expression
```


Evaluates the given Python expression and reports the result.
`py:runresult` attempts to convert from Python data types to NetLogo data types.
Numbers, strings, and booleans convert as you would expect.
Any list-like object in Python (that is, anything with a length that you can iterate through) will be converted to a NetLogo list.
For instance, Python lists and NumPy arrays will convert to NetLogo lists.
Python dicts (and dict-like objects) will convert to a NetLogo list of key-value pairs (where each pair is represented as a list).
`None` will be converted to `nobody`.
Other objects will simply be converted to a string representation.

Note that due a [current issue](https://github.com/qiemem/PythonExtension/issues/6), dict keys will always be reported as strings.
If you need to report non-string keys, report the `.items()` of the dict instead of the dict itself.



### `py:set`

```NetLogo
py:set variable-name value
```


Sets a variable in the Python session with the given name to the given NetLogo value. NetLogo objects will be converted to Python objects as expected.

```NetLogo
py:set "x" [1 2 3]
show py:runresult "x" ;; Shows [1 2 3]
```

Agents are converted into dictionaries with elements for each agent variable. Agentsets are converted into lists of agent dictionaries.

```NetLogo
breed [goats goat]
goats-own [energy ]
create-goats 1 [ set heading 0 set color 75 ]
ask goat 0 [ set energy 42 ]
py:set "goat" goat 0
py:runresult "str(goat)" ;; Should output: "{'WHO': 0, 'COLOR': 75, 'HEADING': 0, 'XCOR': 0, 'YCOR': 0, 'SHAPE': 'default', 'LABEL': '', 'LABEL-COLOR': 9.9, 'BREED': 'GOATS', 'HIDDEN?': False, 'SIZE': 1, 'PEN-SIZE': 1, 'PEN-MODE': 'up', 'ENERGY': 42}"
```

Agents with variables containing references to agentsets will have those variables converted into the string representation of that agentset.



