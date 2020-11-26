package com.shelgon.nopage.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.shelgon.nopage.model.Room;

public interface RoomService {
	/**
	 * 保存成功返回房间信息
	 * @param room
	 * @return
	 */
	
	public Room add(Room room,int uid); 
	/**
	 * 通过房间名删除房间
	 * @param name
	 */
	public void deleteRoomByName(String name);
	
	
	/**
	 * 分页获取房间
	 * page :页
	 * num:每次获取的数目
	 * des:根据哪个字段排序
	 * @return
	 */
	public Page getAlls(int page, int num,String des);
	/**
	 * 查询所有
	 * @return
	 */
	public List<Room> getALLS();
	
	
	/**
	 * 
	 * @param   根据房间名精确查找
	 * @param   根据标题名模糊查找
	 * @return
	 */
	public List<Room> getRoomsByNameOrTitle(String data);
	
	/**
	 * 根据房间名 更改房间信息
	 * @param RoomName
	 * @param openid
	 * 
	 */
	public void updateRoomByNameAndOpenid(Room room);
	
	/**
	 * 根据房间名获取一个房间
	 * @param name
	 * @return
	 */
	public Room getOneRoomByName(String name);
	
	/**
	 * 根据uid 获取所有个人创建的房间
	 * @param openid
	 * @return
	 */
	public List<Room> getPersonRooms(int uid);
	
	/**
	 * 判断是否为房主
	 * @param rid
	 * @param uid
	 * @return
	 */
	public boolean isMaster(String roomName,int uid);
	
	/**
	 * 修改房间密码
	 * @param roomName
	 * @param pass
	 */
	public void updateRoomPass(String roomName,String pass);
	
	/**
	 * 更新房间信息
	 * @param r
	 */
	public void updateRoomInfo(Room r);

}
