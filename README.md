# JRegr - a Regression tester in Java

Very often you want to automatically run a program with certain inputs
and compare that to some known output.

There are some tools to do this already, like DejaGnu, Expect etc., but
for some reason or other these did not fit my purpose, or possibly
taste.

## Synopsis

In a configuration file per directory, `.jregr`, you specify a pattern
for what is considered a test case, and the command(s) that should be
performed on each of the test cases.

The output (meaning everything that is sent to the standard and/or
error outputs) is considered the actual output of the test case. This
is compared to the content of a file with the same basename ($1) and
extension `.expected`.

If they are identical the test case passed, if not it failed.

## Format of configuration file

The following is the simplest configuration file imaginable:

    .sh : sh $1.sh

It should be read like:

-   each test case is a file ending in `.sh`
-   for each such file in this directory: run `sh` on it

The general pattern is

    <extension> ':' <command> [ '<' <input> ]

The elements are space separated, so e.g. you need a space between
`<extension>` and the `:`.

If the `<` is present, Jregr will try to find a filename after it and
use that as the standard input to the `<command>`.  It is possible to
have multiple lines, which will be run in sequence, with each line
having an `<extension>`, a `<command>` and an optional `<input>`.

The actual output will be the total output of running all lines.

Note1: you should avoid using paths in the command. Better to use the `-bin` option to find executables that are not in the path.

Note2: currently there is no way to use the `-bin` option more than once, so if you need multiple executables they must exist in the same directory (for now).


## Configuration File Variables

There are some 'variables' available for use in the configuration
file:

`$1` - the case name, which is always the basename of the file matching
the extension on the first line in the configuration file

`$2` - the complete file name for the file matching the extension on
the current line

So actually the shortest possible `.jregr` file is something like:

    .sh : sh $2
    
Since `$2` is the complete testname matched with the extension `.sh`
it will be the same as the testname with the extension concatenated at
the end.

(Except that 'sh' does not exist on all platforms...)


## Limitations of commands

The command is executed with Javas `Runtime.exec()` so it must be
directly executable (e.g. shell scripts might be executable on some platforms but
are not on others) and it does not handle wildcards or pipes. This might
still work but is not by design but an effect of how Java on that OS
happens to perform the `exec()` call. E.g. with a Windows Java it won't
work, but might on Linux et.al.


## Running

Navigate to a directory containing a `.jregr` configuration file and run

    java -jar jregr.jar

With no options or arguments, `jregr` will run all test cases and
report the progress on the standard output. If you have an ANSI
capable terminal (most are these days), passing tests will disappear
from view, while failing, suspended and ignored test cases will remain
visible. Finally `jregr` will give a summary of the number of tests
run.

You can run a single test by just adding its full name as an argument.

For convenience the execution is wrapped inside a script, `jregr`,
which is something like this

    #! /bin/bash
    d=`dirname "$0"`
    uname=`uname -a`
    if [[ "$uname" == *[Cc]ygwin* ]]; then
      d=`cygpath -d "$d"`\\;
    else
      d=$d/;
    fi
    java -jar "$d"jregr.jar $@
    exit

This script also handles the case where you are running `jregr` from a cywin
environment using your Windows java.


## Options

`-bin` find any executable files in this directory

`-dir` find testcases and expected output in this directory, also write
actual output there

`-xml` create Junit/Ant compatible `xml` files instead of output to console
(to collect in Jenkins etc.)

`-noansi` don't overwrite passing test names from the console output (which
is done using ANSI control codes), so will show the name of every test case
as it runs


## Character Encodings

Sometimes it might be important to preserve character encodings so that the
expected output can be matched correctly. There is no option for this, instead
use the Java VM option '-Dfile.encoding=<encoding>', like

    java -jar -Dfile.encoding=iso-8859-1 "$d"jregr.jar $@

    
# TODO

- Allow empty .jregr files
- Recurse through directories (-r option or default)
- An empty .jregr file should signify the re-use/inherited rules from above directory
- Ensure -bin directory is always given relative to the current directory - will force adding directory parts while recursing