package bot.workflow.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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

public class MessageHarvester {
	
	public String name;
	public String description;
	public Date deadline;
	public Long projectId;
	public List<TeamMember> team;
	public MessageChannel objMsgCh;
	
	public MessageHarvester(String name, String description, Date deadline, Long projectId, List<TeamMember> team,
			MessageChannel objMsgCh) {
		super();
		this.name = name;
		this.description = description;
		this.deadline = deadline;
		this.projectId = projectId;
		this.team = team;
		this.objMsgCh = objMsgCh;
	}
	
	public static MessageHarvester harvest(Message objMsg) {
		
		MessageChannel objMsgCh = objMsg.getChannel();
		//Takes whatever is in between ""
		String name;
		try {
			name = between(objMsg, "\"","\"");
		} catch (Exception e1) {
			name = objMsgCh.getName();
		}
		
		//Takes whatever is in between ()
		String description = "";
		try {
			description = between(objMsg, "(",")");
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
			objMsgCh.sendMessage("Improperly formatted deadline.").queue();
			return null;
		}
		Long projectId = objMsgCh.getIdLong();
		if(objMsg.getMentionedChannels().size() > 0) {
			projectId = objMsg.getMentionedChannels().get(0).getIdLong();
		}
		
		
		if(workflowDB.hasProject(projectId)) {
			objMsgCh.sendMessage("This channel already has a project.").queue();
			return null;
		}
		
		List<Member> taggedMembers = objMsg.getMentionedMembers();
		List<TeamMember> team = new ArrayList<TeamMember>();
		for(Member m : taggedMembers) {
			TeamMember tm = new TeamMember(m.getUser().getIdLong(),projectId);
			team.add(tm);
		}
		MessageHarvester mh = new MessageHarvester(name, description, deadline, projectId, team, objMsgCh);
		return mh;
	}
	
	public static MessageHarvester harvest(Message objMsg,boolean suppressWarnings) {
		
		MessageChannel objMsgCh = objMsg.getChannel();
		//Takes whatever is in between ""
		String name;
		try {
			name = between(objMsg, "\"","\"");
		} catch (Exception e1) {
			name = "";
		}
		
		//Takes whatever is in between ()
		String description = "";
		try {
			description = between(objMsg, "(",")");
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
			if(!suppressWarnings) {
				EmbedBuilder eb = new EmbedBuilder();
    			eb.setColor(Ref.RED);
    			eb.setTitle("Error: Improperly formatted deadline!");
    			objMsgCh.sendMessage(eb.build()).queue();
				return null;
			}
		}
		Long projectId = objMsgCh.getIdLong();
		if(objMsg.getMentionedChannels().size() > 0) {
			projectId = objMsg.getMentionedChannels().get(0).getIdLong();
		}
		
		
		if(workflowDB.hasProject(projectId)) {
			if(!suppressWarnings) {
				EmbedBuilder eb = new EmbedBuilder();
    			eb.setColor(Ref.RED);
    			eb.setTitle("Error: #" + App.jda.getTextChannelById(projectId).getName() + "is already associated with a project.");
    			objMsgCh.sendMessage(eb.build()).queue();
				return null;
			}
		}
		
		List<Member> taggedMembers = objMsg.getMentionedMembers();
		List<TeamMember> team = new ArrayList<TeamMember>();
		for(Member m : taggedMembers) {
			TeamMember tm = new TeamMember(m.getUser().getIdLong(),projectId);
			team.add(tm);
		}
		MessageHarvester mh = new MessageHarvester(name, description, deadline, projectId, team, objMsgCh);
		return mh;
	}
	
	public static MessageHarvester harvestProjectEdits(Message objMsg,Project p) {
		
		MessageChannel objMsgCh = objMsg.getChannel();
		//Takes whatever is in between ""
		String name;
		try {
			name = between(objMsg, "\"","\"");
		} catch (Exception e1) {
			name = p.getName();
			
		}
		
		//Takes whatever is in between ()
		String description = p.getDescription();
		try {
			description = between(objMsg, "(",")");
		} catch (Exception e1) {
			
		}
		
		//Takes whatever is in between <>
		String deadlineString = Ref.dateFormat.format(p.getDeadline());
		try {
			deadlineString = between(objMsg,"<",">");
		} catch (Exception e1) {
		}
		Date deadline = p.getDeadline();
		try {
			 deadline = Ref.dateFormat.parse(deadlineString);
		} catch (ParseException e) {
			deadline = p.getDeadline();
		}
		Long projectId = p.getProjectId();
		if(objMsg.getMentionedChannels().size() > 0) {
			projectId = objMsg.getMentionedChannels().get(0).getIdLong();
		}
		
		
		List<Member> taggedMembers = objMsg.getMentionedMembers();
		List<TeamMember> team = new ArrayList<TeamMember>();
		if(taggedMembers.size() > 0) {
			p.clearMembers();
		}else if(taggedMembers.size() == 0)  {
			team = p.getTeam();
		}
		for(Member m : taggedMembers) {
			TeamMember tm = new TeamMember(m.getUser().getIdLong(),projectId);
			team.add(tm);
		}
		MessageHarvester mh = new MessageHarvester(name, description, deadline, projectId, team, objMsgCh);
		return mh;
	}
	
	public static MessageHarvester harvestTaskEdits(Message objMsg,Task t) {
		
		MessageChannel objMsgCh = objMsg.getChannel();
		//Takes whatever is in between ""
		String name;
		try {
			name = between(objMsg, "\"","\"");
		} catch (Exception e1) {
			name = t.getName();
			
		}
		
		//Takes whatever is in between ()
		String description = t.getDescription();
		try {
			description = between(objMsg, "(",")");
		} catch (Exception e1) {
			
		}
		
		//Takes whatever is in between <>
		String deadlineString = Ref.dateFormat.format(t.getDeadline());
		try {
			deadlineString = between(objMsg,"<",">");
		} catch (Exception e1) {
		}
		Date deadline = t.getDeadline();
		try {
			 deadline = Ref.dateFormat.parse(deadlineString);
		} catch (ParseException e) {
			deadline = t.getDeadline();
		}
		Long projectId = t.getProjectId();
		if(objMsg.getMentionedChannels().size() > 0) {
			projectId = objMsg.getMentionedChannels().get(0).getIdLong();
		}
		
		
		List<Member> taggedMembers = objMsg.getMentionedMembers();
		List<TeamMember> team = new ArrayList<TeamMember>();
		if(taggedMembers.size() > 0) {
			t.clearMembers();
		}else if(taggedMembers.size() == 0)  {
			team = t.getAssignedMembers();
		}
		for(Member m : taggedMembers) {
			TeamMember tm = new TeamMember(m.getUser().getIdLong(),projectId);
			team.add(tm);
		}
		MessageHarvester mh = new MessageHarvester(name, description, deadline, projectId, team, objMsgCh);
		return mh;
	}
	
    public static String between(Message m, String s1, String s2) throws Exception{
    	String output; 
    	String input = m.getContentRaw();
		output = input.substring(input.indexOf(s1) + 1, input.lastIndexOf(s2));
		return output;
    }
	
	
}
