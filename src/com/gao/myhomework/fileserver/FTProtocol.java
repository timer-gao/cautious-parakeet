package com.gao.myhomework.fileserver;

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
		String client = socket.getInetAddress().getHostName() + "("
				+ socket.getInetAddress().getHostAddress() + ")";
		FileInfo fileInfo = null;
		try {
			// 成员变量

			// 客户端流变量
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			DataInputStream dis = new DataInputStream(is);
			DataOutputStream dos = new DataOutputStream(os);

			// 服务器部分客户端1
			SenderThread senderthread1 = null;
			PipedInputStream pi1 = null;
			PipedInputStream pii1 = null;
			PipedOutputStream po1 = null;
			PipedOutputStream poo1 = null;
			DataOutputStream dpo1 = null;
			DataInputStream dpi1 = null;

			boolean isSever1Connected = true;

			// 服务器部分客户端2
			SenderThread senderthread2 = null;
			PipedInputStream pi2 = null;
			PipedInputStream pii2 = null;
			PipedOutputStream po2 = null;
			PipedOutputStream poo2 = null;
			DataOutputStream dpo2 = null;
			DataInputStream dpi2 = null;

			boolean isSever2Connected = true;

			// 临时变量
			String filename = null;
			String storagefilename=null;
			long len = 0;
			byte[] buffer = new byte[4096];
			long r = 0;
			int rr = 0;
			StorageNode[] server = null;

			// 创建用户空间记录实例
			fileInfo = new FileInfo(dis.readUTF());

			while (true) {
				int command = dis.readInt();
				switch (command) {
				case 1: // file upload
					filename = dis.readUTF();
					len = dis.readLong();

					// 判断文件在服务器中的信息，有则续传，否则从找出两个可用节点

					long point = fileInfo.getUpPoint(filename);
					dos.writeLong(point);
					if (point == len)
						{
						dos.writeUTF("文件已存在！");
							break;
						}
					String upPointServer = fileInfo.getUpPointServer(filename);

					if (point != 0) {
						server = new StorageNode[1];
						server[0] = FileStorageManager
								.getStorageNodeByName(upPointServer);
					} else
						server = FileStorageManager.getUploadServerInfo(
								filename, len);

					fileInfo.addFile(filename, len, server,true);
					// 创建客户端
					if (server != null) {
						try {
							System.out.println(server[0].getIp());
							System.out.println(server[0].getPort());
							senderthread1 = new SenderThread(server[0].getIp(),
									server[0].getPort());
							pi1 = senderthread1.getPipedInputputStream();
							po1 = new PipedOutputStream();
							dpo1 = new DataOutputStream(po1);
							po1.connect(pi1);
							senderthread1.start();

							dpo1.writeInt(command);
							dpo1.writeUTF(filename
									+ fileInfo.findUUID(filename));
							dpo1.writeLong(len);
							dpo1.flush();
						} catch (Exception e) {
							// if (point != 0)
							server[0].removeFile(len);
							isSever1Connected = false;
							e.printStackTrace();
							System.out.println("存储节点\""
									+ server[0].getServerName() + "\"连接失败");
						}

						if (server.length == 2) {
							try {
								System.out.println(server[1].getIp());
								System.out.println(server[1].getPort());
								senderthread2 = new SenderThread(
										server[1].getIp(), server[1].getPort());
								pi2 = senderthread2.getPipedInputputStream();
								po2 = new PipedOutputStream();
								dpo2 = new DataOutputStream(po2);
								po2.connect(pi2);
								senderthread2.start();

								dpo2.writeInt(command);
								dpo2.writeUTF(filename
										+ fileInfo.findUUID(filename));
								dpo2.writeLong(len);
								dpo2.flush();
							} catch (Exception e) {
								server[1].removeFile(len);
								isSever2Connected = false;
								e.printStackTrace();
								System.out.println("存储节点\""
										+ server[1].getServerName() + "\"连接失败");
							}
						} else
							isSever2Connected = false;

						r = 0;
						rr = 0;
						while (r < len
								&& (isSever1Connected || isSever2Connected)) {
							if (len - r >= buffer.length) {
								rr = dis.read(buffer, 0, buffer.length);
							} else {
								rr = dis.read(buffer, 0, (int) (len - r));
							}

							// 发送

							if (r >= point * buffer.length) {
								if (!(isSever1Connected || isSever2Connected))
									break;
								if (isSever1Connected) {
									try {
										dpo1.write(buffer, 0, rr);
										dpo1.flush();
									} catch (IOException e) {
										isSever1Connected = false;
										fileInfo.setUpPoint(filename, r,
												server[0].getServerName());
									}
								}
								if (isSever2Connected) {
									try {
										dpo2.write(buffer, 0, rr);
										dpo2.flush();
									} catch (IOException e) {
										isSever2Connected = false;
										fileInfo.setUpPoint(filename, r,
												server[1].getServerName());
									}
								}

							}

							r += rr;
						}
						if (r == len)
							dos.writeUTF("服务器消息：上传成功！");

						// 添加记录到用户空间和存储节点
						if (isSever1Connected && isSever2Connected) {
							fileInfo.setUpPoint(filename, len,
									server[0].getServerName());
							fileInfo.addFile(filename, len, server,false);
						} else if (isSever1Connected) {
							fileInfo.setUpPoint(filename, len,
									server[0].getServerName());
							StorageNode[] sn = new StorageNode[1];
							sn[0] = server[0];
							fileInfo.addFile(filename, len, sn,false);
						} else if (isSever2Connected) {
							fileInfo.setUpPoint(filename, len,
									server[1].getServerName());
							StorageNode[] sn = new StorageNode[1];
							sn[0] = server[1];
							fileInfo.addFile(filename, len, sn,false);
						} else
							dos.writeUTF("后台存储节点异常，请重试\n");

						// 关闭发送端
						try {
							dpo1.close();
							po1.close();
							pi1.close();
							senderthread1.closeSoket();
							senderthread1.stop();

							dpo2.close();
							po2.close();
							pi2.close();
							senderthread2.closeSoket();
							senderthread2.stop();
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else
						dos.writeUTF("无可用存储节点\n");
					break;

				case 2: // file download
					filename = dis.readUTF();

					//找到存储位置
					String serverName[] = fileInfo.findFile(filename);

					server = new StorageNode[serverName.length];
					for (int i = 0; i < serverName.length; i++) {
						server[i] = FileStorageManager
								.getStorageNodeByName(serverName[i]);
					}

					if (server != null) {
						try {
							System.out.println(server[0].getIp());
							System.out.println(server[0].getPort());
							senderthread1 = new SenderThread(server[0].getIp(),
									server[0].getPort());
							pi1 = senderthread1.getPipedInputputStream();
							poo1 = new PipedOutputStream();
							dpo1 = new DataOutputStream(poo1);
							poo1.connect(pi1);

							po1 = senderthread1.getPipedOutputStream();
							pii1 = new PipedInputStream();
							dpi1 = new DataInputStream(pii1);
							po1.connect(pii1);
							senderthread1.start();

							dpo1.writeInt(command);
							dpo1.writeUTF(filename
									+ fileInfo.findUUID(filename));
							dpo1.flush();
							storagefilename=dpi1.readUTF();
							len = dpi1.readLong();
							isSever2Connected=false;
						} catch (Exception e) {

							isSever1Connected = false;
							e.printStackTrace();
							System.out.println("存储节点\""
									+ server[0].getServerName() + "\"连接失败");

							if (server.length == 2) {
								System.out.println("准备第二个备份...");
								System.out.println(server[1].getIp());
								System.out.println(server[1].getPort());
								try {
									senderthread2 = new SenderThread(
											server[1].getIp(),
											server[1].getPort());
									pi2 = senderthread2
											.getPipedInputputStream();
									poo2 = new PipedOutputStream();
									dpo2 = new DataOutputStream(poo2);
									poo2.connect(pi1);

									po2 = senderthread1.getPipedOutputStream();
									pii2 = new PipedInputStream();
									dpi2 = new DataInputStream(pii2);
									po2.connect(pii2);
									senderthread2.start();

									dpo2.writeInt(command);
									dpo2.writeUTF(filename
											+ fileInfo.findUUID(filename));
									dpo2.flush();
									storagefilename=dpi1.readUTF();
									len = dpi2.readLong();
								} catch (Exception e1) {

									isSever2Connected = false;
									e1.printStackTrace();
									System.out.println("存储节点\""
											+ server[1].getServerName()
											+ "\"连接失败");
								}

							} else
								isSever2Connected = false;
						}

						dos.writeUTF(storagefilename);
						dos.writeLong(len);
						dos.flush();

						//发送
						buffer = new byte[4096];
						r = 0;
						rr = 0;
						if (isSever1Connected) {
							while (r < len) {
								if (len - r >= buffer.length) {
									rr = dpi1.read(buffer, 0, buffer.length);
								} else {
									rr = dpi1.read(buffer, 0, (int) (len - r));
								}

								r = r + rr;
								dos.write(buffer, 0, rr);
								dos.flush();
							}
							dpi1.close();
							pii1.close();
							pi1.close();
							dpo1.close();
							poo1.close();
							po1.close();
							senderthread1.closeSoket();
							senderthread1.stop();
						} else if (isSever2Connected) {
							while (r < len) {
								if (len - r >= buffer.length) {
									rr = dpi1.read(buffer, 0, buffer.length);
								} else {
									rr = dpi1.read(buffer, 0, (int) (len - r));
								}

								r = r + rr;
								dos.write(buffer, 0, rr);
								dos.flush();
							}
							dpi2.close();
							pii2.close();
							pi2.close();
							dpo2.close();
							poo2.close();
							po2.close();
							senderthread2.closeSoket();
							senderthread2.stop();
						}
					}
					break;
				case 4: // remove files
					filename = dis.readUTF();
					String preFileName = filename + fileInfo.findUUID(filename);

					serverName = fileInfo.findFile(filename);
					server = new StorageNode[serverName.length];
					for (int i = 0; i < serverName.length; i++) {
						server[i] = FileStorageManager
								.getStorageNodeByName(serverName[i]);
					}

					if (server != null) {
						try {
							System.out.println(server[0].getIp());
							System.out.println(server[0].getPort());
							senderthread1 = new SenderThread(server[0].getIp(),
									server[0].getPort());
							pi1 = senderthread1.getPipedInputputStream();
							poo1 = new PipedOutputStream();
							dpo1 = new DataOutputStream(poo1);
							poo1.connect(pi1);

							po1 = senderthread1.getPipedOutputStream();
							pii1 = new PipedInputStream();
							dpi1 = new DataInputStream(pii1);
							po1.connect(pii1);
							senderthread1.start();

							dpo1.writeInt(command);

							dpo1.writeUTF(preFileName);
							dpo1.flush();

							for (int i = 0; i < server.length; i++) {
								server[i].removeFile(fileInfo
										.findFileLength(filename));
							}
							fileInfo.removeFile(filename);

							dos.writeUTF(dpi1.readUTF());
							dos.flush();
						} catch (Exception e) {

							e.printStackTrace();
							System.out.println("存储节点\""
									+ server[0].getServerName() + "\"连接失败");
							dos.writeUTF("存储节点\"" + server[0].getServerName()
									+ "\"连接失败");
						}

						if (server.length == 2) {

							try {
								senderthread2 = new SenderThread(
										server[1].getIp(), server[1].getPort());
								pi2 = senderthread2.getPipedInputputStream();
								poo2 = new PipedOutputStream();
								dpo2 = new DataOutputStream(poo2);
								poo2.connect(pi2);

								po2 = senderthread1.getPipedOutputStream();
								pii2 = new PipedInputStream();
								dpi2 = new DataInputStream(pii2);
								po2.connect(pii2);
								senderthread2.start();

								dpo2.writeInt(command);
								dpo2.writeUTF(preFileName);
								dpo2.flush();

								dos.writeUTF(dpi2.readUTF());
								dos.flush();
							} catch (Exception e1) {

								e1.printStackTrace();
								System.out.println("存储节点\""
										+ server[1].getServerName() + "\"连接失败");
								dos.writeUTF("存储节点\""
										+ server[1].getServerName() + "\"连接失败");
							}

						} else {
							dos.writeUTF("没有找到备份");
						}
					} else {
						dos.writeUTF("不存在：" + filename);
					}
					break;

				case 5: // rename files
					filename = dis.readUTF();
					preFileName = filename + fileInfo.findUUID(filename);
					String newName = dis.readUTF();
					serverName = fileInfo.findFile(filename);
					server = new StorageNode[serverName.length];
					for (int i = 0; i < serverName.length; i++) {
						server[i] = FileStorageManager
								.getStorageNodeByName(serverName[i]);
					}

					if (server != null) {
						try {
							System.out.println(server[0].getIp());
							System.out.println(server[0].getPort());
							senderthread1 = new SenderThread(server[0].getIp(),
									server[0].getPort());
							pi1 = senderthread1.getPipedInputputStream();
							poo1 = new PipedOutputStream();
							dpo1 = new DataOutputStream(poo1);
							poo1.connect(pi1);

							po1 = senderthread1.getPipedOutputStream();
							pii1 = new PipedInputStream();
							dpi1 = new DataInputStream(pii1);
							po1.connect(pii1);
							senderthread1.start();

							dpo1.writeInt(command);

							dpo1.writeUTF(preFileName);
							

							len = fileInfo.findFileLength(filename);
							fileInfo.addFile(newName, len, server,true);
							fileInfo.setUpPoint(newName, len,
									fileInfo.getUpPointServer(filename));
							fileInfo.removeFile(filename);

							dpo1.writeUTF(newName + fileInfo.findUUID(newName));
							dpo1.flush();
							dos.writeUTF(dpi1.readUTF());
							
						} catch (Exception e) {

							isSever1Connected = false;
							e.printStackTrace();
							System.out.println("存储节点\""
									+ server[0].getServerName() + "\"连接失败");
							dos.writeUTF("存储节点\"" + server[0].getServerName()
									+ "\"连接失败");
						}

						if (server.length == 2) {

							try {
								senderthread2 = new SenderThread(
										server[1].getIp(), server[1].getPort());
								pi2 = senderthread2.getPipedInputputStream();
								poo2 = new PipedOutputStream();
								dpo2 = new DataOutputStream(poo2);
								poo2.connect(pi2);

								po2 = senderthread2.getPipedOutputStream();
								pii2 = new PipedInputStream();
								dpi2 = new DataInputStream(pii2);
								po2.connect(pii2);
								senderthread2.start();

								dpo2.writeInt(command);
								dpo2.writeUTF(preFileName);
								dpo2.writeUTF(newName
										+ fileInfo.findUUID(newName));
								dpo2.flush();
								dos.writeUTF(dpi2.readUTF());
								dos.flush();
							} catch (Exception e1) {

								isSever2Connected = false;
								e1.printStackTrace();
								System.out.println("存储节点\""
										+ server[1].getServerName() + "\"连接失败");
								dos.writeUTF("存储节点\""
										+ server[1].getServerName() + "\"连接失败");
							}

						} else {
							dos.writeUTF("没有找到备份");
						}
					} else {
						
						dos.writeUTF("不存在：" + filename);
						dos.writeUTF("不存在：" + filename);
					}
					break;

				}
				//变量重新初始化
				senderthread1 = null;
				pi1 = null;
				pii1 = null;
				po1 = null;
				poo1 = null;
				dpo1 = null;
				dpi1 = null;

				isSever1Connected = true;

				senderthread2 = null;
				pi2 = null;
				pii2 = null;
				po2 = null;
				poo2 = null;
				dpo2 = null;
				dpi2 = null;

				isSever2Connected = true;

				filename = null;
				storagefilename=null;
				len = 0;
				buffer = new byte[4096];
				r = 0;
				rr = 0;
				server = null;
			}
		} catch (Exception e) {
			if (e instanceof EOFException) {
				fileInfo.savaFileInfo();//保存用户空间到文件
				System.out.println(client + " disconnected");
			} else {
				fileInfo.savaFileInfo();//保存用户空间到文件
				try {
					FileStorageManager.saveServerInformationToProperties();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}

		}
	}
}
