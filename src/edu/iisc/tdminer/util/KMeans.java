package edu.iisc.tdminer.util;

// Written by - Karl Mittmann (karl.mittmann@gmail.com)
// Date: Saturday March 27, 2010
// Version: Java 1.6.0

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

public class KMeans {

	public File output_file = new File("/home/karl/workspace/KMeansClustering/src/data/output.txt");
	public FileWriter out = null; {
	try {
		out= new FileWriter(output_file);
    } catch (IOException e) {
    	e.printStackTrace();}
    }
	
	private ArrayList<double[]> matrix;	//Matrix containing all points as rows and columns as attributes.
	private int num_centers; 			//Number of centers to be found.
	private int iterations; 			//Number of iterations performed if the algorithm doesn't converge.
	private int dimensions;				//Number of dimensions in n-dimensional point.
	private int[] v_clusters;			//ArrayList of clusters each vector falls 
									//(associated with vectors of matrix variable).
	private double[][] centroids;		//List of centroids obtained using algorithm
	
	/*
	 * Default Constructor for KMeans (Clustering) class
	 * @param _matrix		Matrix containing points as rows and columns as attributes
	 * @param _num_centers	Number of centroids used for clustering
	 * @param _iterations	Number of iterations performed if the algorithm doesn't converge.
	 */
	public KMeans(ArrayList<double[]> _matrix, int _num_centers, int _iterations) {
		this.matrix = _matrix;
		this.num_centers = _num_centers;
		this.iterations = _iterations;
		this.dimensions = matrix.get(0).length;
		this.v_clusters = new int[_matrix.size()];
		this.centroids = new double[num_centers][dimensions];
		this.Cluster();	//Perform clustering.
	}
	
	/*
	 * Getter Methods
	 */
	public double[][] getCentroids() {return this.centroids;}
	public int[] getClusters() {return this.v_clusters;}
	
	/*
	 * Perform the clustering algorithm.
	 * @param none
	 * @return double[][] updated centroids.
	 */
	private void Cluster() {
		//Set centroids to first "num_centers" arbitrary points.
		for (int i = 0; i < num_centers; i++) {
			for (int j = 0; j < dimensions; j++)
			{
				centroids[i][j] = matrix.get(i)[j];
			}
		}
		//Perform k means clustering to determine positions of centroids.
		int i = 0;
		double[][] cur_centroids;
		ArrayList<double[]> distance_matrix;
		
		while (i < iterations) {
			distance_matrix = getDistanceMatrix(matrix);
			cur_centroids = updateCentroids(distance_matrix);
			System.out.println(i + " Iteration:\n");
			printCentroids();
			
			if (cur_centroids == centroids) { //Converged
				break;
			}
			centroids = cur_centroids;
			i++;
		}
		return;
	}
	
	public void printCentroids() {
		for (int i = 0; i < centroids.length; i++) {
			for (int j = 0; j < centroids[i].length; j++) {
				System.out.println(centroids[i][j] + " ");
			}
			System.out.println("\n");
		}
	}
	
	/*
	 * Return the Euclidean Distance of two n-dimensional points.
	 * 
	 * @param	point 1 in the form of an array of doubles
	 * @param	point 2 in the form of an array of doubles
	 * @return 	Euclidean distance of the two points 
	 */
	private double getDistance(double[] point_1, double[] point_2) {
		double[] sum_array = new double[5];
		double sum = 0;
		for (int i = 0; i < point_1.length; i++){
			sum_array[i] = pow((point_1[i] - point_2[i]), 2);
			sum += sum_array[i];
		}
		return sqrt(sum);
	}
	
	/*
	 *Returns the Euclidean distance matrix.
	 *@param _matrix Matrix containing all points as rows and attributes as columns.	 
	 *@return distance_matrix Euclidean distance matrix
	 */
	public ArrayList<double[]> getDistanceMatrix(ArrayList<double[]> _matrix) {
		
		ArrayList<double[]> distance_matrix = new ArrayList<double[]>();
		
		for (int i = 0; i < num_centers; i++) { //Iterate over centroids
			double[] temp = new double[_matrix.size()];
			for (int j = 0; j < _matrix.size(); j++) { //Iterate over all points
				temp[j] = getDistance(centroids[i], _matrix.get(j));
			}
			distance_matrix.add(temp);
		}
		return distance_matrix;
	}
	
	/*
	 * Update the centroids for each iteration of the k means algorithm.
	 * @param _matrix	 euclidian distance matrix
	 * @param _centroids current centroids
	 * @return updated centroids
	 */
	private double[][] updateCentroids(ArrayList<double[]> _matrix) {
		//Determine which centroid each point is closest to
		//Array of ArrayLists to contain each cluster of points
		double[][] _centroids = new double[num_centers][dimensions];
		ArrayList<Integer>[] clusters = clusterPoints(_matrix);
		
		//Reset Centroids
		for (int i = 0; i < _centroids.length; i++) {
			for (int j = 0; j < _centroids[i].length; j++) {
				_centroids[i][j] = 0;
			}
		}
		
		//Sum up all attributes over each point in each cluster, then average
		for (int i = 0; i < clusters.length; i++) { //Centroids
			for (int j = 0; j < clusters[i].size(); j++) { //Points in cluster
				int temp = clusters[i].get(j).intValue();
				for (int k = 0; k < matrix.get(temp).length; k++) { //All attributes of point
					_centroids[i][k] += matrix.get(temp)[k];
					v_clusters[temp] = i;
				}
			}
			for (int j = 0; j < _centroids[i].length; j++) {
				if (clusters[i].size() != 0) {
					_centroids[i][j] /= clusters[i].size();	
				}
				else {
					_centroids[i][j] = 0;
				}
			}
		}
		return _centroids;
	}
	
	/*
	 * Clusters the points with the current centroids and returns those clusters
	 * @param _matrix	 euclidean distance matrix
	 * @param _centroids current centroids
	 */
	private ArrayList[] clusterPoints(ArrayList<double[]> _matrix) {
		ArrayList<Integer>[] clusters = new ArrayList[num_centers];
		
		for (int i = 0; i < num_centers; i++) {
			clusters[i] = new ArrayList<Integer>();
		}
		
		for (int i = 0; i < matrix.size(); i++) { //Traverse through point distances 
			int cluster_pos = 0;
			double min = _matrix.get(0)[i];
			for (int j = 0; j < num_centers; j++) { //Traverse through centroids
				if ( _matrix.get(j)[i] < min )
				{
					min = _matrix.get(j)[i];
					cluster_pos = j;
				}
			}
			Integer intObj = new Integer(i);
			clusters[cluster_pos].add(intObj);
		}
		return clusters;
	}
}
