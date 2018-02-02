#include <stdio.h>

int main(int argc, char **argv) {
  int i;
  char buf[1000];
  int *p;

  while (fgets(buf, 1000, stdin) != NULL)
    i++;

  if (i != 1000)
    *p = 5; /* Segment violation!! */
}
