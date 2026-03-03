package com.weblog.mapper;

import com.weblog.util.LogParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper for HTTP Method Distribution analysis.
 * 
 * Input: Each line of Apache access log
 * Output: (httpMethod, 1)
 * 
 * Counts the distribution of HTTP methods (GET, POST, PUT, DELETE, HEAD, etc.)
 * to understand the nature of traffic hitting the server.
 */
public class HTTPMethodMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private final Text method = new Text();
    private final LogParser parser = new LogParser();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        if (parser.parse(line)) {
            method.set(parser.getHttpMethod());
            context.write(method, ONE);
        }
    }
}
