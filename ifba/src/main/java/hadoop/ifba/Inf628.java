package hadoop.ifba;

import hadoop.ifba.MapReduceSideJoin.PostJoinMapper;
import hadoop.ifba.MapReduceSideJoin.UserJoinMapper;
import hadoop.ifba.MapReduceSideJoin.UserJoinReducer;
import hadoop.ifba.ReplicatedJoin.ReplicatedJoinMapper;
import hadoop.ifba.FilterMap;


import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/* Trabalho da disciplina INF628[2015]
 * Aluno: Ailton Filho 
 * Prof.: Sandro
 * 
 * Objetivo: Aplicação de Design Patterns para sistemas MapReduce.
 * Atividades:
 * 
 * 		- Realizar filtro dos usuários brasileiros [ok]
 * 		- Realizar reduce side join (inner join) com dataset de posts [ok]
 * 		- Obter os top ten usuários que mais postaram [ok]
 * 		- Realizar join exibindo id do usuario, displayName e dados da outra tabela do join [ok]
 * 
 * OBSERVAÇÕES: 
 * 		- Executar o comando: mvn clean install p/ compilar projeto e gerar o arquivo .jar
 * 		- hadoop jar nome_do_jar.jar hadoop.ifba.Inf628 [INPUT_PATH_FILTER] [INPUT_PATH_POSTS]
 */
public class Inf628 extends Configured implements Tool {

	public static String JOIN_TYPE = "";
	public static String INPUT_PATH_POSTS = "";
	public static String INPUT_PATH_FILTER = "";
	public static String OUTPUT_PATH_FILTER = "./filter_output";	
	public static String OUTPUT_PATH_REDUCE_SIDE_JOIN = "./join_topten_output";
	public static String OUTPUT_PATH_REPLICATED_JOIN =  "./join_replicated_output";
	
