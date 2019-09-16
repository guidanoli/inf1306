/*
 * t.route.c, v.0.1.0
 *
 * Route test
 */

#include <stdio.h>
#include "parser.h"
#include "route.h"
#include "lwct.h"

#define NULL ((void *) 0)

void testNull(lwct_state *S)
{
	free_route(NULL);
	lwct_assert(S, 1); // should not halt
}

void testRoute(lwct_state *S)
{
	struct instance_t *instance = parse_gvrp_instance();
	lwct_submit_desconstructor(S, free_gvrp_instance, instance);
	lwct_fatal_assert(S, instance);
	
	struct route_t *route = create_route();
	lwct_submit_desconstructor(S, free_route, route);
	lwct_fatal_assert(S, route);
	
	for (int i = 0; i < instance->customer_cnt; i++) {
		route_ret ret = add_customer(route, &(instance->customers[i]));
		lwct_assert(S, ret == ROUTE_OK);
	}
	
	for (int i = 0; i < instance->customer_cnt; i++) {
		route_ret ret = remove_customer(route, &(instance->customers[i]));
		lwct_assert(S, ret == ROUTE_OK);
	}
}

int main(void)
{
	lwct_submit_test(testNull);
	lwct_submit_test(testRoute);
}
