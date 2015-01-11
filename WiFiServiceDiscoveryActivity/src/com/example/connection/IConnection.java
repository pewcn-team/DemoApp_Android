package com.example.connection;

public interface IConnection {
	enum ConnectionState
	{
		INITITAL,
		SEEK_PEER,
		CONNECTED,
		TIMEOUT,
		DISCONNECT,
		REST;
	};
	public interface IOnStateChangeListener
	{
		public void onStateChange(ConnectionState state);
	}
	
	public void initial();
	/**
	 * 搜索连接对象
	 */
	public void seekPeer();
	
	/**
	 * 连接时不再依赖何种连接方式，继承类设置好连接目标，调用connect即可以连接
	 */
	public void connect();
	
	/**
	 * 当连接超时的时候，会调用这里
	 */
	public void timeout();
	
	/**
	 * 断开连接
	 */
	public void disconnect();
	
	/**
	 * 重置状态
	 */
	public void reset();
	
	/**注册状态监听器
	 * @param listener
	 */
	public void registerOnStateChangeListener(IOnStateChangeListener listener);

}
