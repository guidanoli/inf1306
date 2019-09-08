/*
 * t.parser.c, v.0.3
 *
 * Parser test
 */

#include "parser.h"
#include "lwct.h"

void dump_instance_info(struct instance_t *instance)
{
        printf("Name: %s\n", instance->name);
        printf("#Customers: %u\n", instance->customer_cnt);
        printf("#Vehicles: %u\n", instance->vehicle_cnt);
        printf("#Sets: %u\n", instance->set_cnt);
        printf("Capacity: %u\n", instance->max_cap);
        printf("Depot: (x=%u, y=%u)\n", instance->depot->x, instance->depot->y);
        for (unsigned int i = 0; i < instance->customer_cnt; i++) {
                printf("Customer #%u: (x=%u, y=%u)\n", i+1,
                        instance->customers[i].node->x,
                        instance->customers[i].node->y);
        }
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
