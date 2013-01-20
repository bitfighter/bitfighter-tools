'''
Created on Jan 14, 2013

@author: raptor
'''

import core
import logging
import os
import subprocess
import sys


# The hard part. check the runtime environment
isQtGui = False
isGIGtk = False
isPyGtk = False

isNotify2 = False
isDBus = False
isCmdNotifySend = False


# detect GUI
def checkQtGui():
    global QApplication, QSystemTrayIcon, QMenu, QIcon, QImage, QObject, SIGNAL, isQtGui
    try:
        from PyQt4.QtGui import QApplication, QSystemTrayIcon, QMenu, QIcon, QImage
        from PyQt4.QtCore import QObject, SIGNAL
        isQtGui = True
    except:
        pass
    return isQtGui


def checkGIGtk():
    global gtk, gobject, GdkPixbuf, isGIGtk
    try:
        from gi.repository import Gtk as gtk
        from gi.repository import GObject as gobject
        from gi.repository import GdkPixbuf
        isGIGtk = True
    except:
        pass
    return isGIGtk


def checkPyGtk():
    global gtk, gobject, isPyGtk
    try:
        import pygtk
        import gtk
        import gobject
        isPyGtk = True
    except:
        pass
    return isPyGtk


# Detect notification engine
def checkNotify2():
    global notify2, isNotify2
    try:
        import notify2
        isNotify2 = True
    except:
        pass
    return isNotify2


def checkDBus():
    global dbus, isDBus
    try:
        import dbus
        isDBus = True
    except:
        pass
    return isDBus


