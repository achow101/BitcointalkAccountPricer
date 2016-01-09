package com.achow101.bctalkaccountpricer.shared;

import java.io.Serializable;

public class QueueRequest implements Serializable{

	private static final long serialVersionUID = -1012714783248122922L;
	private boolean go = false;
	private int pos = 0;
	private boolean newRequest = true;
	private String ip;
	private long requestedTime;
	private String token;
	private String[] result;
	private int uid = 0;
	private boolean processing = false;
	private boolean done = false;
	private long expirationTime = 86400;
	private boolean merch;
	private long completedTime;
	
	public QueueRequest(){}
	
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
	
	public void setRequestedTime(long unixTime)
	{
		requestedTime = unixTime;
	}
	
	public long getRequestedTime()
	{
		return requestedTime;
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
		setRequestedTime(System.currentTimeMillis() / 1000L);
	}
	
	public boolean isDone()
	{
		return done;
	}
	
	public void setExpirationTime(long secs)
	{
		expirationTime = secs;
	}
	
	public long getExpirationTime()
	{
		return expirationTime;
	}
	
	public boolean isExpired()
	{
		return (System.currentTimeMillis() / 1000L) > (completedTime + expirationTime);
	}
	
	public void setMerchant(boolean merch)
	{
		this.merch = merch;
	}
	
	public boolean isMerchant()
	{
		return merch;
	}
	
	public void setCompletedTime(long unixtime)
	{
		this.completedTime = unixtime;
	}
	
	public long getCompletedTime()
	{
		return completedTime;
	}
}