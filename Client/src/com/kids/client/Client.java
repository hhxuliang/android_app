package com.kids.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.way.chat.common.bean.CommonMsg;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;

/**
 * 客户端
 * 
 * @author way
 * 
 */
public class Client {

	private Socket client = null ;
	private ClientThread clientThread = null;
	private String ip;
	private int port;

	public Client(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public void stopNet() {
		if (clientThread == null || client == null)
			return;
		clientThread.stopNet();
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean start() {
		try {
			if(clientThread!=null)
				clientThread.stopNet();
			if(client!=null)
				client.close();
			
			client = new Socket();
			// client.connect(new InetSocketAddress(Constants.SERVER_IP,
			// Constants.SERVER_PORT), 3000);
			client.connect(new InetSocketAddress(ip, port), 5000);
			if (client.isConnected()) {
				// System.out.println("Connected..");
				clientThread = new ClientThread(client);
				clientThread.start();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 直接通过client得到读线程
	public ClientInputThread getClientInputThread() {
		if (clientThread == null)
			return null;
		return clientThread.getIn();
	}

	// 直接通过client得到写线程
	public ClientOutputThread getClientOutputThread() {
		if (clientThread == null)
			return null;
		return clientThread.getOut();
	}

	// 直接通过client停止读写消息
	public void setIsStart(boolean isStart) {
		clientThread.getIn().setStart(isStart);
		clientThread.getOut().setStart(isStart);
	}

	public void sendHeartBeat() {
		if (client != null && client.isConnected() && clientThread != null
				&& getClientOutputThread().isStart()) {
			CommonMsg cm = new CommonMsg();
			cm.setarg1("1");
			cm.setarg2("1");
			cm.setarg3("1");
			TranObject<CommonMsg> msg2Object = new TranObject<CommonMsg>(
					TranObjectType.HEARTBEAT);
			msg2Object.setObject(cm);
			getClientOutputThread().setMsg(msg2Object);
		}
	}

	public class ClientThread extends Thread {

		private ClientInputThread in;
		private ClientOutputThread out;

		public ClientThread(Socket socket) {
			in = new ClientInputThread(socket);
			out = new ClientOutputThread(socket);
		}

		public void run() {
			in.setStart(true);
			out.setStart(true);
			in.start();
			out.start();
		}

		// 得到读消息线程
		public ClientInputThread getIn() {
			return in;
		}

		// 得到写消息线程
		public ClientOutputThread getOut() {
			return out;
		}

		public void stopNet() {
			in.stopNet();
			out.stopNet();

		}
	}
}
