package com.gao.myhomework.fileserver;

import java.net.*;
import java.io.*;

import com.gao.myhomework.filestorage.FTServer;
/**
 * 服务器客户端类部分，负责与后端存储节点通信（转发流）
 * @author 天空蓝：）
 *
 */
public class FTClient {

	Socket s = null;
	
	PipedInputStream in = null;
	PipedOutputStream out = null;

	InputStream is=null;
	OutputStream os=null;
	/**
	 * 转发协议，转发流
	 * @param server
	 * @param port
	 * @throws Exception
	 */
	public void start(String server, int port) throws Exception {
		s = establish(server, port);
		is = s.getInputStream();
		os = s.getOutputStream();
		DataInputStream dis=new DataInputStream(is);
		DataOutputStream dos=new DataOutputStream(os);
		DataInputStream din=new DataInputStream(in);
		DataOutputStream dout=new DataOutputStream(out);
		int command=din.readInt();
		switch(command){
		case 1://upload
		new Thread(){
			public void run(){
				int r=0;
				try{
				while((r=is.read())!=-1)
					out.write(r);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}.start();
		dos.writeInt(command);
		dos.writeUTF(din.readUTF());
		long len=din.readLong();
		dos.writeLong(len);
		dos.flush();
		byte buffer[]=new byte[4096];
		int r = 0;
		int rr = 0;

		while (r < len) {
			if (len - r >= buffer.length) {
				rr = in.read(buffer, 0, buffer.length);
			} else {
				rr = in.read(buffer, 0, (int) (len - r));
			}

			r = r + rr;
			os.write(buffer, 0, rr);
		}	
		os.flush();
		break;
		case 2://download
			dos.writeInt(command);
			dos.writeUTF(din.readUTF());
			dos.flush();
			dout.writeUTF(dis.readUTF());
			len=dis.readLong();
			dout.writeLong(len);
			dout.flush();
			buffer=new byte[4096];
			r = 0;
			rr = 0;

			while (r < len) {
				if (len - r >= buffer.length) {
					rr = is.read(buffer, 0, buffer.length);
				} else {
					rr = is.read(buffer, 0, (int) (len - r));
				}

				r = r + rr;
				out.write(buffer, 0, rr);
				out.flush();
			}	
			
			break;
		case 3://list 服务器可用，不提供给客户端，用于查询存储节点中的文件信息
			dos.writeInt(command);
			len=dis.readInt();
			dout.writeInt((int) len);
			dout.flush();
			for(int i=0;i<len;i++){
				dout.writeUTF(dis.readUTF());
			}
			dout.flush();
			break;
		case 4://remove 
			dos.writeInt(command);
			dos.writeUTF(din.readUTF());
			dos.flush();
			dout.writeUTF(dis.readUTF());
			dout.flush();
			break;
		case 5://rename
			dos.writeInt(command);
			dos.writeUTF(din.readUTF());
			dos.writeUTF(din.readUTF());
			dos.flush();
			dout.writeUTF(dis.readUTF());
			dout.flush();
			break;
	}
		
}
	/**
	 * 建立socket连接
	 * @param server
	 * @param port
	 * @return socket
	 */
	public Socket establish(String server, int port) {
		try {
			Socket s = new Socket(server, port);
			return s;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}