import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;

public class DataCollector3 {
    private static final int TIME_LIMIT_IN_SECONDS = 100000;
    private static final int BEST_FIRST_CAP = 5;
    private final long startingTime;
    private String[] headers = new String[] {
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
            "BestFirst-tardiness"       };

    private String outputFileName = "testoutput.csv";
    private String inputFileName = "data/provided-answers.txt";

    private LinkedList<String> fileQueue;

    public DataCollector3() {
        startingTime = System.currentTimeMillis();
        fileQueue = new LinkedList<>();
        fillFileQueue();

        new File(outputFileName).delete();
        print_headers();
        processFile();
    }

    private void init() {

    }

    /**
     * Read the first file in the queue
     * Expected filename format: random_RDD=1.0_TF=1.0_#100.dat
     * @return
     */
    private void processFile() {
        HashMap<String, String> result = new HashMap<>();

        while (!fileQueue.isEmpty()) {

//            ExecutorService executorService = Executors.newFixedThreadPool(3);

            String filename = fileQueue.pop();

            final ProblemInstance problemInstance = ComputeTardiness
                    .readInstance
                    ("data/provided/" + filename);

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

//            Callable<String[]> dynamicTask = () -> runDynamic(problemInstance);
//            Callable<String[]> greedyTask = () -> runGreedy(problemInstance);
//            Callable<String[]> bestFirstTask = () -> runBestFirst
//                    (problemInstance);
//
//            Future<String[]> submitDynamic = executorService.submit(dynamicTask);
//            Future<String[]> submitGreedy = executorService.submit(greedyTask);
//            Future<String[]> submitBestFirst = executorService.submit(bestFirstTask);

            String[] dynamicResult = runDynamic(problemInstance);
//                    getResultFromThread(submitDynamic);
            String[] greedyResult = runGreedy(problemInstance);
//                    getResultFromThread(submitGreedy);
            String[] bestFirstResult = runBestFirst(problemInstance);
//                    getResultFromThread(submitBestFirst);


            result.put("Dynamic-runtime",           dynamicResult[0]);
            result.put("Dynamic-tardiness",         dynamicResult[1]);
//            result.put("Approx-e=0.1-runtime",      approxe01Result[0]);
//            result.put("Approx-e=0.1-tardiness",    approxe01Result[1]);
//            result.put("Approx-e=0.2-runtime",      approxe02Result[0]);
//            result.put("Approx-e=0.2-tardiness",    approxe02Result[1]);
//            result.put("Approx-e=0.3-runtime",      approxe03Result[0]);
//            result.put("Approx-e=0.3-tardiness",    approxe03Result[1]);
            result.put("Greedy-runtime",            greedyResult[0]);
            result.put("Greedy-tardiness",          greedyResult[1]);
            result.put("BestFirst-runtime",         bestFirstResult[0]);
            result.put("BestFirst-tardiness",       bestFirstResult[1]);

            System.out.println((System.currentTimeMillis() - startingTime)
                    /1000.0 +
                    "s: Finished processing " + filename + ". Left: " +
                    fileQueue.size());
            print_resultline(result);

//            executorService.shutdown();
        }
        System.out.println("Finished processing, shutting down collector");
    }

    private String[] runDynamic(ProblemInstance problemInstance) {
        try {
            long before = System.currentTimeMillis();
            int tardiness = new Dynamic(problemInstance).calculateTardiness();
            return new String[] {
                    "" + (System.currentTimeMillis() - before),
                    "" + tardiness};
        } catch (Exception e) {
            e.printStackTrace();
            return new String[] {"NaN", "NaN"};
        }
    }

    private String[] runGreedy(ProblemInstance problemInstance) {
        try {
            long before = System.currentTimeMillis();
            int tardiness = new Greedy(problemInstance).getSchedule().getTardiness();
            return new String[] {
                    "" + (System.currentTimeMillis() - before),
                    "" + tardiness};
        } catch (Exception e) {
            e.printStackTrace();
            return new String[] {"NaN", "NaN"};
        }
    }

    private String[] runBestFirst(ProblemInstance problemInstance) {
        if (problemInstance.getNumJobs() > BEST_FIRST_CAP)
            return new String[] {"NaN", "NaN"};
        try {
            long before = System.currentTimeMillis();
            int tardiness = new BestFirst(problemInstance).getSchedule()
                    .getTardiness();
            return new String[] {
                    "" + (System.currentTimeMillis() - before),
                    "" + tardiness};
        } catch (Exception e) {
            e.printStackTrace();
            return new String[] {"NaN", "NaN"};
        }
    }

    private String[] getResultFromThread(Future<String[]> future) {
        try {
            return future.get();
        } catch (   InterruptedException
                | ExecutionException e
//                | TimeoutException e
                ) {
            e.printStackTrace();
            future.cancel(true);
            return new String[]{"NaN", "NaN"};
        }
    }

    private void fillFileQueue() {
        File file = new File(inputFileName);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ( (line = br.readLine()) != null
//                    && fileQueue.size() < 5
                    ) {
                String filename = line.split("\t")[0] + ".dat";
                fileQueue.add(filename);
            }
            System.out.println("Added " + fileQueue.size() + " files to queue");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void print_headers() {
        HashMap<String, String> headerMap = new HashMap<>();
        for(String header : headers)
            headerMap.put(header, header);
        print_resultline(headerMap);
    }

    private void print_resultline(HashMap<String, String> line_to_print) {
//        System.out.println((System.currentTimeMillis() - startingTime)/1000.0 +
//                "Prepare to " +
//                "print");
        try {
            File file = new File(outputFileName);
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < headers.length - 1; i++) {
                sb.append(line_to_print.get(headers[i]) + ", ");
            }
            sb.append(line_to_print.get(headers[headers.length - 1]));
            String line = sb.toString();
            pw.println(line);
            pw.close();
            System.out.println("Printed:\t" + line);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
//        System.out.println((System.currentTimeMillis() - startingTime)/1000.0 +
//                "Printed");
    }



    public static void main(String[] args) throws FileNotFoundException {
        DataCollector3 dataCollector = new DataCollector3();
    }
}