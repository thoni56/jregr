JRegr - a Regression tester in Java
===================================

Very often you want to automatically run a program with certain inputs
and compare that to some known output.

There are some tools to do this already, like DejaGnu, Expect etc., but
for some reason or other these did not fit my purpose, or possibly
taste.

Synopsis
--------

In a configuration file per directory, `.jregr`, you specify a file
extension for what files are considered being test cases in this
directory, and the command(s) that should be performed on each of those
test cases.

The test case name is the basename of a file with matching extension.

The output of those commands (meaning everything that is sent to the
standard and/or error outputs) is considered the actual output of the
test case. This is compared to the expected output, which is the content
of a file with the same basename and extension `.expected`.

If they are identical the test case passed, if not, it failed.

Format of configuration file
----------------------------

The following is the simplest configuration file imaginable:

    .sh : sh $1.sh

It should be read like:

-   each test case is a file ending in `.sh`
-   for each such file in this directory: run `sh` on it

The general pattern is

    <extension> ':' <command> { <arg> } { <input_output_redirection> }

The elements are space separated, so e.g. you need a space between
`<extension>` and the `:`.

A `.jregr` file may consist of multiple lines. They will be run in
sequence, with each line having an `<extension>` which needs to be
matches, and its own command, arguments and optional redirection.

If there are multiple lines in the configuration file, the extension for
test cases is the extension on the first line.

The actual output of the test will be the total output of running all
lines.

Note1: You can often use relative paths in the command. Absolute paths
will of course not be portable. It is generally better to use the `-bin`
option to find executables that are not in the path.

Note2: Currently there is no way to use the `-bin` option more than
once, so if you need multiple executables they must exist in the same
directory (for now).

Input and output redirection
----------------------------

You can redirect input (mostly), but also output from a particular
command.

For input redirection, which is the most common case, if the `<` is
present, Jregr will use the next item as the standard input to the
`<command>`.

If the output is redirected using `>` the next item will be the name of
the file which will get the output of that line. Note that that output
will then *not* be part of the output of the test.

A special case is if the name of the output file is exactly `/dev/null`
in which case the output of that line will not be saved anywhere.

Configuration File Variables
----------------------------

There are some 'variables' available for use in the configuration file:

`$1` - the case name, which is always the basename of the file matching
the extension on the first line in the configuration file

`$2` - the complete file name for the file matching the extension on the
current line

So the shortest possible `.jregr` file is something like:

    .sh : sh $2

Since `$2` is the complete testname matched with the extension `.sh` it
will be the same as the test name with the extension concatenated at the
end.

(Except that 'sh' does not exist on all platforms...)

Limitations of commands
-----------------------

The command is executed with Javas `Runtime.exec()` so it must be
directly executable (e.g. shell scripts might be executable on some
platforms but are not on others) and it does not handle wildcards or
pipes. Although this might work it is not by design but an effect of how
Java on that particular OS happens to perform the `exec()` call. E.g.
with a Windows Java it won't work, but might on Linux et.al.

Running
-------

Navigate to a directory containing a `.jregr` configuration file and run

    java -jar jregr.jar

With no options or arguments, `jregr` will run all test cases and report
the progress on the standard output. If you have an ANSI capable
terminal (most are these days), passing tests will disappear from view,
while failing, suspended and ignored test cases will remain visible.
Finally `jregr` will give a summary of the number of tests run.

You can run a single test by just adding its full name as an argument.

For convenience there is a script, `jregr`, which finds the jar file in
the same directory as the script it self, so you can link to the script
and get the correct jar.

The script also handles the case where you are running `jregr` from a
cywin environment using your Windows java.

Options
-------

`-bin` find any executable files in this directory

`-dir` find testcases and expected output in this directory, also write
actual output there

`-xml` create Junit/Ant compatible `xml` files instead of output to
console (to collect in Jenkins etc.)

`-verbose` print also all passing test names on the console output
(which is done using ANSI control codes), normally they will be
overwritten to make output more compact

`-version` print the version of Jregr

Character Encodings
-------------------

Sometimes it might be important to preserve character encodings so that
the expected output can be matched correctly. There is no option for
this, instead use the Java VM option '-Dfile.encoding=<encoding>', like

    java -jar -Dfile.encoding=iso-8859-1 "$d"jregr.jar $@

Test case status
----------------

A test case (the basename of a file matching the extension applicable in
this directory) can be in one of a set of states:

-   `VIRGIN` - there is an expected output, but no output
-   `PENDING` - there is no expected output
-   `FATAL` - the commands for the test did not execute correctly
    (non-existing command, segmentation violation, ...)
-   `FAIL` - the output did not match the expected
-   `SUSPENDED` - the test case is suspended (there exists a file
    `<basename>.suspended`.

Suspended test cases that have expected output will still be run. They
are reported as "Suspended and failed or "Suspended and passed".

This is useful for test cases that run but produces the wrong output.
Adjust the output to what it should be, save that as the expected
output. Then suspend the case. When running all cases you will still get
green, but you can see what suspended test cases you have, and how they
are doing.

Recursion
---------

Jregr will automatically recurse into subdirectories.

-   Recursion will only happen if the subdirectory contains a `.jregr`
    file.

-   If the `.jregr` in the subdirectory is empty, Jregr will re-use the
    commands from the `.jregr` file in the directory above.

-   If a `-bin` option was given on the command line and it was
    relative, it will be adjusted accordingly.

-   There is no way to give specific `-bin` options for subdirectories
    so all required `-bin` directories need to be specified on the
    command line. (NYI - multiple `-bin` options)
