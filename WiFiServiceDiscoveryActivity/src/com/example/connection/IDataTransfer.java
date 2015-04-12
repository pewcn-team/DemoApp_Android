package com.example.connection;

/**
 * Created by yihao on 15/4/8.
 * 用于手机端和设备端数据传输，当手机和设备连接成功后，可以建立Transfer。然后，调用{@link IDataTransfer#startTransfer}来开启数据传输线程，
 * 不用时或者连接断开时，要调用{@link IDataTransfer#stopTransfer}.
 * 可以实现{@link ITransferListener}并注册侦听器{@link IDataTransfer#registerTransferListener}来侦听IDataTransfer是否创建成功，和对方发过来的数据。
 */
public interface IDataTransfer {
    /**
     * 用来侦听数据传输是否建立和收到的数据
     */
    public static interface ITransferListener
    {
        /**
         * 当socket建立时被调用
         */
        public void onCreate();

        /**
         * 当收到数据时被调用
         * @param data 数据，字节流
         */
        public void onDataReceived(byte[] data);

    }

    /**
     * 启动transfer,会开启一个数据收发的线程
     */
    public void startTransfer();

    /**
     * 发送数据
     * @param data
     */
    public void sendData(byte[] data);

    /**
     * 停止transfer,停止数据收发的线程
     */
    public void stopTransfer();

    /**
     * 注册侦听器
     * @param listener
     */
    public void registerTransferListener(ITransferListener listener);

    /**
     * 注销侦听器
     * @param listener
     */
    public void unregisterTransferListener(ITransferListener listener);


}
