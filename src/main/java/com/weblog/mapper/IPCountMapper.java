package com.weblog.mapper;

import com.weblog.util.LogParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper for Top IP Address analysis.
 * 
 * Input: Each line of Apache access log
 * Output: (ipAddress, 1)
 * 
 * Identifies the most active visitors, bots, and crawlers
 * by counting requests per IP address.
 */
public class IPCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private final Text ipAddress = new Text();
    private final LogParser parser = new LogParser();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        if (parser.parse(line)) {
            ipAddress.set(parser.getIp());
            context.write(ipAddress, ONE);
        }
    }
}
