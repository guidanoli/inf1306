/*
 * parser.h
 *
 * GVRP instance file parser
 */

/*
 * Instance information
 * @instance_name       - instance name
 * @dimension_cnt       - dimension count
 * @vehicle_cnt         - vehicle count
 * @set_cnt             - set count
 * @max_cap             - maximum vehicle capacity
 * @nodes               - customer nodes
 *
 * - First node is the depot
 */
struct instance_t {
        char *instance_name;
        int dimension_cnt;
        int vehicle_cnt;
        int set_cnt;
        int max_cap;
        struct node_t *nodes;
};

/*
 * Customer node
 * @x   - x coordinate
 * @y   - y coordinate
 * @set - which set the node belong to
 */
struct node_t {
        int x;
        int y;
        int set;
};
