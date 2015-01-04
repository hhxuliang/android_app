package com.kids.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

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
	private Vector object = new Vector();

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
		if(isStart==false)
			return;
		synchronized (this) {
			if (msg.getType() == TranObjectType.MESSAGE) {
				for (int i = 0; i < object.size(); i++) {
					if (msg.getType() == TranObjectType.MESSAGE
							&& ((TextMessage) ((TranObject) object.get(i))
									.getObject()).getDatekey().equals(
									((TextMessage) msg.getObject())
											.getDatekey()))
						return;
				}

			}

			object.add(msg);
			notify();
		}
	}

	public void stopNet() {
		
		if (isStart) {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		isStart = false;
	}

	@Override
	public void run() {
		boolean close = false;
		try {
			while (isStart) {
				synchronized (this) {
					Iterator i = object.iterator(); // Must be in synchronized
													// block
					while (i.hasNext()) {
						TranObject msg = (TranObject) i.next();
						oos.writeObject(msg);
						oos.flush();
						oos.reset();

						if (msg.getType() == TranObjectType.LOGOUT) {// 如果是发送下线的消息，就直接跳出循环
							close = true;
							break;
						}
					}
					object.removeAllElements();
					if (close == true)
						break;
					wait();// 发送完消息后，线程进入等待状态
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stopNet();
	}

}
