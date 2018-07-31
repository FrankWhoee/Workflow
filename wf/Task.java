package bot.workflow.wf;

import java.awt.Color;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import bot.workflow.core.App;
import bot.workflow.core.Ref;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;



public class Task extends TimerTask {
	private List<TeamMember> assignedMembers;
	private String name;
	private String description;
	private Date deadline;
	private Long projectId;
	private int completion;
	private boolean isCompleted;
	
	public Task(List<TeamMember> assignedMembers, String name, String description, Date deadline, Long projectId,
			int completion, boolean isCompleted) {
		super();
		this.assignedMembers = assignedMembers;
		this.name = name;
		this.description = description;
		this.deadline = deadline;
		this.projectId = projectId;
		this.completion = completion;
		this.isCompleted = isCompleted;
	}


	public int getCompletion() {
		return completion;
	}

	public void setCompletion(int completion) {
		this.completion = completion;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public Task(List<TeamMember> assignedMembers, String name, String description, Date deadline, Long projectId) {
		super();
		this.assignedMembers = assignedMembers;
		this.name = name;
		this.description = description;
		this.deadline = deadline;
		this.projectId = projectId;
	}
	
	@Override
	public void run() {
		MessageChannel objMsgCh = App.jda.getTextChannelById(projectId);
		objMsgCh.sendMessage(getEmbed(Ref.RED)).queue();
		
	}
	
	public MessageEmbed getEmbed(Color c) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(name);
		eb.setDescription(description);
		String collaborators = "";
		for(TeamMember tm : assignedMembers) {
			collaborators += tm.getUser().getAsMention() + "\n";
		}
				
		eb.addField("Members Assigned to this task:", collaborators,false);
		String completion = isCompleted ? "Task is complete" : "Task is in progress";
		eb.addField(completion,"Completion: " + this.completion + "%",false);
		eb.setFooter("Task Deadline: " + Ref.dateFormat.format(deadline), Ref.logoURL);
		eb.setColor(c);
		return eb.build();
	}
	
	public void setTimer() {
		Timer t = new Timer();
		t.schedule(this, (deadline.getTime() - new Date().getTime()));
	}

	public List<TeamMember> getAssignedMembers() {
		return assignedMembers;
	}

	public void setAssignedMembers(List<TeamMember> assignedMembers) {
		this.assignedMembers = assignedMembers;
	}
	
	public void addMember(TeamMember tm) {
		if(!hasMember(tm)) {
			assignedMembers.add(tm);
		}
	}
	
	public void addMembers(List<TeamMember> members) {
		for(TeamMember tm : members) {
			addMember(tm);
		}
	}
	
	public void clearMembers() {
		assignedMembers.clear();
	}
	
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}


	public void removeMember(TeamMember tm) {
		if(hasMember(tm)) {
			for(int i = assignedMembers.size() - 1; i >= 0; i--) {
				if(assignedMembers.get(i).getUser().getIdLong() == tm.getUser().getIdLong()) {
					assignedMembers.remove(i);
				}
			}
		}
	}
	
	public void removeMembers(List<TeamMember> members) {
		for(TeamMember tm : members) {
			removeMember(tm);
			
		}
	}
	
	public boolean hasMember(TeamMember tm) {
		for(TeamMember teamMember : assignedMembers) {
			if(teamMember.getUser().getIdLong() == (tm.getUser().getIdLong())) {
				return true;
			}
		}
		return false;
	}

	public Date getDeadline() {
		return deadline;
	}
	
	public String getDeadlineString() {
		return Ref.dateFormat.format(deadline);
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	
	
	
}
