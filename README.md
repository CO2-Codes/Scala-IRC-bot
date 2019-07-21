# Scala-IRC-bot

This is a Scala implementation of an IRC bot, on top of https://github.com/pircbotx/pircbotx.

## How to install
First, make a configuration file, based on [the example](example.conf).

Use `sbt assembly` to create a .jar. Then run the bot with `java -jar <filename.jar> <path/to/configfile>`.

To run the bot without making a jar, use `sbt "run <path/to/configfile>"`

In the configuration file, the array of `bot-admins` is used to decide who is allowed to run admin-only commands (currently
 only !quit in the adminListener). The separate `puppet-masters` array is used to decide who may puppet the bot.
  
The `listeners` array is the most important. This decides which functionality your
 instance of the bot will have.
 
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

# Known issues
There appears to be a race condition in the HttpClient where sometimes a Future fails. This is hard to debug since adding
logging seems to make this not happen anymore. The result of this bug is that sometimes a title doesn't come through.