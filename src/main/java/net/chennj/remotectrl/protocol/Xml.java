package net.chennj.remotectrl.protocol;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.chennj.remotectrl.bean.CommandEntity;
import net.chennj.remotectrl.bean.CommandFrom;
import net.chennj.remotectrl.bean.CommandType;
import net.chennj.remotectrl.bean.IEntity;
import net.chennj.remotectrl.utils.FuncUtil;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class Xml implements IProtocol{

	public String protocol() {
		return "xml";
	}
	
    public byte[] assembleSendXml(IEntity entity) {
			
		CommandEntity trans = (CommandEntity)entity;
        String xmlmsg = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
                "<package><body>"+
                "<type>"+trans.getType()+"</type>"+
                "<content>"+trans.getContent()+"</content>"+
                "<terminal>"+trans.getTerminal()+"</terminal>"+
                "<companyid>"+trans.getCompanyid()+"</companyid>"+
                "<weixinno>"+trans.getWeixinno()+"</weixinno>"+
                "<imei>"+trans.getImei()+"</imei>"+
                "<amount>"+trans.getAmount()+"</amount>"+
                "<paytimestamp>"+trans.getTimestamp()+"</paytimestamp>"+
                "</body></package>";

        System.out.println("发送的信息:"+xmlmsg);

        //将int类型转为网络大字节序的4个byte
        int xmlmsg_l=0;
		try {
			xmlmsg_l = xmlmsg.getBytes("utf-8").length;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.asIntBuffer().put(xmlmsg_l);
        byte[] head = bb.array();

        //将发送信息转为byte[]
        return FuncUtil.bytesMerger(head, xmlmsg.getBytes());
    }

	private IEntity processTrans(Document doc) {
		
        String type = doc.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
		String weixinno = doc.getElementsByTagName("weixinno").item(0).getFirstChild().getNodeValue();
		String companyid = doc.getElementsByTagName("companyid").item(0).getFirstChild().getNodeValue();
		String terminal = doc.getElementsByTagName("terminal").item(0).getFirstChild().getNodeValue();
		String imei = doc.getElementsByTagName("imei").item(0).getFirstChild().getNodeValue();
		String content = doc.getElementsByTagName("content").item(0).getFirstChild().getNodeValue();
		String amount = doc.getElementsByTagName("amount").item(0).getFirstChild().getNodeValue();
		String paytimestamp = doc.getElementsByTagName("paytimestamp").item(0).getFirstChild().getNodeValue();
		
		CommandEntity entity = new CommandEntity();
		
		entity.setCommandFrom(CommandFrom.fromTypeName(terminal));
		entity.setCommandType(CommandType.fromTypeName(type));
		
		entity.setAmount(amount);
		entity.setCompanyid(companyid);
		entity.setContent(content);
		entity.setImei(imei);
		entity.setWeixinno(weixinno);
		entity.setTimestamp(paytimestamp);
		entity.setTerminal(terminal);
		entity.setType(type);
		
		return entity;
	}
	
	public byte[] unwrap(IEntity entity){
				
		return assembleSendXml(entity);
	}

	public IEntity wrap(byte[] bytes) {

		String recvMsg = FuncUtil.getStringFromSocketBytes(bytes);
		System.out.println("接收到的信息："+recvMsg);
		String errmsg = null;
		if ("".equals(recvMsg))return null;
		
        try {        	
        	recvMsg = recvMsg.replaceAll("[\u0000-\u001f]", "");
        	recvMsg = recvMsg.replaceAll("\n", "");
            StringReader sr = new StringReader(recvMsg);
            InputSource is = new InputSource(sr);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            return processTrans(doc);
        } catch (SAXException e) {
            System.out.println("信息处理异常：" + e.getMessage());
            errmsg = e.getMessage();
        } catch (ParserConfigurationException e) {
        	System.out.println("信息处理异常：" + e.getMessage());
        	errmsg = e.getMessage();
        } catch (IOException e) {
        	System.out.println("信息处理异常：" + e.getMessage());
        	errmsg = e.getMessage();
        } catch (NullPointerException e){
        	System.out.println("信息处理异常：" + e.getMessage());
            errmsg = "数据包格式错误";
        }
        
        CommandEntity entity = new CommandEntity();
        entity.setAmount("0");
		entity.setCompanyid("0");
		entity.setContent(errmsg);
		entity.setImei("0");
		entity.setWeixinno("0");
		entity.setTimestamp("0");
		entity.setTerminal("0");
		entity.setType("9999");
		entity.setCommandFrom(CommandFrom.COMMAND_FROM_LOCAL);
		entity.setCommandType(CommandType.COMMAND_ERROR);
        return entity;
	}
}
