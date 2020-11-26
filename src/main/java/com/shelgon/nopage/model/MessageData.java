package com.shelgon.nopage.model;

public class MessageData<T> {
	
	private int code;
	
	private T data;
	
	private String msg;

	@Override
	public String toString() {
		return "MessageData [code=" + code + ", data=" + data + ", msg=" + msg + "]";
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	

}
