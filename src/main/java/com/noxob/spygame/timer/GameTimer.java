package com.noxob.spygame.timer;

import java.awt.Color;
import java.util.TimerTask;

import com.noxob.spygame.App;
import com.noxob.spygame.util.Utils;

import net.dv8tion.jda.core.EmbedBuilder;

public class GameTimer extends TimerTask{
	Utils util = new Utils();
	
	@Override
	public void run() {
		if(System.currentTimeMillis() - App.startTime >= App.gameDuration) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Time is up! Spy Victory!");
			eb.setDescription("No one successfully guessed the spy.\nThe spy ***" + App.players.get("Spy").getName() + "*** gets 2 pts");
			eb.setColor(Color.RED);
			util.addScore(App.players.get("Spy").getName(), 2);
			App.channel.sendMessage(eb.build()).queue();
			App.started = false;
		}else {
			System.out.println("the game was over but time was not cancelled");
		}
		
	}

}
