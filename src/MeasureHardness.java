import java.util.Arrays;
import java.util.Comparator;

public class MeasureHardness {

    public static int measure(ProblemInstance instance) {

        // Create a node for each job


//        int[][] jobs = instance.getJobs();

        int[][] jobs = { {4,3}, {3,4}, {2,5}};

        Node[] nodes = new Node[jobs.length];
        for (int i = 0; i < nodes.length; i++)
            nodes[i] = new Node(jobs[i][0], jobs[i][1]);

        // Sort by deadline
        Arrays.sort(nodes, new SortByDeadline());
        for (int i = 0; i < nodes.length; i++)
            nodes[i].id = i;


        int sum = 0;
        Arrays.sort(nodes, new SortByProcessingTime());
        for (int i = 0; i < nodes.length; i++)
            sum += Math.pow((nodes[i].id - i), 2);

        return sum;
    }

    static class Node {
        int p, d, ip, id;

        public Node(int p, int d) {
            this.p = p;
            this.d = d;
        }
    }


    /**
     * Sort the 2D jobs array by deadline (2nd element of each pair)
     */
    static class SortByDeadline implements Comparator<Node> {
        public int compare(Node a, Node b) {
            return a.d - b.d;
        }
    }

    /**
     * Sort the 2D jobs array by processing time (1st element of each pair)
     */
    static class SortByProcessingTime implements Comparator<Node> {
        public int compare(Node a, Node b) {
            return a.p - b.p;
        }
    }

}
