package com.gao.myhomework.fileserver;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
/**
 * �ͻ��˲��ַ����߳�֧��
 * @author ���������
 *
 */
public class SenderThread extends Thread {
	
		private PipedInputStream in = null;
		private PipedOutputStream out = null;
		private String ip;
		private int port;
		private Socket s=null;	
		public int state=0;//��ȡ�߳�����״̬��0��������  1�����   -1���쳣�ж�
		/**
		 * ���캯��
		 * @param ip
		 * @param port
		 */
		public SenderThread(String ip,int port){
			this.ip=ip;
			this.port=port;
		}
		/**
		 * ��ȡ����ܵ���
		 * @return in -PipedInputStream
		 */
		public PipedInputStream getPipedInputputStream()
		{
			in = new PipedInputStream();
		    return in;
		}
		/**
		 * ��ȡ����ܵ���
		 * @return out -PipedOutputStream
		 */
		public PipedOutputStream getPipedOutputStream()
		{
			out=new PipedOutputStream();
			return out;
		}
		/**
		 * �ر�socket
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
