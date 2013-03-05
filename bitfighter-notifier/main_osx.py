#!/usr/bin/env python

import objc
from Foundation import *
from AppKit import *
from PyObjCTools import NibClassBuilder, AppHelper
import logging
import core
import platform


class Alert(object):
    def __init__(self, messageText):
        super(Alert, self).__init__()
        self.messageText = messageText
        self.informativeText = ""
        self.buttons = []
    
    
    def displayAlert(self):
#        alert = NSAlert.alloc().init()
#        alert.setMessageText_(self.messageText)
#        alert.setInformativeText_(self.informativeText)
#        alert.setAlertStyle_(NSInformationalAlertStyle)
#        for button in self.buttons:
#            alert.addButtonWithTitle_(button)
#        NSApp.activateIgnoringOtherApps_(True)
#        self.buttonPressed = alert.runModal()
        graphicsRect = NSMakeRect(100.0, 350.0, 450.0, 400.0)
        myWindow = NSWindow.alloc().initWithContentRect_styleMask_backing_defer_(
            graphicsRect, 
            NSTitledWindowMask | NSClosableWindowMask | NSResizableWindowMask,
            NSBackingStoreBuffered, False
            )
        myWindow.setTitle_('Bitfighter')
#        myView = DemoView.alloc().initWithFrame_(graphicsRect)
#        myWindow.setContentView_(myView)
#        myDelegate = AppDelegate.alloc().init()
#        myWindow.setDelegate_(myDelegate)
        myWindow.display()
        myWindow.orderFrontRegardless()


class MessengerOSXLegacy(NSObject, core.MessengerBase):
    def __init__(self, timeout):
        core.MessengerBase.__init__(timeout)
        
    
    def notify(self, comein, goout):
        pool = NSAutoreleasePool.alloc().init()
        
        ap = Alert("Bitfighter")
        ap.informativeText = self.makeMessage(comein, goout)
        ap.displayAlert()
        
#        return ap.buttonPressed
        
        del pool
        
    def setMembers(self, timeout):
        self.timeout = timeout
        

class MessengerOSXMountainLion(NSObject, core.MessengerBase):
    def __init__(self, timeout):
        core.MessengerBase.__init__(timeout)
        

    def notify(self, comein, goout):
        # We need to set up our own autorelease pool because this happens outside of the main 
        # thread where there is no pool
        pool = NSAutoreleasePool.alloc().init()
        
        NSUserNotification = objc.lookUpClass('NSUserNotification')
        NSUserNotificationCenter = objc.lookUpClass('NSUserNotificationCenter')
        notification = NSUserNotification.alloc().init()
        notification.setTitle_(self.title)
        notification.setInformativeText_(self.makeMessage(comein, goout))
        NSUserNotificationCenter.defaultUserNotificationCenter().setDelegate_(self)
        NSUserNotificationCenter.defaultUserNotificationCenter().scheduleNotification_(notification)
        
        del pool
        
        
    def setMembers(self, timeout):
        self.timeout = timeout
        

startTime = NSDate.date()

class GuiApplicationOSX(NSObject, core.GuiApplicationBase):
    statusbar = None
    state = 'idle'
    statusitem = None

    
    def setMembers(self, executable, iconPath):
        self.iconPath = iconPath
            
            
    def setTimer(self, timer):
        self.timer = timer
        
    
    def applicationDidFinishLaunching_(self, notification):
        statusbar = NSStatusBar.systemStatusBar()
        self.statusitem = statusbar.statusItemWithLength_(NSVariableStatusItemLength)
        
        try:
            self.statusitem.setImage_(NSImage.alloc().initByReferencingFile_(self.iconPath))
        except:
            logging.exception("Unable to load icon, use label instead")
            self.statusitem.setTitle_(self.title)
        
        self.statusitem.setHighlightMode_(1)

        # Build a very simple menu
        self.menu = NSMenu.alloc().init()

        # Default event
        menuitem = NSMenuItem.alloc().initWithTitle_action_keyEquivalent_('Exit', 'terminate:', '')
        self.menu.addItem_(menuitem)

        # Bind it to the status item
        self.statusitem.setMenu_(self.menu)
        
        # FIXME: We need to bind the self.timer.cancel() method to a quit callback
        # OSX seems to kill thread anyways...  so..  maybe not needed?


    def refreshToolTip(self, players):
        if self.statusitem is None:
            return
        
        pool = NSAutoreleasePool.alloc().init()
        
        self.statusitem.setToolTip_(self.formTooltip(players))
        
        del pool
        
        
class NotifierOSX(core.NotifierBase):
    def __init__(self, configFile = "bitfighter-notifier.ini"):
        super(NotifierOSX, self).__init__(configFile)
        logging.debug("OSX!")
        

    def run(self):
        
        osxVersion, _, _ = platform.mac_ver()
        majorVersion = int(osxVersion.split('.')[0])
        minorVersion = int(osxVersion.split('.')[1])
        
        logging.debug("major: %s; minor: %s", majorVersion, minorVersion)
        
        app = NSApplication.sharedApplication()
        guiApp = GuiApplicationOSX.alloc().init()
        app.setDelegate_(guiApp)

        guiApp.setMembers(self.executable, self.iconPath)

        messenger = None
        if minorVersion >= 8:
            messenger = MessengerOSXMountainLion.alloc().init()
        else:
            messenger = MessengerOSXLegacy.alloc().init()
        
        messenger.setMembers(self.notificationTimeout)
        
        receiver = core.PlayersListReceiver(self.url, messenger, guiApp)
        
        timer = core.PeriodicTimer(self.refreshInterval, receiver.refresh)
        timer.start()
        
        guiApp.setTimer(timer)
        AppHelper.runEventLoop()
        

def main():
    iniPath = 'bitfighter-notifier-osx.ini'
    
    notifier = NotifierOSX(iniPath)
    notifier.run()


if __name__ == '__main__':
    main()
