package com.hiccup.tools;



import java.io.Closeable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据池. <br>
 * @author chen
 */
public abstract class DataPool<T extends IPool> implements Closeable, Runnable {

    private final static String TAG = "DataPool";
    private final ReentrantLock lock;
    private final T[] datas;
    private final int maxSize;
    /**
     * 满时是否返回新对象
     */
    private final boolean fullNewInstance;
    private final String name;
    /**
     * 是否所有对象都在使用中
     */
    private boolean full = false;

    public DataPool(String name, boolean fullNewInstance, T[] datas) {
        this.maxSize = datas.length;
        this.datas = datas;
        this.name = name;
        this.fullNewInstance = fullNewInstance;
        lock = new ReentrantLock();
    }

    public T getData() {
        lock.lock();
        try
        {
            for (int i = 0; i < maxSize; i++)
            {
                T t = datas[i];
                if (t == null)
                {
                    t = newInstance();
                    datas[i] = t;
                    t.setBusy(true);
                    full = false;
                    return t;
                } else if (!t.isBusy())
                {
                    t.setBusy(true);
                    full = false;
                    return t;
                }
            }
        } finally
        {
            lock.unlock();
        }
        full = true;
        return fullNewInstance ? newInstance() : null;
    }

    @Override
    public void close() {

    }

    public T[] getDatas() {
        return datas;
    }

    @Override
    public void run() {
        close();
    }

    protected abstract T newInstance();

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

}
