package bot.workflow.wf;

import bot.workflow.core.App;
import net.dv8tion.jda.core.entities.User;

public class TeamMember {
	private Long discordId;
	private Long projectId;
	
	public TeamMember(Long discordId, Long projectId) {
		super();
		this.discordId = discordId;
		this.projectId = projectId;
	}
	public User getUser() {
		return App.jda.getUserById(discordId);
	}
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	
	
}
