package com.noxob.spygame;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.noxob.spygame.models.Location;
import com.noxob.spygame.timer.GameTimer;
import com.noxob.spygame.util.Utils;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageResponder extends ListenerAdapter {

	Utils util = new Utils();
	
	public void onMessageReceived(MessageReceivedEvent event){
		String command[] = event.getMessage().getContentRaw().split(" ");
		EmbedBuilder eb = new EmbedBuilder();
		MessageEmbed me = null;
		
		if("s!".equals(command[0])) {
			if("commands".equals(command[1])) {
				eb.setTitle("Available Commands");
				eb.setColor(Color.CYAN);
				eb.addField("s! start", "Starts a new round.", false);
				eb.addField("s! guess <Location Name/Id>", " Let's spy reveal himself/herself and guess the location.", false);
				eb.addField("s! blame @username", "Let's players blame someone of being the spy.", false);
				eb.addField("s! time", "Shows the remaining time.", false);
				eb.addField("s! scoreboard", "Shows the scoreboard.", false);
				eb.addField("s! clear", "Clears the scoreboard.", false);
				me = eb.build();
				if(event.getChannelType().isGuild())
					event.getTextChannel().sendMessage(me).queue();
				else
					event.getPrivateChannel().sendMessage(me).queue();;
			}else if("help".equals(command[1])) {
				eb.setTitle("How to play?");
				eb.addField("Summary", "The game contains one spy and at max seven non-spy players. "
						+ "The identity of the spy is hidden and spy is chosen randomly by the bot. "
						+ "At the start of the game bot randomly chooses a player as questioner and a location. "
						+ "Each non-spy player has a secret role that is related to the secret location. "
						+ "Non-spy players know the secret location but the spy doesn't. "
						+ "The first questioner asks any question to a player of their choice then player questioned answers the question. "
						+ "After that every answering player will be the next one who asks the question. "
						+ "The answers given by the non-spy players should hint the other non-spy players that they know the secret location but it shouldn't reveal the location to the spy. "
						+ "The game ends in three conditions; when the timer runs out, when spy reveals himself/herself and tries to guess the secret location "
						+ "or when players decide to accuse someone of being the spy.", false);
				eb.addField("Objective", "The spy’s objective is to avoid exposure until the end of a given round or identify the current location.\n" + 
						"The non-spies’ objective is to establish consensus on the identity of the spy and expose him or her.", false);
				eb.addField("Start of the Round", "You start a round using ```s! start``` command. After you hit the plus reaction on the bot's message, the game will start. "
						+ "Bot is going to choose a random player who will ask the first question to any player he/she chooses. "
						+ "Every player who answers the question proceeds to ask any other player a question of their own, "
						+ "but cannot ask a question of the player who just asked them a question.", false);
				eb.addField("End of the Round", "A round ends when one of the following three things happen:"
						+ "\n***1. Eight minutes have passed***"
						+ "\nWhen timer runs out the spy wins the round."
						+ "\n***When a Player Gets Suspicious***"
						+ "\nWhen a player gets suspicious he/she can blame another player (s! blame @username) for being the spy, this will pause the timer and start a poll. If the poll passes and the accused player found guilty,"
						+ "Non-Spy players win the game if the accused is the actual spy otherwise they lose the game."
						+ "\n***At the Spy’s Request***"
						+ "\nSpy can reveal himself/herself (s! guess <Location Name/Id>) and guess the location. If he is successful he wins the game.", false);
				eb.addField("Scoring", "Play the desired number of rounds and scores will be saved to the scoreboard (s! scoreboard).", false);
				eb.addField("Spy Victory", "- The spy earns 2 points if no one is successfully accused of being the spy\n" + 
						"- The spy earns 4 points if a non-spy player is successfully accused of being a spy\n" + 
						"- The spy earns 4 points if the spy stops the game and successfully guesses the location", false);
				eb.addField("Non-Spy Victory", "- ***Victory:*** Each non-spy player earns 1 point\n" + 
						"- The player who initiated the successful accusation of the spy earns 2 points instead", false);
				eb.addField("Objectives and Strategies", "The objectives of the ***non-spy*** players are to identify the spy and avoid revealing their location.\r\n" + 
						"\n" + 
						"Therefore, the non-spies should refrain from being too explicit in their questions: (for example, \"How much cash did the robbers steal yesterday?\" The spy will instantly identify the location as the bank).\r\n" + 
						"\n" + 
						"However, when a player’s questions and answers are too vague, other players might start suspecting them of being the spy, enabling the real spy to win.\r\n" + 
						"\n" + 
						"The ***spy’s*** objective is to listen as carefully as possible to what the other players say and do their best to avoid blowing their cover while also trying to identify the location before eight minutes have passed. A spy who doesn’t attempt to guess the location is taking a risk — it is entirely possible that the other players will identify them after discussion and voting.", false);
				eb.addField("Full List of Commands", "```s! commands```", false);
				eb.setColor(Color.CYAN);
				event.getTextChannel().sendMessage(eb.build()).queue();;
				
			}else if("start".equals(command[1]) && !App.started) {
				eb.setTitle("Game Starting!");
				eb.setDescription("If you want to join the game, click the plus reaction below.");
				eb.setColor(Color.CYAN);
				me = eb.build();
				event.getTextChannel().sendMessage(me).complete();
				List<Message> history = event.getChannel().getHistory().retrievePast(10).complete();
				Message ours = null;
				for(Message m: history) {
					if(m.getAuthor().getId().equals(App.jda.getSelfUser().getId())) {
						ours = m;
						break;
					}
				}
	            ours.addReaction("\u2795").complete();
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	            if(event.getChannel().getMessageById(ours.getId()).complete().getReactions().get(0).getCount() > 2) {
	            	String description = "**Players:** ";
	            	List<User> voters = event.getChannel().getMessageById(ours.getId()).complete().getReactions().get(0).getUsers().complete();
	            	voters.removeIf(u -> u.isBot());
	            	
	            	Location current = App.locations.get((int) (Math.random() * App.locations.size()));
	            	Map<String, User> players = new HashMap<>();
	            	
	            	if(voters.size() > 8) {
	            		while(voters.size() > 8) {
	            			voters.remove(voters.size() - 1);
	            		}
	            		eb.setTitle("More than 8 players joined!");
	            		eb.setDescription("The first 8 players will be able to play the game");
	            		eb.setColor(Color.YELLOW);
	            		event.getTextChannel().sendMessage(eb.build()).queue();
	            	}
	            	
	            	for(int i = 0; i < voters.size(); i++) {
	            		if(!voters.get(i).isBot()) {
	            			description += "\n" + voters.get(i).getName();
	            		}else {
	            			voters.remove(i);
	            			i--;
	            		}
	            	}
	            	
	            	String firstAsker = voters.get((int) (Math.random() * voters.size())).getName();
	            	App.startTime = System.currentTimeMillis();
	            	App.channel = event.getTextChannel();
	            	App.timer = new Timer();
	            	App.timer.schedule(new GameTimer(), App.gameDuration);
	            	App.blamers = new HashMap<String,Boolean>();
	            	int spyIndex = (int) (Math.random() * voters.size());
	            	players.put("Spy", voters.get(spyIndex));
	            	eb.setTitle("You are the Spy!");
	            	StringBuilder locs = new StringBuilder();
	            	App.locations.forEach(loc -> locs.append("\n" + loc.getId() + " - " + loc.getName()));
	            	eb.setDescription("***Your objective*** is to avoid exposure until the end of a given round or identify the current location.\nWhen you figure out the location, you can use ```s! guess <Location Name>``` or ```s! guess <Location Id>``` to guess the location.\nYou don't have to guess the location but if you do so and ***succeed***, you ***win***.\n\n***Possible Locations:***"+ locs);
	            	eb.setColor(Color.RED);
	            	voters.get(spyIndex).openPrivateChannel().complete().sendMessage(eb.build()).queue();
	            	voters.remove(spyIndex);
	            	while(voters.size() > 0) {
	            		int randomPlayerIndex = (int) (Math.random() * voters.size());
	            		int randomRoleIndex = (int) (Math.random() * current.getRoles().size());
	            		players.put(current.getRoles().get(randomRoleIndex), voters.get(randomPlayerIndex));
	            		eb.setTitle("You are on the Non-Spy Team!");
		            	eb.setDescription("Your role is: ***"+ current.getRoles().get(randomRoleIndex) + "***\nSecret Location: ***" + current.getName() + "***\n\n***Your objective*** is to figure out who is the spy and expose him/her ***without revealing the secret location***!\nWhen you suspect someone of being the spy, you can use ```s! blame @username``` command to blame the spy and this will start a poll. If the vote passes and the person accused is the actual spy, you ***win*** otherwise you ***lose***."
		            			+ "\nBut be careful! You only get to blame ***once*** a round!");
		            	eb.setColor(Color.GREEN);
		            	voters.get(randomPlayerIndex).openPrivateChannel().complete().sendMessage(eb.build()).queue();
		            	voters.remove(randomPlayerIndex);
		            	current.getRoles().remove(randomRoleIndex);
	            	}
	            	App.players = players;
	            	App.currentLocation = current;
	            	eb.setTitle("Starting the Game!");
	            	eb.setDescription(description + "\n\n***"+ firstAsker + " is going to start asking first. After that every player that answers a question will ask the next question to someone of their choice, until the game ends.***\nMore info: ,```s! help```");
	            	eb.setColor(Color.CYAN);
	            	me = eb.build();
	            	event.getTextChannel().sendMessage(me).queue();
	            	App.started = true;
	            }else {
	            	eb.setTitle("Not enough players voted to play.");
	            	eb.setColor(Color.YELLOW);
	            	eb.setDescription("The number of players who voted to play did not meet the requirments.");
	            	me = eb.build();
	            	event.getTextChannel().sendMessage(me).queue();;
	            }
	            ours.delete().queue();
				
			}else if("start".equals(command[1]) && App.started) {
				eb.setTitle("Error!");
				eb.setDescription("A game is already being played...");
				eb.setColor(Color.YELLOW);
				event.getTextChannel().sendMessage(eb.build()).complete();
			}else if("guess".equals(command[1]) && App.started) {
				
				boolean playing = false;
				
				for(Map.Entry<String, User> user: App.players.entrySet()) {
					if(user.getValue().getId().equals(event.getMessage().getAuthor().getId())) {
						playing = true;
						break;
					}
				}
				
				if(!playing) {
					return; //the person issuing the command is not playing the game
				}
				
				if(command.length > 2 && !command[2].isEmpty()) {
					if(event.getMessage().getAuthor().getId().equals(App.players.get("Spy").getId())) {
						//TODO: CHECK IF THE GUESS WAS CORRECT
						 try {
						        double d = Double.parseDouble(command[2]);
						        if(App.currentLocation.getId() == d) {
						        	eb.setTitle("Spy Victory!");
						        	eb.setColor(Color.RED);
						        	eb.setDescription("Spy ***" + event.getMessage().getAuthor().getName() + "*** successfully guessed the location (***"+ App.currentLocation.getName() +"***)and got ***4pts***.");
						        	util.addScore(App.players.get("Spy").getName(), 4);
						        }else {
						        	eb.setTitle("Non-Spy Victory!");
						        	eb.setColor(Color.GREEN);
						        	eb.setDescription("Spy ***" + event.getMessage().getAuthor().getName() + "*** failed guessing the location and ***Non-Spy players got 1pt***.");
						        	App.players.remove("Spy");
						        	for(Map.Entry<String, User> entry: App.players.entrySet()) {
						        		util.addScore(entry.getValue().getName(), 1);
						        	}
						        }
					    } catch (NumberFormatException | NullPointerException nfe) {
					        if(event.getMessage().getContentRaw().substring(command[0].length()+ command[1].length()+2).trim().equalsIgnoreCase(App.currentLocation.getName())) {
					        	eb.setTitle("Spy Victory!");
					        	eb.setColor(Color.RED);
					        	eb.setDescription("Spy ***" + event.getMessage().getAuthor().getName() + "*** successfully guessed the location and got ***4pts***.");
					        	util.addScore(App.players.get("Spy").getName(), 4);
					        }else {
					        	eb.setTitle("Non-Spy Victory!");
					        	eb.setColor(Color.GREEN);
					        	eb.setDescription("Spy ***" + event.getMessage().getAuthor().getName() + "*** failed guessing the location and ***Non-Spy players got 1pt***.");
					        	App.players.remove("Spy");
					        	for(Map.Entry<String, User> entry: App.players.entrySet()) {
					        		util.addScore(entry.getValue().getName(), 1);
					        	}
					        }
					    }
						event.getTextChannel().sendMessage(eb.build()).queue();
						App.timer.cancel();
						App.timer.purge();
						App.started = false;
					}
				}
				
			}else if("blame".equals(command[1]) && App.started) {
				
				boolean playing = false;
				
				for(Map.Entry<String, User> user: App.players.entrySet()) {
					if(user.getValue().getId().equals(event.getMessage().getAuthor().getId())) {
						playing = true;
						break;
					}
				}
				
				if(!playing) {
					return; //the person issuing the command is not playing the game
				}
				
				
				if(event.getMessage().getMentionedUsers() != null && !event.getMessage().getMentionedUsers().isEmpty()) {
					if(event.getMessage().getMentionedUsers().size() == 1) {
						//TODO: BLAME SOMEONE (VOTING STARTS)
						if(event.getMessage().getAuthor().getId().equals(event.getMessage().getMentionedUsers().get(0).getId())) {
							eb.setTitle("Error!");
							eb.setDescription("You cannot blame yourself!");
							eb.setColor(Color.YELLOW);
							event.getTextChannel().sendMessage(eb.build()).queue();
							return;//you cannot blame yourself
						}
						
						if(App.blamers.get(event.getMessage().getAuthor().getId())!=null) {
							eb.setTitle("Error!");
							eb.setDescription("You already blamed someone this round!");
							eb.setColor(Color.YELLOW);
							event.getTextChannel().sendMessage(eb.build()).queue();
							return;
						}
						
						boolean found = false;
						for(Map.Entry<String, User> entry: App.players.entrySet()) {
							if(entry.getValue().getId().equals(event.getMessage().getMentionedUsers().get(0).getId())) {
								found=true;
								break;
							}
						}
						
						if(!found) {
							eb.setTitle("Error!");
							eb.setDescription("You can only blame someone who is playing the game!");
							eb.setColor(Color.YELLOW);
							event.getTextChannel().sendMessage(eb.build()).queue();
							return;
						}
						App.timer.cancel();
						App.timer.purge();
						eb.setTitle(event.getMessage().getAuthor().getName() + " blamed " + event.getMessage().getMentionedUsers().get(0).getName());
						eb.setColor(Color.CYAN);
						eb.setDescription("To approve hit: \u2705 \nTo disapprove hit:\u274e \n(Suspect votes doesn't count! You have 10 seconds.)");
						event.getTextChannel().sendMessage(eb.build()).complete();
						App.blamers.put(event.getMessage().getAuthor().getId(), true);
						List<Message> history = event.getChannel().getHistory().retrievePast(10).complete();
						Message ours = null;
						for(Message m: history) {
							if(m.getAuthor().getId().equals(App.jda.getSelfUser().getId())) {
								ours = m;
								break;
							}
						}
						ours.addReaction("\u2705").complete();
						ours.addReaction("\u274e").complete();
						
						try {
							Thread.sleep(10 * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						int onay = 0;
						String votes = "";
						for(User u: event.getChannel().getMessageById(ours.getId()).complete().getReactions().get(0).getUsers()) {
							if(!u.isBot() && !u.getId().equals(event.getMessage().getMentionedUsers().get(0).getId())) {
								votes += u.getName() + " \u2705\n";
								onay++;
							}
						}
						int ret = 0;
						for(User u: event.getChannel().getMessageById(ours.getId()).complete().getReactions().get(1).getUsers()) {
							if(!u.isBot() && !u.getId().equals(event.getMessage().getMentionedUsers().get(0).getId())) {
								votes += u.getName() + " \u274e\n";
								ret++;
							}
						}
						
						if(onay > ret) {
							if(event.getMessage().getMentionedUsers().get(0).getId().equals(App.players.get("Spy").getId())) {
								eb.setTitle("Non-Spy Victory!");
								eb.setColor(Color.GREEN);
								eb.setDescription("Non-Spy players successfully blamed the Spy.\nEveryone gets 1 pts.\n" + event.getMessage().getAuthor().getName() + " gets 2 pts."
										+ "\n\n***Votes:***\n"+ votes);
								App.players.remove("Spy");
								for(Map.Entry<String, User> entry: App.players.entrySet()) {
									if(entry.getValue().getId().equals(event.getMessage().getAuthor().getId())) {
										util.addScore(entry.getValue().getName(), 2);
									}else {
										util.addScore(entry.getValue().getName(), 1);
									}
								}
							}else {
								eb.setTitle("Spy Victory!");
								eb.setDescription("Non-Spy players falsely accused " + event.getMessage().getMentionedUsers().get(0).getName() + " for being the spy but the spy was ***" 
								+ App.players.get("Spy").getName() + "*** and got 4pts.");
								util.addScore(App.players.get("Spy").getName(), 4);
								eb.setColor(Color.RED);
							}
							event.getTextChannel().sendMessage(eb.build()).queue();
							App.started = false;
						}else {
							eb.setTitle("Vote did not go through!");
							eb.setDescription("Players decided not to blame this person of being the spy."
									+ "\n\n***Votes:***\n" + votes);
							eb.setColor(Color.CYAN);
							event.getMessage().getTextChannel().sendMessage(eb.build()).queue();
							App.startTime = App.startTime + 10 * 1000; // we need to add game 10 more seconds because of voting duration
							App.timer = new Timer();
							App.timer.schedule(new GameTimer(), App.gameDuration - (System.currentTimeMillis() - App.startTime));
						}
						ours.delete().complete();
					}else {
						eb.setTitle("Error!");
						eb.setDescription("You can only blame one person at a time.");
						eb.setColor(Color.YELLOW);
						event.getMessage().getTextChannel().sendMessage(eb.build()).queue();
					}
				}else {
					eb.setTitle("Error!");
					eb.setDescription("```s! blame @UserName```");
					eb.setColor(Color.YELLOW);
					event.getMessage().getTextChannel().sendMessage(eb.build()).queue();
				}
				
			}else if("scoreboard".equals(command[1])) {
				//TODO:SEND SCOREBOARD
				String scores = "";
				scores = App.scoreboard.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining("\n"));
				eb.setTitle("Scoreboard:");
				eb.setDescription(scores);
				eb.setColor(Color.CYAN);
				event.getTextChannel().sendMessage(eb.build()).queue();
			}else if("clear".equals(command[1])) {
				//TODO:CLEAR SCOREBOARD
				App.scoreboard = new HashMap<String,Integer>();
				eb.setColor(Color.CYAN);
				eb.setTitle("Done!");
				eb.setDescription("Scoreboard has been cleared");
				event.getTextChannel().sendMessage(eb.build()).queue();
			}else if("time".equals(command[1]) && App.started) {
				eb.setColor(Color.CYAN);
				eb.setTitle("Time Remaining:");
				long remaining = App.gameDuration - (System.currentTimeMillis() - App.startTime);
				long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
		        remaining = remaining - minutes * 1000 * 60;
		        long seconds = TimeUnit.MILLISECONDS.toSeconds(remaining);
				eb.setDescription(minutes + " minutes " + seconds + " seconds");
				event.getTextChannel().sendMessage(eb.build()).queue();
			}
			
		}
	}
	
}
