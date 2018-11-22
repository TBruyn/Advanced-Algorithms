import java.util.Arrays;

public class ProblemInstance {
	private int numJobs;
	private int[][] jobs; //length = [num_jobs][2], for every job [0] is the length, [1] is the due time
	
	public ProblemInstance(int numJobs, int[][] jobs) {
		this.numJobs = numJobs;
		this.jobs = jobs;
	}
	
	public int getNumJobs() {
		return numJobs;
	}
	
	public int[][] getJobs() {
		return jobs;
	}

	public ProblemInstance copy() {
		int[][] copyOfJobs = new int[jobs.length][2];
		for(int i = 0; i < jobs.length; i++) {
			copyOfJobs[i] = Arrays.copyOf(jobs[i], jobs[i].length);
		}
		return new ProblemInstance(numJobs, copyOfJobs);
	}
}
