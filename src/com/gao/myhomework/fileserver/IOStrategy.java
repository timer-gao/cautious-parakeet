package com.gao.myhomework.fileserver;

import java.net.*;

/**
 * �ṩЭ����Զ���
 * @author ���������
 *
 */

public interface IOStrategy
{
	public void service(Socket socket);
}
