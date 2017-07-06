package com.gao.myhomework.filestorage;

import java.net.Socket;
import java.io.*;
import java.util.*;

/**
 * 传输协议实现类，服务用户，向客户端提供统一接口，解析客户端命令并判断， 负责维护用户空间记录和存储节点信息，并与后端存储节点通信获取服务
 * 
 * @author 天空蓝：）
 * 
 */
public class FTProtocol implements IOStrategy {

	@Override
	public void service(Socket socket) {
		String client = socket.getInetAddress().getHostName() + "(" + socket.getInetAddress().getHostAddress() + ")";

		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			DataInputStream dis = new DataInputStream(is);
			DataOutputStream dos = new DataOutputStream(os);

			String filename = null;
			long len = 0;
			byte[] buffer = new byte[4096];
			long r = 0;
			int rr = 0;

			while (true) {
				int command = dis.readInt();
				switch (command) {
				case 1: // file upload
					filename = dis.readUTF();
					len = dis.readLong();
					FileOutputStream fos = new FileOutputStream(new File(
							FTServer.root_folder, filename));
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					r = 0;
					rr = 0;

					while (r < len) {
						if (len - r >= buffer.length) {
							rr = dis.read(buffer, 0, buffer.length);
						} else {
							rr = dis.read(buffer, 0, (int) (len - r));
						}

						r = r + rr;
						bos.write(buffer, 0, rr);
					}

					bos.close();
					fos.close();
					break;
				case 2: // file download
					filename = dis.readUTF();
					dos.writeUTF(filename);
					File t = new File(FTServer.root_folder, filename);
					dos.writeLong(t.length());
					dos.flush();
					FileInputStream fis = new FileInputStream(t);
					BufferedInputStream bis = new BufferedInputStream(fis);

					while ((rr = bis.read(buffer)) != -1) {
						dos.write(buffer, 0, rr);
						dos.flush();
					}
					
					bis.close();
					fis.close();
					break;

				case 3: // list files
					String[] files = FTServer.root_folder.list();
					List<String> list = new LinkedList<String>();
					for(int i=0;i<files.length;i++) {
						if(new File(FTServer.root_folder, files[i]).isDirectory()) continue;
						list.add(files[i]);
					}
					
					files = list.toArray(new String[0]);
					
					dos.writeInt(files.length);
					dos.flush();
					for (int i = 0; i < files.length; i++) {
						dos.writeUTF(files[i]);
					}
					dos.flush();
					break;
				case 4: // remove files
					filename = dis.readUTF();					
					try{
						File removeFile = new File(FTServer.root_folder, filename);
						if(removeFile.delete())
							dos.writeUTF(filename+" removed success!");
						else
							dos.writeUTF(filename+" remove failed!");
						dos.flush();
						break;
					}
					catch(IOException e){
						dos.writeUTF(e.getMessage());
						dos.flush();
						break;
					}
				case 5: // rename files
					filename = dis.readUTF();					
					try{
						File reNameFile = new File(FTServer.root_folder,filename);
						String newName=dis.readUTF();
						if(reNameFile.renameTo(new File(FTServer.root_folder+"\\"+newName)))
							dos.writeUTF(filename+" renamed success!");
						else
							dos.writeUTF(filename+" renamed failed!");
						dos.flush();
						break;
					}
					catch(IOException e){
						dos.writeUTF(e.getMessage());
						dos.flush();
						break;
					}
				}
			}
		} catch (Exception e) {
			if (e instanceof EOFException) {
				System.out.println(client + " disconnected");
			} else {
				e.printStackTrace();
			}

		}
	}
}
