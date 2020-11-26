package com.shelgon.nopage.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.shelgon.nopage.model.UserRoom;

public interface UserRoomDao extends JpaRepository<UserRoom, Integer>{

	public UserRoom getByRoomId(int room_id);
	
	public UserRoom getByUid(int id);
	
	@Modifying
	@Query("delete from UserRoom ur where ur.roomId=?1")
	public void deleteByRoomId(int room_id);
	@Modifying
	@Query("delete from UserRoom ur where ur.uid=?1")
	public void deleteByUid(int uid);
	
	public UserRoom getByRoomIdAndUid(int room_id,int uid);
	
}
