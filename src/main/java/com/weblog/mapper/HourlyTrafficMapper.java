package com.weblog.mapper;

import com.weblog.util.LogParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper for Hourly Traffic Pattern analysis.
 * 
 * Input: Each line of Apache access log
 * Output: (hour, 1)
 * 
 * Extracts the hour (0-23) from each request timestamp
 * to reveal peak and off-peak traffic periods.
 */
public class HourlyTrafficMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private final Text hour = new Text();
    private final LogParser parser = new LogParser();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        if (parser.parse(line)) {
            int hourValue = parser.getHour();
            if (hourValue >= 0 && hourValue <= 23) {
                // Format hour with leading zero for proper sorting (00-23)
                hour.set(String.format("%02d:00-%02d:59", hourValue, hourValue));
                context.write(hour, ONE);
            }
        }
    }
}
