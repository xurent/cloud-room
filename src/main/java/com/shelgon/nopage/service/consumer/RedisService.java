package com.shelgon.nopage.service.consumer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public interface RedisService {

	  @RequestMapping(value = "put", method = RequestMethod.POST)
	    public String put(@RequestParam(value = "key") String key, @RequestParam(value = "value") String value, @RequestParam(value = "seconds") long seconds);

	    @RequestMapping(value = "get", method = RequestMethod.GET)
	    public Object get(@RequestParam(value = "key") String key);
	    /**
	     * �ж�redis�������Ƿ��ж�Ӧ��key
	     * @param key
	     * @return
	     */
	    @RequestMapping(value = "exists")
	    boolean exists(@RequestParam(value = "key")final String key);

	    /**
	     * redis����keyɾ����Ӧ��value
	     * @param key
	     * @return
	     */
	    @RequestMapping(value = "remove")
	    public boolean remove(@RequestParam(value = "key")final String key);
	    
	    public String getInstance(String service);
	    
	    public String put_notime( String key, Object value);
}
