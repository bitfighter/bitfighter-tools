'''
Created on Jul 5, 2013

@author: dbuck
'''
import threading
from threads import PeriodicTimer


SCAN_DELAY = 300

class UpdateConfig(threading.Thread):
    def __init__(self, config):
        threading.Thread.__init__(self)
        
        self.config = config
        return
    
    def run(self):
        timer = PeriodicTimer(SCAN_DELAY, self.reload_config)
        timer.start()
        
    def reload_config(self):
        if self.config is not None:
            self.config.read('main.cfg')
            
        return True