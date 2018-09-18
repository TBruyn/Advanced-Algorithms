package com.company;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class Main {

    /*
    First row is processing time, second row is due dates
     */
    private int[][] jobs;

    public static void main(String args[]) {
        if (args.length == 0) {
            System.exit(1);
        }
        FileInputStream in;
        try {
            in = new FileInputStream(args[1]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initJobs() {

    }

    private int calculateTardiness(int[] set) {
        return 0;
    }

    /*
    S
     */
    private int[] getSubset(int i, int j, int k) {
        return null;
    }

    private int calculateCompletionTime(int startingTime, int[] set) {
        return 0;
    }
}
