import java.io.*;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class MP1 {
    Random generator;
    String userName;
    String inputFileName;
    String delimiters = " \t,;.?!-:@[](){}_*/";
    String[] stopWordsArray = {"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
            "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its",
            "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that",
            "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having",
            "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while",
            "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before",
            "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again",
            "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each",
            "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than",
            "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"};

    void initialRandomGenerator(String seed) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA");
        messageDigest.update(seed.toLowerCase().trim().getBytes());
        byte[] seedMD5 = messageDigest.digest();

        long longSeed = 0;
        for (int i = 0; i < seedMD5.length; i++) {
            longSeed += ((long) seedMD5[i] & 0xffL) << (8 * i);
        }

        this.generator = new Random(longSeed);
    }

    Integer[] getIndexes() throws NoSuchAlgorithmException {
        Integer n = 10000;
        Integer number_of_lines = 50000;
        Integer[] ret = new Integer[n];
        this.initialRandomGenerator(this.userName);
        for (int i = 0; i < n; i++) {
            ret[i] = generator.nextInt(number_of_lines);
        }
        return ret;
    }

    public MP1(String userName, String inputFileName) {
        this.userName = userName;
        this.inputFileName = inputFileName;
    }

    public String[] process() throws Exception {
        String[] ret = new String[20];
        FileInputStream inputStream = new FileInputStream(inputFileName);
        Scanner reader = new Scanner(inputStream, "UTF-8");
        String[] text = new String[50000];
        List<String> stops = Arrays.asList(stopWordsArray);

        Map<String,Integer> map = new HashMap<String,Integer>();
        int line_num = 0;
        while(reader.hasNextLine()){
            text[line_num++] = reader.nextLine();
        }

        Integer[] indexes = getIndexes();
        for(Integer i:indexes){
            StringTokenizer tokens = new StringTokenizer(text[i], delimiters);
            while (tokens.hasMoreTokens()) {
                String word = tokens.nextToken().toLowerCase().trim();
                if(!stops.contains(word)){
                    if(map.containsKey(word)) map.put(word,map.get(word)+1);
                    else map.put(word,1);
                }
            }
        }

        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        for(int i=0;i<ret.length;i++){
            int max = 0;
            String max_key = "";
            Map.Entry<String, Integer> e = null;
            for(Map.Entry<String, Integer> entry : entrySet){
                if(entry.getValue()>max || (entry.getValue()==max && entry.getKey().compareTo(max_key)<0)){
                    max = entry.getValue();
                    max_key = entry.getKey();
                    e = entry;
                }
            }
            ret[i] = max_key;
            entrySet.remove(e);
        }

        return ret;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1){
            System.out.println("MP1 <User ID>");
        }
        else {
            String userName = args[0];
            String inputFileName = "./input.txt";
            MP1 mp = new MP1(userName, inputFileName);
            String[] topItems = mp.process();
            for (String item: topItems){
                System.out.println(item);
            }
        }
    }
}
