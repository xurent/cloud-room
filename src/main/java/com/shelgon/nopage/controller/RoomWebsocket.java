package com.shelgon.nopage.controller;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.shelgon.nopage.model.UserInfo;
import com.shelgon.nopage.utils.RedisUtils;
import com.shelgon.nopage.utils.UserInfoUtils;


@ServerEndpoint("/websocket/{roomName}/{sid}")
@Component
public class RoomWebsocket {

	static Log log=LogFactory.getLog(RoomWebsocket.class);
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    //private static CopyOnWriteArraySet<RoomWebsocket> webSocketSet = new CopyOnWriteArraySet<RoomWebsocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //接收用户id
    private String sid="";
    //接收房间id
    private String roomName="";
    //每个房间存着所有成员的WebSocket对象
    public static Map<String,CopyOnWriteArraySet<RoomWebsocket>> room=new HashMap<>();
    String data="";
    /**
     * 100 	发送错误
     * 101  系统通知消息
     * 102  进入房间
     * 103 	退出房间
     * 104  图片信息
     * 105  音频信息
     * 106  普通推送信息
     * 107 	权限变更
     * 108 演示权限赋予
     * 109  演示权限撤回
     */
      
    
    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session,@PathParam("sid") String sid,@PathParam("roomName")String RoomName) {
        this.session = session;
        this.sid=sid;
        this.roomName=RoomName;
       // webSocketSet.add(this);     //加入set中
        
        addOnlineCount();           //在线数加1
        log.info(roomName+"有新窗口开始监听:"+sid+",当前在线人数为" + getOnlineCount());
        sendMessage(101,"建立连接成功!");
        
        //如果缓存中房间为空则创建
        CopyOnWriteArraySet<RoomWebsocket> Set=room.get(roomName);
        if(Set==null) {
			   Set = new CopyOnWriteArraySet<RoomWebsocket>();
			   
		   } 
        Set.add(this);
        room.put(roomName, Set);
       UserInfo u= UserInfoUtils.getUserInfo(sid);
       
       if(u!=null)data=u.getUsername();
      //通知
        for (RoomWebsocket item : Set) {     	     	
        	if(item.roomName.equals(RoomName)&&!item.sid.equals(sid)) {      		
        		//不发给自己   
            		item.sendMessage(102,data+"进入房间!");
        	}
        }
  
    }


    
//    public static CopyOnWriteArraySet<RoomWebsocket> getWebSocketSet() {
//		return webSocketSet;
//	}
//
//	public static void setWebSocketSet(CopyOnWriteArraySet<RoomWebsocket> webSocketSet) {
//		RoomWebsocket.webSocketSet = webSocketSet;
//	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}
	
	public Session getSession() {
		return session;
	}

	public String getRoomName() {
		return roomName;
	}



	/**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid,@PathParam("roomName")String RoomName) {
    	CopyOnWriteArraySet<RoomWebsocket> Set=room.get(roomName);
    	sendMessage(101,"你已经退出,请关闭socket连接！");
    	Set.remove(this);
    	UserInfo u=UserInfoUtils.getUserInfo(sid);
    	if(u!=null)data=u.getUsername();
    	//通知
        for (RoomWebsocket item : Set) {     	     	
        	if(item.roomName.equals(RoomName)&&!item.sid.equals(sid)) {      		
        		//不发给自己   
        		item.sendMessage(103,data+"退出房间");
        	}
        }
        //webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
//        if(Set.size()==0) {
//			room.remove(RoomName);
//		}else {
//			room.put(RoomName, Set);	//放回容器
//		}
		
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(@PathParam("roomName")String RoomName,String message, Session session) {
    	log.info("收到来自用户"+sid+"的信息:"+message);
    	CopyOnWriteArraySet<RoomWebsocket> webSocketSet=room.get(RoomName);	
    	if(webSocketSet==null) {
    		sendMessage(101, RoomName+"房间不存在!");
    		return;
    	}
    	//Integer activity=StartController.translate.get(RoomName);
    	String activity= RedisUtils.get(RoomName);
    	if(!RedisUtils.get("room-play-"+RoomName).equals(sid))return;
    	if(activity==null)return;
    	if(activity.equals("1")) {
    		int Code=101;
        	if(message.contains("data:image/png;")) {
            	Code=104;
            }else if(message.contains("data:audio/mp3;")) {
            	Code=105;
            }else {
            	Code=106;
            }
            //群发消息
            for (RoomWebsocket item : webSocketSet) {
            	if(item.sid==sid) {
            		continue;
            	}
            	item.sendMessage(Code,message);
            }
    	}   	  	
       
    }

	/**
	 * 
	 * @param session
	 * @param error
	 */
    @OnError
    public void onError(Session session, Throwable error,@PathParam("roomName")String RoomName) {
        log.error("发生错误");
        sendMessage(100, "发送错误!"+error.getMessage());
        error.printStackTrace();
        room.get(RoomName).remove(this);
       // webSocketSet.remove(this);
    }

    /**
	 * 实现服务器主动推送
	 */
    public void sendMessage(int code,String message){
        try {
        	String data="{\"code\":"+code+",\"msg\":\""+message+"  \"}";
        	if(session.isOpen()) {
			this.session.getBasicRemote().sendText(data);
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("websocket IO异常");
		}
    }

//    /**
//     * 群发自定义消息
//     * */
//    
//    public static void sendInfo(String message,@PathParam("sid") String sid) {
//    	log.info("推送消息到窗口"+sid+"，推送内容:"+message);
//        for (RoomWebsocket item : webSocketSet) {
//            //这里可以设定只推送给这个sid的，为null则全部推送
//			if(sid==null) {
//				item.sendMessage(102,message);
//			}else if(item.sid.equals(sid)){
//				item.sendMessage(102,message);
//			}
//        }
//    }

    
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
    	RoomWebsocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
    	RoomWebsocket.onlineCount--;
    }

}
