package com.shelgon.nopage.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shelgon.nopage.dao.RoomDao;
import com.shelgon.nopage.dao.UserRoomDao;
import com.shelgon.nopage.model.Room;
import com.shelgon.nopage.model.UserRoom;

@Service("roomService")
@Transactional
public class RoomServicelmpl implements RoomService{
	
	@Autowired
	private RoomDao roomDao;
	
	@Autowired
	private UserRoomDao userRoomDao;

	/**
	 * 如果房间创建成功返回房间信息
	 * 如果房间已经存在返回null
	 */
	
	@Override
	public Room add(Room room,int uid) {		
		Room r=roomDao.getByName(room.getName());		
		if(r==null) {
			r=roomDao.save(room);//保存房间
			UserRoom ur=new UserRoom();
			ur.setRoomId(r.getId());
			ur.setUid(uid);
			userRoomDao.save(ur);   //保存用户和房间的关系
			return r;
			
		}	
		return null;
	}

	@Override
	public void deleteRoomByName(String name) {
		Room r=getOneRoomByName(name);
		if(r!=null) {
			int room_id=r.getId(); //要删除房间的ID
			roomDao.deleteByName(name); //删除房间
			userRoomDao.deleteByRoomId(room_id);  //删除关系
		}
	}

	@Override
	public Page getAlls(int page, int num, String des) {
		PageRequest pageable=PageRequest.of(page-1, num,Direction.DESC,des);
		Page <Room> pager= roomDao.findAll(pageable);
		return  pager;
	}

	@Override
	public List<Room> getRoomsByNameOrTitle(String data) {
		List<Room> r=roomDao.findRoomLikeRoomTitle(data);
		if(r.isEmpty())	{
			r=new ArrayList<>();
			r.add(getOneRoomByName(data));
		}
			
		return r;
	}

	@Override
	public void updateRoomByNameAndOpenid( Room room) {	
		Room r=getOneRoomByName(room.getName());
		System.out.println(r.toString());
		if(r!=null) {
			roomDao.updateStateByRoomId(r.getId(), room.getRoomState());
		}
		
	}

	@Override
	public Room getOneRoomByName(String name) {
		
		return roomDao.getByName(name);
	}

	@Override
	public List<Room> getPersonRooms(int uid) {	
		return roomDao.getPersonalRooms(uid);
	}

	@Override
	public boolean isMaster(String RoomName,int uid) {
		Room r=getOneRoomByName(RoomName);
		if(r!=null) {
			int room_id=r.getId();
			UserRoom ur= userRoomDao.getByRoomIdAndUid(room_id, uid);
			if(ur!=null) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public List<Room> getALLS() {
		
		return roomDao.findAll();
	}

	@Override
	public void updateRoomPass(String roomName, String pass) {
		
		roomDao.updateRoomPassByRoomName(roomName, pass);
	}

	@Override
	public void updateRoomInfo(Room r) {
		roomDao.saveAndFlush(r);
		
	}

}
