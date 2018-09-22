package bot.workflow.core;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;

import bot.workflow.database.workflowDB;
import bot.workflow.util.MessageHarvester;
import bot.workflow.util.StringUtil;
import bot.workflow.wf.Project;
import bot.workflow.wf.Task;
import bot.workflow.wf.TeamMember;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;


public class App extends ListenerAdapter
{
	
	public static JDA jda;
    public static void main( String[] args )
    {
        try {
			jda = new JDABuilder(AccountType.BOT).setToken(Key.TOKEN).buildBlocking();
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        jda.addEventListener(new App());
        jda.getPresence().setGame(Game.of(Game.GameType.DEFAULT, Ref.prefix + "help"));
        
        //Check if temp is a folder
        if(!Ref.temp.exists()) {
        	//If not, create temp.
        	Ref.temp.mkdir();
        }
        
        workflowDB.parseDB();
    }
    
    @SuppressWarnings("static-access")
	@Override
    public void onMessageReceived(MessageReceivedEvent evt) {
    	//Objects
    	Message objMsg = evt.getMessage();
    	MessageChannel objMsgCh = evt.getTextChannel();
    	User objUser = evt.getAuthor();
    	Guild objGuild = evt.getGuild();
    	
    	String raw = objMsg.getContentRaw();
    	
    	if(!raw.startsWith(Ref.prefix)) {
    		return;
    	}
    	
    	String command = "";
    	String input = "";
    	try{
    		command = raw.substring(Ref.prefix.length(), raw.indexOf(" ")).trim();
    		input = raw.substring(command.length() + Ref.prefix.length() +1).trim();
    	}catch (Exception e) {
    		try {
    			command = raw.substring(Ref.prefix.length()).trim();
    		}catch(Exception exc){
    			return;
    		}	
    		//System.out.print(command);
    		
    	}
    	MessageHarvester mh = MessageHarvester.harvest(objMsg);
    	
    	if(command.equalsIgnoreCase("help")) {
    		objMsgCh.sendMessage(Ref.generalHelpMessage).queue();
    		return;
    		
    	}else if(command.equalsIgnoreCase("helpProject")) {
    		objMsgCh.sendMessage(Ref.helpProjectMessage).queue();
    		return;
    	}else if(command.equalsIgnoreCase("helpTask")) {
    		objMsgCh.sendMessage(Ref.helpTaskMessage).queue();
    		return;
    	}else if(command.equalsIgnoreCase("helpPT")) {
    		objMsgCh.sendMessage(Ref.helpPTMessage).queue();
    		return;
    		
    	}else if(command.equalsIgnoreCase("helpMember")) {
    		objMsgCh.sendMessage(Ref.helpMemberMessage).queue();
    		return;
    		
    	}else if(command.equalsIgnoreCase("helpMisc")) {
    		objMsgCh.sendMessage(Ref.helpMiscMessage).queue();
    		return;
    		
    	}else if(command.equalsIgnoreCase("helpCosmetic")) {
    		objMsgCh.sendMessage(Ref.helpCosmeticMessage).queue();
    		return;
    		
    	}else if(command.equalsIgnoreCase("status")) {
    		EmbedBuilder eb = new EmbedBuilder();
    		eb.setTitle("Workflow " + Ref.version);
    		eb.addField("Status","Online",false);
    		eb.setColor(Ref.BLUE);
    		objMsgCh.sendMessage(eb.build()).queue();
    		return;
    		
    	}else if(command.equalsIgnoreCase("createProject") || command.equalsIgnoreCase("addProject") || command.equalsIgnoreCase("newProject")) {
    		if(workflowDB.hasProject(mh.projectId)) {
    			objMsgCh.sendMessage("This channel already has a project.").queue();
    			return;
    		}
    		if(mh.deadline == null) {
    			mh.deadline = Ref.DEFAULT_DATE;
    		}
    		if(mh.team == null) {
    			mh.team = new ArrayList<>();
    		}
    		mh.team.add(new TeamMember(objUser.getIdLong(),mh.projectId));
    		if(mh.description == null) {
    			mh.description = "";
    		}
    		Project p = new Project(mh.team, mh.name, mh.description, mh.deadline, mh.projectId);
    		workflowDB.addProject(p);
    		try{
    			p.setTimer();
    		}catch(IllegalArgumentException iae) {
    			EmbedBuilder eb = new EmbedBuilder();
        		eb.setColor(Ref.RED);
    			eb.setTitle("Error: Timer delay is invalid.");
    			objMsgCh.sendMessage(eb.build()).queue();
        		return;
    		}
    		workflowDB.save();
    		objMsgCh.sendMessage(p.getEmbed(Ref.GREEN)).queue();
    		return;
    	}else if(command.equalsIgnoreCase("import")) {
    		
    		//Verify file is attached
    		if(objMsg.getAttachments().size() > 0) {
    			if(!(objMsg.getAttachments().size() == 1)) {
    				EmbedBuilder eb = new EmbedBuilder();
    	    		eb.setColor(Ref.RED);
    				eb.setTitle("Error: More than one file attached!");
    				objMsgCh.sendMessage(eb.build()).queue();
    	    		return;
        		}
    		}else {
    			EmbedBuilder eb = new EmbedBuilder();
	    		eb.setColor(Ref.RED);
				eb.setTitle("Error: No file attached!");
				objMsgCh.sendMessage(eb.build()).queue();
	    		return;
    		}
    		
    		//Only one file guaranteed.
    		String downloadURL = objMsg.getAttachments().get(0).getUrl();
    		File f = new File(Ref.temp.getPath() + "/import_"+(Math.random() * 10000000)+".json");
    		try {
				StringUtil.saveFileFromUrl(f, downloadURL);
			} catch (IOException e) {
				e.printStackTrace();
				EmbedBuilder eb = new EmbedBuilder();
	    		eb.setColor(Ref.RED);
				eb.setTitle("Error while downloading import file.");
				objMsgCh.sendMessage(eb.build()).queue();
	    		return;
			}
    		String json;
    		try {
				json = StringUtil.readFileAsString(f.getPath());
			} catch (Exception e) {
				e.printStackTrace();
				EmbedBuilder eb = new EmbedBuilder();
	    		eb.setColor(Ref.RED);
				eb.setTitle("Error while reading import file.");
				objMsgCh.sendMessage(eb.build()).queue();
	    		return;
			}
    		Project p = Project.fromJson(json);
    		p.setProjectId(objMsgCh.getIdLong());
    		p.activate();
    		f.delete();
    		workflowDB.addProject(p);
    		objMsgCh.sendMessage(p.getEmbed(p.getBEGINNING())).queue();
    		workflowDB.save();
    		
    	
    	}else if(command.equalsIgnoreCase("terminate") && Ref.adminIds.contains(objUser.getIdLong())) {
    		EmbedBuilder eb = new EmbedBuilder();
    		eb.setColor(Ref.RED);
			eb.setTitle("Shutting down...");
			objMsgCh.sendMessage(eb.build()).queue();
    		jda.shutdown();
    	}
    	
    	if(!workflowDB.database.has("" + mh.projectId)) {
    		EmbedBuilder eb = new EmbedBuilder();
    		eb.setColor(Ref.RED);
			eb.setTitle("Error: #" + App.jda.getTextChannelById(mh.projectId).getName() + " is not associated with a project.");
			objMsgCh.sendMessage(eb.build()).queue();
    		return;
		}
    	
    	//Now guaranteed that this channel or tagged channel is associated with a project.
    	Project p = workflowDB.getProject(mh.projectId);
    	
    	//Verify that user is part of the project or is admin and has command ?force?
    	if(!Ref.adminIds.contains(objUser.getIdLong()) || !mh.command.equalsIgnoreCase("force")) {
    		if(!p.hasMember(objUser.getIdLong()) && !mh.command.equalsIgnoreCase("force")) {
        		EmbedBuilder eb = new EmbedBuilder();
        		eb.setColor(Ref.RED);
    			eb.setTitle("Error: " + objUser.getName() + " is not on this team.");
    			objMsgCh.sendMessage(eb.build()).queue();
        		return;
    		}
    	}
    	
    	if(mh.command.equalsIgnoreCase("force") && Ref.adminIds.contains(objUser.getIdLong())) {
    		EmbedBuilder eb = new EmbedBuilder();
    		eb.setColor(Ref.YELLOW);
			eb.setTitle("Permission granted.");
			objMsgCh.sendMessage(eb.build()).queue();
    	}
    	
    	//Now guaranteed that this user has access to this project.
    	if(command.equalsIgnoreCase("delete")) {
    		if(mh.name != null && mh.name.equals(p.getName())) {
    			EmbedBuilder eb = new EmbedBuilder();
    			workflowDB.removeProject(mh.projectId);
        		eb.setTitle("Project '"+p.getName()+"' deleted.");
        		eb.setColor(Ref.RED);
        		objMsgCh.sendMessage(eb.build()).queue();
        		workflowDB.save();
    		}else {
    			EmbedBuilder eb = new EmbedBuilder();
    			eb.setTitle("Please type wf.delete \"" + p.getName() + "\" to delete this project.");
    			eb.setColor(Ref.YELLOW);
    			objMsgCh.sendMessage(eb.build()).queue();
    		}
    		
    	}else if(command.equalsIgnoreCase("addTask") || command.equalsIgnoreCase("newTask")) {
    		//System.out.println(mh.deadline);
    		Date deadline = mh.deadline;
    		if(mh.deadline == null) {
    			deadline = Ref.DEFAULT_DATE;
    		}
    		//System.out.println(mh.deadline);
    		//System.out.println(deadline);
    		//System.out.println(Ref.DEFAULT_DATE);
    		if(mh.team == null) {
    			mh.team = new ArrayList<>();
    			mh.team.add(TeamMember.toTeamMember(objUser, p.getProjectId()));
    		}
    		Task t = new Task(mh.team, mh.name, mh.description, deadline, mh.projectId);
    		System.out.println(t.getDeadlineString());
    		p.addTask(t);
    		workflowDB.save();
    		t.setTimer();
    		objMsgCh.sendMessage(t.getEmbed(p.getBEGINNING())).queue();
    	}else if(command.equalsIgnoreCase("getProject") || command.equalsIgnoreCase("Project")) {   		
    		objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    	}else if(command.equalsIgnoreCase("getTask") || command.equalsIgnoreCase("task")) {
    		if(mh.index <= 0) {
    			EmbedBuilder eb = new EmbedBuilder();
	    		eb.setColor(Ref.RED);
				eb.setTitle("Error: Improperly formatted index. Must be a whole number smaller than or equal to " + (p.getTasks().size()) + " but greater than or equal to 1.");
				objMsgCh.sendMessage(eb.build()).queue();
    		}else {
    			objMsgCh.sendMessage(p.getTask(mh.index - 1).getEmbed(p.getDEFAULT())).queue();
    		}
    	}else if(command.equalsIgnoreCase("getTasks") || command.equalsIgnoreCase("tasks") || command.equalsIgnoreCase("todo")) {
    		if(objMsg.getMentionedMembers().size() > 0) {
    			for(MessageEmbed me : p.getTasksByMember(mh.team.get(0))) {
    				objMsgCh.sendMessage(me).queue();
    			}
    		}else {
    			objMsgCh.sendMessage(p.getTasksEmbed()).queue();
    		}
    	}else if(command.equalsIgnoreCase("addMembers") || command.equalsIgnoreCase("addMember")) {
    		if(mh.index == -1) {
    			p.addMembers(mh.team);
    			objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    		}else {
    			int index = mh.index;
    			if(p.hasTask(index - 1)) {
    				Task t = p.getTask(index - 1);
    				t.addMembers(mh.team);
    				objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
    			}else {
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(Ref.RED);
    				eb.setTitle("Error: Task does not exist. Index out of bounds. Select an integer from 1-" + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    			}
    		}
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("removeMembers") || command.equalsIgnoreCase("removeMember")) {
    		if(mh.index == -1) {
    			p.removeMembers(mh.team);
    			objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    		}else {
    			int index = mh.index;
    			if(p.hasTask(index - 1)) {
    				Task t = p.getTask(index - 1);
    				t.removeMembers(mh.team);
    				objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
    			}else {
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(Ref.RED);
    				eb.setTitle("Error: Task does not exist. Index out of bounds. Select an integer from 1-" + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    			}
    		}
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("edit")) {
    		if(mh.index == -1) {
    			Project project = workflowDB.getProject(objMsgCh.getIdLong());
        		
        		if(mh.deadline != null) {
        			project.setDeadline(mh.deadline);
        		}
        		if(mh.description != null) {
        			project.setDescription(mh.description);
        		}
        		if(mh.name != null) {
        			project.setName(mh.name);
        		}
        		if(mh.team != null) {
        			project.setTeam(mh.team);
        		}
        		objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    		}else {
    			Task task = p.getTask(mh.index);
        		if(task == null) {
        			EmbedBuilder eb = new EmbedBuilder();
        			eb.setColor(Ref.RED);
        			eb.setTitle("Error: The task '" + mh.name + "' does not exist. Please check for spelling errors.");
        			objMsgCh.sendMessage(eb.build()).queue();
        			return;
        		}
        		if(mh.deadline != null) {
        			task.setDeadline(mh.deadline);
        		}
        		if(mh.description != null) {
        			task.setDescription(mh.description);
        		}
        		if(mh.name != null) {
        			task.setName(mh.name);
        		}
        		if(mh.team != null) {
        			task.setAssignedMembers(mh.team);
        		}
        		objMsgCh.sendMessage(task.getEmbed(p.getDEFAULT())).queue();
    		}
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("complete") || command.equalsIgnoreCase("done")) {
    		if(mh.index == -1) {
        		p.setCompleted(true);
        		objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    		}else {
    			int index = mh.index;
    			if(p.hasTask(index - 1)) {
    				Task t = p.getTask(index - 1);
    				t.setCompleted(true);
        			objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
    			}else {
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(Ref.RED);
    				eb.setTitle("Error: Task does not exist. Index out of bounds. Select an integer from 1-" + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    			}
    		}
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("WIP")) {
    		if(mh.index == -1) {
        		p.setCompleted(false);
        		objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    		}else {
    			int index = mh.index;
    			if(p.hasTask(index - 1)) {
    				Task t = p.getTask(index - 1);
    				t.setCompleted(false);
        			objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
    			}else {
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(Ref.RED);
    				eb.setTitle("Error: Task does not exist. Index out of bounds. Select an integer from 1-" + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    			}
    		}
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("setCompletion") || command.equalsIgnoreCase("completion")) {
    		if(mh.index == -1) {
        		p.setCompletion(mh.percentage);
        		objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    		}else {
    			int index = mh.index;
    			System.out.println(mh.percentage);
    			if(p.hasTask(index - 1)) {
    				Task t = p.getTask(index - 1);
    				t.setCompletion(mh.percentage);
        			objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
    			}else {
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(Ref.RED);
    				eb.setTitle("Error: Task does not exist. Index out of bounds. Select an integer from 1-" + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    			}
    		}
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("removeTask") || command.equalsIgnoreCase("remove")) {
    		if(mh.index == -1) {
    			if(p.hasTask(mh.name)) {
    				p.removeTask(mh.name);
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(p.getWARNING());
    				eb.setTitle("Task deleted.");
    				objMsgCh.sendMessage(eb.build()).queue();
    			}else {
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(Ref.RED);
    				eb.setTitle("Error: Task does not exist. Did you forget to put quotation marks around the task name? Try typing \"TASK_NAME\"");
    				objMsgCh.sendMessage(eb.build()).queue();
    			}
    		}else {
    			int index = mh.index;
    			if(p.hasTask(index - 1)) {
    				p.removeTask(index - 1);
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(p.getWARNING());
    				eb.setTitle("Task deleted.");
    				objMsgCh.sendMessage(eb.build()).queue();
    			}else {
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(Ref.RED);
    				eb.setTitle("Error: Task does not exist. Index out of bounds. Select an integer from 1-" + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    			}
    		}
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("setLogo")) {
    		p.setLogoURL(mh.name);
    		objMsgCh.sendMessage(p.getLogo()).queue();
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("colour") || command.equalsIgnoreCase("color")) {
    		Color c = null; 
    		try {
    			c = mh.harvestColor(objMsg);
    		}catch(Exception e) {
    			e.printStackTrace();
    			EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Ref.RED);
				eb.setTitle("Error: Improperly formatted Color code. Use the format `" + Ref.prefix + "colour \"COLOUR NAME\" #RRGGBB`");
				objMsgCh.sendMessage(eb.build()).queue();
				return;
    		}
    		if(mh.name.equalsIgnoreCase("WARNING")) {
    			p.setWARNING(c);
    		}else if(mh.name.equalsIgnoreCase("DEFAULT")) {
    			p.setDEFAULT(c);
    		}else if(mh.name.equalsIgnoreCase("BEGINNING")) {
    			p.setBEGINNING(c);
    		}else {
    			EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Ref.RED);
				eb.setTitle("Error: Colour category does not exist. Try WARNING, DEFAULT, or BEGINNING.");
				objMsgCh.sendMessage(eb.build()).queue();
				return;
    		}
    		for(MessageEmbed me : p.getColours()) {
    			objMsgCh.sendMessage(me).queue();
    		}
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("getColours") || command.equalsIgnoreCase("getColors")) {
    		for(MessageEmbed me : p.getColours()) {
    			objMsgCh.sendMessage(me).queue();
    		}
    	}else if(command.equalsIgnoreCase("getLogo")) {
    		objMsgCh.sendMessage(p.getLogo()).queue();
    	}else if(command.equalsIgnoreCase("broadcast")) {
    		String message = mh.name;
    		EmbedBuilder eb = new EmbedBuilder();
    		
    		eb.setTitle("Message from " + objUser.getName());
    		eb.setDescription(message);
    		eb.setColor(p.getDEFAULT());
    		
    		p.broadcast(eb.build());
    	}else if(command.equalsIgnoreCase("export")) {
    		File export = new File(Ref.temp.getPath() + "/export_"+(Math.random() * 10000000)+".json");
    		StringUtil.writeToFile(p.toJsonString(), export.getPath());
    		objMsgCh.sendFile(export).queue(message -> {
    			export.delete();
    		});
    	}
    }
    

}

