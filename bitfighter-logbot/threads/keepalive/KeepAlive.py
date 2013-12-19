'''
Created on Jul 5, 2013

@author: dbuck
'''
from threads import PeriodicTimer
import threading
import time


PING_DELAY = 60

class KeepAlive(threading.Thread):
    def __init__(self, connection):
        threading.Thread.__init__(self)
        
        self.connection = connection
        return
    
    def run(self):
        timer = PeriodicTimer(PING_DELAY, self.do_ping)
        timer.start()
    
    def do_ping(self):
        if self.connection is not None:
            currentdate = time.strftime("%H%M%S", time.localtime(time.time()))
            self.connection.send_raw("PING LAG" + currentdate)
            
        return True

    def on_ping(self, c, e):
        self.do_ping()
