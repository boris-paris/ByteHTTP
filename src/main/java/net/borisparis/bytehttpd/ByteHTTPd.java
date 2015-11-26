package net.borisparis.bytehttpd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author borparis
 */
public class ByteHTTPd {
    
    private static final Logger LOG = Logger.getLogger(ByteHTTPd.class.getName());
    
    private static final int PORT = 8080;
    
    private static Selector clientSelector;
    
    public static void main(String[] args) {

        // Open a new ServerSocketChannel, which will act as the server
        ServerSocketChannel server;
        try {
            server = ServerSocketChannel.open();   
            server.socket().bind(new InetSocketAddress(PORT));
            // Opens a Selector, which will hold all the client SocketChannels.
            clientSelector = Selector.open();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to start the Server", ex);
            return;
        }
        
        // Server loop. Waiting indefinetly for new incoming connections. Opens
        // a new SocketChannel for every new incoming connection.
        while(true) {
            try {
                // This will create a new SocketChannel for every new incoming connection.
                SocketChannel client = server.accept();
                // In order to use this Channel in a Selector, we have to make it non-blocking
                client.configureBlocking(false);
                // Register the client SocketChannel in the Selector
                // SelectionKey.OP_READ will fire an event when the
                // SocketChannel is ready for reading.
                client.register(clientSelector, SelectionKey.OP_READ);
                LOG.log(Level.INFO, "Accepted connection from {0}", client);
                process();
                printInformation();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Failed to establish connection", ex);
            }
        }
    }

    private static void process() {
        try {
            if (clientSelector.selectNow() != 0) {
                // Returns all the SelectionKeys that are ready for processing
                Set<SelectionKey> selectedKeys = clientSelector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                
                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    
                    if (key.isReadable()) {
                        // Get the clients SocketChannel back from the SelectionKey
                        SocketChannel client = (SocketChannel) key.channel();
                        // TODO: make buffer size dynamic
                        ByteBuffer buf = ByteBuffer.allocate(48);
                        int bytesRead = client.read(buf);
                        StringBuilder builder = new StringBuilder();
                        while(bytesRead > 0) {
                            buf.flip(); // flip ByteBuffer to read mode
                            
                            // Read the request header                            
                            while(buf.hasRemaining()) {;
                                builder.append((char) buf.get());
                            }
                                                        
                            // We have read the first request line from the
                            // request. And mark the buffer which bytes we've
                            // already read.
                            buf.clear();
                            bytesRead = client.read(buf);
                        }
                        LOG.info(builder.toString());
                    }
                    else {
                        LOG.warning("ClientChannel is ready but not readable.");
                    }
                    
                    keyIterator.remove();
                }                                
            }
        } catch (IOException ex) {
           LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static void printInformation() {
        
    }
}
