/*
 * parser.c, v.1.2.0
 *
 * GVRP instance file parser
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parser.h"

#define BUFFER_SIZE 128

/*
 * ABORT_PARSE is a handy macro that does mainly three things:
 * - Prints the line in code it aborted (for debugging purposes)
 * - Frees pointers to structures allocated within parser
 * - Returns NULL (to interrupt parsing)
 */
#define ABORT_PARSE(...) do { printf("LINE OF ABORTION = %d\n",__LINE__); \
        void *pta[] = {__VA_ARGS__}; \
        for (int i = 0; i < sizeof(pta)/sizeof(void*); i++) \
        { \
            free(pta[i]); \
        } \
        return NULL; \
} while(0)

/*
 * FREE_FIELD serves the purpose of deallocating every field
 * in a structure array, if present, and setting it equal to NULL.
 */
#define FREE_FIELD(p, size, field) do { \
        for (int j = 0; j < size; j++) \
                if (p[j].field != NULL) { \
                        free(p[j].field); \
                        p[j].field = NULL; \
                } \
} while(0)

static struct node_t *parse_depot(unsigned int index);
static struct customer_t *parse_customers(unsigned int customer_cnt);
static struct set_t *parse_sets(struct customer_t * customers,
                        unsigned int customer_cnt, unsigned int set_cnt);

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

        if (scanf("EDGE_WEIGHT_TYPE : %s ", buf) != 1
                                                || strcmp(buf, "EUC_2D") != 0)
                ABORT_PARSE(instance);

        if (scanf("%s ", buf) != 1 || strcmp(buf, "NODE_COORD_SECTION") != 0)
                ABORT_PARSE(instance);

        struct node_t *depot = parse_depot(1);
        if (!depot)
                ABORT_PARSE(instance);
        instance->depot = depot;

        struct customer_t *customers = parse_customers(instance->customer_cnt);
        if (!customers)
                ABORT_PARSE(instance, depot);
        instance->customers = customers;

        if (scanf("%s ", buf) != 1 || strcmp(buf, "GVRP_SET_SECTION") != 0)
                ABORT_PARSE(instance, depot, customers);

        struct set_t *sets = parse_sets(instance->customers,
                                instance->customer_cnt, instance->set_cnt);
        if (!sets)
                ABORT_PARSE(instance, depot, customers);
        instance->sets = sets;

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
        struct customer_t *customers = malloc(sizeof(struct customer_t)
                                                                *customer_cnt);
        if (!customers)
                return NULL;

        for (int i = 0; i < customer_cnt; i++) {
                unsigned int id = i + 2;
                struct node_t *node = parse_depot(id);
                if (!node) {
                        for (int j = 0; j < i; j++) free(customers[j].node);
                        ABORT_PARSE(customers);
                }
                customers[i].node = node;
                customers[i].set = NULL; // not defined yet
                customers[i].id = i + 1;
        }

        return customers;
}

static struct set_t *parse_sets(struct customer_t * customers,
                                unsigned int customer_cnt, unsigned int set_cnt)
{
        unsigned int uint1, uint2;
        char buf[BUFFER_SIZE];

        struct set_t *sets = malloc(sizeof(struct set_t)*set_cnt);
        if (!sets)
                return NULL;

        for (int i = 0; i < set_cnt; i++) {
                 /* Set index */
                if (scanf("%u ", &uint1) != 1 || uint1 != i + 1) {
                        FREE_FIELD(sets, set_cnt, customers);
                        ABORT_PARSE(sets);
                }

                struct set_t *current_set = &sets[uint1 - 1];
                current_set->id = uint1;

                /* Customers in the i+1-th set */
                unsigned int customers_in_set = 0;
                while (1) {
                        if (scanf("%u ", &uint2) != 1) {
                                FREE_FIELD(sets, set_cnt, customers);
                                ABORT_PARSE(sets);
                        }
                        if (uint2 == -1)
                                break;
                        customers[uint2 - 2].set = current_set;
                        customers_in_set++;
                }

                /* Allocate customer array to set data structure */
                struct customer_t **customer_lst = malloc(
                        sizeof(struct customer_t *)*customers_in_set);
                if (!customer_lst) {
                        FREE_FIELD(sets, set_cnt, customers);
                        ABORT_PARSE(sets);
                }
                current_set->customer_cnt = customers_in_set;
                for (int j = 0; j < customer_cnt; j++)
                        if (customers[j].set == current_set)
                                customer_lst[--customers_in_set] = &customers[j];
                current_set->customers = customer_lst;
        }

        if (scanf("%s ", buf) != 1 || strcmp(buf, "DEMAND_SECTION") != 0) {
                FREE_FIELD(sets, set_cnt, customers);
                ABORT_PARSE(sets);
        }

        for (int i = 0; i < set_cnt; i++) {
                /* Set index */
               if (scanf("%u ", &uint1) != 1 || uint1 != i + 1) {
                       FREE_FIELD(sets, set_cnt, customers);
                       ABORT_PARSE(sets);
               }

               struct set_t *current_set = &sets[uint1 - 1];

               /* Set demand */
               if (scanf("%u ", &uint2) != 1) {
                       FREE_FIELD(sets, set_cnt, customers);
                       ABORT_PARSE(sets);
               }
               current_set->demand = uint2;
        }

        return sets;
}
