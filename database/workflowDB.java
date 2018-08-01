package bot.workflow.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.*;

import bot.workflow.core.Ref;
import bot.workflow.util.StringUtil;
import bot.workflow.wf.*;

public class workflowDB {
	
	public static JsonObject database;
	private static List<Project> projects = new ArrayList<Project>();
	
	
	public static void parseDB() {
		try {
			String raw = StringUtil.readFileAsString(Ref.workflowDB.getPath());
			database = new JsonParser().parse(raw).getAsJsonObject();
			for(String key : database.keySet()) {
				Project p = Project.fromJson(database.get(key).toString());
				try{
					p.activate();
				}catch(Exception e) {
					
				}
				projects.add(p);
			}
			System.out.println("Database parsed succesfully.");
		}catch(JsonParseException jpe) {
			System.err.println("Corrupted JSON detected. Printing stack trace...");
		}catch(Exception e) {
			System.err.println("Error reading JSON database.");
			if(!Ref.workflowDB.exists()) {
				System.err.println("JSON does not exist.");
				if(!Ref.workflowDBParent.exists()) {
					System.err.println("Folder does not exist. Creating new folder...");
					try {
						Ref.workflowDBParent.mkdir();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				database = new JsonObject();
				rawSave();
				System.err.println("New JSON file created.");
			}else {
				System.err.println("Unknown error! Printing stack trace...");
				e.printStackTrace();
			}
		}		
	}
	
	public static void addProject(Project p) {
		projects.add(p);
	}
	
	public static void removeProject(Project p) {
		projects.remove(p);
	}
	
	public static void removeProject(Long projectId) {
		for(int i = 0; i < projects.size(); i++) {
			Project p = projects.get(i);
			if(p.getProjectId().equals(projectId)) {
				projects.remove(i);
				return;
			}
		}
	}
	
	public static boolean hasProject(Project p) {
		return projects.contains(p);
	}
	
	public static boolean hasProject(Long projectId) {
		for(Project p : projects) {
			if(p.getProjectId().equals(projectId)) {
				return true;
			}
		}
		return false;
	}
	
	public static Project getProject(Long projectId) {
		for(Project p : projects) {
			if(p.getProjectId().equals(projectId)) {
				return p;
			}
		}
		return null;
	}
	
	/*public static Project getProject(Long projectId) {
		return Project.fromJson(database.get(Long.toString(projectId)).toString());
	}*/
	
	public static void rawSave() {
		StringUtil.writeToFile(database.toString(), Ref.workflowDB.getPath());
	}
	
	public static void save() {
		for(Project p : projects) {
			database.add(Long.toString(p.getProjectId()), p.toJson());
		}
		Set<String> keys = new TreeSet<String>();
		for(String key : keys) {
			keys.add(key);
		}
		for(String key : keys) {
			if(!hasProject(Long.parseLong(key))) {
				database.remove(key);
			}
		}
		StringUtil.writeToFile(database.toString(), Ref.workflowDB.getPath());
	}
	
}
