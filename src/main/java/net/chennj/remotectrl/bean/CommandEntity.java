package net.chennj.remotectrl.bean;

public class CommandEntity implements IEntity{

	public Class<?> entity_type() {

		return CommandEntity.class;
	}

	public String get_id() {

		return companyid;
	}

	private CommandType commandType;
	private CommandFrom commandFrom;
	
	private String type;
	private String content;
	private String companyid;
	private String weixinno;
	private String imei;
	private String amount;
	private String timestamp;
	private String terminal;
	
	public CommandType getCommandType() {
		return commandType;
	}

	public void setCommandType(CommandType commandType) {
		this.commandType = commandType;
	}

	public CommandFrom getCommandFrom() {
		return commandFrom;
	}

	public void setCommandFrom(CommandFrom commandFrom) {
		this.commandFrom = commandFrom;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCompanyid() {
		return companyid;
	}

	public void setCompanyid(String companyid) {
		this.companyid = companyid;
	}

	public String getWeixinno() {
		return weixinno;
	}

	public void setWeixinno(String weixinno) {
		this.weixinno = weixinno;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTerminal() {
		return terminal;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
	
}
