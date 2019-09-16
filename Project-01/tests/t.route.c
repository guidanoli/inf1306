/*
 * t.route.c, v.0.1.0
 *
 * Route test
 */

#include <stdio.h>
#include "parser.h"
#include "route.h"
#include "lwct.h"

void testRoute(lwct_state *S)
{
	struct instance_t *instance = parse_gvrp_instance();
	lwct_submit_desconstructor(S, free_gvrp_instance, instance);
	lwct_fatal_assert(S, instance);
}

int main(void)
{
	lwct_submit_test(testRoute);
}
