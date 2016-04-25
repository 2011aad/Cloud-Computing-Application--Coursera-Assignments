
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class FileReaderSpout implements IRichSpout {
  private SpoutOutputCollector _collector;
  private TopologyContext context;
  private FileReader file;
  private boolean completed = false;
  private BufferedReader reader;

  @Override
  public void open(Map conf, TopologyContext context,
                   SpoutOutputCollector collector) {

     /*
    ----------------------TODO-----------------------
    Task: initialize the file reader


    ------------------------------------------------- */
    try {
        this.file = new FileReader(conf.get("inputFile").toString());
    } catch (FileNotFoundException e) {
        throw new RuntimeException("Error reading file " + conf.get("inputFile"));
    }
    this.reader = new BufferedReader(this.file);

    this.context = context;
    this._collector = collector;
  }

  @Override
  public void nextTuple() {

     /*
    ----------------------TODO-----------------------
    Task:
    1. read the next line and emit a tuple for it
    2. don't forget to sleep when the file is entirely read to prevent a busy-loop

    ------------------------------------------------- */
        if (completed) {
            Utils.sleep(100);
        }
        else{
            String str;
            try {
                if((str = this.reader.readLine()) != null) {
                    str = str.trim();
                    if (!str.equals("")) this._collector.emit(new Values(str));
                }
                else{
                    completed = true;
                }
            } catch (Exception e) {
                throw new RuntimeException("Error reading typle", e);
            }
        }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {

    declarer.declare(new Fields("word"));

  }

  @Override
  public void close() {
   /*
    ----------------------TODO-----------------------
    Task: close the file


    ------------------------------------------------- */
    try {
      this.file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }


  @Override
  public void activate() {
  }

  @Override
  public void deactivate() {
  }

  @Override
  public void ack(Object msgId) {
  }

  @Override
  public void fail(Object msgId) {
  }

  @Override
  public Map<String, Object> getComponentConfiguration() {
    return null;
  }
}
