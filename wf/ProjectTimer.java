package bot.workflow.wf;

import java.awt.Color;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import bot.workflow.core.App;
import bot.workflow.database.workflowDB;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;

public class ProjectTimer  extends TimerTask{

	Long projectId;
	
	public ProjectTimer(Long projectId) {
		this.projectId = projectId;
	}
	
	public void activate() {
		Project p = workflowDB.getProject(projectId);
		Timer t = new Timer();
		t.schedule(this, p.getDeadline());
		
	}
	
	@Override
	public void run() {
		Project p = workflowDB.getProject(projectId);
		Date deadline = p.getDeadline();
		Color WARNING = p.getWARNING();
		
		//Verify that deadline has not changed
		if(deadline.after(new Date()) || Math.abs((deadline.getTime() - new Date().getTime())) < 30000) {
			MessageChannel objMsgCh = App.jda.getTextChannelById(projectId);
			objMsgCh.sendMessage(p.getEmbed(WARNING)).queue();
			
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Deadline reached for project '" + p.getName() + "'");
			eb.setColor(WARNING);
			p.broadcast(eb.build());
			
			p.broadcast(p.getEmbed(WARNING));
		}
		
	}

	
	
	
}
