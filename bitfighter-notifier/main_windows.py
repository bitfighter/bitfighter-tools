'''
Created on Jan 14, 2013

@author: raptor

You need the following packages to run this:
 - pywin32
 - systray
'''

from systray import systray
import core
import logging
import subprocess


class MessengerWindows(core.MessengerBase):
    def __init__(self, timeout, guiApp):
        super(MessengerWindows, self).__init__(timeout)
        
        self.guiApp = guiApp


    def notify(self, comein, goout):
        self.guiApp.trayapp.systray.show_message(self.makeMessage(comein, goout))


class GuiApplicationWindows(core.GuiApplicationBase):
    def __init__(self, executable, iconPath):
        super(GuiApplicationWindows, self).__init__(iconPath)
        
        self.executable = executable
        self.trayapp = None
        
        
    def getNotificationIcon(self):
        try:
            return None
        except:
            return None
        
        
    def launchExecutable(self, *args):
        try:
            subprocess.Popen(self.executable, shell=True)
        except:
            logging.error("Unable to run {0}".format(self.cmd))


    def rightClickEvent(self, icon, button, time):
        pass


    def refreshToolTip(self, players):
        if self.trayapp is None:
            return
        
        self.trayapp.systray.set_status("\n" + self.formTooltip(players))

        
    def onQuit(self, _systrayApp):
        if self.timer:
            self.timer.cancel()


    def run(self):
        self.trayapp = systray.App('Bitfighter Master', self.iconPath)
        self.trayapp.on_quit = self.onQuit

        self.trayapp.start()
        


class NotifierWindows(core.NotifierBase):
    def __init__(self, configFile = "bitfighter-notifier.ini"):
        super(NotifierWindows, self).__init__(configFile)
        logging.debug("Windows!")


    def run(self):
        guiApp = GuiApplicationWindows(self.executable, self.iconPath)
        messenger = MessengerWindows(self.notificationTimeout, guiApp)
        
        plr = core.PlayersListReceiver(self.url, messenger, guiApp)
        
        timer = core.PeriodicTimer(self.refreshInterval, plr.refresh)
        timer.start()
        
        # Bind the timer
        guiApp.setTimer(timer)
        guiApp.run()
        

def main():
    iniPath = 'bitfighter-notifier-windows.ini'
    
    notifier = NotifierWindows(iniPath)
    notifier.run()
    

if __name__ == '__main__':
    main()