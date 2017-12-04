package net.chennj.remotectrl.bean;

public enum CommandFrom {

	
	COMMAND_FROM_TARGET("9998","数据来自于命令接收端"),
	COMMAND_FROM_CONTROL("9997","数据来自于控制端"),
	COMMAND_FROM_LOCAL("9996","数据来自于自己，用于发送错误信息");
	
	private String name;
	private String desc;
	
	private CommandFrom(String name, String desc){
		
		this.name = name;
		this.desc = desc;
	}

	public static CommandFrom fromTypeName(String typeName){
		
		for(CommandFrom type : CommandFrom.values()){
			
			if(type.getName().equals(typeName)){
				return type;
			}
		}
		return null;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
