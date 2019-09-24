package gvrp.diff;

import gvrp.Route;

public class IntraRelocate implements Move {

	Route route;
	int ini;
	int fin;
	
	public IntraRelocate(Route route, int initialPos, int finalPos) {
		this.route = route;
		this.ini = initialPos;
		this.fin = finalPos;
	}
	
	public void undo() {
		route.add(ini, route.remove(fin));
	}

}
