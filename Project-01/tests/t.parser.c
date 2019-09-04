/*
 * t.parser.c, v.0.2
 *
 * Parser test
 */

#include "parser.h"
#include "lwct.h"

char *filename;

void testParser(lwct_state *S)
{
        struct instance_t *instance = parse_gvrp_instance(filename);
        lwct_fatal_assert(S, instance);
}

/*
 * Parameter:
 *      [1] instance path
 */
int main(int argc, char **argv)
{
        filename = argv[1];
        lwct_submit_test(testParser);
}
