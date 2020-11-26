package com.shelgon.nopage.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.EncodeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.shelgon.nopage.model.MessageData;
import com.shelgon.nopage.model.Room;
import com.shelgon.nopage.model.UserInfo;
import com.shelgon.nopage.model.Yicheng;
import com.shelgon.nopage.service.RoomService;
import com.shelgon.nopage.service.consumer.RedisService;
import com.shelgon.nopage.utils.RedisUtils;
import com.shelgon.nopage.utils.UserInfoUtils;
import net.sf.json.JSONObject;

@RestController
public class RoomController {
	
	@Autowired
	private RoomService roomService;	
	//public static Map<String,CopyOnWriteArraySet<RoomWebsocket>> room=new HashMap<>(); 
	
	@Autowired
	private RedisService redisService;
	
	
    
	/**
	 * 创建房间
	 * @param id
	 * @param r  房间信息
	 * @return
	 */
	
	@ResponseBody
	@PostMapping("/create_room/{openid}")
	public Object CreateRoom(@PathVariable("openid") String  id,@RequestBody Room r) {
		if(r==null) {
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("提交的数据有误!");
			return msg;
		}
		//生成房间号码
		int number=(int)(Math.random()*100000+10000);
		System.out.println("房间号码:---->"+number);
		r.setName(String.valueOf(number));
		System.out.println("openid--->"+id);
		
		//调用用户信息模块，openid换取用户信息
		UserInfo userInfo=UserInfoUtils.getUserInfo(id);
		if(userInfo==null) {
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("openid不存在!!");
			return msg;
		}
		
		r.setRoomCreateTime(new Date());
		r.setMasterID(id);
		r.setRoomMaster(userInfo.getUsername());
		r.setRoomState(0);
		if(r.getMaxSize()==null||r.getMaxSize()<1) {
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("房间容量小于1,创建房间失败!");
			return msg;
		}
		
		//保存房间时要保存用户和房间的关系
		Integer uid=UserInfoUtils.OpenidTranslation(id);
		if(roomService.add(r,uid)==null) {
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("房间存在,创建房间失败!");
			return msg;
		}
		//建立房主和房间权限关系
		RestTemplate rt=new RestTemplate();
		String obj=rt.getForObject(redisService.getInstance("authority-service")+"/addRoomPermission?"
				+ "roomid="+number+"&isUpfile=0&isDownfile=1&isReadfile=1&isdeletefile=0&isModifyfile=0", String.class);
		
		redisService.put("room"+r.getName(), r.toString(), 60*60*24);
		
		redisService.put("roominfo-"+number,r.toString(),8*60*60);
		redisService.put_notime("room-play-"+number, id);//赋予房主演示权限
		
		//创建房间文件夹
		
		RedisUtils.Posttemplate(redisService.getInstance("file-service")+"/room/mkRoomDir","theme="+r.getRoomTitle()+"&uid="+id+"&roomid="+number);
		
		MessageData<Room> msg=new MessageData<>();
		msg.setCode(200);
		msg.setData(r);
		msg.setMsg("创建房间成功!");
		return msg;
	}
	
	/**
	 * 获取所有房间
	 * @param pid
	 * @param limit
	 * @param des
	 * @return
	 */
	
	@ResponseBody
	@GetMapping("/take_room_info")
	public Object getRoomInfo(@RequestParam("page") Integer pid,@RequestParam("limit") Integer limit,
			@RequestParam(value="des",required=false,defaultValue="id") String des) {
		if(pid==null)pid=1;
		if(limit==null)limit=10;
		
		Page page= roomService.getAlls(pid, limit,des);
		List<Room> rooms=page.getContent();
		//重要信息不能获取全部置空
		for(Room room:rooms) {
			room.setRoomFileServer("");
			if(room.getRoomPass()!=null) {
				if(!room.getRoomPass().isEmpty()) {
					room.setRoomPass("1");  //有密码
				}else {
					room.setRoomPass("0");//无密码
				}
			}else {
				room.setRoomPass("0");//无密码
			}
			
		}
		if(rooms==null) {
			MessageData<Room> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("数据为空!");
			return msg;
		}
		
		MessageData<List<Room>> msg=new MessageData<>();
		msg.setCode(200);
		msg.setData(rooms);
		msg.setMsg(String.valueOf(page.getTotalPages()));
		return msg;
	}
	
