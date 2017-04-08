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


public class TextAnalyzer extends Configured implements Tool {
    // The four template data types are:
    //     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
    public static class TextMapper extends Mapper<LongWritable, Text, Text, Tuple> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
        {
            String sentence = value.toString();
            sentence = filter(sentence);
            String[] words = sentence.split(" ");

            for (String queryWord : words) {
                for (String contextWord : words) {
                    if (!queryWord.equals(contextWord)) {
                        context.write(new Text(contextWord), new Tuple(queryWord, new IntWritable(1)))
            }
        }
        
        private String filter(String unfiltered) {
            return unfiltered.toLowerCase().replaceAll("[^a-z0-9 ]", " ");
        }
    }
    

    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper
    public static class TextCombiner extends Reducer<Text, Tuple, Text, Tuple> {
        public void reduce(Text key, Iterable<Tuple> tuples, Context context) throws IOException, InterruptedException
        {
            Map<String, Integer> queryWordQuantities = new HashMap<String, Integer>();

            for (Tuple tuple : tuples) {
                if (queryWordQuantities.contains(tuple.getQueryWord())) {
                    queryWordQuantities.put(tuple.getQueryWord(), tuple.getCount());
                }
                else {
                    queryWordQuantities.put(tuple.getQueryWord(), queryWordQuantities.get(tuple.getQueryWord()) + tuple.getCount());
                }
            }

            for (String queryWord : queryWordQuantities.keySet()) {
                context.write(queryWord, queryWordQuantities.get(queryWord));
            }                
        }
    }

    public static class TextReducer extends Reducer<Text, Tuple, Text, Text> {
        private final static Text emptyText = new Text("");

        public void reduce(Text key, Iterable<Tuple> queryTuples, Context context) throws IOException, InterruptedException
        {
            Map<String, Integer> queryWordQuantities = new HashMap<String, Integer>();

            for (Tuple tuple : tuples) {
                if (queryWordQuantities.contains(tuple.getQueryWord())) {
                    queryWordQuantities.put(tuple.getQueryWord(), tuple.getCount());
                }
                else {
                    queryWordQuantities.put(tuple.getQueryWord(), queryWordQuantities.get(tuple.getQueryWord()) + tuple.getCount());
                }
            }

            // Write out the results; you may change the following example
            // code to fit with your reducer function.
            //   Write out the current context key
            context.write(key, emptyText);
            //   Write out query words and their count
            for(String queryWord: queryWordQuantities.keySet()){
                String count = queryWordQuantities.get(queryWord).toString() + ">";
                queryWordText.set("<" + queryWord + ",");
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
        // job.setCombinerClass(TextCombiner.class);
        job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        //job.setMapOutputKeyClass(?.class);
        //job.setMapOutputValueClass(?.class);

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

    public static class Tuple implements WriteableComparable {
        private Text queryWord;
        private IntWritable count;

        public Tuple(Text queryWord, Text count) {
            this.queryWord = queryWord;
            this.count = count;
        }

        public Tuple() {
            this.queryWord = "";
            this.count = 0;
        }

        public void updateCount(IntWritable count) {
            this.count = count;
        }

        public Text getQueryWord() {
            return first;
        }

        public Text getCount() {
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

        @Override
        public int compareTo(Tuple tuple) {
            int compare = queryWord.compareTo(tuple.queryWord);

            if (cmp != 0) {
                return compare;
            }

            return count.compareTo(tuple.count);
        }

        @Override
        public int hashCode(Object object) {
            return queryWord.hashCode()*163 + count.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Tuple) {
                Tuple tuple = (Tuple) object; 
                return queryWord.equals(tuple.queryWord) && count.equals(tuple.count);
            }
            return false;
        }
    }
}
