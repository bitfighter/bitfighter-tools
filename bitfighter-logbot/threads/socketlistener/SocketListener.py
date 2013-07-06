from threads.socketlistener.handler.CommitMessagesHandler import CommitMessagesHandler
import socket
import threading

'''
    This thread has our socket listener.  It launches other threads to handle the data
    immediately
'''
class SocketListener(threading.Thread):
    def __init__(self, host, port, bot):
        threading.Thread.__init__(self)
        self.host = host
        self.port = port
        self.bot = bot
        
        self.serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.serversocket.bind((host, port))
        self.serversocket.listen(5)
        
        self.loop = True
    
    def run(self):
        try:
            while self.loop:
                (clientsocket, address) = self.serversocket.accept()
                
                # Launch client socket in a new thread
                self.handle(clientsocket)
        finally:
            self.serversocket.close()
    
    def stop(self):
        self.loop = False

    '''
        Handle the incomming socket by delegating it to a handler class
    '''    
    def handle(self, clientsocket):
        # TODO register different handlers based on input data?
        clientthread = self.ClientThread(CommitMessagesHandler, clientsocket, self.bot)
        clientthread.start()


    '''
        Simple thread wrapper for any data handlers
    '''
    class ClientThread(threading.Thread):
        def __init__(self, clazz, clientsocket, bot):
            threading.Thread.__init__(self)
            
            self.clazz = clazz()
            self.clientsocket = clientsocket
            self.bot = bot
                
        def run(self):
            try:
                if hasattr(self.clazz, 'handle_data'):
                    self.clazz.handle_data(self.clientsocket, self.bot)
                else:
                    print("Class: " + self.clazz.__name__ + " must be subclass of Handler!")
                    
            finally:
                self.clientsocket.close()


# For testing
def main():
    print("starting up")
    socketlistener = SocketListener('localhost', 25959, None)
    socketlistener.start()

if __name__ == "__main__":
    main()
