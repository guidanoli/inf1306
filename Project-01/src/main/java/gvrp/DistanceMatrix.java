package gvrp;

import java.util.ArrayList;
import java.util.StringJoiner;

public class DistanceMatrix {

	int [][] matrix;
	
	public DistanceMatrix(ArrayList<Customer> customers, Point depot) {
		int matrixDimension = customers.size();
		matrix = new int[matrixDimension][matrixDimension];
		for (int i = 0; i < matrixDimension; i++) {
			Customer ci = customers.get(i);
			setDistanceToDepot(ci, depot);
			for (int j = 0; j < matrixDimension; j++) {
				setDistance(ci, customers.get(j));
			}
		}
	}
		
	private void setDistanceToDepot(Customer ci, Point depot) {
		int iid = ci.getId();
		int dist = ci.distanceFrom(depot);
		matrix[iid][0] = matrix[0][iid] = dist;
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
