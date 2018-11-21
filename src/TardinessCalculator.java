import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class TardinessCalculator {
	
	private static int numJobs = 0;
    private static int[][] jobs; 	//length = [num_jobs][2], for every job [0] is the length, [1] is the due time

    private int[][] testjobs = new int[][]{
            {98, 314},
            {26, 287},
            {82, 285},
            {67, 253},
            {85, 256}
    };

    
    private int calculateTardiness(int i, int j, int k, int t) {

        if (i == j) return jobs[0][i];

        int minimalTardiness = Integer.MAX_VALUE;
        for (int delta = k; delta < jobs.length; delta++) {

            int kPrime = findBiggestJob(getSubset(i, j, k));
            int cPrime = calculateCompletionTime(
                    t, getSubset(i, k + delta, kPrime));

            int subsetTardiness = calculateTardiness(i, k + delta, kPrime, t)
                    + Integer.max(0, cPrime - jobs[1][kPrime])
                    + calculateTardiness(kPrime + delta + 1, j, kPrime, cPrime);
            if (minimalTardiness > subsetTardiness)
                minimalTardiness = subsetTardiness;
        }
        return minimalTardiness;
    }

    private ArrayList<Integer> calculateDeltas(ArrayList<Integer> set) {
        int k = findBiggestJob(set);
        return new ArrayList<Integer>();
    }

    /*
    S
     */
    private ArrayList<Integer> getSubset(int i, int j, int k) {
        ArrayList<Integer> subset = new ArrayList<>();

        for (int id = i; id <= j; id++) {
            if (jobs[0][id] < jobs[0][k]) {
                subset.add(id);
            }
        }
        return subset;
    }

    private int calculateCompletionTime(int startingTime, ArrayList<Integer> set) {

        int productionTimeSum = 0;
        for (int id : set)
            productionTimeSum += jobs[0][id];

        return startingTime + productionTimeSum;
    }

    private int findBiggestJob(ArrayList<Integer> set) {
        int max = 0;
        int idOfMax = 0;

        for (int id : set) {
            if (max < jobs[0][id]) {
                max = jobs[0][id];
                idOfMax = id;
            }
        }

        return idOfMax;
    }
    
    public static ProblemInstance readInstance(String filename) { //same as initJob()
		ProblemInstance instance = null; 
		
		try {
			Scanner sc = new Scanner(new BufferedReader(new FileReader(filename)));
			if (sc.hasNextInt()) {
				numJobs = sc.nextInt();
				jobs = new int[numJobs][2];
				int nextJobID = 0;
				
				while(sc.hasNextInt() && nextJobID < numJobs) {
					jobs[nextJobID][0] = sc.nextInt();
					jobs[nextJobID][1] = sc.nextInt();
					nextJobID++;
				}
				
			}
			System.out.println("number of jobs: " + numJobs);
			System.out.println(Arrays.deepToString(jobs));
			
			sc.close();
			instance = new ProblemInstance(numJobs, jobs);
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
		
		return instance;
	}
    
    public static void main(String args[]) {
    	readInstance(args[0]); 	//reads file and initializes "jobs"
    }

}