	/*
	 * Rodando Jobs de forma encadeada
	 * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
	 */
	@SuppressWarnings("deprecation")
	public int run(String[] args) throws Exception {
		 
		System.out.println("hadoop jar nome_do_jar.jar hadoop.ifba.Inf628 [INPUT_PATH_FILTER] [INPUT_PATH_POSTS]");
		 
		Configuration conf = getConf();	 
		Path outputFilter = new Path(OUTPUT_PATH_FILTER);
		Path outputJoin = new Path(OUTPUT_PATH_REDUCE_SIDE_JOIN);
		Path outputReplicatedJoin = new Path(OUTPUT_PATH_REPLICATED_JOIN);
	  
	    // Excluindo diretorios caso existam
	    FileSystem hdfs = FileSystem.get(conf);
	    if (hdfs.exists(outputFilter))
	    	hdfs.delete(outputFilter, true);	  
	    if (hdfs.exists(outputJoin))
		    hdfs.delete(outputJoin, true);
	    if (hdfs.exists(outputReplicatedJoin))
		    hdfs.delete(outputReplicatedJoin, true);
	 		 
	   /*
	    * JOB FILTER
	    */		
		Job jobFilter = Job.getInstance(conf, "Filter");
		jobFilter.setJarByClass(FilterMap.class);
		jobFilter.setOutputKeyClass(Text.class);
		jobFilter.setOutputValueClass(IntWritable.class);
		//Este Job é MapOnly, nao tem Reduce
		jobFilter.setNumReduceTasks(0);
		jobFilter.setMapperClass(FilterMap.class);
		jobFilter.setInputFormatClass(TextInputFormat.class);
		//job.setInputFormatClass(XmlInputFormat.class);
		jobFilter.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(jobFilter, new Path(INPUT_PATH_FILTER));
		FileOutputFormat.setOutputPath(jobFilter, new Path(OUTPUT_PATH_FILTER));			
		
		jobFilter.waitForCompletion(true);
		System.out.println("Saída do Filter em: " + OUTPUT_PATH_FILTER);
		
	   /*
	    * REALIZANDO REDUCE SIDE JOIN DO RESULTADO DO FILTER COM DATASET DE POSTS 
	    */
		Job jobReduceSideJoin = Job.getInstance(conf, "Reduce Side Join");
		jobReduceSideJoin.getConfiguration().set("join.type", JOIN_TYPE);
		jobReduceSideJoin.setJarByClass(MapReduceSideJoin.class);
		//DATASET CONTENDO APENAS USERS BRASILEIROS
		MultipleInputs.addInputPath(jobReduceSideJoin, new Path(OUTPUT_PATH_FILTER+"/part-m-00000"), TextInputFormat.class, UserJoinMapper.class);
		//DATASET DE POSTS
		MultipleInputs.addInputPath(jobReduceSideJoin, new Path(INPUT_PATH_POSTS), TextInputFormat.class, PostJoinMapper.class);
		jobReduceSideJoin.setReducerClass(UserJoinReducer.class);
		FileOutputFormat.setOutputPath(jobReduceSideJoin, new Path(OUTPUT_PATH_REDUCE_SIDE_JOIN));
		jobReduceSideJoin.setOutputKeyClass(Text.class);
		jobReduceSideJoin.setOutputValueClass(Text.class);
		jobReduceSideJoin.waitForCompletion(true);
		
		System.out.println("Saída do Reduce Side Join em: " + OUTPUT_PATH_REDUCE_SIDE_JOIN);
	   
		/*
	    * REALIZANDO REPLICATED JOIN COM RESULTADO DO TOP TEN
	    */		
		Job jobReplicatedJoin = Job.getInstance(conf, "Replicated Join");
		jobReplicatedJoin.getConfiguration().set("join.type", JOIN_TYPE);
		jobReplicatedJoin.setJarByClass(ReplicatedJoin.class);

		jobReplicatedJoin.setMapperClass(ReplicatedJoinMapper.class);
		jobReplicatedJoin.setNumReduceTasks(0);
		//setando path do dataset de posts para o join
		TextInputFormat.setInputPaths(jobReplicatedJoin, new Path(INPUT_PATH_POSTS));
		TextOutputFormat.setOutputPath(jobReplicatedJoin, new Path(OUTPUT_PATH_REPLICATED_JOIN));

		jobReplicatedJoin.setOutputKeyClass(Text.class);
		jobReplicatedJoin.setOutputValueClass(Text.class);

		// Configurando DistributedCache
		URI uriPath = new Path(OUTPUT_PATH_REDUCE_SIDE_JOIN +"/part-r-00000").toUri();
		jobReplicatedJoin.addCacheFile(uriPath);
		DistributedCache.setLocalFiles(jobReplicatedJoin.getConfiguration(), OUTPUT_PATH_REDUCE_SIDE_JOIN+"/part-r-00000");

		jobReplicatedJoin.waitForCompletion(true);
		System.out.println("Saída do Replicated Join em: " + OUTPUT_PATH_REPLICATED_JOIN);

		return 0;
	}
	 
	/* 
	 * args[0] - INPUT PATH DO FILTER
	 * args[1] - INPUT PATH DO COUNT POSTS
	 * 
	 * Uso: hadoop jar nome_do_jar.jar hadoop.ifba.Inf628 [INPUT_PATH_FILTER] [INPUT_PATH_POSTS] 
	 */
	public static void main(String[] args) throws Exception {
		
	  INPUT_PATH_FILTER = args[0].toString();
	  INPUT_PATH_POSTS = args[1].toString();
	  JOIN_TYPE = "inner";

	  if (args.length != 2) {
		   System.err.println("Número de argumentos inválido:");
		   System.err.println("hadoop jar nome_do_jar.jar hadoop.ifba.Inf628 [INPUT_PATH_FILTER] [INPUT_PATH_POSTS]");
		   System.exit(0);
	  }
	  ToolRunner.run(new Configuration(), new Inf628(), args);
    }
}       
