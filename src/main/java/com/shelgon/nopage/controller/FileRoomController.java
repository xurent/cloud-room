package com.shelgon.nopage.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.shelgon.nopage.dao.RoomDao;
import com.shelgon.nopage.model.MessageData;
import com.shelgon.nopage.model.Room;
import com.shelgon.nopage.model.RoomFile;
import com.shelgon.nopage.service.RoomFileService;

@RestController
public class FileRoomController {
	
	@Autowired
	private RoomFileService roomFileService;
	@Autowired
	private RoomDao roomDao;
	
	/**
	 * 设置房间与文件联系
	 * @param Name
	 * @param fid
	 * @return
	 */
	@ResponseBody
	@PostMapping("/set_file/{room}")
	public Object FileReciver(@PathVariable("room")String Name,@RequestParam("fid")Integer fid) {
		
		MessageData msg=new MessageData<String>();
		if(Name.isEmpty()||fid==null) {
			msg.setCode(404);
			msg.setMsg("文件ID和用户ID为空,保存失败!");
			return msg;
		}
		Room r=roomDao.getByName(Name);
		if(r==null) {
			msg.setCode(404);
			msg.setMsg("房间不存在,保存失败!");
			return msg;
		}
		RoomFile rf=new RoomFile();
		rf.setFileId(fid);
		rf.setRoomId(r.getId());
		roomFileService.Add(rf);
		msg.setCode(200);
		msg.setMsg("ok");
		return msg;
	}
	
	/**
	 * 获取房间文件ID
	 * @param rId
	 * @param fid
	 * @return
	 */
	@ResponseBody
	@PostMapping("/get_file/{room}")
	public Object FileSend(@PathVariable("room")String Name) {
		
		
		if(Name.isEmpty()) {
			MessageData msg=new MessageData<String>();
			msg.setCode(404);
			msg.setMsg("error");
			return msg;
		}		
		List<RoomFile> rfs=roomFileService.getRoomFile(Name);
		Set set=new HashSet<>();
		for(int i=0;i<rfs.size();i++) {
			set.add(rfs.get(i).getFileId());
		}	
		MessageData<Set> msg=new MessageData<Set>();
		msg.setCode(200);
		msg.setData(set);
		msg.setMsg("ok");
		return msg;
	}

}
