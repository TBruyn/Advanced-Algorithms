import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

public class DataCollector {
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

    private DataCollector() {
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

        String filename = fileQueue.pop();
        String[] filenameParts = filename.split("_");

        result.put( "Filename",
                    filename);
        result.put( "RDD",
                    filenameParts[1].split("=")[1]);
        result.put( "TF",
                    filenameParts[2].split("=")[1]);
        result.put( "Size",
                    filenameParts[3]
                            .substring(1, filenameParts[3].length() - 4));

        System.out.println(result);
        return result;
    }

    private void fillFileQueue() {
        File file = new File(inputFileName);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ( (line = br.readLine()) != null) {
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
        dataCollector.fillFileQueue();
    }
}
