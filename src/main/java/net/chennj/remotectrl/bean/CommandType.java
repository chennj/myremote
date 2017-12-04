package net.chennj.remotectrl.bean;

public enum CommandType {

	COMMAND_CONTROL_ERWMA("1000","提取二维码"),
	COMMAND_TARGET_ERWMA("1001","返回二维码"),
	COMMAND_KEEP_HEART("1002","心跳"),
	COMMAND_PAY_SUCCESS("1004","支付成功"),
	COMMAND_PAY_FAILED("1011","支付失败"),
	COMMAND_CONTROL_TIXIAN("1013","要求提现"),
	COMMAND_ERROR("9999","错误");
	
	private String name;
	private String desc;
	
	private CommandType(String name, String desc){
		
		this.name = name;
		this.desc = desc;
	}

	public static CommandType fromTypeName(String typeName){
		
		for(CommandType type : CommandType.values()){
			
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
