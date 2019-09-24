package gvrp.diff;

import gvrp.Route;

public class InterOpt2Star implements Move {

	Route r1;
	Route r2;
	int t1;
	int t2;
	
	public InterOpt2Star(Route r1, Route r2, int t1, int t2) {
		this.r1 = r1;
		this.r2 = r2;
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public void undo() {
		int r1size = r1.size(), r2size = r2.size();
		for (int i = 0; i < r1size - 1 - t1; i++)
			r2.add(t2 + 1, r1.removeLast());
		for (int i = 0; i < r2size - 1 - t2; i++)
			r1.add(t1 + 1, r2.removeLast());
	}

}
