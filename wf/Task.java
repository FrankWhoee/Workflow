package bot.workflow.wf;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import bot.workflow.core.App;
import bot.workflow.core.Ref;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;



public class Task extends TimerTask {
	private List<TeamMember> assignedMembers;
	private String name;
	private String description;
	private Date deadline;
	private Long projectId;
	
	public Task(List<TeamMember> assignedMembers, String name, String description, Date deadline, Long projectId) {
		super();
		this.assignedMembers = assignedMembers;
		this.name = name;
		this.description = description;
		this.deadline = deadline;
		this.projectId = projectId;
		
		Timer t = new Timer();
		t.schedule(this, (deadline.getTime() - new Date().getTime()));
	}
	
	@Override
	public void run() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(name);
		eb.setDescription(description);
		String collaborators = "";
		for(TeamMember tm : assignedMembers) {
			collaborators += tm.getUser().getAsMention() + "\n";
		}
				
		eb.addField("Members Assigned to this task:", collaborators,false);
		eb.setFooter("Task Deadline: " + Ref.dateFormat.format(deadline), "");
		eb.setColor(Ref.RED);
		MessageChannel objMsgCh = App.jda.getTextChannelById(projectId);
		objMsgCh.sendMessage(eb.build()).queue();
		
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
	
	public void addMembers(List<TeamMember> assignedMembers) {
		this.assignedMembers.addAll(assignedMembers);
	}
	
	public void addMember(TeamMember tm) {
		assignedMembers.add(tm);
	}

	public Date getDeadline() {
		return deadline;
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
