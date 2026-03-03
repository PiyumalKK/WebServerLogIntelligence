package com.weblog.driver;

import com.weblog.mapper.*;
import com.weblog.reducer.SumReducer;
import com.weblog.reducer.TopNReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.mockito.Mockito;

import java.io.*;
import java.util.*;

/**
 * Pure Java Local Runner - Simulates Hadoop MapReduce without needing a Hadoop
 * installation.
 * 
 * Running real Hadoop MapReduce on Windows locally is notoriously difficult due
 * to
 * missing native binaries (winutils.exe, hadoop.dll) and permission issues.
 * This class completely bypasses the Hadoop execution framework and simulates
 * the Map -> Shuffle -> Reduce phases purely in Java to let you test your logic
 * locally.
 */
public class LocalRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println(
                    "Usage: java -cp ... com.weblog.driver.LocalRunner <analysis_type> <input_file> <output_dir>");
            return;
        }

        String analysisType = args[0].toLowerCase();
        String inputFile = args[1];
        String outputDir = args[2];

        if ("all".equals(analysisType)) {
            String[] jobs = { "statuscode", "topips", "hourly", "topurls", "httpmethod" };
            System.out.println("Running all 5 jobs locally...");
            for (String job : jobs) {
                runSimulatedMapReduce(job, inputFile, outputDir + "/" + job);
            }
        } else {
            runSimulatedMapReduce(analysisType, inputFile, outputDir);
        }
    }

    private static void runSimulatedMapReduce(String analysisType, String inputFile, String outputDir)
            throws Exception {
        System.out.println("Starting local simulation for: " + analysisType);

        // 1. Determine Mapper and Reducer based on analysis type
        Mapper<LongWritable, Text, Text, IntWritable> mapper;
        Reducer<Text, IntWritable, Text, IntWritable> reducer;
        Configuration conf = new Configuration();

        switch (analysisType) {
            case "statuscode":
                mapper = new StatusCodeMapper();
                reducer = new SumReducer();
                break;
            case "topips":
                mapper = new IPCountMapper();
                reducer = new TopNReducer();
                conf.setInt("topn.count", 10);
                break;
            case "hourly":
                mapper = new HourlyTrafficMapper();
                reducer = new SumReducer();
                break;
            case "topurls":
                mapper = new TopURLMapper();
                reducer = new TopNReducer();
                conf.setInt("topn.count", 10);
                break;
            case "httpmethod":
                mapper = new HTTPMethodMapper();
                reducer = new SumReducer();
                break;
            default:
                throw new IllegalArgumentException("Unknown analysis type: " + analysisType);
        }

        // 2. Simulated Contexts (using simple lists to capture exact output)
        List<Map.Entry<Text, IntWritable>> mapOutput = new ArrayList<>();
        Mapper<LongWritable, Text, Text, IntWritable>.Context mapContext = Mockito.mock(Mapper.Context.class);

        Mockito.doAnswer(invocation -> {
            Text key = new Text((Text) invocation.getArgument(0)); // Clone to avoid reference issues
            IntWritable val = new IntWritable(((IntWritable) invocation.getArgument(1)).get());
            mapOutput.add(new AbstractMap.SimpleEntry<>(key, val));
            return null;
        }).when(mapContext).write(Mockito.any(), Mockito.any());

        // 3. MAP PHASE
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            long byteOffset = 0;
            while ((line = reader.readLine()) != null) {
                java.lang.reflect.Method mapMethod = mapper.getClass().getDeclaredMethod("map", Object.class,
                        Object.class, Mapper.Context.class);
                mapMethod.setAccessible(true);
                mapMethod.invoke(mapper, new LongWritable(byteOffset), new Text(line), mapContext);
                byteOffset += line.length();
            }
        }

        // 4. SHUFFLE PHASE (Group by key)
        Map<String, List<IntWritable>> groupedData = new HashMap<>();
        for (Map.Entry<Text, IntWritable> entry : mapOutput) {
            String keyStr = entry.getKey().toString();
            groupedData.putIfAbsent(keyStr, new ArrayList<>());
            groupedData.get(keyStr).add(entry.getValue());
        }

        // 5. REDUCE PHASE
        List<Map.Entry<Text, IntWritable>> reduceOutput = new ArrayList<>();
        Reducer<Text, IntWritable, Text, IntWritable>.Context reduceContext = Mockito.mock(Reducer.Context.class);

        Mockito.doAnswer(invocation -> {
            Text key = new Text((Text) invocation.getArgument(0));
            IntWritable val = new IntWritable(((IntWritable) invocation.getArgument(1)).get());
            reduceOutput.add(new AbstractMap.SimpleEntry<>(key, val));
            return null;
        }).when(reduceContext).write(Mockito.any(), Mockito.any());
        Mockito.when(reduceContext.getConfiguration()).thenReturn(conf);

        // Call setup()
        java.lang.reflect.Method setupMethod = Reducer.class.getDeclaredMethod("setup", Reducer.Context.class);
        setupMethod.setAccessible(true);
        setupMethod.invoke(reducer, reduceContext);

        // Call reduce() for each group
        for (Map.Entry<String, List<IntWritable>> group : groupedData.entrySet()) {
            java.lang.reflect.Method reduceMethod = reducer.getClass().getDeclaredMethod("reduce", Object.class,
                    Iterable.class, Reducer.Context.class);
            reduceMethod.setAccessible(true);
            reduceMethod.invoke(reducer, new Text(group.getKey()), group.getValue(), reduceContext);
        }

        // Call cleanup()
        java.lang.reflect.Method cleanupMethod = Reducer.class.getDeclaredMethod("cleanup", Reducer.Context.class);
        cleanupMethod.setAccessible(true);
        cleanupMethod.invoke(reducer, reduceContext);

        // 6. Output to file
        new File(outputDir).mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputDir + "/part-r-00000"))) {
            for (Map.Entry<Text, IntWritable> entry : reduceOutput) {
                writer.println(entry.getKey() + "\t" + entry.getValue());
            }
        }

        System.out.println("✓ Finished " + analysisType + " -> output saved to " + outputDir + "/part-r-00000");
    }
}
