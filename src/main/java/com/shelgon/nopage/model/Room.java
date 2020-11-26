package com.shelgon.nopage.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="tb_room_info")
public class Room implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id; //房间ID
	
	@NotEmpty
	private String name; //房间名
	@NotEmpty
	private String roomMaster; //房主
	
	private Integer maxSize; //房间限制的人数
	private String roomTitle; //房间标题
	private String roomContent;//房间内容
	private String roomImg; //房间图片
	
	private String masterID;//房主ID
	
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date roomCreateTime; //房间创建时间
	private Integer roomState=0; //房间状态
	private String roomPass=null; //房间密码
	private String roomFileServer; //房间文件服务器
	
	private String roomaGenda;
		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRoomMaster() {
		return roomMaster;
	}
	public void setRoomMaster(String roomMaster) {
		this.roomMaster = roomMaster;
	}
	public Integer getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}
	public String getRoomTitle() {
		return roomTitle;
	}
	public void setRoomTitle(String roomTitle) {
		this.roomTitle = roomTitle;
	}
	public String getRoomContent() {
		return roomContent;
	}
	public void setRoomContent(String roomContent) {
		this.roomContent = roomContent;
	}
	public String getRoomImg() {
		return roomImg;
	}
	public void setRoomImg(String roomImg) {
		this.roomImg = roomImg;
	}
	public Date getRoomCreateTime() {
		return roomCreateTime;
	}
	public void setRoomCreateTime(Date roomCreateTime) {
		this.roomCreateTime = roomCreateTime;
	}
	public Integer getRoomState() {
		return roomState;
	}
	public void setRoomState(Integer roomState) {
		this.roomState = roomState;
	}
	public String getRoomPass() {
		return roomPass;
	}
	public void setRoomPass(String roomPass) {
		this.roomPass = roomPass;
	}
	public String getRoomFileServer() {
		return roomFileServer;
	}
	public void setRoomFileServer(String roomFileServer) {
		this.roomFileServer = roomFileServer;
	}
	
	public String getRoomagenda() {
		return roomaGenda;
	}
	public void setRoomagenda(String roomaGenda) {
		this.roomaGenda = roomaGenda;
	}
	public String getMasterID() {
		return masterID;
	}
	public void setMasterID(String masterID) {
		this.masterID = masterID;
	}
	public String getRoomaGenda() {
		return roomaGenda;
	}
	public void setRoomaGenda(String roomaGenda) {
		this.roomaGenda = roomaGenda;
	}
	@Override
	public String toString() {
		return "Room [id=" + id + ", name=" + name + ", roomMaster=" + roomMaster + ", maxSize=" + maxSize
				+ ", roomTitle=" + roomTitle + ", roomContent=" + roomContent + ", roomImg=" + roomImg + ", masterID="
				+ masterID + ", roomCreateTime=" + roomCreateTime + ", roomState=" + roomState + ", roomPass="
				+ roomPass + ", roomFileServer=" + roomFileServer + ", roomaGenda=" + roomaGenda + "]";
	}
	
	
	
	
}
