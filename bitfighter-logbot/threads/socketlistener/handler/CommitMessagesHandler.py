from threads.socketlistener.handler.Handler import Handler
from irc.client import Event
import time

COMMIT_BREAK = "BREAK";

COMMIT_POST_DELAY = 1.5


def chunker(seq, size):
    return (seq[pos:pos + size] for pos in xrange(0, len(seq), size))


class CommitMessagesHandler(Handler):
    def __init__(self):
        Handler.__init__(self)
        
    def handle_data(self, clientsocket, bot):
        commits = []
        
        ''' Read in and parse data from socket '''
        try:
            # make a file object to do the buffering
            socketfile = clientsocket.makefile()
            
            commit = ''
            for line in socketfile.readlines():
                line = line.strip()
                
                if line == COMMIT_BREAK:
                    commits.append(commit)
                    commit = ''
                    continue
                
                commit += line + ' '
                
            # Add final entry
            commits.append(commit)
        finally:
            socketfile.close()


        ''' Now post commits to the channel '''
        for post in commits:
            ''' Chunk the string if it is too long '''
            for chunk in chunker(post, 450):  # Should be safe for the 512-byte constraint
                # Post the commit!
                bot.connection.action(bot.channel, chunk)
                                
                # Log it
                event = Event("action", bot.connection.get_nickname(), bot.channel, [chunk])
                bot.on_action(bot.connection, event)
                
                # Don't spam too fast
                time.sleep(COMMIT_POST_DELAY)
        
        