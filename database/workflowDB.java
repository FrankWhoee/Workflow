package bot.workflow.database;

import com.google.gson.*;

import bot.workflow.core.Ref;
import bot.workflow.util.StringUtil;
import bot.workflow.wf.*;

public class workflowDB {
	
	public static JsonObject database;
	
	
	public static void parseDB() {
		try {
			String raw = StringUtil.readFileAsString(Ref.workflowDB.getPath());
			database = new JsonParser().parse(raw).getAsJsonObject();
		}catch(Exception e) {
			System.err.println("Error reading JSON database.");
			if(Ref.workflowDB.exists()) {
				System.err.println("Corrupted JSON detected.");
			}else {
				System.err.println("JSON does not exist.");
				database = new JsonObject();
				save();
				System.err.println("New JSON file created.");
			}
		}		
	}
	
	public static Project getProject(Long projectId) {
		return Project.fromJson(database.get(Long.toString(projectId)).toString());
	}
	
	public static void save() {
		StringUtil.writeToFile(database.toString(), Ref.workflowDB.getPath());
	}
	
}
