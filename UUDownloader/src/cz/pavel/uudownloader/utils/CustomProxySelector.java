package cz.pavel.uudownloader.utils;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom proxy selector. It delegates its logic to default proxy selector.
 * The only exception is the <code>socket</code> protocol - this protocol
 * has no proxy by default.
 * 
 * @author Pavel Zeman
 *
 */
public class CustomProxySelector extends ProxySelector {
    
	/** Socket protocol name */
	private static final String PROTOCOL_SOCKET = "socket";
	
	/** Original default proxy selector */
	private ProxySelector originalSelector;
    
    public CustomProxySelector(ProxySelector originalSelector) {
    	this.originalSelector = originalSelector;
    }

    /**
     * Selects proxy for given URL. If the protocol is socket,
     * it returns no proxy, otherwise it delegates the selection
     * to the default proxy selector.
     * 
     * @param uri URI to get proxy for
     * @return List of proxies for given URL.
     */
    @Override
    public List<Proxy> select(URI uri) {
        if (uri == null) {
        	throw new IllegalArgumentException("URI can't be null.");
        }
        
        // check protocol - if it is socket, return direct connection
    	if (PROTOCOL_SOCKET.equals(uri.getScheme())) {
    		ArrayList<Proxy> result = new ArrayList<Proxy>(1);
    		result.add(Proxy.NO_PROXY);
    		return result;
    	} else {
    		return originalSelector.select(uri);
    	}
    }

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		originalSelector.connectFailed(uri, sa, ioe);
	}
}