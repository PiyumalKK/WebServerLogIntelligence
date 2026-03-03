package com.weblog.driver;

import com.weblog.mapper.*;
import com.weblog.reducer.SumReducer;
import com.weblog.reducer.TopNReducer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Main Driver class for Web Server Log Intelligence.
 * 
 * Configures and runs MapReduce jobs for different log analysis tasks.
 * 
 * Usage:
 * hadoop jar WebServerLogIntelligence-1.0.jar
 * com.weblog.driver.LogAnalysisDriver
 * <analysis_type> <input_path> <output_path>
 * 
 * Analysis Types:
 * statuscode - HTTP Status Code Distribution
 * topips - Top 10 IP Addresses by Request Count
 * hourly - Hourly Traffic Pattern Analysis
 * topurls - Top 10 Most Accessed URLs
 * httpmethod - HTTP Method Distribution (GET, POST, etc.)
 * all - Run ALL analyses sequentially
 */
public class LogAnalysisDriver extends Configured implements Tool {

    /**
     * Prints the usage information for this program.
     */
    private static void printUsage() {
        System.err.println("========================================================");
        System.err.println("  Web Server Log Intelligence - MapReduce Analysis Tool");
        System.err.println("========================================================");
        System.err.println();
        System.err.println("Usage: hadoop jar WebServerLogIntelligence-1.0.jar \\");
        System.err.println("       com.weblog.driver.LogAnalysisDriver \\");
        System.err.println("       <analysis_type> <input_path> <output_path>");
        System.err.println();
        System.err.println("Analysis Types:");
        System.err.println("  statuscode  - HTTP Status Code Distribution (200, 404, 500, etc.)");
        System.err.println("  topips      - Top 10 IP Addresses by Request Count");
        System.err.println("  hourly      - Hourly Traffic Pattern Analysis (00:00 - 23:59)");
        System.err.println("  topurls     - Top 10 Most Accessed URLs");
        System.err.println("  httpmethod  - HTTP Method Distribution (GET, POST, PUT, etc.)");
        System.err.println("  all         - Run ALL analyses sequentially");
        System.err.println();
        System.err.println("Example:");
        System.err.println("  hadoop jar WebServerLogIntelligence-1.0.jar \\");
        System.err.println("    com.weblog.driver.LogAnalysisDriver \\");
        System.err.println("    statuscode /input/access.log /output/statuscode");
        System.err.println();
    }

    @Override
    public int run(String[] args) throws Exception {

        if (args.length < 3) {
            printUsage();
            return -1;
        }

        String analysisType = args[0].toLowerCase();
        String inputPath = args[1];
        String outputBasePath = args[2];

        if ("all".equals(analysisType)) {
            return runAllJobs(inputPath, outputBasePath);
        }

        return runSingleJob(analysisType, inputPath, outputBasePath);
    }

    /**
     * Runs all five analysis jobs sequentially.
     */
    private int runAllJobs(String inputPath, String outputBasePath) throws Exception {
        String[] analyses = { "statuscode", "topips", "hourly", "topurls", "httpmethod" };
        String[] names = {
                "HTTP Status Code Distribution",
                "Top 10 IP Addresses",
                "Hourly Traffic Pattern",
                "Top 10 Most Accessed URLs",
                "HTTP Method Distribution"
        };

        System.out.println("==========================================================");
        System.out.println("  Running ALL 5 Analyses on Web Server Access Logs");
        System.out.println("==========================================================\n");

        int successCount = 0;
        for (int i = 0; i < analyses.length; i++) {
            System.out.println("------------------------------------------------------");
            System.out.printf("  [%d/5] Running: %s%n", (i + 1), names[i]);
            System.out.println("------------------------------------------------------");

            String outputPath = outputBasePath + "/" + analyses[i];
            int result = runSingleJob(analyses[i], inputPath, outputPath);

            if (result == 0) {
                System.out.printf("  ✓ %s completed successfully.%n%n", names[i]);
                successCount++;
            } else {
                System.err.printf("  ✗ %s FAILED.%n%n", names[i]);
            }
        }

        System.out.println("==========================================================");
        System.out.printf("  Completed: %d/5 analyses successful.%n", successCount);
        System.out.printf("  Results saved to: %s%n", outputBasePath);
        System.out.println("==========================================================");

        return (successCount == analyses.length) ? 0 : 1;
    }

