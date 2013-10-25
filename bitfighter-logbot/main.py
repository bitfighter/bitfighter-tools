'''
Created on Jul 4, 2013

@author: raptor

'''
from irc.client import NickMask, ip_numstr_to_quad
from modules.fortune.Fortune import Fortune
from threads.keepalive.KeepAlive import KeepAlive
from threads.socketlistener.SocketListener import SocketListener
from threads.updateconfig.UpdateConfig import UpdateConfig
import ConfigParser
import cgi
import irc.bot
import logging
import re
import sys
import time
import traceback

# Set up a basic logging handler
logging.basicConfig(level=logging.INFO)
# logging.basicConfig(level=logging.DEBUG)


# For link-searching
URL_REGEX = re.compile(r'''((http|https|ftp|irc)://[^\s]+)''')

    
'''
    Our main object!
    
    BitfighterLogBot does everything you want and more!
'''
class BitfighterLogBot(irc.bot.SingleServerIRCBot):
    def __init__(self, config, channel, nickname, server, port=6667):
        irc.bot.SingleServerIRCBot.__init__(self, [(server, port)], nickname, nickname)
        self.channel = channel
        self.config = config

        self.modules  = []
    
    
    """
        Do start-up stuff
    """
    def our_start(self):
        ''' Load our other irc modules '''
