package com.gao.myhomework.fileserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
/**
 * ��˴洢�ڵ���Ϣ�����ࣨ������̬�ࣩ�����ṩ�Դ洢�ڵ���Ϣʵ���Ĳ�����
 * �磺��ȡ�����棬�����ļ���ɾ���ļ������ؿ��ýڵ㣬���ؾ����
 * @author ���������
 *
 */
public class FileStorageManager {
	private static Properties p=null;
	/**��ѯ��ʼ���������ڸ��ؾ���*/
	private static int startIndex=0;
	/**�洢�ڵ�����*/
	private static StorageNode storageNode[]=null;
	/**��������Ϣά�������ļ�·��*/
	private static String storageProperties[]=null;
	/**
	 * ��ʼ������ȡ�洢�ڵ�������ļ����ҽ������洢�ڵ���Ϣ
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
	 * ����ر�ʱ���ã����浱ǰ�洢�ڵ�״̬���ݵ������ļ�
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
	 * ���ؿ��ô洢�ڵ�����
	 * @param fileName
	 * @param fileSize
	 * @return storageNode-StorageNode[]
	 * <br>û�п��ýڵ㷵��null�����鳤��Ϊ ���ô洢�ڵ���������Ϊ2
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
					
					
					storageNode[startIndex].addFile(fileSize);//���½ڵ�������Ϣ
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
	 * �������ƻ�ȡ�洢�ڵ�ʵ�����Ҳ�������null
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
