package gvrp.diff;

import gvrp.Route;

public class IntraOpt2 implements Move {

	Route route;
	int p1;
	int p2;
	
	public IntraOpt2(Route route, int p1, int p2) {
		this.route = route;
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public void undo() {
		for (int i = 0; i < p2 - p1; i++)
			route.add(p2, route.remove(p1));
	}

}
