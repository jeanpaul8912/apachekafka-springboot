package com.lostsys.youtube;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
public class Web1 {
	
	@Autowired
	private DiscoveryClient discoveryClient;
	
	@Autowired
	private LoadBalancerClient loadBalancerClient;
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	private static ArrayList<String> kafkaMessage = new ArrayList<String>();
	
	public static void main(String[] args) {
		SpringApplication.run(Web1.class, args);
	}
	
	@RequestMapping("/")
	@ResponseBody
	public String home(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>Hello World!</h1>");
		
		if(loadBalancerClient.choose("Spring1") != null){
			sb.append("<p>load balancer: "+loadBalancerClient.choose("Spring1").getInstanceId()+"</p>");
		}
		
		if(discoveryClient.getInstances("Spring1") != null) {
			sb.append("<p>instances: "+discoveryClient.getInstances("Spring1").size()+"</p>");
		}
		
		/* Events */
	    if ( request.getParameter("kafkamsg")!=null ) {
	        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("testyoutube",  request.getParameter("kafkamsg"));
	         
	        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
	            public void onSuccess(SendResult<String, String> result) { System.out.println( result ); }
	            public void onFailure(Throwable ex) { ex.printStackTrace(); }
	            });  
	        }
	    
	    sb.append( "<p>msgs: "+kafkaMessage+"</p>" );
	    sb.append( "<p><form method='post' action='/'><input name='kafkamsg' /><input type='submit' value='Enviar'/></form></p>" );
	     
		
		return sb.toString();
	}
	
	@KafkaListener(topics = "testyoutube", groupId = "group1")
	public void listenTopic2(String message) {
		kafkaMessage.add(message);
	}
}