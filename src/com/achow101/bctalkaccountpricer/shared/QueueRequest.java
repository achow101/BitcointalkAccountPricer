package com.achow101.bctalkaccountpricer.shared;

import java.io.Serializable;

public class QueueRequest implements Serializable{
	
	private boolean go = false;
	private int pos = 0;
	private boolean newRequest = true;
	private String ip;
	private long time;
	private String token;
	private String[] result;
	private int uid = 0;
	private boolean processing = false;
	private boolean done = false;
	
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
	
	public void setResult(String[] result)
	{
		this.result = result;
	}
	
	public String[] getResult()
	{
		return result;
	}
	
	public void setUid(int uid)
	{
		this.uid = uid;
	}
	
	public int getUid()
	{
		return uid;
	}
	
	public void setProcessing(boolean processing)
	{
		this.processing = processing;
	}
	
	public boolean isProcessing()
	{
		return processing;
	}
	
	public void setDone(boolean done)
	{
		this.done = done;
	}
	
	public boolean isDone()
	{
		return done;
	}
}