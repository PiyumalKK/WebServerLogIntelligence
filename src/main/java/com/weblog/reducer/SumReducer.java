package com.weblog.reducer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Generic Sum Reducer - Sums up all integer values for each key.
 * 
 * This reducer is reused across multiple analysis jobs:
 * - Status Code Distribution
 * - IP Count
 * - Hourly Traffic Pattern
 * - URL Count
 * - HTTP Method Distribution
 * 
 * Input: (key, [1, 1, 1, ...])
 * Output: (key, totalCount)
 */
public class SumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private final IntWritable result = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {

        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }

        result.set(sum);
        context.write(key, result);
    }
}
