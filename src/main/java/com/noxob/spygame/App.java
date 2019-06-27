package com.noxob.spygame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.security.auth.login.LoginException;

import com.noxob.spygame.models.Location;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class App 
{
	public static JDA jda = null;
	
	public static boolean started = false;
	public static long startTime;
	public static TextChannel channel;
	public static long gameDuration = 1 * 60 * 1000;
	public static Timer timer = new Timer();
	public static boolean commencing = false;
	public static Map<String, User> players;
	public static Map<String,Boolean> blamers;
	public static Map<String,Integer> scoreboard = new HashMap<String,Integer>();
	public static Location currentLocation;
	
	public static List<Location> locations;
    public static void main( String[] args )
    {
    	
        try {
            jda  = new JDABuilder(AccountType.BOT).setToken("INSERT-YOUR-BOT-TOKEN-HERE").buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initLocations();
        jda.getPresence().setGame(Game.playing("Spy Game"));
        jda.addEventListener(new MessageResponder());
    }
    
    public static void initLocations() {
    	locations = new ArrayList<Location>();
    	locations.add(new Location(0, "Airplane", new LinkedList<>(Arrays.asList("First Class Passenger", "Air Marshall", "Mechanic", "Economy Class Passenger", "Stewardess", "Co-Pilot", "Captain"))));
    	locations.add(new Location(1, "Bank", new LinkedList<>(Arrays.asList("Armored Car Driver", "Manager", "Consultant", "Customer", "Robber", "Security Guard", "Teller"))));
    	locations.add(new Location(2, "Beach", new LinkedList<>(Arrays.asList("Beach Waitress", "Kite Surfer", "Life Guard", "Thief", "Beach Goer", "Beach Photographer", "Ice Cream Truck Driver"))));
    	locations.add(new Location(3, "Casino", new LinkedList<>(Arrays.asList("Bartender", "Head Security Guard", "Bouncer", "Manager", "Hustler", "Dealer", "Gambler"))));
    	locations.add(new Location(4, "Cathedral", new LinkedList<>(Arrays.asList("Priest", "Beggar", "Sinner", "Parishioner", "Tourist", "Sponsor", "Choir Singer"))));
    	locations.add(new Location(5, "Circus Tent", new LinkedList<>(Arrays.asList("Acrobat", "Animal Trainer", "Magician", "Visitor", "Fire Eater", "Clown", "Juggler"))));
    	locations.add(new Location(6, "Corporate Party", new LinkedList<>(Arrays.asList("Entertainer", "Manager", "Unwelcomed Guest", "Owner", "Secretary", "Accountant", "Delivery Boy"))));
    	locations.add(new Location(7, "Crusader Army", new LinkedList<>(Arrays.asList("Monk", "War prisoner", "Servant", "Bishop", "Squire", "Archer", "Knight"))));
    	locations.add(new Location(8, "Day Spa", new LinkedList<>(Arrays.asList("Customer", "Stylist", "Masseuse", "Manicurist", "Makeup Artist", "Dermatologist", "Beautician"))));
    	locations.add(new Location(9, "Embassy", new LinkedList<>(Arrays.asList("Security Guard", "Secretary", "Ambassador", "Government Official", "Tourist", "Refugee", "Diplomat"))));
    	locations.add(new Location(10, "Hospital", new LinkedList<>(Arrays.asList("Nurse", "Doctor", "Anesthesiologist", "Intern", "Patient", "Therapist", "Surgeon"))));
    	locations.add(new Location(11, "Hotel",  new LinkedList<>(Arrays.asList("Doorman", "Security Guard", "Manager", "Housekeeper", "Customer", "Bartender", "Bellman"))));
    	locations.add(new Location(12, "Military Base", new LinkedList<>(Arrays.asList("Deserter", "Colonel", "Medic", "Soldier", "Sniper", "Officer", "Tank Engineer"))));
    	locations.add(new Location(13, "Movie Studio", new LinkedList<>(Arrays.asList("Stuntman", "Sound Engineer", "Cameraman", "Director", "Costume Artist", "Actor", "Producer"))));
    	locations.add(new Location(14, "Ocean Liner", new LinkedList<>(Arrays.asList("Rich Passenger", "Cook", "Captain", "Bartender", "Musician", "Waiter", "Mechanic"))));
    	locations.add(new Location(15, "Passenger Train", new LinkedList<>(Arrays.asList("Mechanic", "Border Patrol", "Train Attendant", "Passenger", "Restaurant Chef", "Engineer", "Stoker"))));
    	locations.add(new Location(16, "Pirate Ship", new LinkedList<>(Arrays.asList("Cook", "Sailor", "Slave", "Cannoneer", "Bound Prisoner", "Cabin Boy", "Brave Captain"))));
    	locations.add(new Location(17, "Polar Station", new LinkedList<>(Arrays.asList("Medic", "Geologist", "Expedition Leader", "Biologist", "Radioman", "Hydrologist", "Meteorologist"))));
    	locations.add(new Location(18, "Police Station", new LinkedList<>(Arrays.asList("Detective", "Lawyer", "Journalist", "Criminalist", "Archivist", "Patrol Officer", "Criminal"))));
    	locations.add(new Location(19, "Restaurant", new LinkedList<>(Arrays.asList("Musician", "Customer", "Bouncer", "Hostess", "Head Chef", "Food Critic", "Waiter"))));
    	locations.add(new Location(20, "School", new LinkedList<>(Arrays.asList("Gym Teacher", "Student", "Principal", "Security Guard", "Janitor", "Lunch Lady", "Maintenance Man"))));
    	locations.add(new Location(21, "Service Station", new LinkedList<>(Arrays.asList("Manager", "Tire Specialist", "Biker", "Car Owner", "Car Wash Operator", "Electrician", "Auto Mechanic"))));
    	locations.add(new Location(22, "Space Station", new LinkedList<>(Arrays.asList("Engineer", "Alien", "Space Tourist", "Pilot", "Commander", "Scientist", "Doctor"))));
    	locations.add(new Location(23, "Submarine", new LinkedList<>(Arrays.asList("Cook", "Commander", "Sonar Technician", "Electronics Technician", "Sailor", "Radioman", "Navigator"))));
    	locations.add(new Location(24, "Supermarket", new LinkedList<>(Arrays.asList("Customer", "Cashier", "Butcher", "Janitor", "Security Guard", "Food Sample Demonstrator", "Shelf Stocker"))));
    	locations.add(new Location(25, "Broadway Theater", new LinkedList<>(Arrays.asList("Coat Check Lady", "Prompter", "Cashier", "Visitor", "Director", "Actor", "Crewman"))));
    	locations.add(new Location(26, "University", new LinkedList<>(Arrays.asList("Graduate Student", "Professor", "Dean", "Psychologist", "Maintenance Man", "Janitor", "Student"))));
    }
}