def checkCmdNotifySend():
    global isCmdNotifySend
    try:
        isCmdNotifySend = subprocess.call(["type", "notify-send"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True) == 0
    finally:
        return isCmdNotifySend


# XXX Change order here for precedence
guiChecks = [checkQtGui, checkPyGtk, checkGIGtk]
engineChecks = [checkNotify2, checkDBus, checkCmdNotifySend]


for check in guiChecks:
    if check():
        logging.info("using: %s", check.__name__)
        break
else:
    logging.error("No suitable GUI toolkit found. Install either PyQt4 or GTK+ bindings for Python")
    exit(1)

for check in engineChecks:
    if check():
        logging.info("using: %s", check.__name__)
        break
else:
    logging.error("No suitable notification interface found. Install Python library notify2 or Python bindings to dbus or notify-send command")
    exit(1)


# =============================================================
# Set up GUI classes depending on what was detected

if isQtGui:
    class GuiApplicationLinux(core.GuiApplicationBase):
        def __init__(self, executable, iconPath, parent=None):
            super(GuiApplicationLinux, self).__init__(iconPath)
            
            self.eventLoop = 'qt'
            self.app = QApplication(sys.argv) # this should be done before anything else
            self.executable = executable
            
            if QIcon.hasThemeIcon(iconPath):
                icon = QIcon.fromTheme(iconPath)
            else:
                icon = QIcon(iconPath)
                
            self.statusIcon = QSystemTrayIcon(icon, parent)
            self.menu = QMenu(parent)
    
            exitAction = self.menu.addAction("Exit")
            exitAction.triggered.connect(self.quit)
            
            self.statusIcon.setContextMenu(self.menu)
            
            def activate(reason):
                if reason == QSystemTrayIcon.Trigger:
                    return self.launchExecutable()

            QObject.connect(self.statusIcon,
                SIGNAL("activated(QSystemTrayIcon::ActivationReason)"), activate)

            self.statusIcon.show()
            
        
        # A portable icon wrapper. Notify2 demands icon class to be compatible with GdkPixbuf so we 
        # provide a compatibility layer
        class IconWrapper(object):
            def __init__(self, iconName):
                if QIcon.hasThemeIcon(iconName):
                    icon = QIcon.fromTheme(iconName)
                else:
                    icon = QIcon(iconName)
                size = icon.availableSizes()[0]
                self.image = icon.pixmap(size).toImage().convertToFormat(QImage.Format_ARGB32)
                self.image = self.image.rgbSwapped() # otherwise colors are weird :/
    
            def get_width(self):
                return self.image.width()
    
            def get_height(self):
                return self.image.height()
    
            def get_rowstride(self):
                return self.image.bytesPerLine()
    
            def get_has_alpha(self):
                return self.image.hasAlphaChannel()
    
            def get_bits_per_sample(self):
                return self.image.depth() // self.get_n_channels()
    
            def get_n_channels(self):
                if self.image.isGrayscale():
                    return 1
                elif self.image.hasAlphaChannel():
                    return 4
                else:
                    return 3; 
    
            def get_pixels(self):
                return self.image.bits().asstring(self.image.numBytes())
        # end of wrapper class
 
    
        def getNotificationIcon(self):
            try:
                return self.IconWrapper(self.iconPath)
            except:
                logging.error("Failed to get notification icon")
                return None


        def refreshToolTip(self, players):
            self.statusIcon.setToolTip(self.formTooltip(players))
            
            
        def launchExecutable(self, *args):
            try:
                subprocess.Popen(self.executable, shell=True)
            except:
                logging.error("Unable to run {0}".format(self.cmd))

            
        def run(self):
            sys.exit(self.app.exec_())


        def quit(self):
            self.timer.cancel()
            sys.exit(0)

        
elif isGIGtk or isPyGtk:
    class GuiApplicationLinux(core.GuiApplicationBase):
        def __init__(self, executable, iconPath, parent = None):
            super(GuiApplicationLinux, self).__init__(iconPath)
            
            self.eventLoop = 'glib'
            self.executable = executable
            self.statusIcon = gtk.StatusIcon()
            self.statusIcon.set_from_file(iconPath)
            self.statusIcon.connect("activate", self.launchExecutable)
            self.statusIcon.connect("popup-menu", self.rightClickEvent)
            self.statusIcon.set_visible(True)
            
            
        def getNotificationIcon(self):
            try:
                if isGIGtk:
                    return GdkPixbuf.Pixbuf.new_from_file(self.iconPath)
                else:
                    return gtk.gdk.pixbuf_new_from_file(self.iconPath)
            except:
                return None
            
            
        def launchExecutable(self, *args):
            try:
                subprocess.Popen(self.executable, shell=True)
            except:
                logging.error("Unable to run {0}".format(self.cmd))


        def rightClickEvent(self, icon, button, time):
            menu = gtk.Menu()

            quitMenu = gtk.MenuItem("Exit")
            quitMenu.connect("activate", gtk.main_quit)
        
            menu.append(quitMenu)
            menu.show_all()

            if isGIGtk:
                menuPosition = gtk.StatusIcon.position_menu
                menu.popup(None, None, menuPosition, self.statusIcon, button, time)
            else:
                menuPosition = gtk.status_icon_position_menu
                menu.popup(None, None, menuPosition, button, time, self.statusIcon)

    
        def refreshToolTip(self, players):
            self.statusIcon.set_tooltip_text(self.formTooltip(players))


        def run(self):
            gobject.threads_init()   # This is needed or our python-based timer will fail
            gtk.quit_add(0, self.quit)
            gtk.main()
            
            
        def quit(self):
            self.timer.cancel()
            sys.exit(0)

# =============================================================
# Set up messenger classes depending on what was detected

if isNotify2:
    class MessengerLinux(core.MessengerBase):
        def __init__(self, appName, timeout, guiApp):
            super(MessengerLinux, self).__init__(timeout)
            
            notify2.init(appName, guiApp.eventLoop)
            self.notification = None
            
            if guiApp.iconPath:
                self.icon = guiApp.getNotificationIcon()


        def notify(self, comein, goout):
            body = self.makeMessage(comein, goout)
            if not self.notification:
                self.notification = notify2.Notification(self.title, body)
                if self.icon and self.icon.get_width() > 0:
                    self.notification.set_icon_from_pixbuf(self.icon)
                self.notification.timeout = self.timeout
            else:
                self.notification.update(self.title, body)
            self.notification.show()


elif isDBus:
    class MessengerLinux(core.MessengerBase):
        def __init__(self, appName, timeout, guiApp):
            super(MessengerLinux, self).__init__(timeout)
            
            self.appName = appName
            # copied from pynotify2
            if guiApp.eventLoop == 'glib':
                from dbus.mainloop.glib import DBusGMainLoop
                self.mainloop = DBusGMainLoop()
            elif guiApp.eventLoop == 'qt':
                from dbus.mainloop.qt import DBusQtMainLoop
                self.mainloop = DBusQtMainLoop()
                
            if guiApp.iconPath:
                self.icon = guiApp.getNotificationIcon()


        def notify(self, comein, goout):
            item = 'org.freedesktop.Notifications'
            path = '/org/freedesktop/Notifications'
            interface = 'org.freedesktop.Notifications'
            iconName = ''
            actions = []
            hints = {}
            if self.icon and self.icon.get_width() > 0:
                struct = (
                    self.icon.get_width(),
                    self.icon.get_height(),
                    self.icon.get_rowstride(),
                    self.icon.get_has_alpha(),
                    self.icon.get_bits_per_sample(),
                    self.icon.get_n_channels(),
                    dbus.ByteArray(self.icon.get_pixels())
                )
                hints['icon_data'] = struct
            body = self.makeMessage(comein, goout)

            bus=dbus.SessionBus(self.mainloop)
            nobj = bus.get_object(item, path)
            notify = dbus.Interface(nobj, interface)
            notify.Notify(self.appName, 0, iconName, self.title, body, actions, hints, self.timeout)


elif isCmdNotifySend:
    class MessengerLinux(core.MessengerBase):
        def __init__(self, appName, timeout, guiApp):
            super(MessengerLinux, self).__init__(timeout)
            
            self.appName = appName
            
            if guiApp.iconPath and os.path.exists(guiApp.iconPath):
                self.iconPath = os.path.abspath(guiApp.iconPath) # the path must be absolute
            else:
                self.iconPath = guiApp.iconPath 


        def notify(self, comein, goout):
            body = self.makeMessage(comein, goout)
            args = ["notify-send", "--app-name", self.appName, "--expire-time", str(self.timeout)]
            
            if self.iconPath:
                args.append("--icon")
                args.append(self.iconPath)
            args.append(self.title)
            args.append(body)
            
            try:
                subprocess.call(args,  stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            except:
                logging.exception("notify-send ({0}) call failed".format(args))
                
        
class NotifierLinux(core.NotifierBase):
    def __init__(self, configFile = "bitfighter-notifier.ini"):
        super(NotifierLinux, self).__init__(configFile)
        logging.debug("Linux!")
        
#        self.guiType = None
#        self.engineType = None


    def run(self):
#        self.detectGui()
#        self.detectEngine()
    
        guiApp = GuiApplicationLinux(self.executable, self.iconPath)
        messenger = MessengerLinux(self.appName, self.notificationTimeout, guiApp)
        
        plr = core.PlayersListReceiver(self.url, messenger, guiApp)
        
        timer = core.PeriodicTimer(self.refreshInterval, plr.refresh)
        timer.start()
        
        guiApp.setTimer(timer)
        guiApp.run()
        
        
    """
    # Try to detect the GUI toolkit to use
    def detectGui(self):
        if self.guiType is None:
            global QApplication, QSystemTrayIcon, QMenu, QIcon, QImage, QTimer, QObject, SIGNAL, isQtGui
            try:
                from PyQt4.QtGui import QApplication, QSystemTrayIcon, QMenu, QIcon, QImage
                from PyQt4.QtCore import QTimer, QObject, SIGNAL
                
                isQtGui = True
            except:
                pass
        
        if self.guiType is None:
            global gtk, gobject, GdkPixbuf, isGIGtk
            try:
                from gi.repository import Gtk as gtk
                from gi.repository import GObject as gobject
                from gi.repository import GdkPixbuf
                
                isGIGtk = True
            except:
                pass
            
        if self.guiType is None:
            global gtk, gobject, isPyGtk
            try:
                import pygtk
                import gtk
                import gobject
                
                isPyGtk = True
            except:
                pass

        if self.guiType is None:
            logging.error("No GUI found.  Exiting")
            exit(1)
        else:
            logging.info("Detected GUI: %s", self.guiType)
        
        
    # Try to detect the notification engine to use
    def detectEngine(self):
        if self.engineType is None:
            global notify2, isNotify2
            try:
                import notify2

                isNotify2 = True
            except:
                pass
        
        if self.engineType is None:
            global dbus
            try:
                import dbus
                
                self.engineType = 'dbus'
            except:
                pass
        
        if self.engineType is None:
            try:
                isCmdNotifySend = subprocess.call(["type", "notify-send"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True) == 0
            
                if isCmdNotifySend:
                    self.engineType = 'notify-send'
            except:
                pass
                
        if self.engineType is None:
            logging.error("No notification engine found.  Exiting")
            exit(1)
        else:
            logging.info("Detected engine: %s", self.engineType)
    """


# =============================================================     

def main():
    """
    userConfigDir = os.environ.get('XDG_CONFIG_HOME', os.path.join(os.path.expanduser("~"), '.config'))
    sysConfigDirs = os.environ.get('XDG_CONFIG_DIRS', '/etc').split(':')
    configFiles = [os.path.join(d, 'bitfighter-notifier.ini') for d in sysConfigDirs + [userConfigDir]]
    """
    
    iniPath = 'bitfighter-notifier-linux.ini'
    
    notifier = NotifierLinux(iniPath)
    notifier.run()
    

if __name__ == '__main__':
    main()