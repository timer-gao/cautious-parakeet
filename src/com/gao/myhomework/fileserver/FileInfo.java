package com.gao.myhomework.fileserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
/**
 * 文件信息类，记录服务器端一个用户空间文件信息<br>包括：<i><br>key:文件名<br>value:<blockquote>文件长度<br>所在存储节点服务器名称数组</blockquote>
 * <br>key:文件名_up_point<br>value:<br><br><dd>上传断点
 * @author 天空蓝：）
 *
 */
public class FileInfo {

	//private String fileName;
	//private long fileLength;
	private Properties p;
	private String userName;
	/**
	 * 构造函数，打开或创建用户空间配置文件
	 * @param userName
	 * @throws IOException
	 */
	public FileInfo(String userName) throws IOException{
		p=new Properties();
		this.userName=userName;
		File f=new File(FTServer.share+"\\"+userName+"FileInfo.properties");
		if(!f.exists())
		new FileWriter(f).write("\n");
		p.load(new FileReader(f));
	}
	/**
	 * 设置断点
	 * @param fileName
	 * @param point
	 * @param servername
	 */
	public void setUpPoint(String fileName,long point,String servername){
		p.setProperty(fileName+"_up_point", point+","+servername);
	}
	/**
	 * 获取断点
	 * @param fileName
	 * @return point -long
	 */
	public long getUpPoint(String fileName){
		String s=p.getProperty(fileName+"_up_point",0+",");
		try{return Long.parseLong(s.substring(0,s.indexOf(",")));}
		catch(Exception e){
			return 0;
		}
	}
	/**
	 * 获取断点所在存储节点名称
	 * @param fileName
	 * @return storageServerName -String
	 */
	public String getUpPointServer(String fileName){
		String s=p.getProperty(fileName+"_up_point",(long)0+",");
		try{s=s.substring(s.indexOf(",")+1);}
		catch(Exception e){
			s=null;
		}
		return s;
	}
	/**
	 * 增加文件记录到用户空间，自动生成UUID并记录文件长度和所在位置信息
	 * @param fileName
	 * @param fileLength
	 * @param locations
	 */
	public void addFile(String fileName,long fileLength,StorageNode[] locations,boolean isCreateUUID){
		String temp="";
		if(isCreateUUID)
			temp=UUID.randomUUID().toString()+";"+fileLength+"";
		else
			temp=findUUID(fileName)+";"+fileLength+"";
		for(int i=0;i<locations.length;i++)
		{
			temp +=","+locations[i].getServerName();
		}
		p.setProperty(fileName, temp);
	}
	/**
	 * 查找完整文件长度
	 * @param fileName
	 * @return length -long
	 */
	public long findFileLength(String fileName){
		String s=p.getProperty(fileName);
		long len=0;
		try{len=Long.parseLong(s.substring(s.indexOf(";")+1,s.indexOf(",")));}
		catch(Exception e){
			len=0;
			return len;
		}
		return len;
	}
	/**
	 * 根据文件名查找，返回所在位置存储节点的名称数组
	 * @param fileName
	 * @return storageServerName -String[]
	 */
	public String[] findFile(String fileName){
		
		if(getUpPoint(fileName)!=findFileLength(fileName))
			return null;
		String s=p.getProperty(fileName);
		String[] str=null;
		try{s=s.substring(s.indexOf(",")+1);
		
		if(s.indexOf(",")==-1)
		{
			str=new String[1];
			str[0]=s;
		}
		else{
			str=new String[2];
			str[0]=s.substring(0,s.indexOf(","));
			str[1]=s.substring(s.indexOf(",")+1);
		}}
		catch(Exception e)
		{
			str=null;
		}
		return str;
	}
	/**
	 * 查找文件的UUID
	 * @param fileName
	 * @return UUID -String
	 */
	public String findUUID(String fileName){
		String s=p.getProperty(fileName);
		try{s=s.substring(0,s.indexOf(";"));}catch(Exception e)
		{
			s=null;
		}
		return s;
	}
	/**
	 * 删除用户文件记录
	 * @param fileName
	 */
	public void removeFile(String fileName){
		p.setProperty(fileName, "");
		p.setProperty(fileName+"_up_point", "");
	}
	/**
	 * 保存用户已上传文件信息到文本文件（包括正在上传中的断点）
	 * <br><br><dd>客户端断开时调用
	 */
	public void savaFileInfo(){
		try {
			p.store(new FileWriter(new File(FTServer.share+"\\"+userName+"FileInfo.properties")), ":");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
