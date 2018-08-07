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
    	MessageHarvester mh = MessageHarvester.harvest(objMsg,true);
    	
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
    		
    	}else if(command.equalsIgnoreCase("createProject")) {
    		MessageHarvester createMH = MessageHarvester.harvest(objMsg);
    		
    		Project p = new Project(createMH.team, createMH.name, createMH.description, createMH.deadline, createMH.projectId);
    		try{
    			p.setTimer();
    		}catch(IllegalArgumentException iae) {
    			EmbedBuilder eb = new EmbedBuilder();
        		eb.setColor(Ref.RED);
    			eb.setTitle("Error: Timer delay is invalid.");
    			objMsgCh.sendMessage(eb.build()).queue();
        		return;
    		}
    		workflowDB.addProject(p);
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
    		
    	
    	}else if(command.equalsIgnoreCase("terminate") && objUser.getIdLong() == Ref.myId) {
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
    	
    	//Verify that user is part of the project
    	if(!p.hasMember(objUser.getIdLong())) {
    		EmbedBuilder eb = new EmbedBuilder();
    		eb.setColor(Ref.RED);
			eb.setTitle("Error: " + objUser.getAsMention() + " is not on this team.");
			objMsgCh.sendMessage(eb.build()).queue();
    		return;
    	}
    	//Now guaranteed that this user has access to this project.
    	if(command.equalsIgnoreCase("delete")) {	
    		EmbedBuilder eb = new EmbedBuilder();
			workflowDB.removeProject(mh.projectId);
    		eb.setTitle("Project "+mh.projectId+" deleted.");
    		eb.setColor(Ref.RED);
    		objMsgCh.sendMessage(eb.build()).queue();
    		workflowDB.save();
    		//workflowDB.parseDB();
    	}else if(command.equalsIgnoreCase("addTask")) {
    		
    		Task t = new Task(mh.team, mh.name, mh.description, mh.deadline, mh.projectId);
    		t.setTimer();
    		p.addTask(t);
    		workflowDB.save();
    		
    		objMsgCh.sendMessage(t.getEmbed(p.getBEGINNING())).queue();
    	}else if(command.equalsIgnoreCase("getProject") || command.equalsIgnoreCase("Project")) {   		
    		objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    	}else if(command.equalsIgnoreCase("getTask")) {
    		if(mh.description.equalsIgnoreCase("")) {
    			objMsgCh.sendMessage(p.getTask(mh.name).getEmbed(p.getDEFAULT())).queue();
    		}else {
    			int index = 0;
    			try {
    				index = Integer.parseInt(mh.description);
    			}catch(Exception e) {
    				EmbedBuilder eb = new EmbedBuilder();
    	    		eb.setColor(Ref.RED);
    				eb.setTitle("Error: Improperly formatted index.");
    				objMsgCh.sendMessage(eb.build()).queue();
    				return;
    			}
    			
    			objMsgCh.sendMessage(p.getTask(index - 1).getEmbed(p.getDEFAULT())).queue();
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
    		if(mh.description.equalsIgnoreCase("")) {
    			if(mh.name.equalsIgnoreCase("")){
        			p.addMembers(mh.team);
        			objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
        		}else {
        			Task t = p.getTask(mh.name);
        			t.addMembers(mh.team);
        			objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
        		}
    		}else {
    			int index = 0;
    			try {
    				index = Integer.parseInt(mh.description);
    			}catch(Exception e) {
    				EmbedBuilder eb = new EmbedBuilder();
    	    		eb.setColor(Ref.RED);
    				eb.setTitle("Error: Improperly formatted index. Must be a whole number smaller than or equal to " + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    				return;
    			}
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
    		if(mh.description.equalsIgnoreCase("")) {
        		workflowDB.save();if(mh.name.equalsIgnoreCase("")){
        			p.removeMembers(mh.team);
        			objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
        		}else {
        			Task t = p.getTask(mh.name);
        			t.removeMembers(mh.team);
        			objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
        		}
    		}else {
    			int index = 0;
    			try {
    				index = Integer.parseInt(mh.description);
    			}catch(Exception e) {
    				EmbedBuilder eb = new EmbedBuilder();
    	    		eb.setColor(Ref.RED);
    				eb.setTitle("Error: Improperly formatted index. Must be a whole number smaller than or equal to " + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    				return;
    			}
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
    	}else if(command.equalsIgnoreCase("editProject")) {
    		Project project = workflowDB.getProject(objMsgCh.getIdLong());
    		MessageHarvester editmh = MessageHarvester.harvestProjectEdits(objMsg, p);
    		
    		project.setDeadline(editmh.deadline);
    		project.setDescription(editmh.description);
    		project.setName(editmh.name);
    		project.setProjectId(editmh.projectId);
    		project.setTeam(editmh.team);
    		objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("editTask")) {
    		Task t = p.getTask(mh.name);
    		if(t == null) {
    			EmbedBuilder eb = new EmbedBuilder();
    			eb.setColor(Ref.RED);
    			eb.setTitle("Error: The task '" + mh.name + "' does not exist. Please check for spelling errors.");
    			objMsgCh.sendMessage(eb.build()).queue();
    			return;
    		}
    		MessageHarvester editmh = MessageHarvester.harvestTaskEdits(objMsg, t);
    		t.setDeadline(editmh.deadline);
    		t.setDescription(editmh.description);
    		t.setAssignedMembers(editmh.team);
    		objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("complete")) {
    		if(mh.description.equalsIgnoreCase("")) {
    			Task t = p.getTask(mh.name);
        		if(t == null) {
        			p.setCompleted(true);
        			objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
        		}else {
        			t.setCompleted(true);
        			objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
        		}
    		}else {
    			int index = 0;
    			try {
    				index = Integer.parseInt(mh.description);
    			}catch(Exception e) {
    				EmbedBuilder eb = new EmbedBuilder();
    	    		eb.setColor(Ref.RED);
    				eb.setTitle("Error: Improperly formatted index. Must be a whole number smaller than or equal to " + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    				return;
    			}
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
    		if(mh.description.equalsIgnoreCase("")) {
    			Task t = p.getTask(mh.name);
        		if(t == null) {
        			p.setCompleted(false);
        			objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
        		}else {
        			t.setCompleted(false);
        			objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
        		}
    		}else {
    			int index = 0;
    			try {
    				index = Integer.parseInt(mh.description);
    			}catch(Exception e) {
    				EmbedBuilder eb = new EmbedBuilder();
    	    		eb.setColor(Ref.RED);
    				eb.setTitle("Error: Improperly formatted index. Must be a whole number smaller than or equal to " + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    				return;
    			}
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
    	}else if(command.equalsIgnoreCase("setCompletion")) {
    		Task t = null;
    		try{
    			 t = p.getTask(mh.name);
    		}catch(NullPointerException npe) {
    			
    		}
    		if(t == null) {
    			if(input.contains("\"")) {
    				EmbedBuilder eb = new EmbedBuilder();
    				eb.setColor(Ref.RED);
    				eb.setTitle("Error: Task does not exist. Check for spelling errors!");
    				objMsgCh.sendMessage(eb.build()).queue();
    			}else {
        			p.setCompletion(Integer.parseInt(mh.description));
        			objMsgCh.sendMessage(p.getEmbed(p.getDEFAULT())).queue();
    			}
    		}else {
    			t.setCompletion(Integer.parseInt(mh.description));
    			objMsgCh.sendMessage(t.getEmbed(p.getDEFAULT())).queue();
    		}
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("removeTask")) {
    		if(mh.description.equalsIgnoreCase("")) {
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
    			int index = 0;
    			try {
    				index = Integer.parseInt(mh.description);
    			}catch(Exception e) {
    				EmbedBuilder eb = new EmbedBuilder();
    	    		eb.setColor(Ref.RED);
    				eb.setTitle("Error: Improperly formatted index. Must be a whole number smaller than or equal to " + (p.getTasks().size()));
    				objMsgCh.sendMessage(eb.build()).queue();
    				return;
    			}
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
				eb.setTitle("Error: Improperly formatted Color code. Use the format `" + Ref.prefix + "colour \"COLOUR NAME\" #RRRGGBB`");
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

