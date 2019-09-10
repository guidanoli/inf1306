/*
 * utils.c, v.0.1.1
 *
 * Utility functions for GVRP
 */

#include "utils.h"
#include <math.h>

unsigned int e2d_dist(struct node_t *a, struct node_t *b)
{
        int dx = a->x - b->x;
        int dy = a->y - b->y;
        float dist = sqrt( (float)(dx * dx + dy * dy) );
        unsigned int idist = (unsigned int) dist;
        return (dist - idist) < 0.5f ? idist : idist + 1;
}
