import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;


public class allSortPerformances {
    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE =  100;
    static long MINVALUE = -100;
    static int numberOfTrials = 1;
    static int MAXINPUTSIZE  = (int) Math.pow(2,5); // 2^29 size start with smaller first maybe 2^10
    static int MININPUTSIZE  =  1;
    static int cnt = 0;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time


    static String ResultsFolderPath = "/home/matt/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    public static void main(String[] args) {
        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("Exp3.txt");
    }


    static void runFullExperiment(String resultsFileName){

        // To open a file to write to
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize += inputSize) {
            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;

            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");

            long[] testList = createRandomIntegerList(inputSize);
            int low = 0;
            int length = testList.length - 1;

            // Print array
            System.out.print(Arrays.toString(testList));


            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // Verify if it is sorted
                int verify = verifySorted(testList, length);
                if (verify == 1)
                    System.out.println("Sorted.");
                else
                    System.out.println("Not sorted.");

                quickSort(testList, low, length);
                // bubbleSortNumberList(testList);
                // selectionSort(testList, length);

                checkSortCorrectness(testList, length);

                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");

            // print after quickSort
            System.out.print("After sort: ");
            System.out.println(Arrays.toString(testList));
        }
    }

    public static int verifySorted (long[] list, int N) {
        int ver = 1;
        // check if sorted
        for (int i = 0; i < N; ++i) {
            if (list[i] > list[i+1]) {
                ver = -1;
                return ver;
            }
        }
        return ver;
    }

    public static void checkSortCorrectness (long[] list, int N) {
        // call verifySorted to see if it is sorted
        int check = verifySorted(list, N);
        // if the verifySorted is 1 then return 1
        if (check == 1)
            System.out.println("Correctly sorted after running sorting function.");
        else
            System.out.println("Not correctly sorted after running sorting function.");
    }

    public static void bubbleSortNumberList(long[] list) {
        /* make N passes through the list (N is the length of the list) */
        for (int i = 0; i < list.length - 1; ++i) {
            /* for index from 0 to N-1, compare item[index] to next it, swap if needed */
            for (int j = 0; j < list.length - 1 - i; ++j) {
                if (list[j] > list[j + 1]) {
                    long tmp = list[j];
                    list[j] = list[j + 1];
                    list[j + 1] = tmp;
                }
            }
        }
    }

    public static void selectionSort (long[] list, int N) {
        for (int i = 0; i < N; ++i) {
            int tmp = i;
            for (int j = i + 1; j < N+1; ++j) {
                if (list[j] < list[tmp])
                    tmp = j;
            }
            long small = list[tmp];
            list[tmp] = list[i];
            list[i] = small;
        }
    }

    public static void quickSort (long[] list, int low, int high) {
        if (list == null || high == 0)
            return;
        if (low >= high)
            return;
        // pick the pivot
        int middle = low + (high - low) / 2;
        long pivot = list[middle];

        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (list[i] < pivot) {
                ++i;
            }
            while (list[j] > pivot) {
                --j;
            }
            if (i <= j) {
                long temp = list[i];
                list[i] = list[j];
                list[j] = temp;
                ++i;
                --j;
            }
        }

        // recursively sort two sub parts
        if (low < j)
            quickSort(list, low, j);
        if (high > i)
            quickSort(list, i, high);
    }

    public static void naiveQuickSort (long[] list) {

    }

    public static void mergeSort(long[] list, int j) {
        int i = 0;
        int mid = j/2;
        long[] top = new long[mid];
        long[] bottom = new long[j - mid];

        if (j <= 1) {
            return;
        }
        for (i = 0; i < mid; ++i) {
            top[i] = list[i];
        }
        for (i = mid; i < j; ++i) {
            bottom[i-mid] = list[i];
        }
        j = j-mid;
        mergeSort(top, mid);
        mergeSort(bottom, j);
        merge(list, top, bottom, mid, j);
    }

    public static void merge(long[] list, long[] top, long[] bottom, int left, int right) {
        int i = 0, j = 0, k = 0;
        while (i < left && j < right) {
            if (top[i] <= bottom[j]) {
                list[k++] = top[i++];
            }
            else {
                list[k++] = bottom[j++];
            }
        }
        while (i < left) {
            list[k++] = top[i++];
        }
        while (j < right) {
            list[k++] = bottom[j++];
        }
    }

    public static long[] createRandomIntegerList(int size) {
        long[] newList = new long[size];
        for(int j=0;j<size;j++){
            newList[j] = (long)(MINVALUE + Math.random() * (MAXVALUE - MINVALUE));
        }

        return newList;

    }
}
