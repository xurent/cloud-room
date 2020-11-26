package com.shelgon.nopage.service;

import java.util.List;

import com.shelgon.nopage.model.RoomFile;

public interface RoomFileService {

	/**
	 * 建立文件与房间的关系
	 * @param rf
	 */
	public void Add(RoomFile rf);
	
	/**
	 * 获取房间文件
	 * @return
	 */
	public List<RoomFile> getRoomFile(String Name);
	
	/**
	 * 删除文件
	 */
	public void deleteFile(int fid);
	
	
}
