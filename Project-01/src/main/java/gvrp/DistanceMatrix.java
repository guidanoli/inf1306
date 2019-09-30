package gvrp;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

public class DistanceMatrix {

	int [][] matrix;
	
	public DistanceMatrix(List<Customer> customers, Point depot) {
		int matrixDimension = customers.size();
		matrix = new int[matrixDimension][matrixDimension];
		for (Customer ci : customers) {
			setDistanceToDepot(ci, depot);
			for (Customer cj : customers) {
				setDistance(ci, cj);
			}
		}
	}
		
	private void setDistanceToDepot(Customer c, Point depot) {
		int id = c.getId();
		int dist = c.distanceFrom(depot);
		matrix[id][0] = matrix[0][id] = dist;
	}
	
	private void setDistance(Customer ci, Customer cj) {
		int iid = ci.getId();
		int jid = cj.getId();
		int dist = ci.distanceFrom(cj);
		matrix[iid][jid] = matrix[jid][iid] = dist;
	}
	
	public int getDistanceBetween(Customer ci, Customer cj) {
		return matrix[ci.getId()][cj.getId()];
	}
	
	public int getDistanceFromDepot(Customer c) {
		return matrix[0][c.getId()];
	}
	
	public Integer getClosestNeighbourId(int customerId, Predicate<Integer> isVisited) {
		int closestDistance = Integer.MAX_VALUE;
		Integer closestId = null;
		for (int i = 0; i < matrix[customerId].length; i++) {
			if (i == customerId) continue; /* ignore itself */
			if (isVisited.test(i)) continue; /* ignore visited */
			int dist = matrix[customerId][i]; 
			if (dist < closestDistance) {
				closestId = i;
				closestDistance = dist;
			}
		}
		return closestId;
	}
	
	/**
	 * Displays the lower half of the distance matrix
	 */
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		for (int i = 0; i < matrix.length; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j <= i; j++)
				sb.append(matrix[i][j] + "\t");
			sj.add(sb.toString());
		}
		
		return sj.toString();
	}
	
}
