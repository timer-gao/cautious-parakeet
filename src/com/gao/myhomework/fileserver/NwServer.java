package com.gao.myhomework.fileserver;

import java.net.*;

/*
	ʵ������ͨ�ţ����Է������κ�Ӧ�ã�û���ṩЭ�飬
	Ҳ����˵NwServer�����������κ�Э�顣
*/

public class NwServer
{
	private int port = 4321;  //Ĭ�϶˿�
	
	public NwServer(IOStrategy io, int port) throws Exception {
		//��ֻ������ܿͻ��˵��������󣬽������罨����socket���ӣ�
		//Ȼ�������ύ��Э�鴦������
		
		this.port = port;

		ServerSocket server = new ServerSocket(port);
		System.out.println("FTServer is ready");

		
		//���ܿͻ�������
		while(true)
		{
			Socket socket = server.accept();
			InetAddress ia = socket.getInetAddress();
			System.out.print(ia.getHostName() + "(" + ia.getHostAddress() + ") connected."); 		
			try{io.service(socket);}
			catch(Exception e){
				FileStorageManager.saveServerInformationToProperties();
				e.printStackTrace();
			}
		}
	}
}