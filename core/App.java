package bot.workflow.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;

import bot.workflow.database.workflowDB;
import bot.workflow.util.MessageHarvester;
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
    		
    	}else if(command.equalsIgnoreCase("helpProject")) {
    		objMsgCh.sendMessage(Ref.helpProjectMessage).queue();
    		
    	}else if(command.equalsIgnoreCase("helpTask")) {
    		objMsgCh.sendMessage(Ref.helpTaskMessage).queue();
    		
    	}else if(command.equalsIgnoreCase("helpPT")) {
    		objMsgCh.sendMessage(Ref.helpPTMessage).queue();
    		
    	}else if(command.equalsIgnoreCase("helpMisc")) {
    		objMsgCh.sendMessage(Ref.helpMiscMessage).queue();
    		
    	}else if(command.equalsIgnoreCase("status")) {
    		EmbedBuilder eb = new EmbedBuilder();
    		eb.setTitle("Workflow " + Ref.version);
    		eb.addField("Status","Online",false);
    		eb.setColor(Ref.BLUE);
    		objMsgCh.sendMessage(eb.build()).queue();
    		
    	}else if(command.equals("createProject")) {
    		MessageHarvester createMH = MessageHarvester.harvest(objMsg);
    		
    		Project p = new Project(createMH.team, createMH.name, createMH.description, createMH.deadline, createMH.projectId);
    		workflowDB.addProject(p);
    		p.setTimer();
    		workflowDB.save();
    		objMsgCh.sendMessage(p.getEmbed(Ref.GREEN)).queue();
    	}else if(command.equalsIgnoreCase("delete")) {	
    		EmbedBuilder eb = new EmbedBuilder();
    		if(workflowDB.database.has("" + mh.projectId)) {
    			workflowDB.removeProject(mh.projectId);
        		eb.setTitle("Project "+mh.projectId+" deleted.");
        		eb.setColor(Ref.RED);
        		objMsgCh.sendMessage(eb.build()).queue();
        		workflowDB.save();
    		}else {
    			eb.setColor(Ref.RED);
    			eb.setTitle("Error: #" + App.jda.getTextChannelById(mh.projectId).getName() + " is not associated with a project.");
    			objMsgCh.sendMessage(eb.build()).queue();
        		eb.setColor(Ref.RED);
        		objMsgCh.sendMessage(eb.build()).queue();
    		}
    		
    	}else if(command.equalsIgnoreCase("addTask")) {
    		
    		Task t = new Task(mh.team, mh.name, mh.description, mh.deadline, mh.projectId);
    		t.setTimer();
    		Project p = workflowDB.getProject(mh.projectId);
    		p.addTask(t);
    		workflowDB.save();
    		
    		objMsgCh.sendMessage(t.getEmbed(Ref.GREEN)).queue();
    	}else if(command.equals("getProject")) {
    		
    		
    		if(!workflowDB.hasProject(mh.projectId)) {
    			EmbedBuilder eb = new EmbedBuilder();
    			eb.setColor(Ref.RED);
    			eb.setTitle("Error: #" + App.jda.getTextChannelById(mh.projectId).getName() + " is not associated with a project.");
    			objMsgCh.sendMessage(eb.build()).queue();
    			return;
    		}
    		
    		objMsgCh.sendMessage(workflowDB.getProject(mh.projectId).getEmbed(Ref.BLUE)).queue();
    	}else if(command.equals("getTask")) {
    		
    		
    		if(!workflowDB.hasProject(mh.projectId)) {
    			EmbedBuilder eb = new EmbedBuilder();
    			eb.setColor(Ref.RED);
    			eb.setTitle("Error: #" + App.jda.getTextChannelById(mh.projectId).getName() + " is not associated with a project.");
    			objMsgCh.sendMessage(eb.build()).queue();
    			return;
    		}
    		objMsgCh.sendMessage(workflowDB.getProject(mh.projectId).getTask(mh.name).getEmbed(Ref.BLUE)).queue();
    	}else if(command.equals("getTasks")) {
    		
    		
    		if(!workflowDB.hasProject(mh.projectId)) {
    			EmbedBuilder eb = new EmbedBuilder();
    			eb.setColor(Ref.RED);
    			eb.setTitle("Error: #" + App.jda.getTextChannelById(mh.projectId).getName() + " is not associated with a project.");
    			objMsgCh.sendMessage(eb.build()).queue();
    			return;
    		}
    		objMsgCh.sendMessage(workflowDB.getProject(mh.projectId).getTasksEmbed()).queue();
    	}else if(command.equals("addMembers") || command.equals("addMember")) {
    		Project p = workflowDB.getProject(mh.projectId);
    		if(mh.name.equals("")){
    			p.addMembers(mh.team);
    			objMsgCh.sendMessage(p.getEmbed(Ref.BLUE)).queue();
    		}else {
    			Task t = p.getTask(mh.name);
    			t.addMembers(mh.team);
    			objMsgCh.sendMessage(t.getEmbed(Ref.BLUE)).queue();
    		}
    		workflowDB.save();
    	}else if(command.equals("removeMembers") || command.equals("removeMember")) {
    		Project p = workflowDB.getProject(mh.projectId);
    		if(mh.name.equals("")){
    			p.removeMembers(mh.team);
    			objMsgCh.sendMessage(p.getEmbed(Ref.BLUE)).queue();
    		}else {
    			Task t = p.getTask(mh.name);
    			t.removeMembers(mh.team);
    			objMsgCh.sendMessage(t.getEmbed(Ref.BLUE)).queue();
    		}
    		workflowDB.save();
    	}else if(command.equals("editProject")) {
    		Project p = workflowDB.getProject(objMsgCh.getIdLong());
    		MessageHarvester editmh = MessageHarvester.harvestProjectEdits(objMsg, p);
    		
    		p.setDeadline(editmh.deadline);
    		p.setDescription(editmh.description);
    		p.setName(editmh.name);
    		p.setProjectId(editmh.projectId);
    		p.setTeam(editmh.team);
    		objMsgCh.sendMessage(p.getEmbed(Ref.BLUE)).queue();
    		workflowDB.save();
    	}else if(command.equals("editTask")) {
    		Project p = workflowDB.getProject(mh.projectId);  
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
    		objMsgCh.sendMessage(t.getEmbed(Ref.BLUE)).queue();
    		workflowDB.save();
    	}else if(command.equals("complete")) {
    		Project p = workflowDB.getProject(mh.projectId); 
    		Task t = p.getTask(mh.name);
    		if(t == null) {
    			p.setCompleted(true);
    			objMsgCh.sendMessage(p.getEmbed(Ref.BLUE)).queue();
    		}else {
    			t.setCompleted(true);
    			objMsgCh.sendMessage(t.getEmbed(Ref.BLUE)).queue();
    		}
    		
    		workflowDB.save();
    	}else if(command.equalsIgnoreCase("WIP")) {
    		Project p = workflowDB.getProject(mh.projectId); 
    		Task t = p.getTask(mh.name);
    		if(t == null) {
    			p.setCompleted(false);
    			objMsgCh.sendMessage(p.getEmbed(Ref.BLUE)).queue();
    		}else {
    			t.setCompleted(false);
    			objMsgCh.sendMessage(t.getEmbed(Ref.BLUE)).queue();
    		}
    		
    		workflowDB.save();
    	}else if(command.equals("setCompletion")) {
    		Project p = workflowDB.getProject(mh.projectId); 
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
        			p.setCompletion(Integer.parseInt(input.trim()));
        			objMsgCh.sendMessage(p.getEmbed(Ref.BLUE)).queue();
    			}
    		}else {
    			t.setCompletion(Integer.parseInt(input.trim()));
    			objMsgCh.sendMessage(t.getEmbed(Ref.BLUE)).queue();
    		}
    		workflowDB.save();
    	}else if(command.equals("removeTask")) {
    		Project p = workflowDB.getProject(mh.projectId);
			if(p.hasTask(mh.name)) {
				p.removeTask(mh.name);
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Ref.RED);
				eb.setTitle("Task deleted.");
				objMsgCh.sendMessage(eb.build()).queue();
			}else {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Ref.RED);
				eb.setTitle("Error: Task does not exist. Did you forget to put quotation marks around the task name? Try typing \"TASK_NAME\"");
				objMsgCh.sendMessage(eb.build()).queue();
			}
			
    		workflowDB.save();
    	}
    	
    	
    	
    	
    	
    	
    }
    

}

