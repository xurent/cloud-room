package com.shelgon.nopage.model;

public class IdUser {
	
	private int id;
	private String openid;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	@Override
	public String toString() {
		return "IdUser [id=" + id + ", openid=" + openid + "]";
	}
	

}
