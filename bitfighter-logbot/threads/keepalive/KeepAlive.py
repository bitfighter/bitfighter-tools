'''
Created on Jul 5, 2013

@author: dbuck
'''
import threading
import logging


PING_DELAY = 60

class KeepAlive(threading.Thread):
    def __init__(self, bot):
        threading.Thread.__init__(self)
        
        self.bot = bot
        self.event = threading.Event()

    def run(self):
        self.event.wait(PING_DELAY)
        
        while not self.event.is_set():
            logging.debug("Sending PING")
            self.bot.do_ping()
            self.event.wait(PING_DELAY)

    def stop(self):
        self.event.set()
