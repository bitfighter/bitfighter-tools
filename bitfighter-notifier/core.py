'''
Created on Jan 14, 2013

@author: raptor

This class contains shared components of the Bitfighter notifier.  

Classes:
 - NotifierBase
   
   This is main entry point for each port (windows, linux, osx, etc.).
    
 
 - GuiApplicationBase
 
   This handles all GUI (system tray) related functions and set up.
   
   
 - MessengerBase
 
   This sends a message to the GUI.
   
   
 - PlayerListReceiver
 
   This does the JSON download and parsing.
'''

import functools
import inspect
import json
import logging
import os
import sys
import threading

# Load the INI parser
try:
    import configparser
except:
    import ConfigParser as configparser


# Set logging level
logging.basicConfig(level=logging.DEBUG)

# Adapted from a random pastebin on the web: http://pastebin.com/1VcumfC3
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


class PlayersListReceiver(object):
    def __init__(self, url, messenger, guiApp):
        self.players = set()
        self.url = url
        self.messenger = messenger
        self.guiApp = guiApp
#        self.refresh()
#        self.guiApp.refreshToolTip(self.players) # set initial tooltip
        

    def fetch(self):
        # Python version 3 uses this
        if sys.version_info >= (3, 0):
            import urllib.request
            fp = urllib.request.urlopen(self.url)
            bytesInf = fp.read()
            strInf = bytesInf.decode("utf8")
            fp.close()
            return strInf
        # Python 2.x
        else:
            import urllib2
            response = urllib2.urlopen(self.url)
            return response.read()
        
            
    # This method is run in a separate thread and call        
    def refresh(self):
        logging.debug("Refreshing JSON")
        
        try:
            gameInf = json.loads(self.fetch())
        except:
            logging.exception("Unable to fetch data from {0}".format(self.url))
            return False
        
        playersNew = set(gameInf["players"])
        
        if playersNew != self.players:
            # Determine differences
            comein = playersNew.difference(self.players)
            goout = self.players.difference(playersNew)
    
            # Set our new list
            self.players = playersNew
            
            # Send a message and update our tooltip
            self.messenger.notify(comein, goout)
            self.guiApp.refreshToolTip(self.players)
            
        # Must return true to continue the periodic timer
        return True;


class GuiApplicationBase(object):
    def __init__(self, iconPath):
        self.iconPath = iconPath
        
        self.timer = None
            
            
    # Override these methods in sub-classes
    def refreshToolTip(self, players):
        logging.warn("Override me: %s", inspect.stack()[0][3])

    
    # *args is needed here for some callback calls (like with gtk+)
    def launchExecutable(self, *args):
        logging.warn("Override me: %s", inspect.stack()[0][3])
        

    # Common methods
    def setTimer(self, timer):
        self.timer = timer

        
    def formTooltip(self, players):
        if len(players) > 0:
            verb = 'is'
            if len(players) > 1:
                verb = 'are'
                
            return "{0}\n{1} online".format("\n".join(players), verb)
        else:
            return "Nobody is online"


class MessengerBase(object):
    TITLE = "Bitfighter server"
    def __init__(self, timeout):
        self.timeout = timeout
        
            
    # Common methods
    def notify(self, comein, goout):
        logging.warn("Override me: %s", inspect.stack()[0][3])
        
            
    def makeMessage(self, comein, goout):
        if len(comein) and len(goout):
            body="{0} has joined, {1} has left".format(", ".join(comein), ", ".join(goout))
        elif len(comein):
            body="{0} has joined".format(", ".join(comein))
        elif len(goout):
            body="{0} has left".format(", ".join(goout))
        return body


class NotifierBase(object):
    def __init__(self, configFile = "bitfighter-notifier.ini"):
        self.configFile = configFile
        logging.debug("Attempting to load config: %s", self.configFile)
        
        # Now parse the config file, use some default options if none are found
        cfg = configparser.SafeConfigParser({
            'url':      "http://bitfighter.org/bitfighterStatus.json",
            'iconPath': os.path.abspath("icon.png"),
            'appName':  "Bitfighter Notifier Applet",
            'notificationTimeout': '5000',
            'refreshInterval': '10000', 
            'executable': "bitfighter"
            })
        cfg.read(self.configFile)
    
        # Add notifier section if it isn't in config at all
        sectionName = 'notifier'
        if not "notifier" in cfg.sections():
            cfg.add_section(sectionName)
        
        self.url = cfg.get(sectionName, 'url')
        self.iconPath = cfg.get(sectionName, 'iconPath')
        self.appName = cfg.get(sectionName, 'appName')
        self.notificationTimeout = cfg.getint(sectionName, 'notificationTimeout')
        self.refreshInterval = cfg.getint(sectionName, 'refreshInterval')
        self.executable = cfg.get(sectionName, 'executable')

        
    def run(self):
        logging.warn("Override me: %s", inspect.stack()[0][3])
    