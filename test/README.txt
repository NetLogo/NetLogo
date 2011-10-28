RUNNING TESTS

* To run all integration tests including these tests, start sbt then do:
    test
  To run all command or reporter or model tests:
    tc
    tr
    tm
  You can run a single group of tests with:
    tc InCone 
    tr Numbers
    tm Fire
  for any of the files in the test/commands, test/reporters,
  and models/test directories

ALL TESTS

* When you use =>, the value on the right (the expected value of
  the reporter on the left), must be a constant: a number, string,
  list, true/false, or nobody.

* Tests that end with _2D will be run in a 2D environment only,
  tests that end with _3D will be run in a 3D environment only
  all other tests will be run in both environments.

* Tests that start with Generator will only be run when the 
  code generator is enabled
  tests that start with NoGenerator will only be run when the 
  code generator is disabled.

* When you write tests that write files you should always use a unique file name
  see AgentsetImport or File for examples.

REPORTER TESTS

* Keep them simple -- don't use any of the special command-test
  features described below.

* Nothing in reporter tests does anything with agents or agentsets.  If you
  want to test agent stuff, use command-tests, not reporter-tests.

* Nothing in reporter tests does anything involving randomness, since there's
  no way to set the random seed.  Use command-tests.

COMMAND TESTS

* You can define a procedure as part of a test.

* Global variables glob1, glob2, and glob3 are available for use,
  plus a turtle variable tvar and a patch variable pvar.

* Breeds mice and frogs are available.  Both breeds have an "age"
  variable.  Mice also have "fur".  Frogs also have "spots".
  Also available are two link breeds, directed-links (with variable
  "lvar") and undirected-links (with variable "weight").

* Two plots are available, plot1 and plot2.  Both have two
  permanent pens named pen1 and pen2.

* You can add an "extensions" declaration if you need to test
  an extension.  see e.g. TableExtension.txt

* Command tests are run two times:
    - once normally
    - once with each command wrapped in "run"
  If the test should only be run normally, then begin the name with
  an asterisk, e.g. *MyTest

* Always set the random seed if you do anything involving randomness
  (note that includes some primitives you may not expect, such as
  crt, sprout, max-one-of, et al)

MODEL TESTS

* you can open a model at the start using OPEN>. see models/test
  for examples.  by convention, we don't use OPEN> in test/reporters
  or test/commands, only models/test.

* if the test uses a model that isn't used anywhere else, it can go
  next to the test in the models/test directory.

STACK TRACE TESTS

* It's now possible to test NetLogo stack traces from Reporter tests
  and Command tests. The easiest way to learn how is by example. Here
  are examples of both:

*command-task-in-command-task-stack-trace
  O> run task [run task [print __boom]] => STACKTRACE boom!\
  error while observer running __BOOM\
    called by (command task from: procedure __EVALUATOR)\
    called by (command task from: procedure __EVALUATOR)\
    called by procedure __EVALUATOR

*Xor7
  __boom xor false => STACKTRACE boom!\
  error while observer running __BOOM\
    called by procedure __EVALUATOR

  notice the * at the start of the test name. this is to prevent it from
  running in 'run' mode. in run mode, the stack trace is slightly different
  since the top level procedure is run instead of __EVALUATOR.
  its ok that these tests don't run in 'run' mode because we are only testing
  the stack traces, not the results.

