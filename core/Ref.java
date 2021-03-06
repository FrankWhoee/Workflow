package bot.workflow.core;

import java.awt.Color;
import java.io.File;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class Ref {
	
	
	//Paths, prefix and version
	public static final String prefix = "wf.";
	public static final String version = "2.2.0";
	public static String jarPath = ""; 
	public static File workflowDB = new File("../WorkflowDB/workflow.json");
	public static File workflowDBParent  = new File("../WorkflowDB");
	public static File temp = new File("../WorkflowDB/temp/");
	
	public static final ArrayList<Long> adminIds = new ArrayList<>(Arrays.asList(194857448673247235L));

	//Used when running project as a jar file.
	static{
		try {
			workflowDB = new File(App.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "WorkflowDB/workflow.json");
			workflowDBParent = new File(App.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "WorkflowDB");
			temp = new File(App.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "temp/");
		}catch(Exception e) {
			System.out.println("Error getting JAR file path.");
		}
	}

	
	//Dates
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static final DateFormat dateFormatTrimmed = new SimpleDateFormat("yyyy/MM/dd");
	public static final Date DEFAULT_DATE = new Date(32503680000000L);
	
	//URLs
	public static final String logoURL = "https://raw.githubusercontent.com/FrankWhoee/Workflow/master/wfLogo.png";
	
	//Colours
	public static final Color RED = new Color(255, 25, 25);
	public static final Color GREEN = new Color(0, 198, 26);
	public static final Color BLUE = new Color(0, 145, 198);
	public static final Color YELLOW = new Color(0xffe100);
	
	//Help messages
	public static final String generalHelpMessage = "```How to use Workflow:\n"
			+"\n\nSYNTAX:\n"
			+ "\"\" = name \n" + 
			"'' = description\n" + 
			"<> = deadline\n" + 
			"?? = command\n" + 
			"() = index\n" + 
			"[] = subindex [CURRENTLY UNAVAILABLE]\n" + 
			"{} = percentage\n"
			+ "\n\n"+prefix+"helpProject"
					+ "\n     -Returns all the commands for using a project."
			+ "\n\n"+prefix+"helpTask"
					+ "\n     -Returns all the commands for using a task."
			+ "\n\n"+prefix+"helpMember"
					+ "\n     -Returns all the commands for managing members."
			+ "\n\n"+prefix+"helpPT"
					+ "\n     -Returns all the commands that both projects and tasks share."
			+ "\n\n"+prefix+"helpMisc"
					+ "\n     -Returns all the miscellaneous commands."
			+ "\n\n"+prefix+"helpCosmetic"
					+ "\n     -Returns all the cosmetic commands you may need."
			+ "```";
	
	public static final String helpProjectMessage = "**How to manage your projects:**"
			+ "\n\n`"+prefix+"createProject \"Project Name\" 'Project Description' <yyyy/MM/dd HH:mm:ss> #projectChannel @Member1 @Member2 @Member3...`"
			+ "\n*-Creates a new project. The date within <> is the deadline. Each project will have its own channel. If no name is specified, the "
			+ "project name will default to the #projectChannel's name. The project description is optional.*"
			
			+ "\n\n`"+prefix+"getProject #projectChannel`"
			+ "\n*-Shows all the info for #projectChannel. If no project channel is specified, it defaults to the channel the command was typed "
			   + "in.*"
			+ "\n\n`"+prefix+"broadcast \"Message\"`"
			+ "\n*-Broadcasts the Message to all team members in their DMs.*"
			;
	
	public static final String helpTaskMessage = "**How to manage your tasks:**"
			+ "\n\n`"+prefix+"addTask \"Task Name\" 'Task Description' <yyyy/MM/dd HH:mm:ss> #projectChannel @Member1, @Member2, @Member3...`"
			+ "\n*-Creates a new task. The date within <> is the deadline. #projectChannel is optional, if the #projectChannel is not "
			+ "included, it will default to the channel the command was typed in. All parameters are optional, except for the Task name. "
			+ "You can choose to edit other parameters later.*"
			+ "\n\n`"+prefix+"remove #projectChannel (Index)`"
			+ "\n*-Removes a task. #projectChannel is optional, if the #projectChannel is not "
			+ "included, it will default to the channel the command was typed in.*"
			+ "\n\n`"+prefix+"task (Index)`"
			+ "\n*-Shows all the info for the specified task. Only one parameter is neccesary.*"
			+ "\n\n`"+prefix+"getTasks #projectChannel`"
			+ "\n*-Shows all the tasks for #projectChannel. If no project channel is specified, it defaults to the channel the command was typed in.*"
			;
	
	public static final String helpMemberMessage = "**How to manage your members:**"
			+ "\n\n`"+prefix+"addMembers/addMember #projectChannel \"Task name\"/(Index) @Member1 @Member2...`"
			+ "\n*-Adds members to the task/project. If no task name is specified, Workflow adds members to project.*"
			+ "\n\n`"+prefix+"removeMembers/removeMember #projectChannel \"Task name\"/(Index) @Member1 @Member2...`"
			+ "\n*-Removes members from the task/project. If no task name is specified, Workflow removes members to project.*"
			;
	
	public static final String helpPTMessage = "**How to manage your projects and tasks:**"
			+ "\n\n`"+prefix+"edit \"Name\" 'Description' <yyyy/MM/dd HH:mm:ss> #projectChannel @Member1 @Member2 @Member3...`"
			+ "\n*-All parameters are optional. Must enter at least one parameter. If any members are tagged, then list of members is reset to "
			   + "tagged members in that list.*"
			+ "\n\n`"+prefix+"complete #projectChannel \"Task name\"`"
			+ "\n*-Sets the status of the project/task to Complete. Does not affect completion %. If no Task name is specified, then the project will be set to Complete. "
			+ "If no project channel is specified, then the it will default to the project in the channel the command was typed in.*"
			+ "\n\n`"+prefix+"wip #projectChannel \"Task name\"`"
			+ "\n*-Sets the status of the project/task to in progress. Does not affect completion %. If no Task name is specified, then the project will be set to in progress. "
			+ "If no project channel is specified, then the it will default to the project in the channel the command was typed in.*"
			+ "\n\n`"+prefix+"completion  #projectChannel \"Task name\" (INTEGER)`"
			+ "\n*-Sets the project/task's completion % to the INTEGER. Do not include % in the message. Completion can be greater than 100% or less than 0%*"
			;
	
	public static final String helpMiscMessage = "**Miscellaneous commands:**"
			+ "\n\n`"+prefix+"status`"
					+ "\n*-Prints Workflow's status.*"
			;
	
	public static final String helpCosmeticMessage = "**How to personalise your project:**"
			+ "\n\n`"+prefix+"setLogo \"LOGO_URL\"`"
					+ "\n     -Sets the logo that will be used by Workflow when displaying your project."
			+ "\n\n`"+prefix+"setColour \"Color category\" #0xXXXXXX`"
					+ "\n*-Sets the colours that will be used. Color category can be one of these three:* `WARNING,BEGINNING,DEFAULT` "
					+ "*To choose a colour you want, go to Google and search \"colour picker\" then copy and paste the values in the format of #RRGGBB*"
			+ "\n\n`"+prefix+"getLogo`"
					+ "\n*-Returns logo.*"
			+ "\n\n`"+prefix+"getColours`"
					+ "\n*-Returns colour settings.*"
			;
}
