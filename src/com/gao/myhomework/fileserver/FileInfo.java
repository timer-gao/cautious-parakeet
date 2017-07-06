package com.gao.myhomework.fileserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
/**
 * �ļ���Ϣ�࣬��¼��������һ���û��ռ��ļ���Ϣ<br>������<i><br>key:�ļ���<br>value:<blockquote>�ļ�����<br>���ڴ洢�ڵ��������������</blockquote>
 * <br>key:�ļ���_up_point<br>value:<br><br><dd>�ϴ��ϵ�
 * @author ���������
 *
 */
public class FileInfo {

	//private String fileName;
	//private long fileLength;
	private Properties p;
	private String userName;
	/**
	 * ���캯�����򿪻򴴽��û��ռ������ļ�
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
	 * ���öϵ�
	 * @param fileName
	 * @param point
	 * @param servername
	 */
	public void setUpPoint(String fileName,long point,String servername){
		p.setProperty(fileName+"_up_point", point+","+servername);
	}
	/**
	 * ��ȡ�ϵ�
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
	 * ��ȡ�ϵ����ڴ洢�ڵ�����
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
	 * �����ļ���¼���û��ռ䣬�Զ�����UUID����¼�ļ����Ⱥ�����λ����Ϣ
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
	 * ���������ļ�����
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
	 * �����ļ������ң���������λ�ô洢�ڵ����������
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
	 * �����ļ���UUID
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
	 * ɾ���û��ļ���¼
	 * @param fileName
	 */
	public void removeFile(String fileName){
		p.setProperty(fileName, "");
		p.setProperty(fileName+"_up_point", "");
	}
	/**
	 * �����û����ϴ��ļ���Ϣ���ı��ļ������������ϴ��еĶϵ㣩
	 * <br><br><dd>�ͻ��˶Ͽ�ʱ����
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