	/**
	 * 进入房间
	 * @param roomName 房间号码
	 * @param id  用户openid
	 * @param pass 房间密码
	 * @return
	 */
	@ResponseBody
	@GetMapping("/entry_room/{roomName}")
	public Object entryRoom(@PathVariable("roomName") String roomName,@RequestParam("openid")String id,
			@RequestParam(value="password",required=false) String pass) {
			
		UserInfo u=UserInfoUtils.getUserInfo(id);
		//判断id是否存在
		if(u==null) {
			MessageData<String> msg=new MessageData<>();
    		msg.setCode(404);
    		msg.setMsg("该用户不存在!");
    		return msg;
		}
		
		//判断房间是否存在
		Room r=roomService.getOneRoomByName(roomName);
        if(r==null) {
        	MessageData<String> msg=new MessageData<>();
    		msg.setCode(404);
    		msg.setMsg("该房间不存在!");
    		return msg;	
        }
        Integer uid=UserInfoUtils.OpenidTranslation(id);
        System.out.println("用户Id:"+uid);
        //如果不是房主裁判断密码
        boolean isMaster=roomService.isMaster(roomName, uid);
        if(!isMaster) {       	     
        String password=r.getRoomPass();
        if(password!=null&&!password.isEmpty()) {
        	
        	if(pass==null) {
        		MessageData<String> msg=new MessageData<>();
        		msg.setCode(404);
        		msg.setMsg("请输入房间密码!");
        		return msg;
        	}else if(!pass.equals(r.getRoomPass())) {
        		MessageData<String> msg=new MessageData<>();
        		msg.setCode(404);
        		msg.setMsg("房间密码输入错误!");
        		return msg;
        	}
        	
        }      
        }
        if(isMaster) {
        	redisService.put_notime("room-play-"+roomName, id);//赋予房主演示权限
        }
        
  /*      //记录进入该房间的用户socket
        RoomWebsocket usersocket=null;
        
        //如果缓存中房间为空则创建
        CopyOnWriteArraySet<RoomWebsocket> Set=room.get(roomName);
        if(Set==null) {
			   Set = new CopyOnWriteArraySet<RoomWebsocket>();
			   
		   } 
        //进入房间的人的websocket放入Set
       for(RoomWebsocket websocket: RoomWebsocket.getWebSocketSet()) {
    	   System.out.println("websocket->sid:"+websocket.getSid());
    	   if(websocket.getSid().equals(id)) {
    		   usersocket=websocket;
    		   Set.add(websocket);
    	   }
       }  
       room.put(roomName, Set);*/
       
//       if(r.getMaxSize().toString().isEmpty())r.setMaxSize(5);//默认最大5
       System.out.println("room-"+roomName);
       if(RoomWebsocket.room.get(roomName)==null) {
    	   CopyOnWriteArraySet<RoomWebsocket> Set = new CopyOnWriteArraySet<RoomWebsocket>();
    	   RoomWebsocket.room.put(roomName, Set);   
       }
       int room_present_num=RoomWebsocket.room.get(roomName).size();
       	room_present_num++;
       //超出房间限制人数
       if(r.getMaxSize()<room_present_num) {
       	MessageData<String> msg=new MessageData<>();
   		msg.setCode(404);
   		msg.setMsg("进入失败,超出房间最大限制人数!");
   		return msg;
       }
       
       
       //redisService.put("userid-openid-"+r.getId(), id,60*60*2);
      /* Iterator<RoomWebsocket> it=room.get(roomName).iterator();
       
       String data="用户 :"+u.getUsername()+"进入房间!";
       System.out.println(data);*/
       
       //通知
   /*    while(it.hasNext()) {
    	   RoomWebsocket socket=it.next();
    	  if( !socket.equals(usersocket)) { //进入房间的消息不发给自己
    		  socket.sendMessage(102,data);
    		  }
       }*/
     /*  //获取房间成员信息
       List<UserInfo> users=new ArrayList<>();
       for(RoomWebsocket socket:Set) {
    	  UserInfo userInfo= UserInfoUtils.getUserInfo(socket.getSid());   	  
    	  if(userInfo!=null) {
    		  users.add(userInfo);
    	  }
       }*/
       redisService.put("ROOM-"+id, roomName, 60*60);
       
       //添加用户到房间
       RestTemplate rt=new RestTemplate();
		String obj=rt.getForObject(redisService.getInstance("authority-service")+"/addUserRoom?"
				+"uid="+id+"&roomid="+roomName+"&state=0", String.class);
       
       MessageData<Yicheng> msg=new MessageData<>();
       Yicheng data=new Yicheng();
       data.setNumber(String.valueOf(room_present_num));
       data.setMaster(r.getMasterID());
       data.setState(String.valueOf(r.getRoomState()));
       data.setContent(r.getRoomagenda());
       msg.setCode(200);
       msg.setData(data);
       msg.setMsg("成功进入房间"+roomName+"!");
       return msg;
	}
	
	

	
	
	
	/**
	 * 退出房间
	 * @param rid  房间号
	 * @param id
	 * @return
	 */
	@ResponseBody
	@GetMapping("/quit_room/{roomid}")
	public Object quitRoom(@PathVariable("roomid")String rid,@RequestParam("openid") String id) {		
		Room r=roomService.getOneRoomByName(rid);
		 System.out.println("退出房间:"+rid);
		if(r==null) { //房间不存在
			MessageData<String> msg=new MessageData<>();
		      msg.setCode(404);
		      msg.setMsg("房间"+rid+"不存在!");
		      return msg;
		}
		CopyOnWriteArraySet<RoomWebsocket> CWSet=RoomWebsocket.room.get(rid);
		if(CWSet==null) { //房间存在但不在缓存
			MessageData<String> msg=new MessageData<>();
		      msg.setCode(404);
		      msg.setMsg("房间"+rid+"不存在!");
		      return msg;
		}	
//		boolean no_exit_room=true;
//		for(RoomWebsocket socket:CWSet) {
//			System.out.println("cwSet->"+socket.getSid());
//			if(socket.getSid().equals(id)) {
//				//CWSet.remove(socket);  //移除socket对象
//				socket.sendMessage(101,"你已经退出,请关闭socket连接！");
//				no_exit_room=false; 
//			}
//		}
//		
//		if(CWSet.size()==0) {
//			RoomWebsocket.room.remove(rid);
//		}else {
//			RoomWebsocket.room.put(rid, CWSet);	//放回容器
//		}
//		
//		if(no_exit_room) {
//			MessageData<String> msg=new MessageData<>();
//		      msg.setCode(404);
//		      msg.setMsg("你不在该房间里,无法退出!");
//		      return msg;
//		}
		//通知退出
//		String userName=UserInfoUtils.getUserInfo(id).getUsername();
//		String data="用户:"+userName+" 退出房间!";
//		System.out.println(data);
//		for(RoomWebsocket socket:CWSet) {
//			socket.sendMessage(103,data);
//			System.out.println("退出信息通知给用户:"+socket.getSid());
//		}
		Integer uid=UserInfoUtils.OpenidTranslation(id);
		if(roomService.isMaster(rid, uid)) {//房主退出设置结束状态
			r.setRoomState(0);
			roomService.updateRoomByNameAndOpenid(r);
		}
		redisService.remove("ROOM-"+id);
		MessageData<String> msg=new MessageData<>();
	      msg.setCode(200);
	      msg.setMsg("成功退出房间!");
		return msg;
	}
	
	
	
	
	/**
	 * 获取个人创建的房间
	 * @param id  openid
	 * @return
	 */
	@ResponseBody
	@GetMapping("/get_user_room/{id}")
	public Object getPersonalRoom(@PathVariable("id")String id) {
		Integer uid=UserInfoUtils.OpenidTranslation(id);
		if(uid==null) {
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("openid不存在!!");
			return msg;
		}
		List<Room> rooms= roomService.getPersonRooms(uid);
		MessageData<List<Room>> msg=new MessageData<>();
		msg.setCode(200);
		msg.setData(rooms);
		msg.setMsg("获取个人创建的房间成功!");
		return msg;
		
	}
	
