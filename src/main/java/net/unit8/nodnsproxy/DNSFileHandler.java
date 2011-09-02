package net.unit8.nodnsproxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import rabbit.dns.DNSHandler;
import rabbit.util.SProperties;

public class DNSFileHandler implements DNSHandler {
	private static final Logger logger= Logger.getLogger(DNSFileHandler.class.getName());
	private Properties names;

	private void readHosts(File hosts) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(hosts));
			String line;
			while((line = reader.readLine()) != null) {
				if(StringUtils.isBlank(line) || line.startsWith("#"))
					continue;
				String[] addresses = line.split("\\s+");
				if(addresses.length < 2)
					continue;
				for(int i=1; i<addresses.length; i++) {
					names.setProperty(addresses[i], addresses[0]);
				}
			}
		} finally {
			if(reader != null)
				reader.close();
		}

	}
	public void setup(SProperties sproperties) {
		names = new Properties();
		String hostsPath = sproperties.getProperty("hosts");
		if(hostsPath == null) {
			String osname = System.getProperty("os.name");
			hostsPath = (osname.indexOf("Windows")>=0) ? "C:\\Windows\\System32\\drivers\\etc\\hosts" : "/etc/hosts";
		}
		File hostsFile= new File(hostsPath);
		if(!hostsFile.isFile()) {
			logger.severe("hosts file not found.");
			return;
		}
		try {
			readHosts(hostsFile);
		} catch(IOException e) {
			logger.severe("can't read hosts file [" + hostsFile + "]");
			return;

		}
	}

	public InetAddress getInetAddress(URL url) throws UnknownHostException {
		String host = url.getHost();
		String address = names.getProperty(host);
		if(address == null) {
			throw new UnknownHostException(host);
		}
		return InetAddress.getByName(address);
	}

	public InetAddress getInetAddress(String host) throws UnknownHostException {
		String address = names.getProperty(host);
		if(address == null) {
			throw new UnknownHostException(host);
		}
		return InetAddress.getByName(address);
	}

}
