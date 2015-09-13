package com.achow101.bctalkaccountpricer.shared;

import java.io.Serializable;

public class QueueRequest implements Serializable{
	
	private boolean go = false;
	private int pos = 0;
	private boolean newRequest = true;
	private String ip;
	private long time;
	private String token;
	
	public boolean getGo()
	{
		return go;
	}
	
	public void setGo(boolean goNoGo)
	{
		go = goNoGo;
	}
	
	public int getQueuePos()
	{
		return pos;
	}
	
	public void setQueuePos(int qpos)
	{
		pos = qpos;
	}
	
	public String getIp()
	{
		return ip;
	}
	
	public void setIp(String ipAddress)
	{
		ip = ipAddress;
	}
	
	public void setOldReq()
	{
		newRequest = false;
	}
	
	public void setTime(long unixTime)
	{
		time = unixTime;
	}
	
	public long getTime()
	{
		return time;
	}
	
	public boolean isNew()
	{
		return newRequest;
	}
	
	public void setToken(String token)
	{
		this.token = token;
	}
	
	public String getToken()
	{
		return token;
	}
}