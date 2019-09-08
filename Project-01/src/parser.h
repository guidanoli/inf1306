/*
 * parser.h, v.1.2.0
 *
 * GVRP instance file parser
 */

#ifndef _PARSER_H_
#define _PARSER_H_

/*
 * Instance data
 * @ name               - instance name
 * @ depot              - depot node info
 * @ customers          - customer nodes info
 * @ sets               - sets info
 * @ customer_cnt       - customer count
 * @ vehicle_cnt        - vehicle count
 * @ set_cnt            - set count
 * @ max_cap            - maximum vehicle capacity
 */
struct instance_t {
        char *name;
        struct node_t *depot;
        struct customer_t *customers;
        struct set_t *sets;
        unsigned int customer_cnt;
        unsigned int set_cnt;
        unsigned int vehicle_cnt;
        unsigned int max_cap;
};

/*
 * Node data
 * @ x  - x coordinate
 * @ y  - y coordinate
 */
struct node_t {
        int x;
        int y;
};

/*
 * Customer data
 * @ node       - node data
 * @ set        - set identifier
 * @ id         - customer identification
 */
struct customer_t {
        struct node_t *node;
        struct set_t *set;
        unsigned int id;
};

/*
 * Set data
 * @ customers          - customers contained in the set
 * @ customer_cnt       - number of customers in the set
 * @ demand             - set demand
 * @ id                 - set identification
 */
struct set_t {
        struct customer_t **customers;
        unsigned int customer_cnt;
        unsigned int demand;
        unsigned int id;
};

/*
 * Parses all information about a GVRP instance
 * within a formatted input into a C data structure
 * Reads input from stdin (which can be piped from file)
 * > instance information or (void *) 0 on error
 */
struct instance_t *parse_gvrp_instance();

/*
 * Property deallocates instance data structure
 * and its fields, avoiding memory leak.
 */
void free_gvrp_instance(struct instance_t *instance);

#endif
