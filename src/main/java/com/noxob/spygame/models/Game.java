package com.noxob.spygame.models;

import java.util.Map;
import java.util.UUID;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class Game {

	private UUID id;
	private TextChannel channel;
	private Map<String,User> players;
	
}
