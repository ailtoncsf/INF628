package hadoop.ifba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class MapReduceSideJoin {

	public static class UserJoinMapper extends Mapper<Object, Text, Text, Text> {

		private Text outkey = new Text();
		private Text outvalue = new Text();

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			// Parse the input string into a nice map
			Map<String, String> parsed = Util.transformXmlToMap(value.toString());
			
			//obtendo id do usuario com base no dataset do filter de users
			String userId = parsed.get("Id");

			if (userId == null) {
				return;
			}

			// The foreign join key is the user ID
			outkey.set(userId);

			// Flag this record for the reducer and then output
			outvalue.set("A" + value.toString());
			context.write(outkey, outvalue);
		}
	}
		
	public static class PostJoinMapper extends Mapper<Object, Text, Text, Text> {

		private Text outkey = new Text();
		private Text outvalue = new Text();

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			// Parse the input string into a nice map
			Map<String, String> parsed = Util.transformXmlToMap(value.toString());

			//obtendo chave do dataset de posts
			String userId = parsed.get("OwnerUserId");
			if (userId == null) {
				return;
			}

			// The foreign join key is the user ID
			outkey.set(userId);

			// Flag this record for the reducer and then output
			outvalue.set("B" + value.toString());
			context.write(outkey, outvalue);
		}
	}

	public static class UserJoinReducer extends Reducer<Text, Text, Text, Text> {

		private ArrayList<Text> listA_users = new ArrayList<Text>();
		private ArrayList<Text> listB_posts = new ArrayList<Text>();
		private String joinType = null;
		private ArrayList<User> user = new ArrayList<User>();

		@Override
		public void setup(Context context) {
			// Get the type of join from our configuration
			joinType = context.getConfiguration().get("join.type");
		}
		
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			listA_users.clear();
			listB_posts.clear();
			
			for (Text t : values) {					
				if (t.charAt(0) == 'A') {
					//System.out.print("USUARIO: ");
					listA_users.add(new Text(t.toString().substring(1)));
				} else if (t.charAt(0) == 'B') {
					//System.out.print("POSTS: ");
					listB_posts.add(new Text(t.toString().substring(1)));
				}
			}			
			// Execute our join logic now that the lists are filled
			executeJoinLogic(context);
		}
		
		private void executeJoinLogic(Context context) throws IOException, InterruptedException {
									
			//executando inner join
			if (joinType.equalsIgnoreCase("inner")) {
				
				// If both lists are not empty, join A with B
				if (!listA_users.isEmpty() && !listB_posts.isEmpty()) {
					java.util.Map<String, String> parsed = Util.transformXmlToMap(listA_users.get(0).toString());										
					String displayName = parsed.get("DisplayName");
					Integer qtdPosts = listB_posts.size();
					String userId = parsed.get("Id");					
					
					User currentUser = new User();
					currentUser.id = Integer.parseInt(userId);
					currentUser.nome = displayName;
					currentUser.qtdPosts = qtdPosts;				
					user.add(currentUser);

					//ordenando lista
					Collections.sort(user);	
					//removendo ultimo da lista
					if(user.size() >  10){
						user.remove(user.size() - 1);
					}					
				}
			}
		}
		
		@Override
		protected void cleanup(Context context) throws IOException,	InterruptedException {
			Text row = new Text();
			//Gravando do job saida
			for (User item : user){
				row.set("<row nome=\""+ item.nome +"\" id=\""+ item.id + "\" qtdPosts=\""+ item.qtdPosts +"\" />");
				context.write(new Text(item.qtdPosts.toString()), new Text(row));					
			}
		}					
	}
}


