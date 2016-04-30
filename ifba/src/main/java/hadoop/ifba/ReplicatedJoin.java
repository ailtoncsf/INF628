package hadoop.ifba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class ReplicatedJoin {

	public static class ReplicatedJoinMapper extends Mapper<Object, Text, Text, Text> {

		private HashMap<String, String> userIdToInfo = new HashMap<String, String>();

		private Text outvalue = new Text();
		private String joinType = null;

		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			try {
				Path[] files = DistributedCache.getLocalCacheFiles(context.getConfiguration());

				if (files == null || files.length == 0) {
					throw new RuntimeException(
							"Top Ten user is not set in DistributedCache");
				}

				// Read all files in the DistributedCache
				for (Path p : files) {
					BufferedReader rdr = new BufferedReader(
							new InputStreamReader(
									new FileInputStream(
											new File(p.toString()))));

					String line;
					// For each record in the user file
					while ((line = rdr.readLine()) != null) {

						// Get the user ID for this record
						Map<String, String> parsed = Util.transformXmlToMap(line);
						String userId = parsed.get("id");

						if (userId != null) {
							// Map the user ID to the record
							userIdToInfo.put(userId, line);
						}
					}
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// Get the join type
			joinType = context.getConfiguration().get("join.type");
		}

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			// Parse the input string into a nice map
			Map<String, String> parsed = Util.transformXmlToMap(value.toString());

			String ownerUserId = parsed.get("OwnerUserId");

			if (ownerUserId == null) {
				return;
			}

			String userInformation = userIdToInfo.get(ownerUserId);

			// If the user information is not null, then output
			if (userInformation != null) {
				outvalue.set(userInformation);
				context.write(value, outvalue);
			} else if (joinType.equalsIgnoreCase("leftouter")) {
				// If we are doing a left outer join, output the record with an
				// empty value
				context.write(value, new Text(""));
			}
		}
	}
}

