import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;

public class DataCollector2 {
    private static final int TIME_LIMIT_IN_SECONDS = 2;
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

    public DataCollector2() {
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

        ExecutorService executorService = Executors.newFixedThreadPool(6);

        long begin = System.currentTimeMillis();

        while (!fileQueue.isEmpty()) {

            String filename = fileQueue.pop();

            ProblemInstance problemInstance = ComputeTardiness.readInstance
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

            Callable<String[]> runDynamic = () -> {
                long startingTime = System.currentTimeMillis();
                int tardiness = new Dynamic(problemInstance).calculateTardiness();
                long processingTime = System.currentTimeMillis() - startingTime;
                return new String[]{"" + tardiness, "" + processingTime};
            };
            Callable<String[]> runApproxE01 = () -> {
                long startingTime = System.currentTimeMillis();
                int tardiness = new Dynamic(problemInstance).calculateTardiness();
                long processingTime = System.currentTimeMillis() - startingTime;
                return new String[] {"" + tardiness, "" + processingTime};
            };
            Callable<String[]> runApproxE02 = () -> {
                long startingTime = System.currentTimeMillis();
                int tardiness = new Dynamic(problemInstance).calculateTardiness();
                long processingTime = System.currentTimeMillis() - startingTime;
                return new String[] {"" + tardiness, "" + processingTime};
            };
            Callable<String[]> runApproxE03 = () -> {
                long startingTime = System.currentTimeMillis();
                int tardiness = new Dynamic(problemInstance).calculateTardiness();
                long processingTime = System.currentTimeMillis() - startingTime;
                return new String[] {"" + tardiness, "" + processingTime};
            };
            Callable<String[]> runGreedy = () -> {
                long startingTime = System.currentTimeMillis();
                int tardiness = new Greedy(problemInstance).getSchedule().getTardiness();
                long processingTime = System.currentTimeMillis() - startingTime;
                return new String[] {"" + tardiness, "" + processingTime};
            };
            Callable<String[]> runBestFirst = () -> {
                long startingTime = System.currentTimeMillis();
                int tardiness = new BestFirst(problemInstance).getSchedule().getTardiness();
                long processingTime = System.currentTimeMillis() - startingTime;
                return new String[] {"" + processingTime, "" + tardiness};
            };

            Future<String[]> dynamicFuture      = executorService.submit(runDynamic);
            Future<String[]> approxe01Future    = executorService.submit(runApproxE01);
            Future<String[]> approxe02Future    = executorService.submit(runApproxE02);
            Future<String[]> approxe03Future    = executorService.submit(runApproxE03);
            Future<String[]> greedyFuture       = executorService.submit(runGreedy);
            Future<String[]> bestFirstFuture    = executorService.submit(runBestFirst);

            String[] dynamicResult =
                    getResultFromThread(dynamicFuture, TIME_LIMIT_IN_SECONDS);
            String[] approxe01Result =
                    getResultFromThread(approxe01Future, TIME_LIMIT_IN_SECONDS);
            String[] approxe02Result =
                    getResultFromThread(approxe02Future, TIME_LIMIT_IN_SECONDS);
            String[] approxe03Result =
                    getResultFromThread(approxe03Future, TIME_LIMIT_IN_SECONDS);
            String[] greedyResult =
                    getResultFromThread(greedyFuture, TIME_LIMIT_IN_SECONDS);
            String[] bestFirstResult =
                    getResultFromThread(bestFirstFuture, TIME_LIMIT_IN_SECONDS);

            result.put("Dynamic-runtime",           dynamicResult[0]);
            result.put("Dynamic-tardiness",         dynamicResult[1]);
            result.put("Approx-e=0.1-runtime",      approxe01Result[0]);
            result.put("Approx-e=0.1-tardiness",    approxe01Result[1]);
            result.put("Approx-e=0.2-runtime",      approxe02Result[0]);
            result.put("Approx-e=0.2-tardiness",    approxe02Result[1]);
            result.put("Approx-e=0.3-runtime",      approxe03Result[0]);
            result.put("Approx-e=0.3-tardiness",    approxe03Result[1]);
            result.put("Greedy-runtime",            greedyResult[0]);
            result.put("Greedy-tardiness",          greedyResult[1]);
            result.put("BestFirst-runtime",         bestFirstResult[0]);
            result.put("BestFirst-tardiness",       bestFirstResult[1]);

            System.out.println((System.currentTimeMillis() - begin)/1000 +
                    ": Finished processing " + filename);
            print_resultline(result);
        }

        System.out.println("Finished processing, shutting down collector");
        executorService.shutdown();
    }

    private String[] getResultFromThread(Future<String[]> future, int
            timeOutInSeconds) {
        try {
            return future.get(timeOutInSeconds, TimeUnit.SECONDS);
        } catch (   InterruptedException
                | ExecutionException
                | TimeoutException e) {
            return new String[]{"NaN", "NaN"};
        }
    }

    private void fillFileQueue() {
        File file = new File(inputFileName);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ( (line = br.readLine()) != null && fileQueue.size() < 5) {
                String filename = line.split("\t")[0] + ".dat";
                fileQueue.add(filename);
            }

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
    }



    public static void main(String[] args) throws FileNotFoundException {
        DataCollector2 dataCollector = new DataCollector2();
    }
}