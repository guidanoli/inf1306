package mssc;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringJoiner;

public class Instance {
	
	public static class Builder {
		
		int numOfClusters = 0;
		ArrayList<Point> entitiesList = new ArrayList<>();
		
		public Builder setNumOfClusters(int numOfClusters) {
			this.numOfClusters = numOfClusters;
			return this;
		}
		
		public Builder addEntity(Point entity) {
			entitiesList.add(entity);
			return this;
		}
		
		public Instance build() throws Exception {
			if (numOfClusters < 1) {
				throw new Exception("There must be a positive number of clusters.");
			}
			
			if (entitiesList.isEmpty()) {
				throw new Exception("No entities were added.");
			}
			
			final int dimension = entitiesList.get(0).size();
			boolean sameDimension = entitiesList
					.stream()
					.allMatch((e) -> e.size() == dimension);
			if (!sameDimension) {
				throw new Exception("Entities must be of same dimension.");
			}
						
			return new Instance(entitiesList, numOfClusters);
		}
		
	}
	
	ArrayList<Point> entities;
	int numOfClusters;
	
	private Instance(ArrayList<Point> entities, int numOfClusters) {
		this.entities = entities;
		this.numOfClusters = numOfClusters;
	}
	
	public static Instance parse(Scanner sc, int numOfClusters) throws 
			NoSuchElementException, IllegalStateException, InputMismatchException {
		Builder builder = new Builder();
		builder.setNumOfClusters(numOfClusters);
		if (!sc.hasNextInt()) return null;
		int numOfEntities = sc.nextInt();
		if  (numOfClusters > numOfEntities) return null;
		if (!sc.hasNextInt()) return null;
		int dimensions = sc.nextInt();
		for (int i = 0; i < numOfEntities; i++) {
			Point entity = new Point(i+1, "e", dimensions); 
			for (int j = 0; j < dimensions; j++) {
				if (!sc.hasNextDouble()) return null;
				entity.set(j, sc.nextDouble());
			}
			builder.addEntity(entity);
		}
		try {
			return builder.build();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Point getEntityAt(int i) {
		/* Might throw IndexOutOfBoundException */
		return entities.get(i);
	}
	
	public ArrayList<Point> getEntities() {
		return entities;
	}
	
	public int getNumOfEntities() {
		return entities.size();
	}
	
	public int getDimension() {
		return entities.get(0).size();
	}
	
	public int getNumOfClusters() {
		return numOfClusters;
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		sj.add("#clusters = " + numOfClusters);
		for (Point e : entities)
			sj.add("o" + e.getId() + " = " + e);
		return sj.toString();
	}
	
}
