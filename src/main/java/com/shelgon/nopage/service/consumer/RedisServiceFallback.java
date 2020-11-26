package com.shelgon.nopage.service.consumer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import com.shelgon.nopage.utils.RedisUtils;

@Service("redisService")
public class RedisServiceFallback implements RedisService{

	@Autowired
	private DiscoveryClient discoveryClient;
	
	@Override
	public String put(String key, String value, long seconds) {
		System.out.println("接口："+getInstance("redis-service"));
		return RedisUtils.Posttemplate(getInstance("redis-service")+"/put","key="+key+"&value="+value+"&seconds="+seconds);
	}

	@Override
	public String get(String key) {
		// TODO Auto-generated method stub
		return  RedisUtils.get(key);
	}

	@Override
	public boolean exists(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(String key) {
		RedisUtils.Posttemplate(getInstance("redis-service")+"/remove","key="+key);
		return false;
	}

	public String getInstance(String service) {
		
		String serviceId=service;
		List<ServiceInstance> instance=discoveryClient.getInstances(serviceId);
		if(instance==null||instance.isEmpty()) {
			return null;
		}
		ServiceInstance serviceInstance=instance.get(0);
		String url="http://"+serviceInstance.getHost()+":"+serviceInstance.getPort();
		return url;
	}

	@Override
	public String put_notime(String key, Object value) {
			
		return RedisUtils.Posttemplate(getInstance("redis-service")+"/put_notime","key="+key+"&value="+value);
	}

}
