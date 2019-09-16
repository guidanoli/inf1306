/*
 * route.c, v.0.2.0
 * 
 * GVRP route
 */

#include <stdlib.h>
#include "route.h"

struct route_t *create_route()
{
	struct route_t *route = malloc(sizeof(struct route_t));
	if (!route)
		return NULL;
	
	if (lwct_sll_create(&(route->list)) != LWCTL_SLL_OK) {
		free_route(route);
		return NULL;
	}
	
	return route;
}

void free_route(struct route_t *route)
{
	if (!route) return;
	
	if (route->list)
		lwct_sll_destroy(route->list);
	
	free(route);
}

route_ret add_customer(struct route_t *route, struct customer_t *customer)
{
	if (!route || !customer)
		return ROUTE_PARAM;
	
	switch (lwct_sll_insert(route->list, customer, NULL)) {
		case LWCTL_SLL_OK:
			return ROUTE_OK;
		case LWCTL_SLL_MEM:
			return ROUTE_MEM;
		default:
			return ROUTE_UNEXPECTED;
	}
}

route_ret remove_customer(struct route_t *route, struct customer_t *customer)
{
	if (!route || !customer)
		return ROUTE_PARAM;
	
	/* Searches for customer in route and, if found, makes it current */
	switch(lwct_sll_search(route->list, customer)) {
		case LWCTL_SLL_CONTAINS:
			break;
		case LWCTL_SLL_DOES_NOT_CONTAIN:
			return ROUTE_CUSTOMER_NOT_FOUND;
		default:
			return ROUTE_UNEXPECTED;
	}
	
	/* The customer pointer will be overriden but only locally */
	switch (lwct_sll_remove(route->list, &customer)) {
		case LWCTL_SLL_OK:
			return ROUTE_OK;
		case LWCTL_SLL_MEM:
			return ROUTE_MEM;
		default:
			return ROUTE_UNEXPECTED;
	}
}