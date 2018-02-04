JRegr - a Regression tester in Java
===================================

Very often you want to automatically run a program with certain inputs
and compare that to some known output.

There are some tools to do this already, like DejaGnu, Expect etc., but
for some reason or other these did not fit my purpose, or possibly
taste.

Synopsis
--------

In a configuration file per directory, `.jregr`, you specify a pattern
for what is considered a test case, and the command(s) that should be
performed on each of the test cases.

The output (meaning everything that is sent to the standard and/or
error outputs) is considered the actual output of the test case. This
is compared to the content of a file with the same basename ($1) and
extension `.expected`.

If they are identical the test case passed, if not it failed.

Format of configuration file
----------------------------

Possibly, the following is the simplest configuration file
imaginable:

    .sh: sh $1.sh

It should be read like:

-   each test case is a file ending in `.sh`
-   for each such file in this directory: run `sh` on it

The pattern is

    <extension> ':' <command> [ '<' <input> ]

If the `<` is present, Jregr will try to find a file as specified
after it and use that as the standard input to the `<command>`.  It is
possible to have multiple lines, which will be run in sequence, with
each line having an <extension>, a <command> and an optional <input>.

The actual output will be the total output of running all lines.


Configuration File Variables
----------------------------

There are some 'variables' available for use in the configuration
file:

`$1` - the case name, which is always the basename of the file matching
the extension on the first line in the configuration file

`$2` - the complete file name for the file matching the extension on
the current line

Running
-------

Navigate to a directory containing a `.jregr` configuration file and run
`jregr`. With no options or arguments, `jregr` will run all test cases
and report the progress on the standard output. Passing tests will
disappear from view, while failing, suspended and ignored test cases
will remain. Finally `jregr` will give a summary.

Options
-------

`-bin` find any executable files in this directory

`-dir` find testcases and expected output in this directory, also write
actual output there

`-xml` create Junit/Ant compatible `xml`files instead of output to console
(collect in Jenkins etc.)

`-noansi` don't use ansi codes to erase passing tests from console
output, so will show every test case as it runs
