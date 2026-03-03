package com.weblog.reducer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Top-N Reducer - Aggregates counts and outputs only the top N entries.
 * 
 * Used for analyses where we want to find the most frequent entries,
 * such as Top IP addresses or Top URLs.
 * 
 * Uses a TreeMap (sorted by count) to maintain a running top-N list
 * across all reduce calls, then outputs the final top entries in cleanup.
 * 
 * The value of N is configurable via the Hadoop configuration property
 * "topn.count" (default: 10).
 * 
 * Input: (key, [1, 1, 1, ...])
 * Output: (key, totalCount) for the top N entries only
 */
public class TopNReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private final TreeMap<Integer, String> topMap = new TreeMap<>();
    private int topN = 10;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        topN = context.getConfiguration().getInt("topn.count", 10);
    }

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {

        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }

        // Insert into TreeMap (sorted by key = count)
        topMap.put(sum, key.toString());

        // Keep only top N entries
        if (topMap.size() > topN) {
            topMap.remove(topMap.firstKey());
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        // Output top N entries in descending order of count
        for (Map.Entry<Integer, String> entry : topMap.descendingMap().entrySet()) {
            context.write(new Text(entry.getValue()), new IntWritable(entry.getKey()));
        }
    }
}
