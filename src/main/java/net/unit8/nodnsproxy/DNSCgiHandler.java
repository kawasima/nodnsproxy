package net.unit8.nodnsproxy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import rabbit.dns.DNSHandler;
import rabbit.util.SProperties;

public class DNSCgiHandler implements DNSHandler {
	private static final Logger logger = Logger.getLogger(DNSCgiHandler.class.getName());

	private String cgiUrl;
	private Proxy proxy;


	private InetAddress findAddressFromCache(String hostname) throws UnknownHostException {
		CacheManager cacheManager = CacheManager.getInstance();
		Cache cache = cacheManager.getCache("IP");
		Element element = cache.get(hostname);
		if(element == null)
			return null;
		String address = (String)element.getValue();
		return InetAddress.getByName(address);
	}

	private InetAddress findAddressFromCGI(String hostname) throws UnknownHostException {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(cgiUrl+"/"+hostname);
			connection = (HttpURLConnection)url.openConnection(proxy);
			connection.setRequestMethod("GET");
			connection.setReadTimeout(3000);

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String addressStr = in.readLine();
			in.close();
			connection.disconnect();

			InetAddress address = InetAddress.getByName(addressStr);
			CacheManager cacheManager = CacheManager.getInstance();
			Cache cache = cacheManager.getCache("IP");
			cache.put(new Element(hostname, addressStr));
			return address;
		} catch (Exception e) {
			throw new UnknownHostException(hostname);
		} finally {
		}

	}

	public InetAddress getInetAddress(URL url) throws UnknownHostException {
		if(url == null)
			throw new UnknownHostException();

		String hostname = url.getHost();
		InetAddress address = findAddressFromCache(hostname);
		if(address == null) {
			address = findAddressFromCGI(hostname);
		}

		return address;
	}

	public InetAddress getInetAddress(String hostname) throws UnknownHostException {
		if(hostname == null)
			throw new UnknownHostException();
		InetAddress address = findAddressFromCache(hostname);
		if(address == null) {
			address = findAddressFromCGI(hostname);
		}

		return address;
	}

	public void setup(SProperties sproperties) {
		cgiUrl = sproperties.getProperty("cgiUrl");
		if(cgiUrl == null) {
			logger.severe("DNSCgiHandler: cgiUrl is missing.");
			return;
		}
		String proxyhost = sproperties.getProperty("proxyhost");
		String proxyport = sproperties.getProperty("proxyport", "80");

		if(proxyhost == null) {
			proxy = Proxy.NO_PROXY;
		} else {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyhost, Short.parseShort(proxyport)));
		}
	}

}
