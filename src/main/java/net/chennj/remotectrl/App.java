package net.chennj.remotectrl;

import net.chennj.remotectrl.server.TcpServer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        new TcpServer().start();
    }
}
