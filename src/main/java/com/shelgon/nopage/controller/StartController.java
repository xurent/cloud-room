package com.shelgon.nopage.controller;


import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shelgon.nopage.model.MessageData;
import com.shelgon.nopage.model.Room;
import com.shelgon.nopage.service.RoomService;
import com.shelgon.nopage.service.consumer.RedisService;
import com.shelgon.nopage.utils.RedisUtils;
import com.shelgon.nopage.utils.UserInfoUtils;
import com.shelgon.nopage.utils.Utils;

import net.sf.json.JSONObject;

@RestController
public class StartController {
	
	@Autowired
	private RoomService roomService;
	@Autowired
	private RedisService redisService;
	
	/**
	 * 开启房间演示
	 * @param roomName
	 * @param openid
	 * @param state 0:未开始，1：开始，2：结束
	 * @return
	 */
	@ResponseBody
	@GetMapping("/start/{roomName}")
	public Object StartActivity(@PathVariable("roomName")String Name,@RequestParam("openid")String id,
			@RequestParam("state")Integer activity) {
					
		if(activity==null) {
			return null;
		}
		//判断是否是房主
		int uid=UserInfoUtils.OpenidTranslation(id);
		if(!roomService.isMaster(Name, uid)) {
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("你不是房主不允许操作!");
			return msg;
		}
		
		Room r=new Room();
		MessageData<String> msg=new MessageData<>();
		switch (activity) {
		case 0:  //未开始
			r.setRoomState(activity);			
			msg.setCode(0);
			msg.setMsg("房间已设置成未开始!");
			break;
		case 1:  //开始
			r.setRoomState(activity);
			msg.setCode(1);
			msg.setMsg("房间已设置成开始!,可以传输了");
			break;
		case 2:  //结束
			r.setRoomState(activity);
			msg.setCode(2);
			msg.setMsg("房间已设置成结束!");
			break;
		}
		r.setName(Name);
		roomService.updateRoomByNameAndOpenid(r);
		//translate.put(Name, activity);
		redisService.put(Name,String.valueOf(activity),60*60*2);
		return msg;
	}
	
	/**
	 * 通知房间所有人权限发送变化
	 * @param name
	 * @return
	 */
	@ResponseBody
	@GetMapping("/tell_power/{room}")
	public String PowerTell(@PathVariable("room") String name) {
		
		CopyOnWriteArraySet<RoomWebsocket> webSocketSet=RoomWebsocket.room.get(name);
		if(webSocketSet==null) {
			return "error";
		}
		 //群发消息
        for (RoomWebsocket item : webSocketSet) {        	
        	item.sendMessage(107,"权限转移，请重新拉取权限!");
        }
		
		return "ok";
	}

	/**
	 * 演示权限转移
	 * @param openid
	 * @param room
	 * @param uid
	 * @return
	 */
	@ResponseBody
	@GetMapping("/give_play/{id}")
	public Object GivePlay(@PathVariable("id") String openid,
			@RequestParam(value="room",required=true)String room,
			@RequestParam(value="uid",required=true)Integer uid
			) {
		
		if(uid==null) {
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setData("error,指定演示者不能为空");
			return msg;
		}
		
		//判断是否是房主
		int id=UserInfoUtils.OpenidTranslation(openid);
		if(!roomService.isMaster(room, id)) {
		MessageData<String> msg=new MessageData<>();
		msg.setCode(404);
		msg.setMsg("你不是房主不允许操作!");
		return msg;		
		}
		CopyOnWriteArraySet<RoomWebsocket> webSocketSet=RoomWebsocket.room.get(room);
		if(webSocketSet==null) {
			return "error";
		}
		String oid=UserInfoUtils.UserInfoidTranslation(String.valueOf(uid));//赋予权限的人
		String player=(String) redisService.get("room-play-"+room);	//被撤销权限的人
		if(oid!=null) {
			redisService.put_notime("room-play-"+room, oid);   //赋予权限
		//发消息
        for (RoomWebsocket item : webSocketSet) {        	
        	if(item.getSid().equals(oid)) {
        			item.sendMessage(108, "你已被赋予演示权限!");       		
        	}
        	if(item.getSid().equals(player)) {
        		item.sendMessage(109, "你已被撤销演示权限!");  
        	}
        }
		
		}	
        MessageData<String> msg=new MessageData<>();
        msg.setCode(200);
        msg.setMsg("演示权限已经发送，请关闭你的演示");
		return msg;
	}
	
	//接收推送
	@ResponseBody
	@PostMapping("/send_data/{room}")
	public Object Reciver(@PathVariable("room")String roomName,
			@RequestBody String data,
			HttpServletRequest req) {
		String openid=(String) redisService.get(req.getCookies()[0].getComment());
		MessageData<String> msg=new MessageData<>();
		msg.setCode(403);
		msg.setMsg("推送失败");
		if(openid==null) {
			msg.setCode(401);
			msg.setMsg("该用户不存在推送失败");
			return msg;
		}
		//String activity= RedisUtils.get(roomName);
    	//if(!RedisUtils.get("room-play-"+roomName).equals(openid))return msg;
		//if(activity==null) return msg;
		CopyOnWriteArraySet<RoomWebsocket> webSocketSet=RoomWebsocket.room.get(roomName);
		if(webSocketSet==null) {
			msg.setCode(402);
			msg.setMsg("房间不存在");
			return msg;
		}
		int Code=0;
		JSONObject json=JSONObject.fromObject(data);
		String type=json.getString("type");
			if(type.equals("1")) {
            	Code=104;
            }else if(type.equals("2")) {
            	Code=105;
            }else {
            	Code=106;
            }
		
		
		//群发消息
        for (RoomWebsocket item : webSocketSet) {
        	if(!item.getSid().equals(openid)) {
        		item.sendMessage(Code,json.getString("data"));
        	}
        	
        }
        msg.setCode(200);
        msg.setMsg("推送成功!");
		return msg;
	}
	@ResponseBody
	@RequestMapping(value="/post_vioce/{room}",method=RequestMethod.POST)
	public Object getVoice(@PathVariable("room")String room,
			@RequestParam("data")String data,@RequestParam("oid")String openid,
			HttpServletRequest req) {
		MessageData<String> msg=new MessageData<>();
		msg.setCode(403);
		msg.setMsg("推送失败");
		if(openid==null) {
			msg.setCode(401);
			msg.setMsg("该用户不存在推送失败");
			return msg;
		}
		CopyOnWriteArraySet<RoomWebsocket> webSocketSet=RoomWebsocket.room.get(room);
		if(webSocketSet==null) {
			msg.setCode(402);
			msg.setMsg("房间不存在");
			return msg;
		}
		
		Utils.base64ToFile(System.getProperty("user.dir")+"/static/roomFiles/"+room+"/", data, "voice.mp3");
		//群发消息
        for (RoomWebsocket item : webSocketSet) {
        	if(!item.getSid().equals(openid)&&item.getRoomName().equals(room)) {
        		item.sendMessage(105,"https://wx.hexurong.xyz/api/room/temp/"+room+"/voice.mp3?");
        	}
        	
        }
        
		msg.setCode(200);
		msg.setMsg("推送音频成功!");
		return msg;
	}
	
	
	

	
}
