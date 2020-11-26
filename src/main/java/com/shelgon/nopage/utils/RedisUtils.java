package com.shelgon.nopage.utils;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.shelgon.nopage.service.consumer.RedisService;


@Component
public class RedisUtils {

//	public static void put(String key,String value,long seconds) {
//		
//		Posttemplate("http://47.102.97.30:8501/put","key="+key+"&value="+value+"&seconds="+seconds);
//	}
	
	@Autowired
	private RedisService redisService;
	
	private static RedisUtils redisUtils;
	@PostConstruct  
	public void init() {
		redisUtils=this;
		redisService=this.redisService;
		
	}
	
	public static String get(String key) {	
		//System.out.println("?????"+redisUtils.redisService.getInstance());
		RestTemplate rt=new RestTemplate();
		String obj=rt.getForObject(redisUtils.redisService.getInstance("redis-service")+"/get?key="+key, String.class);
		return obj;
	}
	
	
	public static String Posttemplate(String Url,String data) {
		String temp=null;
		try {
			URL url=new URL(Url);
			HttpURLConnection conn=(HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.getOutputStream().write(data.getBytes());
			int Code=conn.getResponseCode();
			System.out.println("Code="+Code);
			if(Code==200) {
				InputStream is=conn.getInputStream();
				ByteArrayOutputStream bos=new ByteArrayOutputStream();
		        byte []Buffer=new byte[1024];
		        int len=-1;
		        while ((len=is.read(Buffer))!=-1){
		            bos.write(Buffer,0,len);
		        }
		        bos.close();
		        temp=bos.toString("gb2312");
		        System.out.println(temp);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}
	
}