	/**
	 * 搜索房间
	 * @param content 根据房间号或者房间标题内容
	 * @return
	 */
	@ResponseBody
	@GetMapping("/find_room")
	public Object findRoom(@RequestParam("content")String content) {		
		if(content==null) { 
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("搜索的内容不能为空");
			return msg;
		}
		List<Room> rooms=roomService.getRoomsByNameOrTitle(content);
		for(Room room:rooms) {
			room.setRoomFileServer("");
			if(room.getRoomPass()!=null) {
				if(!room.getRoomPass().isEmpty()) {
					room.setRoomPass("1");  //有密码
				}else {
					room.setRoomPass("0");//无密码
				}
			}else {
				room.setRoomPass("0");//无密码
			}
		}
		MessageData<List<Room>> msg=new MessageData<>();
		msg.setCode(200);
		msg.setData(rooms);
		msg.setMsg("搜索房间成功!");
		
		return msg;
	}

	/**
	 * 获取房间成员信息表
	 * @param roomName
	 * @param id
	 * @return
	 */
	@ResponseBody
	@GetMapping("/get_room_member/{roomId}")
	public Object getRoomMember(@PathVariable("roomId") String roomName,@RequestParam("openid")String id){
		
		//判断房间是否存在
		CopyOnWriteArraySet<RoomWebsocket> Set=RoomWebsocket.room.get(roomName);
		if(Set==null) {
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("获取房间成员信息失败,该房间不存在!");
			return msg;
		}
		List<UserInfo> users=new ArrayList<>();
		//用户是否在该房间，不在该房间则不允许获取房间成员信息
		boolean authority=false;
		for(RoomWebsocket r:Set) {
			if(r.getSid().equals(id)) {
				authority=true;
			}
			UserInfo u=UserInfoUtils.getUserInfo(r.getSid());
			if(u!=null) {
				users.add(u);
			}
		}
		if(authority==false) {//无权获取
			MessageData<String> msg=new MessageData<>();
			msg.setCode(404);
			msg.setMsg("你不是该房间成员,无法获取成员信息!");
			return msg;
		}
		MessageData<List<UserInfo>> msg=new MessageData<>();
		msg.setCode(200);
		msg.setData(users);
		msg.setMsg("获取房间成员信息成功!");
		
		return msg;
	}
	
