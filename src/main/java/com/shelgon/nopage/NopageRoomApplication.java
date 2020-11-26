package com.shelgon.nopage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@SpringBootApplication
@EnableDiscoveryClient
@ServletComponentScan
@EnableFeignClients
public class NopageRoomApplication {

	public static void main(String[] args) {
		SpringApplication.run(NopageRoomApplication.class, args);
	}
	
	/**
	 * 开启WebSocket支持
	 * @author hexurong
	 *
	 */
	@Configuration  
	public class WebSocketConfig {  
		
	    @Bean  
	    public ServerEndpointExporter serverEndpointExporter() {  
	        return new ServerEndpointExporter();  
	    }  
	  
	}
	

}
