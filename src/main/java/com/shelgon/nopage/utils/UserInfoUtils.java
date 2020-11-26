package com.shelgon.nopage.utils;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.shelgon.nopage.model.IdUser;
import com.shelgon.nopage.model.UserInfo;
import com.shelgon.nopage.service.consumer.RedisService;

import net.sf.json.JSONObject;
@Component
public class UserInfoUtils {

	
	@Autowired
	private RedisService redisService;
	private static UserInfoUtils userInfoUtils;
	
	@PostConstruct  
	public void init() {
		userInfoUtils=this;
		redisService=this.redisService;	
	}
	
	/**
	 * 信息表id 换用户openid
	 * @param uid
	 * @return
	 */
	public static String UserInfoidTranslation(String uid) {
		RestTemplate restTemplate=new RestTemplate();
		IdUser info=restTemplate.getForObject(userInfoUtils.redisService.getInstance("user-login-service")+"/get_user/"+uid, IdUser.class);	
		return info.getOpenid();
	}
	/**
	 * openid 换用户信息表
	 * @param openid
	 * @return
	 */
	public static UserInfo getUserInfo(String openid) {
		RestTemplate restTemplate=new RestTemplate();	
		String info=restTemplate.getForObject(userInfoUtils.redisService.getInstance("user-login-service")+"/get_userinfo/"+openid, String.class);
		JSONObject obj=JSONObject.fromObject(info);
		UserInfo u=(UserInfo) JSONObject.toBean(obj.getJSONObject("userInfo"), UserInfo.class);
		return u;
	}
	
	/**
	 * openid 换用户id
	 * @param uid
	 * @return
	 */
	public static Integer OpenidTranslation(String openid) {
		RestTemplate restTemplate=new RestTemplate();	
		IdUser info=restTemplate.getForObject(userInfoUtils.redisService.getInstance("user-login-service")+"/get_user_by_oid/"+openid, IdUser.class);		
		return info.getId();
	}
	
}
