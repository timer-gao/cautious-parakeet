package com.gao.myhomework.fileserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
/**
 * 后端存储节点信息管理类（公共静态类）负责提供对存储节点信息实例的操作，
 * 如：读取，保存，增加文件，删除文件，返回可用节点，负载均衡等
 * @author 天空蓝：）
 *
 */
public class FileStorageManager {
	private static Properties p=null;
	/**查询开始索引，用于负载均衡*/
	private static int startIndex=0;
	/**存储节点数组*/
	private static StorageNode storageNode[]=null;
	/**服务器信息维护配置文件路径*/
	private static String storageProperties[]=null;
	/**
	 * 初始化，获取存储节点的配置文件并且解析出存储节点信息
	 * @throws IOException
	 */
	public static void initSetServerInformation() throws IOException {
		String[] files = FTServer.share.list();
		List<String> list = new LinkedList<String>();
		for(int i=0;i<files.length;i++) {
			if(files[i].startsWith("storage")&&files[i].endsWith(".properties"))
				list.add(files[i]);
		}
		files = list.toArray(new String[0]);
		storageProperties=new String[list.size()];
		storageNode=new StorageNode[list.size()];
		for(int i=0;i<list.size();i++)
		{
			storageProperties[i]=new String(FTServer.share.getPath()+"\\"+files[i]);
		}
		
		for(int i=0;i<storageProperties.length;i++)
		{
			p = new Properties();
			p.load(new FileReader(new File(storageProperties[i])));
			String s=p.getProperty("volume");
			long volume=0;
			if(s.endsWith("KB"))
				volume=Long.parseLong(s.substring(0,s.length()-2))*1024;
			else if(s.endsWith("MB"))
				volume=Long.parseLong(s.substring(0,s.length()-2))*1024*1024;
			else if(s.endsWith("GB"))
				volume=Long.parseLong(s.substring(0,s.length()-2))*1024*1024*1024;
			else{
				throw new NumberFormatException("volume format error!");
				//System.exit(-2);
			}
			storageNode[i] = new StorageNode(
					p.getProperty("serverName"),
					p.getProperty("ip"),
					Integer.parseInt(p.getProperty("port")),
					p.getProperty("root_folder"), 
					volume,
					Long.parseLong(p.getProperty("actualvolume",0+"")), 
					Long.parseLong(p.getProperty("freevolume",volume+"")),
					Long.parseLong(p.getProperty("filescount",0+"")),
					Boolean.parseBoolean(p.getProperty("isEnable","true")));
		}
	}
	/**
	 * 程序关闭时调用，保存当前存储节点状态数据到配置文件
	 * @throws IOException
	 */
	public static void saveServerInformationToProperties() throws IOException{
		for(int i=0;i<storageNode.length;i++)
		{	
			p=new Properties();
			p.setProperty("serverName",storageNode[i].getServerName());
			p.setProperty("ip", storageNode[i].getIp());
			p.setProperty("port", storageNode[i].getPort()+"");
			p.setProperty("root_folder", storageNode[i].getRoot_folder());
			
			String volume="";
			int j=0;
			long temp=storageNode[i].getVolume();
			for(j=0;(temp/=1024)>=1024&&j<3;j++);
			switch(j){
			case 0:
				volume=temp+"KB";
				break;
			case 1:
				volume=temp+"MB";
				break;
			case 2:
				volume=temp+"GB";
				break;
			}
			p.setProperty("volume",volume);
			p.setProperty("actualvolume", storageNode[i].getActualVolume()+"");
			p.setProperty("freevolume", storageNode[i].getFreeVolume()+"");
			p.setProperty("filescount", storageNode[i].getFilesCount()+"");
			p.setProperty("isEnable", storageNode[i].isEnable()+"");
			p.store(new FileWriter(new File(storageProperties[i])),"=");
		}
	}
	/**
	 * 返回可用存储节点数组
	 * @param fileName
	 * @param fileSize
	 * @return storageNode-StorageNode[]
	 * <br>没有可用节点返回null，数组长度为 可用存储节点个数，最大为2
	 * 
	 */
	public synchronized static StorageNode[] getUploadServerInfo(String fileName,long fileSize){
		
		StorageNode info[]=new StorageNode[2];
		int k=0;
		for(int i=0;i<storageNode.length&&k<info.length;i++)
		{
			if(storageNode[startIndex].isEnable()&&fileSize<storageNode[startIndex].getFreeVolume())
				{
					info[k++]=storageNode[startIndex];
					
					
					storageNode[startIndex].addFile(fileSize);//更新节点容量信息
				}
			startIndex=(startIndex+1)%storageNode.length;
		}
		if(k==0)
			return null;
		if(k==1){
			StorageNode[] node=new StorageNode[1];
			node[0]=info[0];
			return node;
		}	
		return info;
	}
	/**
	 * 根据名称获取存储节点实例，找不到返回null
	 * @param servername
	 * @return storageNode -StorageNode
	 */
	public static StorageNode getStorageNodeByName(String servername){
		for(int i=0;i<storageNode.length;i++){
			if(storageNode[i].getServerName().equals(servername))
				return storageNode[i];
		}
		return null;
	}
}
