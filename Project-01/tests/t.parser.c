/*
 * t.parser.c, v.0.2
 *
 * Parser test
 */

#include "parser.h"
#include "lwct.h"

void dump_instance_info(struct instance_t *instance)
{
        printf("Name: %s\n", instance->name);
        printf("#Nodes: %u\n", instance->node_cnt);
        printf("#Vehicles: %u\n", instance->vehicle_cnt);
        printf("#Sets: %u\n", instance->set_cnt);
        printf("Capacity: %u\n", instance->max_cap);
}

void testParser(lwct_state *S)
{
        struct instance_t *instance = parse_gvrp_instance();
        lwct_fatal_assert(S, instance);
        dump_instance_info(instance);
}

int main(void)
{
        lwct_submit_test(testParser);
}
