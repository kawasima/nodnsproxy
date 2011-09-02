package net.unit8.nodnsproxy;

import rabbit.proxy.HttpProxy;

/**
 * Bootstrap class
 *
 */
public class Main
{
	public static void main( String[] args ) throws Exception
	{
		HttpProxy proxy = new HttpProxy();
		proxy.setConfig("conf/rabbit.conf");
		proxy.start();
	}
}
