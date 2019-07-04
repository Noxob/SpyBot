# SpyBot
This is a Discord bot that let's you play the boardgame SpyFall.

# Made for Discord Hack Week #1
![Discord Hack Week](https://cdn-images-1.medium.com/max/2560/1*lh6NS8hx0pu5mlZeSqnu5w.jpeg)

# How do I play the game?
The game contains one spy and at max seven non-spy players. The identity of the spy is hidden and spy is chosen randomly by the bot. At the start of the game bot randomly chooses a player as questioner and a location. Each non-spy player has a secret role that is related to the secret location. Non-spy players know the secret location but the spy doesn't. The first questioner asks any question to a player of their choice then player questioned answers the question. After that every answering player will be the next one who asks the question. The answers given by the non-spy players should hint the other non-spy players that they know the secret location but it shouldn't reveal the location to the spy. The game ends in three conditions; when the timer runs out, when spy reveals himself/herself and tries to guess the secret location or when players decide to accuse someone of being the spy. More detailed information is given by the bot (use `s! help` command).

# How to host the bot
- Replace the string that reads `INSERT-YOUR-TOKEN-HERE` in the `com.noxob.spygame.App.java` class with your Discord bot token then compile the project and run it.

- To see a full list of available commands, type `s! commands` on the Discord server that you added your bot to.

- Play the game.


# Made with :heart: and
- [Java](https://www.java.com/)
- [JDA](https://github.com/DV8FromTheWorld/JDA)

# Goal
My goal is to make this bot usable by more than one group/server at the same time. The bot does not support multiple servers yet so you will have to host it on your own machine to play the game.

# Disclaimer
I have no connection to creators of the game SpyFall.
