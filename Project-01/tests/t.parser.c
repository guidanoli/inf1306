/*
 * t.parser.c, v.0.4.2
 *
 * Parser test
 */

#include <stdio.h>
#include "parser.h"
#include "lwct.h"

void dump_instance_info(struct instance_t *instance)
{
	printf("Name: %s\n", instance->name);
	printf("#Customers: %u\n", instance->customer_cnt);
	printf("#Vehicles: %u\n", instance->vehicle_cnt);
	printf("#Sets: %u\n", instance->set_cnt);
	printf("Capacity: %u\n", instance->max_cap);
	printf("Depot: (x=%d, y=%d)\n", instance->depot->x, instance->depot->y);
	for (unsigned int i = 0; i < instance->customer_cnt; i++) {
		printf("Customer #%u: (x=%d, y=%d) Set %u\n", i+1,
			instance->customers[i].node->x,
			instance->customers[i].node->y,
			instance->customers[i].set->id
		);
	}
	for (unsigned int i = 0; i < instance->set_cnt; i++) {
		printf("Set #%u - Demand %u - ", instance->sets[i].id,
						instance->sets[i].demand);
		for (unsigned int j = 0; j < instance->sets[i].customer_cnt; j++)
			printf("%u ", instance->sets[i].customers[j]->id);
		printf("(%u)\n", instance->sets[i].customer_cnt);
	}
}

void testParser(lwct_state *S)
{
	struct instance_t *instance = parse_gvrp_instance();
	lwct_fatal_assert(S, instance);
	dump_instance_info(instance);
	free_gvrp_instance(instance);
}

int main(void)
{
	lwct_submit_test(testParser);
}
