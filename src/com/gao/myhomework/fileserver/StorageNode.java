package com.gao.myhomework.fileserver;
/**
 * �洢�ڵ��࣬��¼ά���洢�ڵ���Ϣ 
 * @author ���������
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
	 * ���캯���������洢�����
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
	 * �����ļ������´洢�ڵ�ͳ����Ϣ
	 * @param fileSize long
	 * @throws InterruptedException 
	 */
	public synchronized void addFile(long fileSize) {
		
		actualvolume+=fileSize;
		freevolume-=fileSize;
		filescount++;
		
	}
	/**
	 * ɾ���ļ������´洢�ڵ�ͳ����Ϣ
	 * @param fileSize long
	 * @throws InterruptedException 
	 */
	public synchronized void removeFile(long fileSize) {
		
		actualvolume-=fileSize;
		freevolume+=fileSize;
		filescount--;
		
	}
	
	
	
	//��ó�Ա�����ĺ�����ֻ������
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
