/*
 * parser.c, v.0.1.1
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
        instance->node_cnt = uint;

        if (scanf("VEHICLES : %u ", &uint) != 1)
                ABORT_PARSE(instance);
        instance->vehicle_cnt = uint;

        if (scanf("GVRP_SETS : %u ", &uint) != 1)
                ABORT_PARSE(instance);
        instance->set_cnt = uint;

        if (scanf("CAPACITY : %u ", &uint) != 1)
                ABORT_PARSE(instance);
        instance->max_cap = uint;

        return instance;
}
