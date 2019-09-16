/*
 * route.h, v.0.1.0
 * 
 * GVRP route
 * 
 * A route starts and ends in the depot, therefore, it is implicit.
 * If a route would be formely described as (d, v1, v2, ..., vn, d)
 * In the actual data structure representation, it would be
 * actually implemented contaning only the vertices 1 to n.
 * 
 * That is why add_customer only accepts customers, and not nodes.
 * Not only because of that, but also because only customers contain
 * useful information for local searches.
 */

#ifndef _GVRP_ROUTE_H_
#define _GVRP_ROUTE_H_

#include "lwct_sll.h"
#include "parser.h"

typedef enum {
	
	ROUTE_OK,
	/* Ok */
	
	ROUTE_PARAM,
	/* Parameter is invalid */
	
	ROUTE_MEM,
	/* Not enough memory */
	
	ROUTE_UNEXPECTED,
	/* Unexpected error */
	
	ROUTE_CUSTOMER_NOT_FOUND,
	/* Customer not found in route */
	
} route_ret;

/*
 * Route data
 * @list	- list of nodes visited in order
 */
struct route_t {
	lwct_sll *list;
};

/*
 * Constructs an empty route
 * > route or (void *) 0 on error
 */
struct route_t *create_route();

/*
 * Property deallocates route data structure
 * and its fields, avoiding memory leak.
 */
void free_route(struct route_t *route);

/*
 * Adds a customer to a route
 * > OK, PARAM, MEM, UNEXPECTED
 */
route_ret add_customer(struct route_t *route, struct customer_t *customer);

/*
 * Removes a customer from a route
 */
route_ret remove_customer(struct route_t *route, struct customer_t *customer);

#endif