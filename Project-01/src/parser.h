/*
 * parser.h, v.1.0
 *
 * GVRP instance file parser
 */

#ifndef _PARSER_H_
#define _PARSER_H_

/*
 * Instance data
 * @ name               - instance name
 * @ customers          - customer nodes info
 * @ depot              - depot node info
 * @ customer_cnt       - node count
 * @ vehicle_cnt        - vehicle count
 * @ set_cnt            - set count
 * @ max_cap            - maximum vehicle capacity
 */
struct instance_t {
        char *name;
        struct customer_t *customers;
        struct node_t *depot;
        unsigned int customer_cnt;
        unsigned int vehicle_cnt;
        unsigned int set_cnt;
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
 */
struct customer_t {
        struct node_t *node;
        unsigned int set;
};

/*
 * Parses all information about a GVRP instance
 * within a formatted input into a C data structure
 * Reads input from stdin (which can be piped from file)
 * > instance information or (void *) 0 on error
 */
struct instance_t *parse_gvrp_instance();

#endif
