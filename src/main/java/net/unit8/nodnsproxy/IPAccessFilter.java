package net.unit8.nodnsproxy;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import rabbit.dns.DNSHandler;
import rabbit.dns.DNSJavaHandler;
import rabbit.filter.HttpFilter;
import rabbit.http.HttpHeader;
import rabbit.proxy.Connection;
import rabbit.proxy.HttpProxy;
import rabbit.util.SProperties;

public class IPAccessFilter implements HttpFilter {
	private static final Logger logger = Logger.getLogger(IPAccessFilter.class.getName());
	private static final Pattern ipPattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
	private DNSHandler dnsHandler;

	public IPAccessFilter() {
		dnsHandler = new DNSCgiHandler();
	}

	private String getIPAddress(String hostname) {
        	try {
        		InetAddress address = dnsHandler.getInetAddress(hostname);
        		if(address == null)
        			return hostname;
        		int ind = address.toString().indexOf('/');
        		return address.toString().substring(ind+1);
        	} catch(UnknownHostException e) {
        		return hostname;
        	}
	}
	public HttpHeader doConnectFiltering(SocketChannel socketchannel,
			HttpHeader httpheader, Connection connection) {
		String requestUri = httpheader.getRequestURI();
		String[] token = requestUri.split(":");
		String host = token[0];
        String ipAddress = host;
        if(!ipPattern.matcher(host).matches()) {
        	ipAddress = getIPAddress(host);
        }

        httpheader.setRequestURI(ipAddress+":"+token[1]);

		return null;
	}

	public HttpHeader doHttpInFiltering(SocketChannel socketchannel,
			HttpHeader httpheader, Connection connection) {
		URL uri;
		try {
			String requestUri = httpheader.getRequestURI();
			uri = new URL(requestUri);
		} catch (MalformedURLException e) {
			return connection.getHttpGenerator().get500("URL error", e);
		}
        String ipAddress = uri.getHost();
        if(!ipPattern.matcher(uri.getHost()).matches()) {
        	ipAddress = getIPAddress(uri.getHost());
        }

        URL ipBaseUrl;
		try {
			ipBaseUrl = new URL(uri.getProtocol(), ipAddress, uri.getPort(), uri.getFile());
			httpheader.setRequestURI(ipBaseUrl.toExternalForm());
		} catch (MalformedURLException e) {
			return connection.getHttpGenerator().get500("URL error", e);
		}

		return null;
	}

	public HttpHeader doHttpOutFiltering(SocketChannel socketchannel,
			HttpHeader httpheader, Connection connection) {
		return null;
	}

	public void setup(SProperties sproperties, HttpProxy httpproxy) {
		String dnsHandlerName = sproperties.getProperty("dnsHandler", DNSJavaHandler.class.getName());
		try {
			Class<? extends DNSHandler> clazz = Class.forName(dnsHandlerName).asSubclass(DNSHandler.class);
			dnsHandler = clazz.newInstance();
			dnsHandler.setup(httpproxy.getConfig().getProperties(dnsHandlerName));
		} catch (Exception e) {
			logger.severe("IPAccessFilter: dnsHandler "+ dnsHandlerName +" can't instantiate.");
		}

	}

}
