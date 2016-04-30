package hadoop.ifba;


import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
 * Este mapper faz o filter dos usuários brasileiros
 */	
public class FilterMap extends Mapper<LongWritable, Text, Text, IntWritable> {
	 
    private final static IntWritable one = new IntWritable(1);
    public String mapRegex = ".*[bB]ra[zs]il.*";
        
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String localization = null;
        java.util.Map<String, String> parsed;
        
        try {                  	
        	parsed = Util.transformXmlToMap(line);
        	
        	if(parsed.containsKey("Location")){
        		localization = parsed.get("Location").toLowerCase();
        	}
        	
		 	if(localization != null){
	    		if(localization.toString().matches(mapRegex)) {	    	
	    			context.write(value, one);
	    		}   
		 	}           
         } catch (Exception e) {
        	 System.out.println("Erro : " + e.getMessage());
         }                    
    }
 } 

