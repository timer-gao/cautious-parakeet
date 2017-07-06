package com.gao.myhomework.fileclient;

import java.net.*;
import java.io.*;

/**
 * �ͻ��˳���
 * 
 * @author ���������
 * 
 */
public class FTClient {

	Socket s = null;
	DataInputStream dis = null;
	DataOutputStream dos = null;

	String[] args = null;

	/**
	 * ��������������ӣ�ʵ��Э�飬�ṩ����
	 * 
	 * @param server
	 * @param port
	 * @throws Exception
	 */
	public void start(String server, int port) throws Exception {
		s = establish(server, port);
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());
		System.out.println("please input your username:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		dos.writeUTF(br.readLine());
		while (true) {
			args[0] = new String(br.readLine());		//��ȡ����
			try {
				if (args[0].equals("get")) {   			//����
					try {
						download(br.readLine());
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("����ʧ�ܣ�");
					}
				} else if (args[0].equals("put")) {		//�ϴ�
					args[1] = new String(br.readLine());
					File f = new File(args[1]);
					if (f.isFile()) {
						upload(args[1]);
					} else if (f.isDirectory()) {
						String[] filenames = f.list();
						if (filenames.length == 0) {
							s.close();
							System.out
									.println("no files available in the directory");
							continue;
						}
						for (int i = 0; i < filenames.length; i++) {
							System.out.println(i + 1 + "\t\t" + filenames[i]);
						}
						System.out.print("please input your choice:");

						String c = br.readLine();

						if (c.equalsIgnoreCase("q")) {		//�˳�ѡ��
							s.close();
							continue;
						}

						if (c.equalsIgnoreCase("a")) {		//ȫѡ
							for (int i = 0; i < filenames.length; i++) {
								String dir = f.getCanonicalPath();
								String tf = null;
								if (dir.endsWith(File.separator)) {
									tf = dir + filenames[i];
								} else {
									tf = dir + File.separator + filenames[i];
								}
								if (new File(tf).isDirectory())
									continue;
								upload(tf);
							}
							continue;
						}
						int choice = 0;
						try {
							choice = Integer.parseInt(c);
						} catch (NumberFormatException e) {
							System.out.println("your input is wrong");
							continue;
						}

						if (choice >= 1 && choice <= filenames.length) {
							String dir = f.getCanonicalPath();
							if (dir.endsWith(File.separator)) {
								upload(dir + filenames[choice - 1]);
							} else {
								upload(dir + File.separator
										+ filenames[choice - 1]);
							}

						} else {
							System.out.println("your input is wrong");
							continue;
						}

					} else {

						System.out.println(args[1] + " not exists");
						continue;
					}

				} else if (args[0].equals("remove")) {		//ɾ��
					try {
						remove(br.readLine());
						continue;
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("ɾ��ʧ�ܣ�");
					}
				} else if (args[0].equals("rename")) {		//����

					try {

						rename(br.readLine(), br.readLine());
						continue;

					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("����ʧ�ܣ�");
					}
				} else {
					System.out.println("Usage:");
					System.out.println("get afile");
					System.out.println("put afile");
					System.out.println("remove afile");
					System.out.println("rename file1 to file2");
				}

			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println("Usage:");
				System.out.println("get");
				System.out.println("put afile");
				System.out.println("remove");
				System.out.println("rename");
			}
		}
	}
	/**
	 * ����socket����
	 * @param server
	 * @param port
	 * @return socket -Socket
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

	/**
	 * �ϴ�Э��
	 * @param filename
	 * @throws Exception
	 */
	public void upload(String filename) throws Exception {

		File f = new File(filename);

		if (!f.exists() || !f.isFile()) {
			System.out
					.println("it's wrong, maybe it is not a file or not exists");
			return;
		}

		byte[] buffer = new byte[4096];
		int rr = 0;

		dos.writeInt(1);
		dos.writeUTF(f.getName());
		dos.writeLong(f.length());
		dos.flush();

		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);

		long point = dis.readLong();
		if (point == f.length()) {
			System.out.println("\"" + filename + "\"�Ѵ���");
			return;
		}

		final boolean[] isNeedReupload = new boolean[1];
		Thread tr = new Thread() {
			public void run() {
				try {
					String s = dis.readUTF();
					System.out.println(s);
					if (!s.equals("��������Ϣ���ϴ��ɹ���"))
						isNeedReupload[0] = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		tr.start();
		for (int i = 0; i < point; i++)
			bis.read(buffer);
		while (!isNeedReupload[0] && (rr = bis.read(buffer)) != -1) {
			
			dos.write(buffer, 0, rr);
			dos.flush();
		}

		bis.close();
		fis.close();
		System.out.println("�����ϴ�����");
		tr.stop();
	}

	/**
	 * ����Э��
	 * @param filename
	 * @throws Exception
	 */
	public void download(String filename) throws Exception {
		dos.writeInt(2);
		dos.writeUTF(filename);
		dos.flush();

		filename = dis.readUTF();
		long len = dis.readLong();

		System.out.println(filename);
		byte[] buffer = new byte[4096];
		long r = 0;
		int rr = 0;

		FileOutputStream fos = new FileOutputStream(new File("E:\\"+filename+"(1)"));
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		while (r < len) {
			if (len - r >= buffer.length) {
				rr = dis.read(buffer, 0, buffer.length);
			} else {
				rr = dis.read(buffer, 0, (int) (len - r));
			}

			r = r + rr;
			
			bos.write(buffer, 0, rr);
		}

		System.out.println("���ؽ���");
		
		bos.close();
		fos.close();

	}

	/**
	 * ɾ��Э��
	 * @param filename
	 * @throws Exception
	 */
	public void remove(String filename) throws Exception {
		dos.writeInt(4);
		dos.writeUTF(filename);
		dos.flush();

		System.out.println(dis.readUTF());
		System.out.println(dis.readUTF());
	}

	/**
	 * ����Э��
	 * @param filename
	 * @param newFileName
	 * @throws Exception
	 */
	public void rename(String filename, String newFileName) throws Exception {
		dos.writeInt(5);
		dos.writeUTF(filename);
		dos.writeUTF(newFileName);
		dos.flush();

		System.out.println(dis.readUTF());
		System.out.println(dis.readUTF());

	}

	/**
	 * main�������������
	 * @param args ip port
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		{
			System.out.println("Usage:");
			System.out.println("java FTClient host get");
			System.out.println("java FTClient host put afile");
			// System.exit(0);

		}
		FTClient ftc = new FTClient();
		ftc.args = new String[2];
		ftc.start(args[0], Integer.parseInt(args[1]));

	}
}
