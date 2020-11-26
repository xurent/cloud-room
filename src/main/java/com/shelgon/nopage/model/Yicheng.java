package com.shelgon.nopage.model;

public class Yicheng {

	private String number; //房间人数
	private String content; //议程内容
	private String master;  //房主ID
	private String state; //当前房间状态
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getMaster() {
		return master;
	}
	public void setMaster(String master) {
		this.master = master;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	@Override
	public String toString() {
		return "Yicheng [number=" + number + ", content=" + content + ", master=" + master + ", state=" + state + "]";
	}
	
	
	
}