	//判断是不是房主和房间成员
	@GetMapping("/check_member")
	public Object Room_Master_Remember(@RequestParam("openid")String id,
			@RequestParam("room") String roomName) {
		//type 0:房间不存在或成员不在，1房主，2成员
		Room r=roomService.getOneRoomByName(roomName);
		Integer Id=UserInfoUtils.OpenidTranslation(id);
		if(Id==null) {
			
			return 0;
		}
		int type=0;
		//判断是否成员
		String Name=(String) redisService.get("ROOM-"+id);
		if(Name!=null) {
			if(Name.equals(roomName)) {
				type=2;
			}
		}
		//判断是否房主
		boolean flag=roomService.isMaster(roomName, Id);
		if(flag)type=1;
		
		return type;
	}
	
	/**
	 * 通过 房间号获取房间信息
	 * @param roomName
	 * @return
	 */
	@ResponseBody
	@GetMapping("/get_room_info")
	public Object getRoomInfoByRoomName(@RequestParam("room")String roomName) {
		MessageData <Room> msg=new MessageData<Room>();
		
		if(roomName.isEmpty()) {
			msg.setCode(404);
			msg.setMsg("获取失败,房间号为空!");
			return msg;		
		}
		Room r=roomService.getOneRoomByName(roomName);
		if(r==null) {
			msg.setCode(404);
			msg.setMsg("获取失败,房间不存在!");
			return msg;
		}		
		msg.setCode(200);
		msg.setData(r);
		
		return msg;
	}
	
	/**
	 * 修改房间密码
	 * @param roomId
	 * @param id
	 * @param password
	 * @return
	 */
	
	@ResponseBody
	@PostMapping("/set_room_password/{room}")
	public Object setRoomPass(@PathVariable("room")String roomId,@RequestParam("openid")String id,
			@RequestParam("password")String password) {		
		MessageData <String> msg=new MessageData<String>();
		if(id==null) {
			return "error";
		}
		Integer uid=UserInfoUtils.OpenidTranslation(id);
		if(uid==null) {
			return "error";
		}
		if(!roomService.isMaster(roomId, uid)) {
			msg.setCode(404);
			msg.setMsg("你不是该房间房主设置失败!");
			return msg;
		}
		roomService.updateRoomPass(roomId, password);
		msg.setCode(200);
		msg.setMsg("密码设置成功！");
		return msg;
	}
	
	/**
	 * 修改房间信息
	 * @param roomName
	 * @param req
	 * @param room
	 * @return
	 */
	@ResponseBody
	@PostMapping("/update_roominfo/{room}/{oid}")
	public Object notifyRoomInfo(@PathVariable("room")String roomName,
			@PathVariable("oid")String openid,@RequestBody Room room) {
		System.out.println(openid);
		Integer id= UserInfoUtils.OpenidTranslation(openid);
		if(id==null) {
			return "error";
		}
		if(roomService.isMaster(roomName, id)) {
			Room r=roomService.getOneRoomByName(roomName);
			r.setRoomagenda(room.getRoomagenda());
			r.setRoomPass(room.getRoomPass());
			r.setRoomContent(room.getRoomContent());
			r.setRoomImg(room.getRoomImg());
			r.setRoomTitle(room.getRoomTitle());
			r.setRoomFileServer(room.getRoomFileServer());
			System.out.println(room.toString());
			if(room.getMaxSize()>1)
			r.setMaxSize(room.getMaxSize());
			roomService.updateRoomInfo(r);
			MessageData<String> msg=new MessageData<>();
			msg.setCode(200);
			msg.setMsg("修改成功");
			return msg;
		}
		MessageData<String> msg=new MessageData<>();
		msg.setCode(404);
		msg.setMsg("修改失败");
		
		return msg;
	}
	
}
