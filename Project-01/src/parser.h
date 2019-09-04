/*
 * parser.h, v.0.2
 *
 * GVRP instance file parser
 */

#ifndef _PARSER_H_
#define _PARSER_H_

/*
 * Instance information
 * @ instance_name      - instance name
 * @ nodes              - customer nodes info
 * @ node_cnt           - node count
 * @ vehicle_cnt        - vehicle count
 * @ set_cnt            - set count
 * @ max_cap            - maximum vehicle capacity
 */
struct instance_t {
        char *instance_name;
        struct node_t *nodes;
        unsigned int node_cnt;
        unsigned int vehicle_cnt;
        unsigned int set_cnt;
        unsigned int max_cap;
};

/*
 * Customer node information
 * @ x          - x coordinate
 * @ y          - y coordinate
 * @ dist       - distance from depot
 * @ set        - which set the node belong to
 *
 * Note that dist is zero for depot
 */
struct node_t {
        int x;
        int y;
        unsigned int dist;
        unsigned int set;
};

/*
 * Parses all information about a GVRP instance
 * within a formatted file into a C data structure
 * @ filepath   - path of file containing instance information
 * > instance information or (void *) 0 on error
 */
struct instance_t *parse_gvrp_instance(char *filepath);

#endif
