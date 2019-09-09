/*
 * utils.c, v.0.1.0
 *
 * Utility functions for GVRP
 */

#include "utils.h"
#include <math.h>

unsigned int e2d_dist(struct node_t *a, struct node_t *b)
{
        unsigned int dx = a->x - b->x;
        unsigned int dy = a->y - b->y;
        float dist = sqrt( (float)(dx * dx + dy * dy) );
        unsigned int idist = (unsigned int) dist;
        return (dist - idist) < 0.5f ? dist : dist + 1;
}
