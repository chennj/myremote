package net.chennj.remotectrl.protocol;

import net.chennj.remotectrl.bean.IEntity;

public interface IProtocol {

	public String protocol();
	
	public byte[] unwrap(IEntity entity);
	
	public IEntity wrap(byte[] bytes);
}
