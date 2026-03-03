package com.weblog.mapper;

import com.weblog.util.LogParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper for HTTP Status Code Distribution analysis.
 * 
 * Input:  Each line of Apache access log
 * Output: (statusCode, 1)
 * 
 * Example output: ("200", 1), ("404", 1), ("500", 1)
 */
public class StatusCodeMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private final Text statusCode = new Text();
    private final LogParser parser = new LogParser();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        if (parser.parse(line)) {
            // Emit the exact status code (e.g., "200", "404", "301")
            statusCode.set(String.valueOf(parser.getStatusCode()));
            context.write(statusCode, ONE);
        }
    }
}
