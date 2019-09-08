/*
 * parser.c, v.1.0.0
 *
 * GVRP instance file parser
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parser.h"

#define BUFFER_SIZE 128
#define ABORT_PARSE(instance) do { printf("LINE OF ABORTION = %d\n",__LINE__); \
        free(instance); \
        return NULL; \
} while(0)

static struct node_t *parse_depot(unsigned int index);
static struct customer_t *parse_customers(unsigned int customer_cnt);

struct instance_t *parse_gvrp_instance()
{
        struct instance_t *instance = malloc(sizeof(struct instance_t));
        if (!instance)
                return NULL;

        char buf[BUFFER_SIZE];
        unsigned int uint;

        if (scanf("NAME : %s ", buf) != 1)
                ABORT_PARSE(instance);
        instance->name = strdup(buf);

        if (scanf("COMMENT : %s ", buf) != 1 || strcmp(buf, "GVRP") != 0)
                ABORT_PARSE(instance);

        if (scanf("DIMENSION : %u ", &uint) != 1)
                ABORT_PARSE(instance);
        instance->customer_cnt = uint - 1;

        if (scanf("VEHICLES : %u ", &uint) != 1)
                ABORT_PARSE(instance);
        instance->vehicle_cnt = uint;

        if (scanf("GVRP_SETS : %u ", &uint) != 1)
                ABORT_PARSE(instance);
        instance->set_cnt = uint;

        if (scanf("CAPACITY : %u ", &uint) != 1)
                ABORT_PARSE(instance);
        instance->max_cap = uint;

        if (scanf("EDGE_WEIGHT_TYPE : %s ", buf) != 1 || strcmp(buf, "EUC_2D") != 0)
                ABORT_PARSE(instance);

        if (scanf("%s ", buf) != 1 || strcmp(buf, "NODE_COORD_SECTION") != 0)
                ABORT_PARSE(instance);

        struct node_t *depot = parse_depot(1);
        if (!depot)
                ABORT_PARSE(instance);
        instance->depot = depot;

        struct customer_t *customers = parse_customers(instance->customer_cnt);
        if (!customers)
                ABORT_PARSE(instance);
        instance->customers = customers;

        return instance;
}

struct node_t *parse_depot(unsigned int index)
{
        unsigned int uint1, uint2, uint3;

        struct node_t *depot = malloc(sizeof(struct node_t));
        if (!depot)
                return NULL;

        if (scanf("%u %u %u ", &uint1, &uint2, &uint3) != 3 || uint1 != index)
                ABORT_PARSE(depot);
        depot->x = uint2;
        depot->y = uint3;

        return depot;
}

struct customer_t *parse_customers(unsigned int customer_cnt)
{
        struct customer_t *customers = malloc(sizeof(struct customer_t)*customer_cnt);
        if (!customers)
                return NULL;

        for (int i = 0; i < customer_cnt; i++) {
                struct node_t *node = parse_depot(i+2);
                if (!node) {
                        for (int j = 0; j < i; j++) free(customers[j].node);
                        ABORT_PARSE(customers);
                }
                customers[i].node = node;
                customers[i].set = -1; // undefined
        }

        return customers;
}
