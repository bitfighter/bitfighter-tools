import os.path
import random
import subprocess

        
fortunefiles = (
    "calvin",
)


class Fortune(object):
    def __init__(self, bot):
        self.bot = bot
    
    def get_fortune(self):
        fortunefile = fortunefiles[random.randint(0, len(fortunefiles) - 1)]
        
        try:
            filelocation = os.path.dirname(__file__) + os.sep + 'fortunes' + os.sep + fortunefile

            proc = subprocess.Popen(("/usr/bin/fortune", filelocation),
                                    stderr = subprocess.PIPE,
                                    stdout = subprocess.PIPE)
        except OSError:
            return "no fortune here!"
    
        if proc.wait() == 0:
            message = ''
            for line in proc.stdout:
                message += line.strip() + ' '
                
            return message
        else:
            return "fortune failed: " + proc.stderr.read()
    
    def on_privmsg(self, c, e):
        self._on_msg(c, e)

    def on_pubmsg(self, c, e):
        self._on_msg(c, e)
        
    def _on_msg(self, c, e):
        message = e.arguments[0]
        
        if message == '' or message[0] == "<" or message[0:1] == " <":
            return

        if self.bot._nickname.lower() in message.lower():
            self.bot.prvmsg_append_to_log(c, e.target, self.get_fortune())
            
            
# For testing
def main():
    fortune = Fortune(None)
    print fortune.get_fortune()

if __name__ == "__main__":
    main()