#         self.modules.append(KungFu(self))
        self.modules.append(Fortune(self))
        
        ''' Run our threads '''
        keepalive = KeepAlive(self.connection)
        keepalive.start()
        
        updateconfig = UpdateConfig(self.config)
        updateconfig.start()
        
        port = int(self.config.get('threads', 'socket_listener_port'))
        socketlistener = SocketListener('localhost', port, self)
        socketlistener.start()

        # Start the IRC bot!
        self.start()


    """
        This is run in SimpleIRCClient._dispatcher and let's us call IRC callbacks in the        
        submodules
    """
    def _modules_dispatcher(self, c, e):
        method = "on_" + e.type
        for module in self.modules:
            if hasattr(module, method):
                getattr(module, method)(c, e)
    
                
    """
    Each callback happens on a specific IRC command, on_join -> JOIN, etc.
        c = connection object
        e = event object
    """
    def on_action(self, c, e):
        message = e.arguments[0].strip()
        sender = NickMask(e.source).nick

        self.append_to_log("e", "* " + sender + " " + message)
        
    def on_join(self, c, e):
        mask = NickMask(e.source)
        nick = mask.nick
        user = mask.user
        host = mask.host
        
        self.append_to_log("a", "--> " + nick + " (" + user + "@" + host + ") has joined")
        
    def on_nicknameinuse(self, c, e):
        c.nick(c.get_nickname() + "_")

    def on_welcome(self, c, e):
        c.join(self.channel)

    def on_privmsg(self, c, e):
        message = e.arguments[0].strip()
        sender = e.source.nick
        
        if message.startswith("!"):
            self.do_command(e, c, sender, message.lstrip("!"))

    def on_pubmsg(self, c, e):
        message = e.arguments[0].strip()
        sender = e.source.nick
        
        self.append_to_log("b", "<" + sender + "> " + message)
        
        if message.startswith("!"):
            self.do_command(e, c, self.channel, message.lstrip("!"))
            
    def on_mode(self, c, e):
        source_nick = NickMask(e.source).nick
        self.append_to_log("a", "* " + source_nick + " sets mode " + e.arguments[0])
            
    def on_nick(self, c, e):
        source_nick = NickMask(e.source).nick
        self.append_to_log("a", "* " + source_nick + " is now known as " + e.target)
        
    def on_notice(self, c, e):
        message = e.arguments[0].strip()
        source_nick = NickMask(e.source).nick
        self.append_to_log("c", "-" + source_nick + "- " + message)
        
    def on_privnotice(self, c, e):
        message = e.arguments[0].strip()
        source_nick = NickMask(e.source).nick
        self.append_to_log("c", "-" + source_nick + "- " + message)
            
    def on_part(self, c, e):
        mask = NickMask(e.source)
        nick = mask.nick
        user = mask.user
        host = mask.host
        
        self.append_to_log("a", "<-- " + nick + " (" + user + "@" + host + ") has left " + self.channel)
            
    def on_ping(self, c, e):
        source_nick = NickMask(e.source).nick
        self.append_to_log("f", "[" + source_nick + " PING]")
        
    def on_version(self, c, e):
        source_nick = NickMask(e.source).nick
        self.append_to_log("f", "[" + source_nick + " VERSION]")
            
    def on_quit(self, c, e):
        mask = NickMask(e.source)
        nick = mask.nick
        user = mask.user
        host = mask.host
        
        message = e.arguments[0].strip()
        self.append_to_log("d", "<-- " + nick + " (" + user + "@" + host + ") Quit (" + message + ")")
            
    def on_time(self, c, e):
        source_nick = NickMask(e.source).nick
        self.append_to_log("f", "[" + source_nick + " TIME]")
            
    def on_topic(self, c, e):
        topic = e.arguments[0].strip()
        source_nick = e.source.nick
        
        self.append_to_log("a", "* " + source_nick + " changes topic to '" + topic + "'");
            
    def on_kick(self, c, e):
        source_nick = NickMask(e.source).nick
        kickee = e.arguments[0]
        self.append_to_log("a", "* " + kickee + " was kicked from " + self.channel + " by " + source_nick);
        self.prvmsg_append_to_log(c, self.channel, "Bwahahaha! *snicker*")
        
        if kickee == self._nickname:
            c.join(self.channel)

    def on_dccmsg(self, c, e):
        c.privmsg("You said: " + e.arguments[0])

    def on_dccchat(self, c, e):
        if len(e.arguments) != 2:
            return
        args = e.arguments[1].split()
        if len(args) == 4:
            try:
                address = ip_numstr_to_quad(args[2])
                port = int(args[3])
            except ValueError:
                return
            self.dcc_connect(address, port)


    """
        Overrides
    """
    def get_version(self):
        return self.config.get('irc', 'version_message')


    """
        Other methods
    """
    # This method makes the bot respond and log its response
    def prvmsg_append_to_log(self, c, target, message):
        c.privmsg(target, message)
        self.append_to_log("b", "<" + self._nickname + "> " + message)
    
    # Run one of the various commands that start with a '!'
    def do_command(self, e, c, target, command):
        if command == "commands":
            c.privmsg(target, "Commands are: " + ', '.join(self.config.options('commands')))
            
        elif command == "motd":
            try:
                with open(self.config.get('irc', 'motd_file'), "r") as motdfile:
                    motd = motdfile.read().replace('\n', ' ')
                    c.privmsg(target, motd)
            except:
                traceback.print_exc()
                c.privmsg(target, "failed to load motd file")
            
        # Check for the command in our config, send response to the channel
        elif self.config.has_option('commands', command):
            response = self.config.get('commands', command)
            
            # We log in the main channel
            if target == self.channel:
                self.prvmsg_append_to_log(c, target, response)
            else:
                c.privmsg(target, response)
            
        else:
            c.privmsg(target, "Not understood. See !commands for a list of commands")
            
        return

    # Add a line to the log
    def append_to_log(self, color, line):
        # Escape HTML sequences and make links HTML friendly
        formattedline = URL_REGEX.sub(r'<a href="\1">\1</a>', cgi.escape(line))
        
        now = time.localtime(time.time())
        currentdate = time.strftime("%Y-%m-%d", now)
        currenttime = time.strftime("%H:%M:%S", now)
        
        # This make an entry like this:
        #  [13:56:22] <span class="b">&lt;raptor&gt; hi</span>
        entry = "[" + currenttime + "] <span class=\"" + color + "\">" + formattedline + "</span>\n"
        
        # Now write to the log file
        filename = self.config.get('logging', 'output_dir') + currentdate + ".log"
        
        with open(filename, "a") as logfile:
            # Make sure we re-encode to utf-8, as some messages could be decoded oddly
            # because of other peoples clients sending data in odd encodings
            logfile.write(entry.encode('utf-8'))
            logfile.close()
        
        return


def main():
    # Set up config
    config = ConfigParser.ConfigParser()
    config.read('main.cfg')

    server   =     config.get('irc', 'server')
    port     = int(config.get('irc', 'port'))
    channel  =     config.get('irc', 'channel')
    nickname =     config.get('irc', 'nick')
    
    # Start the bot!
    bot = BitfighterLogBot(config, channel, nickname, server, port)
    
    try:
        bot.our_start()
    except KeyboardInterrupt:
        pass
    except SystemExit:
        pass
    except:
        traceback.print_exc()
        sys.exit(0)

    bot.disconnect(config.get('irc', 'quit_message'))

if __name__ == "__main__":
    main()
