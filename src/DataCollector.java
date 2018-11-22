import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;

public class DataCollector {
    private static final int TIME_LIMIT_IN_SECONDS = 5;
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

    public DataCollector() {
        fileQueue = new LinkedList<>();
        fillFileQueue();
        processFile();

//        new File(outputFileName).delete();
//        print_headers();
    }

    /**
     * Read the first file in the queue
     * Expected filename format: random_RDD=1.0_TF=1.0_#100.dat
     * @return
     */

    private HashMap<String, String> processFile() {
        HashMap<String, String> result = new HashMap<>();

        ExecutorService executorService = Executors.newFixedThreadPool(6);


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

            Future<?> dynamic = executorService.submit(() -> {
                long startingTime = System.currentTimeMillis();
                try {
                    result.put("Dynamic-tardiness",
                            new Dynamic(problemInstance).calculateTardiness() + "");
                    result.put("Dynamic-runtime",
                            (System.currentTimeMillis() - startingTime) + "");
                } catch (Exception e) {
                    result.put("Dynamic-tardiness", "NaN");
                    result.put("Dynamic-runtime", "NaN");
                }

            });
            Future<?> approxE01 = executorService.submit(() -> {
                long startingTime = System.currentTimeMillis();
                try {
                    result.put("Approx-e=0.1-tardiness",
                            new Dynamic(problemInstance).calculateTardiness() + "");
                    result.put("Approx-e=0.1-runtime",
                            (System.currentTimeMillis() - startingTime) + "");
                } catch (Exception e) {
                    result.put("Approx-e=0.1-tardiness", "NaN");
                    result.put("Approx-e=0.1-runtime", "NaN");
                }

            });
            Future<?> approxE02 = executorService.submit(() -> {
                long startingTime = System.currentTimeMillis();
                try {
                    result.put("Approx-e=0.2-tardiness",
                            new Dynamic(problemInstance).calculateTardiness() + "");
                    result.put("Approx-e=0.2-runtime",
                            (System.currentTimeMillis() - startingTime) + "");
                } catch (Exception e) {
                    result.put("Approx-e=0.2-tardiness", "NaN");
                    result.put("Approx-e=0.2-runtime", "NaN");
                }
            });
            Future<?> approxE03 = executorService.submit(() -> {
                long startingTime = System.currentTimeMillis();
                try {
                    result.put("Approx-e=0.3-tardiness",
                            new Dynamic(problemInstance).calculateTardiness() + "");
                    result.put("Approx-e=0.3-runtime",
                            (System.currentTimeMillis() - startingTime) + "");
                } catch (Exception e) {
                    result.put("Approx-e=0.3-tardiness", "NaN");
                    result.put("Approx-e=0.3-runtime", "NaN");
                }
            });
            Future<?> greedy = executorService.submit(() -> {
                long startingTime = System.currentTimeMillis();
                try {
                    result.put("Greedy-tardiness",
                            new Greedy(problemInstance).getSchedule().getTardiness() +
                                    "");
                    result.put("Greedy-runtime",
                            (System.currentTimeMillis() - startingTime) + "");
                } catch (Exception e) {
                    result.put("Greedy-tardiness", "NaN");
                    result.put("Greedy-runtime", "NaN");
                }

            });
            Future<?> bestFirst = executorService.submit(() -> {
                long startingTime = System.currentTimeMillis();
                try {
                    result.put("BestFirst-tardiness",
                            new BestFirst(problemInstance).getSchedule().getTardiness() + "");
                    result.put("BestFirst-runtime",
                            (System.currentTimeMillis() - startingTime) + "");
                } catch (Exception e) {
                    result.put("BestFirst-tardiness", "NaN");
                    result.put("BestFirst-runtime", "NaN");
                }
            });

            try {
                dynamic.get(TIME_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
                approxE01.get(TIME_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
                approxE02.get(TIME_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
                approxE03.get(TIME_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
                greedy.get(TIME_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
                bestFirst.get(TIME_LIMIT_IN_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }

            System.out.println(result);
            result.clear();
        }
        executorService.shutdown();

        return result;
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
            while ( (line = br.readLine()) != null && fileQueue.size() < 2) {
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
                sb.append(line_to_print.get(headers[i]));
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
        DataCollector dataCollector = new DataCollector();
    }
}
