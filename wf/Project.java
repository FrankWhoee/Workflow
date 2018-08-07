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
import net.dv8tion.jda.core.entities.User;

public class Project extends TimerTask{
	
	private List<TeamMember> team = new ArrayList<TeamMember>();
	private List<Task> tasks = new ArrayList<Task>();
	private String name;
	private String description;
	private Date deadline;
	private Long projectId;
	private int completion;
	private boolean isCompleted;
	
	//Cosmetic
	private String logoURL = Ref.logoURL;
	private Color WARNING = Ref.RED;
	private Color BEGINNING = Ref.GREEN;
	private Color DEFAULT = Ref.BLUE;
	
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
		
		int completion = project.get("completion").getAsInt();
		boolean isCompleted = project.get("isCompleted").getAsBoolean();
		
		String logoURL = project.get("logoURL").getAsString();
		
		Type colorType = new TypeToken<Color>() {}.getType();
		Color WARNING = gson.fromJson(project.get("WARNING"), colorType);
		Color BEGINNING = gson.fromJson(project.get("BEGINNING"), colorType);
		Color DEFAULT = gson.fromJson(project.get("DEFAULT"), colorType);
		
	
		
		Project p = new Project(team, tasks, name, description, deadline, projectId, completion, isCompleted, logoURL, WARNING, BEGINNING, DEFAULT);
	
