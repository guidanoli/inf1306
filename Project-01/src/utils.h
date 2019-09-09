/*
 * utils.h, v.0.1.0
 *
 * Utility functions for GVRP
 */

#ifndef _GVRP_UTILS_H_
#define _GVRP_UTILS_H_

#include "parser.h"

/*
 * Euclidian distance between two points in two dimensions
 * Rounds to nearest integer (may lead to triangular inequality)
 */
unsigned int e2d_dist(struct node_t *a, struct node_t *b);

#endif
