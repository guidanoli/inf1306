/*
 * t.utils.c, v.0.1.0
 *
 * Utility functions test
 */

#include <stdio.h>
#include "parser.h"
#include "utils.h"
#include "lwct.h"

void testE2D(lwct_state *S)
{
	struct instance_t *instance = parse_gvrp_instance();
	lwct_submit_desconstructor(S, free_gvrp_instance, instance);
	lwct_fatal_assert(S, instance);
	for (unsigned int i = 0; i < instance->customer_cnt; i++) {
		struct node_t *inode = instance->customers[i].node;
		lwct_assert(S, e2d_dist(inode, inode) == 0);
		for (unsigned int j = i+1; j < instance->customer_cnt; j++) {
			struct node_t *jnode = instance->customers[j].node;
			printf("d([%u]=(%d,%d), [%u]=(%d,%d)) = %u\n", i,
				inode->x, inode->y, j, jnode->x, jnode->y,
							e2d_dist(inode, jnode));
		}
	}
}

int main(void)
{
	lwct_submit_test(testE2D);
}
