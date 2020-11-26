package com.shelgon.nopage.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.shelgon.nopage.model.RoomFile;

public interface RoomFileDao extends JpaRepository<RoomFile, Integer>{
	
	public List<RoomFile> getByRoomId(int rid);
	
	public RoomFile getByFileId(int fid);
	
	@Modifying
	@Query("delete from RoomFile rf where rf.fileId=?1")
	public void deleteByFileId(int fid);
	@Modifying
	@Query("delete from RoomFile rf where rf.roomId=?1")
	public void deleteByRoomId(int rid);
	
	public  RoomFile getByRoomIdAndFileId(int rid,int fid);

}
