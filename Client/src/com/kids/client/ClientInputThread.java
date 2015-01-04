package com.kids.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import com.way.chat.common.tran.bean.TranObject;

/**
 * 客户端读消息线程
 * 
 * @author way
 * 
 */
public class ClientInputThread extends Thread {
	private Socket socket;
	private TranObject msg;
	private boolean isStart = true;

	public boolean isStart() {
		return isStart;
	}

	private ObjectInputStream ois;
	private MessageListener messageListener;// 消息监听接口对象

	public ClientInputThread(Socket socket) {
		this.socket = socket;
		try {
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopNet() {

		if (isStart) {
			try {
				ois.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		isStart = false;
	}

	/**
	 * 提供给外部的消息监听方法
	 * 
	 * @param messageListener
	 *            消息监听接口对象
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	@Override
	public void run() {
		try {
			while (isStart) {
				msg = (TranObject) ois.readObject();
				// 每收到一条消息，就调用接口的方法，并传入该消息对象，外部在实现接口的方法时，就可以及时处理传入的消息对象了
				// 我不知道我有说明白没有？
				messageListener.Message(msg);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		stopNet();
	}
}
