package bot.workflow.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;

import bot.workflow.database.workflowDB;
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
    		input = raw.substring(command.length() + 1).trim();
    	}catch (Exception e) {
    		try {
    			command = raw.substring(Ref.prefix.length()).trim();
    		}catch(Exception exc){
    			return;
    		}	
    		//System.out.print(command);
    		
    	}
    	
    	if(command.equals("createProject")) {
    		//Takes whatever is in between ""
    		String name; 
    		try {
    			name = input.substring(input.indexOf("\"") + 1, input.lastIndexOf("\""));
    		}catch(Exception e) {
    			name = objMsgCh.getName();
    		}
    		
    		//Takes whatever is in between ()
    		String description;
    		try {
    			description = input.substring(input.indexOf("(") + 1, input.indexOf(")"));
    		}catch(Exception e) {
    			description = "";
    		}
    		
    		//Takes whatever is in between <>
    		String deadlineString = input.substring(input.indexOf("<") + 1, input.indexOf(">"));
    		Date deadline;
    		try {
				 deadline = Ref.dateFormat.parse(deadlineString);
			} catch (ParseException e) {
				objMsgCh.sendMessage("Improperly formatted deadline.").queue();
				return;
			}
    		Long projectId = objMsgCh.getIdLong();
    		if(objMsg.getMentionedChannels().size() > 0) {
    			projectId = objMsg.getMentionedChannels().get(0).getIdLong();
    		}
    		
    		
    		if(workflowDB.database.has("" + projectId)) {
    			objMsgCh.sendMessage("This channel already has a project.").queue();
    			return;
    		}
    		
    		List<Member> taggedMembers = objMsg.getMentionedMembers();
    		List<TeamMember> team = new ArrayList<TeamMember>();
    		for(Member m : taggedMembers) {
    			TeamMember tm = new TeamMember(m.getUser().getIdLong(),projectId);
    			team.add(tm);
    		}
    		
    		
    		
    		Project p = new Project(team, name, description, deadline, projectId);
    		workflowDB.database.add(Long.toString(projectId),p.toJson());
    		p.setTimer();
    		workflowDB.save();
    		objMsgCh.sendMessage(p.getEmbed(Ref.GREEN)).queue();
    	}else if(command.equalsIgnoreCase("delete")) {
    		
    		Long projectId = objMsgCh.getIdLong();
    		if(objMsg.getMentionedChannels().size() > 0) {
    			projectId = objMsg.getMentionedChannels().get(0).getIdLong();
    		}
    		
    		EmbedBuilder eb = new EmbedBuilder();
    		if(workflowDB.database.has("" + projectId)) {
    			workflowDB.database.remove("" + projectId);
        		eb.setTitle("Project deleted.");
        		eb.setColor(Ref.RED);
        		objMsgCh.sendMessage(eb.build()).queue();
        		workflowDB.save();
    		}else {
    			eb.setTitle("Error: This channel does not have a project.");
        		eb.setColor(Ref.RED);
        		objMsgCh.sendMessage(eb.build()).queue();
    		}
    		
    		
    		
    		
    	}else if(command.equalsIgnoreCase("status")) {
    		EmbedBuilder eb = new EmbedBuilder();
    		eb.setTitle("Workflow " + Ref.version);
    		eb.addField("Status","Online",false);
    		eb.setColor(Ref.BLUE);
    		objMsgCh.sendMessage(eb.build()).queue();
    		
    	}else if(command.equalsIgnoreCase("help")) {
    		objMsgCh.sendMessage(Ref.helpMessage).queue();
    		
    	}else if(command.equalsIgnoreCase("addTask")) {
    		//Takes whatever is in between ""
    		String name; 
    		try {
    			name = input.substring(input.indexOf("\"") + 1, input.lastIndexOf("\""));
    		}catch(Exception e) {
    			name = objMsgCh.getName();
    		}
    		
    		//Takes whatever is in between ()
    		String description;
    		try {
    			description = input.substring(input.indexOf("(") + 1, input.indexOf(")"));
    		}catch(Exception e) {
    			description = "";
    		}
    		
    		//Takes whatever is in between <>
    		String deadlineString = input.substring(input.indexOf("<") + 1, input.indexOf(">"));
    		Date deadline;
    		try {
				 deadline = Ref.dateFormat.parse(deadlineString);
			} catch (ParseException e) {
				objMsgCh.sendMessage("Improperly formatted deadline.").queue();
				return;
			}
    		Long projectId = objMsgCh.getIdLong();
    		if(objMsg.getMentionedChannels().size() > 0) {
    			projectId = objMsg.getMentionedChannels().get(0).getIdLong();
    		}
    		
    		
    		if(workflowDB.database.has("" + projectId)) {
    			//objMsgCh.sendMessage("This channel already has a project.").queue();
    			//return;
    		}
    		
    		List<Member> taggedMembers = objMsg.getMentionedMembers();
    		List<TeamMember> team = new ArrayList<TeamMember>();
    		for(Member m : taggedMembers) {
    			TeamMember tm = new TeamMember(m.getUser().getIdLong(),projectId);
    			team.add(tm);
    		}
    		
    		
    		
    		Task t = new Task(team, name, description, deadline, projectId);
    		Project p = Project.fromJson(workflowDB.database.get("" + projectId).toString());
    		System.out.println(t.getName());
    		if(p.getTasks() == null) {
    			p.setTasks(new ArrayList<Task>());
    		}
    		p.addTask(t);
    		workflowDB.database.add(Long.toString(projectId),p.toJson());
    		workflowDB.save();
    		objMsgCh.sendMessage(p.getEmbed(Ref.GREEN)).queue();
    	}
    	
    	
    	
    	
    	
    	
    }
}
