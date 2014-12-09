package com.kids.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.kids.activity.chat.GetMsgService;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;

/**
 * 客户端写消息线程
 * 
 * @author way
 * 
 */
public class ClientOutputThread extends Thread {
	private Socket socket;
	private ObjectOutputStream oos;
	private boolean isStart = true;

	public boolean isStart() {
		return isStart;
	}

	private TranObject msg;

	public ClientOutputThread(Socket socket) {
		this.socket = socket;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	// 这里处理跟服务器是一样的
	public void setMsg(TranObject msg) {
		this.msg = msg;
		synchronized (this) {
			notify();
		}
	}

	public void stopNet() {
		isStart = false;
		try {
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		try {
			while (isStart) {
				if (msg != null) {
					oos.writeObject(msg);
					oos.flush();
					oos.reset();
					if (msg.getType() == TranObjectType.LOGOUT) {// 如果是发送下线的消息，就直接跳出循环
						break;
					}
					if (msg.getType() == TranObjectType.MESSAGE) {// 如果是发送下线的消息，就直接跳出循环
						GetMsgService.application.updateDBbyMsgOk(
								((TextMessage) msg.getObject()).getMessage(),
								msg.getToUser());
					}
					synchronized (this) {
						wait();// 发送完消息后，线程进入等待状态
					}
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			oos.close();// 循环结束后，关闭输出流和socket
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isStart = false;
	}

}
