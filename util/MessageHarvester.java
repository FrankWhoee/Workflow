package bot.workflow.util;

import java.awt.Color;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import bot.workflow.core.App;
import bot.workflow.core.Ref;
import bot.workflow.database.workflowDB;
import bot.workflow.wf.Project;
import bot.workflow.wf.Task;
import bot.workflow.wf.TeamMember;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class MessageHarvester {
	
	public String name;
	public String description;
	public Date deadline;
	public String command;
	public int index;
	public int subindex;
	public int percentage;
	public Long projectId;
	public List<TeamMember> team;
	public MessageChannel objMsgCh;
	
	/* SYMBOLS LEGEND
	 *  "" = name 
	 *  '' = description
	 *  <> = deadline
	 *  ?? = command
	 *  () = index
	 *  [] = subindex
	 *  {} = percentage
	 *  */
	
	
	
	public MessageHarvester(String name, String description, Date deadline, String command, int index, int subindex, int percentage,
			Long projectId, List<TeamMember> team, MessageChannel objMsgCh) {
		super();
		this.name = name;
		this.description = description;
		this.deadline = deadline;
		this.command = command;
		this.index = index;
		this.percentage = percentage;
		this.subindex = subindex;
		this.projectId = projectId;
		this.team = team;
		this.objMsgCh = objMsgCh;
	}
	
	public ArrayList<Object> getAttributes(){
		ArrayList<Object> variables = new ArrayList<Object>();
	    Field[] fields = this.getClass().getFields();
	    for(Field field : fields) { 
		    try {
				variables.add(field.get(this));
			}catch (Exception e) {
				
			}
		}
	    return variables;
	}
	
	public static MessageHarvester harvest(Message objMsg) {
		
		MessageChannel objMsgCh = objMsg.getChannel();
		//Takes whatever is in between ""
		String name;
		try {
			name = between(objMsg, "\"","\"");
		} catch (Exception e1) {
			name = null;
		}
		
		//Takes whatever is in between ''
		String description = null;
		try {
			description = between(objMsg, "'","'");
		} catch (Exception e1) {
			
		}
		
		//Takes whatever is in between <>
		String deadlineString = "";
		try {
			deadlineString = between(objMsg,"<",">");
		} catch (Exception e1) {
		}
		Date deadline = new Date();
		try {
			 deadline = Ref.dateFormat.parse(deadlineString);
		} catch (ParseException e) {
			deadline = null;
		}
		
		Long projectId = objMsgCh.getIdLong();
		if(objMsg.getMentionedChannels().size() > 0) {
			projectId = objMsg.getMentionedChannels().get(0).getIdLong();
		}
		
		//Takes whatever is in between ''
		String command = "";
		try {
			command = between(objMsg, "?","?");
		} catch (Exception e1) {
			
		}
		
		//Takes whatever is in between ()
		int index = -1;
		try {
			index = Integer.parseInt(between(objMsg, "(",")"));
		} catch (Exception e1) {
			
		}
		
		//Takes whatever is in between []
		int subindex = -1;
		try {
			subindex = Integer.parseInt(between(objMsg, "[","]"));
		} catch (Exception e1) {
			
		}
		
		//Takes whatever is in between {}
		int percentage = -1;
		try {
			percentage = Integer.parseInt(between(objMsg, "{","}"));
		} catch (Exception e1) {
		}

		
		List<Role> taggedRoles = objMsg.getMentionedRoles();
		List<User> taggedMembers = objMsg.getMentionedUsers();
		
		//Add members that are in tagged roles
		List<Member> roleMembers = new ArrayList<Member>(); 
		for(Role r : taggedRoles) {
			for(Member m : objMsg.getGuild().getMembersWithRoles(r)) {
				roleMembers.add(m);
			}
		}
		
		List<TeamMember> team = new ArrayList<TeamMember>();

		
		//Add tagged members
		for(User m : taggedMembers) {
			TeamMember tm = new TeamMember(m.getIdLong(),projectId);
			team.add(tm);
		}
		
		//If there are tagged roles, add members that belong to those roles
		if(taggedRoles.size() > 0) {
			for(Member m : roleMembers) {
				TeamMember tm = new TeamMember(m.getUser().getIdLong(),projectId);
				team.add(tm);
			}
		}
		
		if(team.size() == 0) {
			team = null;
		}
		//public MessageHarvester(String name, String description, Date deadline, String command, int index, int subindex, int percentage,
		//Long projectId, List<TeamMember> team, MessageChannel objMsgCh) {
		MessageHarvester mh = new MessageHarvester(name, description, deadline,command,index,subindex,percentage, projectId, team, objMsgCh);
		return mh;
	}
	
	public static Color harvestColor(Message objMsg) {
		String input = objMsg.getContentRaw();
		String colourCode = input.substring(input.indexOf("#") + 1);
		Color c = Color.decode("0x" + colourCode);
		return c;
	}

    public static String between(Message m, String s1, String s2) throws Exception{
    	String output; 
    	String input = m.getContentRaw();
		output = input.substring(input.indexOf(s1) + 1, input.lastIndexOf(s2));
		return output;
    }
	
	
}
