package com.gao.myhomework.fileserver;

import java.net.*;

/**
 * 提供协议策略定义
 * @author 天空蓝：）
 *
 */

public interface IOStrategy
{
	public void service(Socket socket);
}