    /**
     * Configures and runs a single MapReduce job based on the analysis type.
     */
    private int runSingleJob(String analysisType, String inputPath, String outputPath)
            throws Exception {

        Configuration conf = getConf();
        Job job;

        switch (analysisType) {
            case "statuscode":
                job = createJob(conf, "Log Analysis: HTTP Status Code Distribution",
                        StatusCodeMapper.class, SumReducer.class, SumReducer.class,
                        inputPath, outputPath);
                break;

            case "topips":
                conf.setInt("topn.count", 10);
                job = createJob(conf, "Log Analysis: Top 10 IP Addresses",
                        IPCountMapper.class, SumReducer.class, TopNReducer.class,
                        inputPath, outputPath);
                break;

            case "hourly":
                job = createJob(conf, "Log Analysis: Hourly Traffic Pattern",
                        HourlyTrafficMapper.class, SumReducer.class, SumReducer.class,
                        inputPath, outputPath);
                break;

            case "topurls":
                conf.setInt("topn.count", 10);
                job = createJob(conf, "Log Analysis: Top 10 Most Accessed URLs",
                        TopURLMapper.class, SumReducer.class, TopNReducer.class,
                        inputPath, outputPath);
                break;

            case "httpmethod":
                job = createJob(conf, "Log Analysis: HTTP Method Distribution",
                        HTTPMethodMapper.class, SumReducer.class, SumReducer.class,
                        inputPath, outputPath);
                break;

            default:
                System.err.println("ERROR: Unknown analysis type: '" + analysisType + "'");
                printUsage();
                return -1;
        }

        // Run the job and wait for completion
        boolean success = job.waitForCompletion(true);

        // Print job counters summary
        System.out.println("\n--- Job Counters ---");
        System.out.printf("  Map input records:    %d%n",
                job.getCounters().findCounter("org.apache.hadoop.mapreduce.TaskCounter", "MAP_INPUT_RECORDS")
                        .getValue());
        System.out.printf("  Map output records:   %d%n",
                job.getCounters().findCounter("org.apache.hadoop.mapreduce.TaskCounter", "MAP_OUTPUT_RECORDS")
                        .getValue());
        System.out.printf("  Reduce output records: %d%n",
                job.getCounters().findCounter("org.apache.hadoop.mapreduce.TaskCounter", "REDUCE_OUTPUT_RECORDS")
                        .getValue());

        return success ? 0 : 1;
    }

    /**
     * Creates and configures a MapReduce Job with the specified components.
     */
    @SuppressWarnings("rawtypes")
    private Job createJob(Configuration conf, String jobName,
            Class<? extends org.apache.hadoop.mapreduce.Mapper> mapperClass,
            Class<? extends org.apache.hadoop.mapreduce.Reducer> combinerClass,
            Class<? extends org.apache.hadoop.mapreduce.Reducer> reducerClass,
            String inputPath, String outputPath) throws Exception {

        Job job = Job.getInstance(conf, jobName);
        job.setJarByClass(LogAnalysisDriver.class);

        // Set Mapper
        job.setMapperClass(mapperClass);

        // Set Combiner (for local aggregation - optimization)
        job.setCombinerClass(combinerClass);

        // Set Reducer
        job.setReducerClass(reducerClass);

        // Set output key/value types
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // Set input/output formats
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Set input/output paths
        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new Configuration(), new LogAnalysisDriver(), args);
        System.exit(exitCode);
    }
}
