package net.chennj.remotectrl.factory;

import java.io.IOException;

import net.chennj.remotectrl.protocol.IProtocol;
import net.chennj.remotectrl.protocol.Xml;

public final class ProtocolFactory {

	public static final int XML = 0;
	public static final int JSON = 1;
	
	public static IProtocol build(int which) throws IOException{
		
		switch(which){
		case XML:{
			return new Xml();
		}
		default:
			throw new IOException("miss protocol");
		}
	}
}
