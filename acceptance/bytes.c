#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Emits exact bytes, so a test can pin down what a program actually wrote
   without a locale, a shell or a printf format getting an opinion first.

   bytes E4 0A        writes the bytes 0xE4 0x0A to stdout
   bytes -stdin       copies stdin to stdout one byte at a time

   Used by the character set acceptance cases: jregr compares what a
   program emitted, so the fixtures have to be able to state exactly what
   that was. */
int main(int argc, char **argv) {
    if (argc > 1 && strcmp(argv[1], "-stdin") == 0) {
        int c;
        while ((c = getchar()) != EOF)
            putchar(c);
    } else {
        for (int i = 1; i < argc; i++) {
            unsigned char byte = (unsigned char) strtoul(argv[i], NULL, 16);
            fwrite(&byte, 1, 1, stdout);
        }
    }
    return 0;
}
