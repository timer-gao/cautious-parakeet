package com.gao.myhomework.filestorage;

import java.util.*;
import java.io.*;

/**
 * 服务器类
 * @author 天空蓝：）
 *
 */
public class FTServer {
	
	public static File root_folder = null;
	public static long volume=0;
	
	public static void main(String[] args) throws Exception  {
		
		int port = 4321;
		
		//判断配置文件命名规则
		if(args.length!=1||!(args[0].startsWith("storage")&&args[0].endsWith(".properties")))
		{
			System.out.println("please input a property file as \"storage*.properties\"");
			System.exit(-3);
		}
		
		Properties p = new Properties();
		p.load(FTServer.class.getClassLoader().getResourceAsStream(args[0]));
		
		//解析容量
		String s=p.getProperty("volume");
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
		
		//解析根路径
		root_folder = new File(p.getProperty("root_folder"));
		System.out.println(p.getProperty("root_folder"));
		if(!root_folder.isDirectory()) {
			System.out.println("root_folder directory not exists or isn't a directory");
			System.exit(-4);
		}
		
		port = Integer.parseInt(p.getProperty("port"));
		
		//实例化服务器
		FTProtocol protocol = new FTProtocol();
		AdvancedSupport as = new AdvancedSupport(protocol);
		NwServer nw = new NwServer(as,port);
		
	}

}
