package com.shelgon.nopage.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shelgon.nopage.dao.RoomDao;
import com.shelgon.nopage.dao.RoomFileDao;
import com.shelgon.nopage.model.Room;
import com.shelgon.nopage.model.RoomFile;

@Service("roomFileService")
public class RoomFileServicelmpl implements RoomFileService{

	@Autowired
	private RoomFileDao roomFileDao;
	@Autowired
	private RoomDao roomDao;
	
	@Override
	public void Add(RoomFile rf) {
		RoomFile r=roomFileDao.getByRoomIdAndFileId(rf.getRoomId(), rf.getFileId());
		if(r==null) {
		roomFileDao.save(rf);
		}
	}

	@Override
	public List<RoomFile> getRoomFile(String Name) {
		Room r=roomDao.getByName(Name);
		if(r!=null) {
		List<RoomFile>lists=roomFileDao.getByRoomId(r.getId());
		return lists;
		}
		return null;
	}

	@Override
	public void deleteFile(int fid) {
		roomFileDao.deleteByFileId(fid);
	}

	
}
