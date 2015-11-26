package net.borisparis.bytehttpd;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author borparis
 */
public class RequestHandler {
    
    private static final Pattern HTTP10_REQUEST_PATTERN = Pattern.compile("^(GET|HEAD|POST) (/.*) (HTTP/1.0)$");
    private static final Pattern HTTP11_REQUEST_PATTERN = Pattern.compile("^(OPTIONS|GET|HEAD|POST|PUT|DELETE|TRACE|CONNECT) (/.*) (HTTP/1.1)$");
    
    private String httpVersion;

    public void handle(SocketChannel client) {
        try {
            // TODO: make buffer size dynamic
            ByteBuffer buf = ByteBuffer.allocate(8192);
            int bytesRead = client.read(buf);
            StringBuilder builder = new StringBuilder();
            char justRead = ' ';
            if (bytesRead > 0 && justRead != '\r') {
                buf.flip(); // flip ByteBuffer to read mode

                // Read the requests first line from channel                            
                while(buf.hasRemaining() && justRead != '\r') {
                    justRead = (char) buf.get();
                    if (justRead != '\r')
                        builder.append(justRead);
                }

                // We have read the first line from the
                // request. And mark the buffer which bytes we've
                // already read.
                buf.compact();
            }
            // Check for valid request
            Matcher http10Matcher = HTTP10_REQUEST_PATTERN.matcher(builder.toString());
            Matcher http11Matcher = HTTP11_REQUEST_PATTERN.matcher(builder.toString());
            if (http11Matcher.matches()) {
                httpVersion = http11Matcher.group(3);
            }
            else if (http10Matcher.matches()) {
                httpVersion = http10Matcher.group(3);
            }
            else {
                throw new UnsupportedOperationException();
            }
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Object getRequestVersion() {
        return httpVersion;
    }
}
