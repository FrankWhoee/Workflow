package bot.workflow.wf;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bot.workflow.core.App;
import bot.workflow.core.Ref;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

public class Project extends TimerTask{
	
	private List<TeamMember> team;
	private List<Task> tasks;
	private String name;
	private String description;
	private Date deadline;
	private Long projectId;
	private int completion;
	private boolean isCompleted;
	
	
	public Project(List<TeamMember> team, List<Task> tasks, String name, String description, Date deadline,
			Long projectId, int completion, boolean isCompleted) {
		super();
		this.team = team;
		this.tasks = tasks;
		this.name = name;
		this.description = description;
		this.deadline = deadline;
		this.projectId = projectId;
		this.completion = completion;
		this.isCompleted = isCompleted;
	}

	public Project(List<TeamMember> team, List<Task> tasks, String name, String description, Date deadline,
			Long projectId) {
		super();
		this.team = team;
		this.tasks = tasks;
		this.name = name;
		this.description = description;
		this.deadline = deadline;
		this.projectId = projectId;
		

	}
	
	public Project(List<TeamMember> team, String name, String description, Date deadline, Long projectId) {
		super();
		this.team = team;
		this.name = name;
		tasks = new ArrayList<Task>();
		this.description = description;
		this.deadline = deadline;
		this.projectId = projectId;
	}

	public void setTimer() {
		Timer t = new Timer();
		t.schedule(this, (deadline.getTime() - new Date().getTime()));
	}
	
	public void addMembers(List<TeamMember> members) {
		team.addAll(members);
	}
	
	public void setTeam(List<TeamMember> team) {
		this.team = team;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	
	public void addTasks(List<Task> tasks) {
		this.tasks.addAll(tasks);
	}
	
	public void addTask(Task t) {
		this.tasks.add(t);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public void addMember(TeamMember tm) {
		team.add(tm);
	}
	
	public static Project fromJson(String json) {
		JsonObject project = new JsonParser().parse(json).getAsJsonObject();
		Gson gson = new Gson();
		
		Type listTeamMemberType = new TypeToken<List<TeamMember>>() {}.getType();
		List<TeamMember> team = gson.fromJson(project.get("team"), listTeamMemberType);
		
		Type listTaskType = new TypeToken<List<Task>>() {}.getType();
		List<Task> tasks = gson.fromJson(project.get("tasks"), listTaskType);
		String name = project.get("name").getAsString();
		String description = project.get("description").getAsString();
		
		Type dateType = new TypeToken<Date>() {}.getType();
		Date deadline = gson.fromJson(project.get("deadline"), dateType);
		Long projectId = project.get("projectId").getAsLong();
		
		Project p = new Project(team, tasks, name, description, deadline, projectId);
	
		return p;
	}
	
	public MessageEmbed getEmbed(Color c) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(name);
		eb.setDescription(description);
		String collaborators = "";
		for(TeamMember tm : team) {
			collaborators += tm.getUser().getAsMention() + "\n";
		}
		eb.addField("Project Channel:", App.jda.getTextChannelById(projectId).getAsMention(), false);
		eb.addField("Members Assigned to this task:", collaborators,false);
		eb.setFooter("Task Deadline: " + Ref.dateFormat.format(deadline), "https://i.ytimg.com/vi/hGENaf830Ag/maxresdefault.jpg");
		eb.setColor(c);
		return eb.build();
	}
	
	public MessageEmbed getEmbedTasks() {
		String tasks = "";
		for(Task t : this.tasks) {
			String names = "";
			for(TeamMember tm : t.getAssignedMembers()) {
				if(t.getAssignedMembers().indexOf(tm) != t.getAssignedMembers().size()) {
					names += tm.getUser().getName() + ", ";
				}else {
					names += tm.getUser().getName();
				}
			}
			tasks += "`" + t.getName() + " assigned to " + names + ". Deadline: " + Ref.dateFormat.format(deadline) + "`\n";
		}
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Tasks");
		eb.addField("", tasks, true);
		return eb.build();
	}
	
	public String toJsonString() {
		return new Gson().toJson(this);
	}
	
	public JsonObject toJson() {
		return new JsonParser().parse(new Gson().toJson(this)).getAsJsonObject(); 
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

	public List<TeamMember> getTeam() {
		return team;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Date getDeadline() {
		return deadline;
	}

	public Long getProjectId() {
		return projectId;
	}

	@Override
	public void run() {
		MessageChannel objMsgCh = App.jda.getTextChannelById(projectId);
		objMsgCh.sendMessage(getEmbed(Ref.RED)).queue();
		
	}
	
}
