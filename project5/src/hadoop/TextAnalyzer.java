
/*
 * Julian Domingo : jad5348
 * Alec Bargas : jad5348
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.*;
import java.io.*;

public class TextAnalyzer extends Configured implements Tool {
    // The four template data types are:
    //     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
    public static final IntWritable ONE = new IntWritable(1);

    public static class TextMapper extends Mapper<LongWritable, Text, Text, Tuple> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
        {
            String sentence = value.toString();
            sentence = filter(sentence);

            StringTokenizer tokenizer = new StringTokenizer(sentence, " +");

            ArrayList<String> words = new ArrayList<String>();

            while (tokenizer.hasMoreTokens()) {
                words.add(tokenizer.nextToken());
            }

            for (int con = 0; con < words.size(); con++) {
                for (int query = 0; query < words.size(); query++) {
                    if (con != query) {
                        Text contextWord = new Text(words.get(con));
                        Text queryWord = new Text(words.get(query));
                        context.write(contextWord, new Tuple(queryWord, ONE));
                    }
                }
            }
        }
        
        private String filter(String unfiltered) {
            unfiltered = unfiltered.toLowerCase();
            //unfiltered = unfiltered.replaceAll("\\W", " ");
            unfiltered = unfiltered.replaceAll("[^A-Za-z]", " ");
            return unfiltered;
        }
    }
    

    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper
    public static class TextCombiner extends Reducer<Text, Tuple, Text, Tuple> {
        public void reduce(Text key, Iterable<Tuple> tuples, Context context) throws IOException, InterruptedException
        {
            Map<String, Integer> map = new HashMap<String, Integer>();

            for (Tuple tuple : tuples) {              
                String queryWord = tuple.getQueryWord().toString(); 
                int count = tuple.getCount().get();

                map.put(queryWord, map.containsKey(queryWord) ? map.get(queryWord) + count : count);
            }

            List<String> lexicographicallySortedWords = new ArrayList<String>();
            lexicographicallySortedWords.addAll(map.keySet());
            Collections.sort(lexicographicallySortedWords);
            
            for (String queryWord : lexicographicallySortedWords) {                
                context.write(key, new Tuple(new Text(queryWord), new IntWritable(map.get(queryWord))));
            }                
        }
    }

    public static class TextReducer extends Reducer<Text, Tuple, Text, Text> {
        private final static Text emptyText = new Text("");

        public void reduce(Text key, Iterable<Tuple> queryTuples, Context context) throws IOException, InterruptedException
        {
            Map<String, Integer> map = new HashMap<String, Integer>();

            for (Tuple tuple : queryTuples) {              
                String queryWord = tuple.getQueryWord().toString(); 
                int count = tuple.getCount().get();

                map.put(queryWord, map.containsKey(queryWord) ? map.get(queryWord) + count : count);
            }

            List<String> lexicographicallySortedWords = new ArrayList<String>();
            lexicographicallySortedWords.addAll(map.keySet());
            Collections.sort(lexicographicallySortedWords);

            // Write out the results; you may change the following example
            // code to fit with your reducer function.
            //   Write out the current context key
            context.write(key, emptyText);
            //   Write out query words and their count
            for (String queryWord : lexicographicallySortedWords) {
                String count = map.get(queryWord).toString() + ">";
                //queryWord.set("<" + queryWord + ",");
                Text queryWordText = new Text("<" + queryWord + ",");
                context.write(queryWordText, new Text(count));
            }
            //   Empty line for ending the current context key
            context.write(emptyText, emptyText);
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();
        // Create job
        Job job = new Job(conf, "jad5348_apb973"); // Replace with your EIDs
        job.setJarByClass(TextAnalyzer.class);

        // Setup MapReduce job
        job.setMapperClass(TextMapper.class);
        //   Uncomment the following line if you want to use Combiner class
        job.setCombinerClass(TextCombiner.class);
        job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        //job.setMapOutputKeyClass(?.class);
        job.setMapOutputValueClass(Tuple.class);

        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputFormatClass(TextOutputFormat.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    // Do not modify the main method
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
        System.exit(res);
    }

    public static class Tuple implements WritableComparable<Tuple> {
        private Text queryWord;
        private IntWritable count;

        public Tuple(Text queryWord, IntWritable count) {
            this.queryWord = new Text(queryWord);
            this.count = new IntWritable(count.get());
        }

        public Tuple() {
            this.queryWord = new Text("");
            this.count = new IntWritable(1);
        } 

        public Text getQueryWord() {
            return queryWord;
        }

        public IntWritable getCount() {
            return count;
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            queryWord.readFields(in);
            count.readFields(in);
        }

        @Override
        public void write(DataOutput out) throws IOException {
            queryWord.write(out);
            count.write(out);
        }        

        @Override
        public String toString() {
            return queryWord + " " + count;
        }

        public int compareTo(Tuple tuple) {
            return queryWord.toString().compareTo(tuple.getQueryWord().toString());
        }

        public int hashCode(Object object) {
            return queryWord.toString().hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Tuple) {
                Tuple tuple = (Tuple) object; 
                return queryWord.toString().equals(tuple.queryWord.toString());
            }
            return false;
        }
    }
}
