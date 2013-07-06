import functools
import threading


class PeriodicTimer(object):
    def __init__(self, interval, callback):
        self.interval = interval
 
        @functools.wraps(callback)
        def wrapper(*args, **kwargs):
            # Do the action!
            result = callback(*args, **kwargs)

            if result:
                # Restart a Timer to call this same action again
                self.thread = threading.Timer(self.interval, self.callback)
                self.thread.start()
 
        self.callback = wrapper
 
    def start(self):
        self.thread = threading.Timer(self.interval, self.callback)
        self.thread.start()
 
    def cancel(self):
        self.thread.cancel()
