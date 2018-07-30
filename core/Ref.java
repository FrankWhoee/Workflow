package bot.workflow.core;

import java.awt.Color;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Ref {
	
	
	public static final String prefix = "wf.";
	public static final String version = "1.0.0";
	public static final File workflowDB = new File("../WorkflowDB/workflow.json");
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static final DateFormat dateFormatTrimmed = new SimpleDateFormat("yyyy/MM/dd");
	
	public static final String logoURL = "https://raw.githubusercontent.com/FrankWhoee/Workflow/master/wfLogo.png";
	
	//Colours
	public static final Color RED = new Color(255, 25, 25);
	public static final Color GREEN = new Color(0, 198, 26);
	public static final Color BLUE = new Color(0, 145, 198);
	
	public static final String helpMessage = "```How to use Workflow:\n"
			+ "\n"+prefix+"createProject \"Project Name\" (Project Description) <yyyy/MM/dd HH:mm:ss> #projectChannel @Member1,@Member2,@Member3..."
					+ "\n     -Creates a new project. The date within <> is the deadline. Each project will have its own channel. If no name is specified, the project name will default to the #projectChannel's name."
								+ " The project description is optional."
			+ "\n\n"+prefix+"addTask \"Task Name\" (Task Description) <yyyy/MM/dd HH:mm:ss> #projectChannel @Member1, @Member2, @Member3..."
					+ "\n     -Creates a new task. The date within <> is the deadline. #projectChannel is optional, if the #projectChannel is not "
								+ "included, it will default to the channel the command was typed in."
								
	+ "```";
}
