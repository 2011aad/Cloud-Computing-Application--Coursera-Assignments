import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.*;

/**
 * a bolt that finds the top n words.
 */
public class TopNFinderBolt extends BaseBasicBolt {
  private HashMap<String, Integer> currentTopWords = new HashMap<String, Integer>();
  private ArrayList<String> words = new ArrayList<String>();
  private ArrayList<Integer> counts = new ArrayList<Integer>();
  private int N;

  private long intervalToReport = 20;
  private long lastReportTime = System.currentTimeMillis();

  public TopNFinderBolt(int N) {
    this.N = N;
  }

  @Override
  public void execute(Tuple tuple, BasicOutputCollector collector) {
 /*
    ----------------------TODO-----------------------
    Task: keep track of the top N words


    ------------------------------------------------- */
    int i;
    currentTopWords.clear();
    String word = tuple.getString(0);
    Integer count = tuple.getIntegerByField("count");

    for(i=0;i<N && i<counts.size();i++){
        if(count>counts.get(i)){
            counts.add(i+1, count);
            words.add(i+1, word);
            break;
        }
    }
    if(i==counts.size()){
        counts.add(count);
        words.add(word);
    }

    if(counts.size()>N){
        counts.subList(N,counts.size()-1).clear();
        words.subList(N,words.size()-1).clear();
    }

    for(i=0;i<counts.size() && i<N;i++) currentTopWords.put(words.get(i), counts.get(i));

    //reports the top N words periodically
    if (System.currentTimeMillis() - lastReportTime >= intervalToReport) {
      collector.emit(new Values(printMap()));
      lastReportTime = System.currentTimeMillis();
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {

     declarer.declare(new Fields("top-N"));

  }

  public String printMap() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("top-words = [ ");
    for (String word : currentTopWords.keySet()) {
      stringBuilder.append("(" + word + " , " + currentTopWords.get(word) + ") , ");
    }
    int lastCommaIndex = stringBuilder.lastIndexOf(",");
    stringBuilder.deleteCharAt(lastCommaIndex + 1);
    stringBuilder.deleteCharAt(lastCommaIndex);
    stringBuilder.append("]");
    return stringBuilder.toString();

  }
}
