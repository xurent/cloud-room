package com.shelgon.nopage.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.shelgon.nopage.model.Room;

public interface RoomDao extends JpaRepository<Room, Integer>,
PagingAndSortingRepository<Room, Integer>{
	
	public Room getByName(String name);
	
	@Modifying
	public void deleteByName(String name);
	
	
	//原生sql语句
	@Query(value="select * from tb_room_info where"
			+ " id in(select room_id from tb_room_user_relationship where uid =?1)", nativeQuery=true)
	public List<Room> getPersonalRooms(int uid);
	
	@Query(value="select * from tb_room_info r where r.room_title like %?1% ", nativeQuery=true)
	public List<Room> findRoomLikeRoomTitle(String data);
	
	@Modifying
	@Query("update Room r set r.roomState=?2 where r.id=?1")
	public void updateStateByRoomId(int id,int state);
	
	
	@Modifying
	@Query("update Room r set r.roomPass=?2 where r.name=?1")
	public void updateRoomPassByRoomName(String name,String pass);
}
