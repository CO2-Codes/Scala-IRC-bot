# Scala-IRC-bot

This is a Scala implementation of an IRC bot, on top of https://github.com/pircbotx/pircbotx.

## How to install
First, make a configuration file, based on [the example](example.conf).

Use `sbt assembly` to create a .jar. Then run the bot with `java -jar <filename.jar> <path/to/configfile>`.

To run the bot without making a jar, use `sbt "run <path/to/configfile>"`

This bot is tested on openJDK 11.

In the configuration file, the array of `bot-admins` is used to decide who is allowed to run admin-only commands.
The separate `puppet-masters` array is used to decide who may puppet the bot.
  
The `listeners` array is the most important. This decides which functionality your instance of the bot will have.
Every listener has an ignore-channels settings which can be used to ignore all messages from those channels for that
specific listener.
 
## Available listeners

### adminListener
This should always be on. It sets the +B (bot user mode that many IRC servers require) on connect, and it listens to
!help messages, and !quit messages from bot-admins. 

This listener also contains the puppet-master functionality:
Use !say #channelname message or !act #channelname message to have the bot send these messages to the channel.
Instead of channel names nicknames can be used for PMs. Note: There's no check in place to see if the bot is in the
given channel or anything, use at your discretion. 

### linkListener
This listener reacts to messages and actions containing http/https links. It attempts to retrieve the <title> tag in
html pages and if it can find one, it will send the title to the channel.

### pronounListener
This listener stores users' personal pronouns (he, she, they, it, other) and can be used to look up the pronouns. It
writes these into a file to keep them between restarts.

### User documentation
If you want to run your own instance of the bot please host your own documentation specific to that instance.
Feel free to use [Isaac's documentation](https://co2.codes/xkcd/isaac-docs.php) (the original instance of this bot)
as a basis for your own. 