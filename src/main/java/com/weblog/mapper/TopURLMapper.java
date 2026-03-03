package com.weblog.mapper;

import com.weblog.util.LogParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper for Most Accessed URLs analysis.
 * 
 * Input: Each line of Apache access log
 * Output: (url, 1)
 * 
 * Counts the frequency of each requested URL/endpoint
 * to identify the most popular pages and resources.
 */
public class TopURLMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private final Text url = new Text();
    private final LogParser parser = new LogParser();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        if (parser.parse(line)) {
            // Normalize URL: remove query parameters for cleaner grouping
            String rawUrl = parser.getUrl();
            String normalizedUrl = normalizeURL(rawUrl);
            url.set(normalizedUrl);
            context.write(url, ONE);
        }
    }

    /**
     * Normalizes the URL by removing query parameters.
     * Example: /product?id=123&color=red -> /product
     */
    private String normalizeURL(String rawUrl) {
        if (rawUrl == null)
            return "/";
        int queryIndex = rawUrl.indexOf('?');
        if (queryIndex > 0) {
            return rawUrl.substring(0, queryIndex);
        }
        return rawUrl;
    }
}
