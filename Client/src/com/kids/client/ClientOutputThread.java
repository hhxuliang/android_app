package com.kids.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

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
	private ArrayList<TranObject> object = new ArrayList<TranObject>();

	public boolean isStart() {
		return isStart;
	}

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
		object.add(msg);
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
		boolean close = false;
		try {
			while (isStart) {
				if (object.size() > 0) {
					for (TranObject msg : object) {
						oos.writeObject(msg);
						oos.flush();
						oos.reset();
						object.remove(msg);
						
						if (msg.getType() == TranObjectType.LOGOUT) {// 如果是发送下线的消息，就直接跳出循环
							close = true;
							break;
						}
					}
					if (close == true)
						break;
				}
				synchronized (this) {
					wait();// 发送完消息后，线程进入等待状态
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
