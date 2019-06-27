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
			if("help".equals(command[1])) {
				eb.setTitle("How to play:");
				eb.setColor(Color.CYAN);
				eb.setDescription("***s! start:*** Starts a new round."
						+ "\n***s! guess <Location Name/Id>:*** Let's spy reveal herself and guess the location."
						+ "\n***s! blame @username:*** Let's players blame someone of being the spy"
						+ "\n***s! time:*** Shows the remaining time."
						+ "\n***s! scoreboard:*** Shows the scoreboard."
						+ "\n***s! clear:*** Clears the scoreboard.");
				me = eb.build();
				if(event.getChannelType().isGuild())
					event.getTextChannel().sendMessage(me).queue();
				else
					event.getPrivateChannel().sendMessage(me).queue();;
			}else if("start".equals(command[1]) && !App.started) {
				eb.setTitle("Game Starting!");
				eb.setDescription("To be able to start playing, hit the plus reaction and wait.");
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
	            if(event.getChannel().getMessageById(ours.getId()).complete().getReactions().get(0).getCount() > 2 && event.getChannel().getMessageById(ours.getId()).complete().getReactions().get(0).getCount() < 9) {
	            	String description = "**Players:** ";
	            	List<User> voters = event.getChannel().getMessageById(ours.getId()).complete().getReactions().get(0).getUsers().complete();
	            	
	            	Location current = App.locations.get((int) (Math.random() * App.locations.size()));
	            	Map<String, User> players = new HashMap<>();
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
	            	eb.setDescription(description + "\n\n***"+ firstAsker + " is going to start asking first. After that every replier will ask a question to someone of their choice, until the game ends.***");
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
