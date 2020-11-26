package com.shelgon.nopage.model;


public class UserInfo {

	private int id; //用户信息表id
	private String username; //用户名
	private String company; //公司
	private String job;//职位
	private String headImg; //头像路径
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public String getHeadImg() {
		return headImg;
	}
	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}
	@Override
	public String toString() {
		return "UserInfo [id=" + id + ", username=" + username + ", company=" + company + ", job=" + job + ", headImg="
				+ headImg + "]";
	}
	
	
}
