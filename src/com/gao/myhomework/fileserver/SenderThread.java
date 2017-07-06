package com.gao.myhomework.fileserver;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
/**
 * 客户端部分发送线程支持
 * @author 天空蓝：）
 *
 */
public class SenderThread extends Thread {
	
		private PipedInputStream in = null;
		private PipedOutputStream out = null;
		private String ip;
		private int port;
		private Socket s=null;	
		public int state=0;//获取线程运行状态，0：运行中  1：完成   -1：异常中断
		/**
		 * 构造函数
		 * @param ip
		 * @param port
		 */
		public SenderThread(String ip,int port){
			this.ip=ip;
			this.port=port;
		}
		/**
		 * 获取输入管道流
		 * @return in -PipedInputStream
		 */
		public PipedInputStream getPipedInputputStream()
		{
			in = new PipedInputStream();
		    return in;
		}
		/**
		 * 获取输出管道流
		 * @return out -PipedOutputStream
		 */
		public PipedOutputStream getPipedOutputStream()
		{
			out=new PipedOutputStream();
			return out;
		}
		/**
		 * 关闭socket
		 * @throws IOException
		 */
		public void closeSoket() throws IOException{
			s.close();
		}
		@Override
		public void run(){
			FTClient ftc = new FTClient();
			ftc.in=in;
			ftc.out=out;
			s=ftc.s;
			try {
				ftc.start(ip,port);
				state=1;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				state=-1;
			}
			
		}
	
}
