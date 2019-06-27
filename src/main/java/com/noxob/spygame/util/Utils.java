package com.noxob.spygame.util;

import com.noxob.spygame.App;

public class Utils {
	public void addScore(String username, Integer points) {
		if(App.scoreboard.get(username) != null) {
			App.scoreboard.put(username, App.scoreboard.get(username)+points);
		}else {
			App.scoreboard.put(username, points);
		}
	}
}
