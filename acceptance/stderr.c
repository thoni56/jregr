#include <stdio.h>

int main(int argc, char **argv) {
    for (int i=1; i<argc; i++)
        fprintf(stderr, "%s\n", argv[i]);
}
