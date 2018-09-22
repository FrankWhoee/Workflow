package bot.workflow.wf;

import java.awt.Color;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import bot.workflow.core.App;
import bot.workflow.database.workflowDB;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;

public class TaskTimer  extends TimerTask{

	Long projectId;
	Long taskId;
	
	public TaskTimer(Long projectId, Long taskId) {
		this.projectId = projectId;
		this.taskId = taskId;
	}
	
	public void activate() {
		Project p = workflowDB.getProject(projectId);
		Task task = p.getTaskById(taskId);
		Timer t = new Timer();
		t.schedule(this, task.getDeadline());
	}
	
	@Override
	public void run() {
		Project p = workflowDB.getProject(projectId);
		Task t = p.getTaskById(taskId);
		Date deadline = t.getDeadline();
		//Verify that deadline has not changed
		if(deadline.after(new Date()) || Math.abs((deadline.getTime() - new Date().getTime())) < 30000) {
			MessageChannel objMsgCh = App.jda.getTextChannelById(projectId);
			Color warning = workflowDB.getProject(projectId).getWARNING();
			
			objMsgCh.sendMessage(t.getEmbed(warning)).queue();
			
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Deadline reached for task '" + t.getName() + "'");
			eb.setColor(warning);
			t.broadcast(eb.build());
			
			t.broadcast(t.getEmbed(warning));
		}
		
	}

	
	
	
}
