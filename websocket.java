package com.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApplicationRunner {
	public static void main(String[] args)
	{
		SpringApplication.run( ApplicationRunner.class, args );
	}
}

-----------------------


package com.websocket.dataservice;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;

@Component
public class SocketHandler extends TextWebSocketHandler {
	
	private Set<WebSocketSession> connectedSessions = Collections.synchronizedSet( new HashSet<WebSocketSession>() );
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session)
	{
		System.out.println("connection established: " + session.getId() + ". Now connected to " + connectedSessions.size() + "clients");;
		connectedSessions.add( session );
	}
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, IOException 
	{
		User transmittedUser = new Gson().fromJson( message.getPayload(), User.class );
		System.out.println("Recieved user: " + transmittedUser );
		for(WebSocketSession s : connectedSessions )
		{
			s.sendMessage( new TextMessage("Hello " + transmittedUser.getFirstName() + " " + transmittedUser.getLastName()));
		}
	}

	public Set<WebSocketSession> getConnectedSessions() {
		return connectedSessions;
	}

	public void setConnectedSessions(Set<WebSocketSession> connectedSessions) {
		this.connectedSessions = connectedSessions;
	}
	
	
}

-------------------------------------------------


package com.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.websocket.dataservice.SocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer  {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(new SocketHandler(), "/name");
	}

}


--------------------------

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>
<groupId>WebsocketDemo</groupId>
<artifactId>WebsocketDemo</artifactId>
<version>0.0.1-SNAPSHOT</version>
  <parent>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-parent</artifactId>
      <version>1.3.3.RELEASE</version>
  </parent>

  <dependencies>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-websocket</artifactId>
      </dependency>
      <dependency>
          <groupId>com.google.code.gson</groupId>
          <artifactId>gson</artifactId>
      </dependency>
  </dependencies>
  
</project>
