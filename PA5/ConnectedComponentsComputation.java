import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;

/**
 * Implementation of the connected component algorithm that identifies
 * connected components and assigns each vertex its "component
 * identifier" (the smallest vertex id in the component).
 */
public class ConnectedComponentsComputation extends
    BasicComputation<IntWritable, IntWritable, NullWritable, IntWritable> {
  /**
   * Propagates the smallest vertex id to all neighbors. Will always choose to
   * halt and only reactivate if a smaller id has been sent to it.
   *
   * @param vertex Vertex
   * @param messages Iterator of messages from the previous superstep.
   * @throws IOException
   */
  @Override
  public void compute(
      Vertex<IntWritable, IntWritable, NullWritable> vertex,
      Iterable<IntWritable> messages) throws IOException {
      //TODO
      int current = vertex.getValue().get();

      if(getSuperstep()==0){
          for(Edge<IntWritable,NullWritable> e: vertex.getEdges()){
              int n = e.getTargetVertexId().get();
              if(n<current) current = n;
          }
          if(current != vertex.getValue().get()){
              vertex.setValue(new IntWritable(current));
              for(Edge<IntWritable,NullWritable> e: vertex.getEdges()){
                  IntWritable n = e.getTargetVertexId();
                  if(n.get()>current) sendMessage(n, vertex.getValue());
              }
          }
          vertex.voteToHalt();
          return;
      }

      boolean flag = false;

      for(IntWritable m: messages){
          int c = m.get();
          if(c<current){
              current = c;
              flag = true;
          }
      }
      if(flag){
          vertex.setValue(new IntWritable(current));
          sendMessageToAllEdges(vertex, vertex.getValue());
      }
      vertex.voteToHalt();
  }
}
