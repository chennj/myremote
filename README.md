# myremote
Remote Control Server

远程控制服务器

命令控制端代码示例

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SocketClient {
	
    public static byte[] bytesMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }
    
    public static String getStringFromSocketBytes(byte[] bytes){

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        InputStreamReader isr = new InputStreamReader(bis);
        BufferedReader br = new BufferedReader(isr);

        String line = ""; String result = "";
        try {
            while ((line = br.readLine()) != null){

                result += line;
            }
        } catch (IOException e) {

            System.out.println("转换异常："+e.getMessage());
        } finally{
        	if (null != bis){
        		try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	if (null != isr){
        		try {
					isr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	if (null != br){
        		try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }

        return result;
    }
    private static byte[] assembleSendXml() throws UnsupportedEncodingException {

        String xmlmsg = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
                "<package><body>"+
                "<type>0000</type>"+
                "<content>0</content>"+
                "<companyid>10435</companyid>"+
                "<weixinno>abcdef</weixinno>"+
                "<terminaltype>2</terminaltype>"+
                "<amount>0.1</amount>"+
                "<terminal>9997</terminal>"+
                "<paytimestamp>0</paytimestamp>"+
                "<imei>0</imei>"+
                "</body></package>";
        
        int xmlmsg_l = xmlmsg.getBytes("utf-8").length;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.asIntBuffer().put(xmlmsg_l);
        byte[] head = bb.array();

        return bytesMerger(head, xmlmsg.getBytes());
    }
    
    public static String parse_xml(String recvMsg){
    	
    	String errmsg = "";
        try {        	
        	recvMsg = recvMsg.replaceAll("[\u0000-\u001f]", "");
        	recvMsg = recvMsg.replaceAll("\n", "");
            StringReader sr = new StringReader(recvMsg);
            InputSource is = new InputSource(sr);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            String type = doc.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
    		String zhifubaono = doc.getElementsByTagName("weixinno").item(0).getFirstChild().getNodeValue();
    		String companyid = doc.getElementsByTagName("companyid").item(0).getFirstChild().getNodeValue();
    		String terminal = doc.getElementsByTagName("terminal").item(0).getFirstChild().getNodeValue();
    		String imei = doc.getElementsByTagName("imei").item(0).getFirstChild().getNodeValue();
    		String content = doc.getElementsByTagName("content").item(0).getFirstChild().getNodeValue();
    		String amount = doc.getElementsByTagName("amount").item(0).getFirstChild().getNodeValue();
    		String paytimestamp = doc.getElementsByTagName("paytimestamp").item(0).getFirstChild().getNodeValue();
    		return type+"-"+content;
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
        	errmsg = e.getMessage();
        }
        
        return errmsg;
    }
    public static void main(String[] args) throws UnknownHostException, IOException{
    	
    	//初始化
    	Socket client 	= new Socket("localhost", 9999);  
    	OutputStream os	= client.getOutputStream();
    	InputStream is 	= client.getInputStream();
    	
    	//发出命令
    	byte[] sndbytes = assembleSendXml();
    	os.write(sndbytes);
    	os.flush();
    	   	   	
    	//设置读超时
    	client.setSoTimeout(15*1000);
    	
    	//循环读取结果
    	//先读取4字节的包头以确定读取长度
    	//然后根据长度读取剩下的数据，这种方法
    	//可一定程度上防止粘包现象
    	while(true){
    		
	    	byte[] head = new byte[4];	    	
	        int number 	= is.read(head);
	        
	        if (-1 < number){
	           
	            ByteBuffer bb 	= ByteBuffer.wrap(head);
	            int recvSize 	= bb.order(ByteOrder.BIG_ENDIAN).getInt();
	            byte[] info 	= new byte[recvSize];
	            number 			= is.read(info);
	
	            System.out.println("接收到的数据大小："+recvSize);
	
	            if (-1 < number){
	                String xmlinfo 	= getStringFromSocketBytes(info);
	                System.out.println("接收到的数据："+xmlinfo);
	                String result 	= parse_xml(xmlinfo);
	                //if (result.startsWith("1002") || result.startsWith("1001")){
	                if (result.startsWith("1002")){
	                	//忽略心跳
	                	continue;
	                }else{
	                	System.out.println(result);
	                	break;
	                }
	            }else{
	            	break;
	            }
	        }else{
	        	break;
	        }
    	}
        client.close();
    }

}

# 服务器功能说明

命令接收端（n)<------------->Remote Control Server<-------------------->命令控制端（m）

由命令控制端发出命令，命令接收端响应命令并返回命令结果

# 图示
图示链接(https://www.processon.com/view/link/5a24afc6e4b0f3a79865dbbe)
