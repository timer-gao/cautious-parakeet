package com.gao.myhomework.fileserver;

import java.util.*;
import java.io.*;

/**
 * 服务器类
 * 
 * @author 天空蓝：）
 * 
 */
public class FTServer {

	public static File share = null;

	public static void main(String[] args) throws Exception {

		int port = 4321;

		// 读取配置文件
		Properties p = new Properties();
		p.load(FTServer.class.getClassLoader().getResourceAsStream(
				"server.properties"));

		share = new File(p.getProperty("share"));
		System.out.println(p.getProperty("share"));
		if (!share.isDirectory()) {
			System.out
					.println("share directory not exists or isn't a directory");
			System.exit(-4);
		}

		port = Integer.parseInt(p.getProperty("port"));

		FileStorageManager.initSetServerInformation();

		//创建实例
		try {
			FTProtocol protocol = new FTProtocol();
			AdvancedSupport as = new AdvancedSupport(protocol);
			NwServer nw = new NwServer(as, port);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			FileStorageManager.saveServerInformationToProperties();
		}
	}

}
