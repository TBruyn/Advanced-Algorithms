import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;

public class DataCollector2 {
    private String[] headers = new String[]{
            "Filename",
            "RDD",
            "TF",
            "Size",
            "Dynamic-runtime",
            "Dynamic-tardiness",
            "Approx-e=0.1-runtime",
            "Approx-e=0.1-tardiness",
            "Approx-e=0.2-runtime",
            "Approx-e=0.2-tardiness",
            "Approx-e=0.3-runtime",
            "Approx-e=0.3-tardiness",
            "Greedy-runtime",
            "Greedy-tardiness",
            "BestFirst-runtime",
            "BestFirst-tardiness"};

    private String outputFileName;
    private String inputFileName = "data/provided-answers.txt";

    public DataCollector2(String outputFileName) {
        this.outputFileName = outputFileName;
        LinkedList<String> queue = fillFileQueue();

        new File(outputFileName).delete();
        print_headers();

        processFile(queue);
    }

    /**
     * Read the first file in the queue
     * Expected filename format: random_RDD=1.0_TF=1.0_#100.dat
     *
     * @return
     */
    private void processFile(LinkedList<String> fileQueue) {
        HashMap<String, String> result = new HashMap<>();

        ExecutorService executorService = Executors.newFixedThreadPool(6);

        long begin = System.currentTimeMillis();

        while (!fileQueue.isEmpty()) {

            String filename = fileQueue.pop();

            final ProblemInstance problemInstance = ComputeTardiness
                    .readInstance("data/provided/" + filename);

            String[] filenameParts = filename.split("_");

            result.put("Filename",
                    filename);
            result.put("RDD",
                    filenameParts[1].split("=")[1]);
            result.put("TF",
                    filenameParts[2].split("=")[1]);
            result.put("Size",
                    filenameParts[3]
                            .substring(1, filenameParts[3].length() - 4));

            if (problemInstance.getNumJobs() > 50) continue;

            try {
                runDynamic(result, filename, problemInstance);
                runApproxE1(result, filename, problemInstance);
                runApproxE2(result, filename, problemInstance);
                runApproxE3(result, filename, problemInstance);
                runGreedy(result, filename, problemInstance);
                runBestFirst(result, filename, problemInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println((System.currentTimeMillis() - begin) / 1000.0 +
                    "s: Finished processing " + filename + ". Left: " +
                    fileQueue.size());

            print_resultline(result);
        }

        System.out.println("Finished processing, shutting down collector");
    }

    private void runBestFirst(HashMap<String, String> result, String filename, ProblemInstance problemInstance) {
        System.out.println(filename + ": Started processing bestFirst");
        try {
            if (problemInstance.getNumJobs() <= 10) {
                long startingTime = System.currentTimeMillis();
                int tardiness = new BestFirst(problemInstance.copy()).getSchedule().getTardiness();
                long processingTime = System.currentTimeMillis() - startingTime;
                result.put("BestFirst-runtime", processingTime + "");
                result.put("BestFirst-tardiness", tardiness + "");
                System.out.println(filename + ": Finished processing bestFirst");

            } else {
                System.out.println(filename + ": Finished processing bestFirst");
                result.put("BestFirst-runtime", "");
                result.put("BestFirst-tardiness", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("BestFirst-runtime", "");
            result.put("BestFirst-tardiness", "");
            System.out.println(filename + ": Finished processing bestFirst");
        }
    }

    private void runGreedy(HashMap<String, String> result, String filename, ProblemInstance problemInstance) {
        System.out.println(filename + ": Started processing greedy");
        long startingTime = System.currentTimeMillis();
        int tardiness = new Greedy(problemInstance.copy()).getSchedule().getTardiness();
        long processingTime = System.currentTimeMillis() - startingTime;
        result.put("Greedy-runtime", processingTime + "");
        result.put("Greedy-tardiness", tardiness + "");
        System.out.println(filename + ": Finished processing greedy");
    }

    private void runApproxE3(HashMap<String, String> result, String filename, ProblemInstance problemInstance) throws Exception {
        System.out.println(filename + ": Started processing e03");
        long startingTime = System.currentTimeMillis();
        int tardiness = new Approx(problemInstance.copy(), 0.3f).calculateTardiness();
        long processingTime = System.currentTimeMillis() - startingTime;
        result.put("Approx-e=0.3-runtime", processingTime + "");
        result.put("Approx-e=0.3-tardiness", tardiness + "");
        System.out.println(filename + ": Finished processing e03");
    }

    private void runApproxE2(HashMap<String, String> result, String filename, ProblemInstance problemInstance) throws Exception {
        System.out.println(filename + ": Started processing e02");
        long startingTime = System.currentTimeMillis();
        int tardiness = new Approx(problemInstance.copy(), 0.2f).calculateTardiness();
        long processingTime = System.currentTimeMillis() - startingTime;
        result.put("Approx-e=0.2-runtime", processingTime + "");
        result.put("Approx-e=0.2-tardiness", tardiness + "");
        System.out.println(filename + ": Finished processing e02");
    }

    private static void runApproxE1(HashMap<String, String> result, String filename, ProblemInstance problemInstance) throws Exception {
        System.out.println(filename + ": Started processing e01");
        long startingTime = System.currentTimeMillis();
        int tardiness = new Approx(problemInstance.copy(), 0.1f).calculateTardiness();
        long processingTime = System.currentTimeMillis() - startingTime;
        result.put("Approx-e=0.1-runtime", processingTime + "");
        result.put("Approx-e=0.1-tardiness", tardiness + "");
        System.out.println(filename + ": Finished processing e01");
    }

    private void runDynamic(HashMap<String, String> result, String filename, ProblemInstance problemInstance) {
        System.out.println(filename + ": Started processing dynamic");
        try {
            long startingTime = System.currentTimeMillis();
            int tardiness = new Dynamic(problemInstance.copy()).calculateTardiness();
            long processingTime = System.currentTimeMillis() - startingTime;
            result.put("Dynamic-runtime", processingTime + "");
            result.put("Dynamic-tardiness", tardiness + "");
        } catch (Exception e) {
            result.put("Dynamic-runtime", "");
            result.put("Dynamic-tardiness", "");
        }
        System.out.println(filename + ": Finished programming dynamic");
    }

    private LinkedList<String> fillFileQueue() {
        LinkedList<String> fileQueue = new LinkedList<>();
        File file = new File(inputFileName);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null
                    ) {
                String filename = line.split("\t")[0] + ".dat";
                fileQueue.add(filename);
            }
            System.out.println("Added " + fileQueue.size() + " files to queue");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileQueue;
    }

    private void print_headers() {
        HashMap<String, String> headerMap = new HashMap<>();
        for (String header : headers)
            headerMap.put(header, header);
        print_resultline(headerMap);
    }

    private void print_resultline(HashMap<String, String> line_to_print) {
        try {
            File file = new File(outputFileName);
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < headers.length - 1; i++) {
                sb.append(line_to_print.get(headers[i])).append(", ");
            }
            sb.append(line_to_print.get(headers[headers.length - 1]));
            String line = sb.toString();
            pw.println(line);
            pw.close();
            System.out.println("Printed:\t" + line);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public static void main(String[] args) {
        for (int i = 10; i <= 10; i++) {
            new DataCollector2("testoutput" + i + ".csv");
        }
    }
}