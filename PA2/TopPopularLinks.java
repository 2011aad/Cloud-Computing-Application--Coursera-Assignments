import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.*;
import java.lang.Integer;
import java.util.*;

// >>> Don't Change
public class TopPopularLinks extends Configured implements Tool {
    public static final Log LOG = LogFactory.getLog(TopPopularLinks.class);

    public static String delimiters = " :";

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TopPopularLinks(), args);
        System.exit(res);
    }

    public static class IntArrayWritable extends ArrayWritable {
        public IntArrayWritable() {
            super(IntWritable.class);
        }

        public IntArrayWritable(Integer[] numbers) {
            super(IntWritable.class);
            IntWritable[] ints = new IntWritable[numbers.length];
            for (int i = 0; i < numbers.length; i++) {
                ints[i] = new IntWritable(numbers[i]);
            }
            set(ints);
        }
    }
// <<< Don't Change

    @Override
    public int run(String[] args) throws Exception {
        // TODO
        Configuration conf = this.getConf();
        FileSystem fs = FileSystem.get(conf);
        Path tmpPath = new Path("/mp2/tmp");
        fs.delete(tmpPath, true);

        Job jobA = Job.getInstance(conf, "Top Popular Links");
        jobA.setOutputKeyClass(IntWritable.class);
        jobA.setOutputValueClass(IntWritable.class);

        jobA.setMapperClass(LinkCountMap.class);
        jobA.setReducerClass(LinkCountReduce.class);

        FileInputFormat.setInputPaths(jobA, new Path(args[0]));
        FileOutputFormat.setOutputPath(jobA, tmpPath);

        jobA.setJarByClass(TopPopularLinks.class);
        jobA.waitForCompletion(true);

        Job jobB = Job.getInstance(conf, "Top Popular Links");
        jobB.setOutputKeyClass(IntWritable.class);
        jobB.setOutputValueClass(IntWritable.class);

        jobB.setMapOutputKeyClass(NullWritable.class);
        jobB.setMapOutputValueClass(IntArrayWritable.class);

        jobB.setMapperClass(TopLinksMap.class);
        jobB.setReducerClass(TopLinksReduce.class);
        jobB.setNumReduceTasks(1);

        FileInputFormat.setInputPaths(jobB, tmpPath);
        FileOutputFormat.setOutputPath(jobB, new Path(args[1]));

        jobB.setInputFormatClass(KeyValueTextInputFormat.class);
        jobB.setOutputFormatClass(TextOutputFormat.class);

        jobB.setJarByClass(TopPopularLinks.class);
        return jobB.waitForCompletion(true) ? 0 : 1;
    }

    public static class LinkCountMap extends Mapper<Object, Text, IntWritable, IntWritable> {
        // TODO
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            //TODO
            String line = value.toString();
            StringTokenizer t = new StringTokenizer(line, delimiters);
            Integer i = Integer.valueOf(t.nextToken());
            context.write(new IntWritable(i), new IntWritable(0));
            while(t.hasMoreTokens()){
                i = Integer.valueOf(t.nextToken());
                context.write(new IntWritable(i), new IntWritable(1));
            }
        }
    }

    public static class LinkCountReduce extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
        // TODO
        @Override
        public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            //TODO
            int sum = 0;
            for(IntWritable i: values){
                sum += i.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    public static class TopLinksMap extends Mapper<Text, Text, NullWritable, IntArrayWritable> {
        Integer N;
        List<Pair<Integer,Integer>> l = new ArrayList<>();

        @Override
        protected void setup(Context context) throws IOException,InterruptedException {
            Configuration conf = context.getConfiguration();
            this.N = conf.getInt("N", 10);
        }
        // TODO
        @Override
        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            // TODO
            l.add(new Pair<>(Integer.valueOf(value.toString()),Integer.valueOf(key.toString())));
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            // TODO
            Collections.sort(l, Collections.reverseOrder());
            for(int i=0;i<N;i++){
                Integer[] nums = {l.get(i).second, l.get(i).first};
                context.write(NullWritable.get(), new IntArrayWritable(nums));
            }
        }
    }

    public static class TopLinksReduce extends Reducer<NullWritable, IntArrayWritable, IntWritable, IntWritable> {
        Integer N;

        @Override
        protected void setup(Context context) throws IOException,InterruptedException {
            Configuration conf = context.getConfiguration();
            this.N = conf.getInt("N", 10);
        }
        // TODO
        @Override
        public void reduce(NullWritable key, Iterable<IntArrayWritable> values, Context context) throws IOException, InterruptedException {
            // TODO
            for(IntArrayWritable v:values){
                IntWritable[] i = (IntWritable[])v.toArray();
                context.write(i[0], i[1]);
            }
        }
    }
}

// >>> Don't Change
class Pair<A extends Comparable<? super A>,
        B extends Comparable<? super B>>
        implements Comparable<Pair<A, B>> {

    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static <A extends Comparable<? super A>,
            B extends Comparable<? super B>>
    Pair<A, B> of(A first, B second) {
        return new Pair<A, B>(first, second);
    }

    @Override
    public int compareTo(Pair<A, B> o) {
        int cmp = o == null ? 1 : (this.first).compareTo(o.first);
        return cmp == 0 ? (this.second).compareTo(o.second) : cmp;
    }

    @Override
    public int hashCode() {
        return 31 * hashcode(first) + hashcode(second);
    }

    private static int hashcode(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair))
            return false;
        if (this == obj)
            return true;
        return equal(first, ((Pair<?, ?>) obj).first)
                && equal(second, ((Pair<?, ?>) obj).second);
    }

    private boolean equal(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ')';
    }
}
// <<< Don't Change
