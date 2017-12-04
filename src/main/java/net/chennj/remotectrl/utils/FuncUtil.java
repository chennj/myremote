package net.chennj.remotectrl.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FuncUtil {

    /**
     * 拼接两个byte[]
     * @param byte_1
     * @param byte_2
     * @return
     */
    public static byte[] bytesMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * 将byte[]转换为String
     * @param bytes
     * @return
     */
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

}
