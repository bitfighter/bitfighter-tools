'''
Created on Jul 5, 2013

@author: dbuck
'''
import threading
import logging


SCAN_DELAY = 300

class UpdateConfig(threading.Thread):
    def __init__(self, config):
        threading.Thread.__init__(self)
        
        self.config = config
        self.event = threading.Event()
    
    def run(self):
        self.event.wait(SCAN_DELAY)
        
        while not self.event.is_set():
            logging.debug("Reloading config")
            self.reload_config()
            self.event.wait(SCAN_DELAY)
        
    def stop(self):
        self.event.set()


    def reload_config(self):
        if self.config is not None:
            self.config.read('main.cfg')
