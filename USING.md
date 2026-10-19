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

See the documentation on `py:set` to learn how to have the extension serialize entire agents and agentsets into Python dictionaries.

There is also a separate interactive Python console that can be found under Python > Interactive Python Console.
This console is connected to the same Python session as all the Python NetLogo primitives, so you can define a variable in your model and access it in the interactive Python console window.

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
