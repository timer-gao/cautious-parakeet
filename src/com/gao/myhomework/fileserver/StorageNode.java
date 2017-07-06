package com.gao.myhomework.fileserver;
/**
 * 存储节点类，记录维护存储节点信息 
 * @author 天空蓝：）
 *
 */
public class StorageNode {
	private String serverName;
	private String ip;
	private int port;
	private String root_folder;
	private long volume;
	private long actualvolume;
	private long	freevolume;
	private long filescount;
	private boolean isEnable;
	/**
	 * 构造函数，建立存储结点类
	 * @param serverName String
	 * @param ip String
	 * @param port int
	 * @param root_folder String
	 * @param volume long
	 * @param actualvolume long
	 * @param freevolume long
	 * @param filescount long
	 * @param isEnable boolean
	 */
	public StorageNode(String serverName,String ip, int port, String root_folder,long volume, long actualvolume,long freevolume, long filescount, boolean isEnable) {
		this.serverName=serverName;
		this.ip=ip;
		this.port=port;
		this.root_folder=root_folder;
		this.volume=volume;
		this.actualvolume=actualvolume;
		this.freevolume=freevolume;
		this.filescount=filescount;
		this.isEnable=isEnable;
	}
	/**
	 * 增加文件，更新存储节点统计信息
	 * @param fileSize long
	 * @throws InterruptedException 
	 */
	public synchronized void addFile(long fileSize) {
		
		actualvolume+=fileSize;
		freevolume-=fileSize;
		filescount++;
		
	}
	/**
	 * 删除文件，更新存储节点统计信息
	 * @param fileSize long
	 * @throws InterruptedException 
	 */
	public synchronized void removeFile(long fileSize) {
		
		actualvolume-=fileSize;
		freevolume+=fileSize;
		filescount--;
		
	}
	
	
	
	//获得成员变量的函数，只读属性
	public String getServerName(){
		return serverName;
	}
	
	public String getIp(){
		return ip;
	}
	
	public int getPort(){
		return port;
	}
	
	public String getRoot_folder(){
		return root_folder;
	}
	
	public long getVolume(){
		return volume;
	}
	
	public long getActualVolume(){
		return actualvolume;
	}
	
	public long getFreeVolume(){
		return freevolume;
	}
	
	public long getFilesCount(){
		return filescount;
	}
	
	public boolean isEnable(){
		return isEnable;
	}
}
