package net.borisparis.bytehttp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import net.borisparis.bytehttpd.RequestHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 *
 * @author borparis
 */
public class TestRequest {
    
    private static final String CRLF = "\r\n";
    
    private static final String DEFAULT_HTTP10_GET_REQUEST = "GET / HTTP/1.0" + CRLF;
    private static final String DEFAULT_HTTP11_GET_REQUEST = "GET / HTTP/1.1" + CRLF;
    private static final String ERROR_HTTP10_PUT_REQUEST = "PUT /foo HTTP/1.0" + CRLF;
    private static final String HTTP11_GET_REQUEST_WITH_HEADERS = DEFAULT_HTTP11_GET_REQUEST
            + "Host: localhost:8080" + CRLF
            + "Connection: keep-alive" + CRLF
            + "Cache-Control: max-age=0" + CRLF
            + "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" + CRLF
            + "Upgrade-Insecure-Requests: 1" + CRLF
            + "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36" + CRLF
            + "Accept-Encoding: gzip, deflate, sdch" + CRLF
            + "Accept-Language: de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4" + CRLF
            + CRLF;

    public TestRequest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testHTTP10RequestReturnsRightVersion() throws IOException {
        // arrange
        RequestHandler handler = new RequestHandler();
        ByteBuffer buf = ByteBuffer.allocate(8192);
        buf.put(DEFAULT_HTTP10_GET_REQUEST.getBytes());
        SocketChannel client = createClientSocketChannel(buf);
        
        // act
        handler.handle(client);
        
        // assert
        assertEquals("HTTP/1.0", handler.getRequestVersion());
    }
    
    @Test
    public void testHTTP11RequestReturnsRightVersion() throws IOException {
        // arrange
        RequestHandler handler = new RequestHandler();
        ByteBuffer buf = ByteBuffer.allocate(8192);
        buf.put(DEFAULT_HTTP11_GET_REQUEST.getBytes());
        SocketChannel client = createClientSocketChannel(buf);
        
        // act
        handler.handle(client);
        
        // assert
        assertEquals("HTTP/1.1", handler.getRequestVersion());        
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testHTTP10PUTRequestReturnsNotImplemented() throws IOException {
        // arrange
        RequestHandler handler = new RequestHandler();
        ByteBuffer buf = ByteBuffer.allocate(8192);
        buf.put(ERROR_HTTP10_PUT_REQUEST.getBytes());
        SocketChannel client = createClientSocketChannel(buf);
        
        // act
        handler.handle(client);
        
        // assert
        // Exception
    }

    private SocketChannel createClientSocketChannel(ByteBuffer request) throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);                
        Answer<Integer> answer = new Answer<Integer>() {
            int i = 0;

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ByteBuffer buf = (ByteBuffer) args[0];
                for (int k = 0; k < buf.limit(); k++) {
                    buf.put(request.get(i));
                    i = i + 1;
                }
                return buf.limit();
            }
        };
        when(socketChannel.read(any(ByteBuffer.class))).thenAnswer(answer);        
        return socketChannel;
    }
}