		return p;
	}
	

	
	public String getLogoURL() {
		return logoURL;
	}


	public void setLogoURL(String logoURL) {
		this.logoURL = logoURL;
	}


	public Color getWARNING() {
		return WARNING;
	}


	public void setWARNING(Color wARNING) {
		WARNING = wARNING;
	}


	public Color getBEGINNING() {
		return BEGINNING;
	}


	public void setBEGINNING(Color bEGINNING) {
		BEGINNING = bEGINNING;
	}


	public Color getDEFAULT() {
		return DEFAULT;
	}


	public void setDEFAULT(Color dEFAULT) {
		DEFAULT = dEFAULT;
	}


	public Project(List<TeamMember> team, List<Task> tasks, String name, String description, Date deadline,
			Long projectId, int completion, boolean isCompleted, String logoURL, Color WARNING, Color BEGINNING,
			Color DEFAULT) {
		super();
		this.team = team;
		this.tasks = tasks;
		this.name = name;
		this.description = description;
		this.deadline = deadline;
		this.projectId = projectId;
		this.completion = completion;
		this.isCompleted = isCompleted;
		this.logoURL = logoURL;
		this.WARNING = WARNING;
		this.BEGINNING = BEGINNING;
		this.DEFAULT = DEFAULT;
	}


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
	
	public void activate() {
		setTimer();
		for(Task t : tasks) {
			try {
				t.setTimer();
			}catch(Exception e) {
				
			}
			
		}
	}

	public void setTimer() {
		Timer t = new Timer();
		t.schedule(this, (deadline.getTime() - new Date().getTime()));
	}
	
	public void addMember(TeamMember tm) {
		if(!hasMember(tm)) {
			team.add(tm);
		}
	}
	
	public void addMembers(List<TeamMember> members) {
		for(TeamMember tm : members) {
			addMember(tm);
		}
	}
	
	public void removeMember(TeamMember tm) {
		if(hasMember(tm)) {
			for(int i = team.size() - 1; i >= 0; i--) {
				if(team.get(i).getUser().getIdLong() == tm.getUser().getIdLong()) {
					team.remove(i);
				}
			}
		}
	}
	
	public void removeMembers(List<TeamMember> members) {
		for(TeamMember tm : members) {
			removeMember(tm);
			
		}
	}
	
	public List<MessageEmbed> getTasksByMember(TeamMember tm) {
		List<MessageEmbed> output = new ArrayList<>();
		for(Task t : tasks) {
			if(t.hasMember(tm)) {
				output.add(t.getEmbed(DEFAULT));
			}
		}
		return output;
	}
	
	public void clearMembers() {
		team.clear();
	}
	
	public boolean hasMember(TeamMember tm) {
		for(TeamMember teamMember : team) {
			if(teamMember.getUser().getIdLong() == (tm.getUser().getIdLong())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasMember(Long discordId) {
		for(TeamMember teamMember : team) {
			if(teamMember.getUser().getIdLong() == discordId) {
				return true;
			}
		}
		return false;
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
	
	public boolean hasTask(Task t) {
		for(Task task : tasks) {
			if(task.getName().equals(t.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasTask(String name) {
		for(Task task : tasks) {
			if(task.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasTask(int index) {
		return index >= 0 && index < tasks.size();
	}
	
	public void removeTask(String name) {
		for(int i = tasks.size() - 1; i >= 0; i--) {
			if(tasks.get(i).getName().equals(name)) {
				tasks.remove(i);
			}
		}
		
	}
	
	public void removeTask(int index) {
		tasks.remove(index);
		
	}
	
	public void removeTask(Task t) {
		for(int i = tasks.size() - 1; i >= 0; i--) {
			if(tasks.get(i).getName().equals(t.getName())) {
				tasks.remove(i);
			}
		}
		
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
		setTimer();
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
		for(Task t : tasks) {
			t.setProjectId(projectId);
		}
		for(TeamMember tm : team) {
			tm.setProjectId(projectId);
		}
	}

	public MessageEmbed getEmbed(Color c) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(name);
		eb.setDescription(description);
		eb.setThumbnail(logoURL);
		String collaborators = "";
		for(TeamMember tm : team) {
			collaborators += tm.getUser().getAsMention() + "\n";
		}
		eb.addField("Project Channel:", App.jda.getTextChannelById(projectId).getAsMention(), false);
		eb.addField("Team:", collaborators,false);
		String completion = isCompleted ? "Project is complete" : "Project is in progress";
		eb.addField(completion,"Completion: " + this.completion + "%",false);
		eb.setFooter("Task Deadline: " + Ref.dateFormat.format(deadline), getLogoURL());
		eb.setColor(c);
		return eb.build();
	}
	
	public Task getTask(String name) {
		for(Task t : tasks) {
			if(t.getName().equals(name)) {
				return t;
			}
		}
		return null;
	}	
	
	public Task getTask(int index) {
		return tasks.get(index);
	}	
	
	public List<MessageEmbed> getColours(){
		EmbedBuilder eb = new EmbedBuilder();
		List<MessageEmbed> output = new ArrayList<MessageEmbed>();
		
		eb.setTitle("BEGINNING");
		eb.setColor(BEGINNING);
		output.add(eb.build());
		eb.clear();
		
		eb.setTitle("DEFAULT");
		eb.setColor(DEFAULT);
		output.add(eb.build());
		eb.clear();
		
		eb.setTitle("WARNING");
		eb.setColor(WARNING);
		output.add(eb.build());
		eb.clear();
		
		return output;
	}
	
	public MessageEmbed getLogo() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Logo");
		eb.setColor(DEFAULT);
		eb.setThumbnail(logoURL);
		return eb.build();
	}
	
	public MessageEmbed getTasksEmbed() {
		String tasks = "";
		for(Task t : this.tasks) {
			String names = "";
			for(TeamMember tm : t.getAssignedMembers()) {
				if(t.getAssignedMembers().indexOf(tm) != t.getAssignedMembers().size() - 1) {
					names += tm.getUser().getName() + ", ";
				}else {
					names += tm.getUser().getName();
				}
			}
			//+ "' assigned to " + names
			String status = t.isCompleted() ? "COMPLETED" : "WIP";
			tasks += "`" + (this.tasks.indexOf(t) + 1)  + ". "+ t.getName()  + "` "
					+ "\n`Deadline: " + Ref.dateFormatTrimmed.format(t.getDeadline()) + " " + status + " [" + t.getCompletion() + "%]`\n\n";
		}
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Tasks");
		eb.addField("", tasks, true);
		eb.setColor(DEFAULT);
		eb.setThumbnail(getLogoURL());
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
	
	public void broadcast(MessageEmbed me) {
		for(TeamMember tm : team) {
			User u = tm.getUser();
			u.openPrivateChannel().queue(channel -> {
				channel.sendMessage(me).queue();
			});
		}
	}
	
	public void broadcast(String s) {
		for(TeamMember tm : team) {
			User u = tm.getUser();
			u.openPrivateChannel().queue(channel -> {
				channel.sendMessage(s).queue();
			});
		}
	}

	@Override
	public void run() {
		//Verify that deadline has not changed
		if(deadline.after(new Date()) || Math.abs((deadline.getTime() - new Date().getTime())) < 30000) {
			MessageChannel objMsgCh = App.jda.getTextChannelById(projectId);
			objMsgCh.sendMessage(getEmbed(WARNING)).queue();
			
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Deadline reached for project '" + name + "'");
			eb.setColor(WARNING);
			broadcast(eb.build());
			
			broadcast(getEmbed(WARNING));
		}
	}
	
}
