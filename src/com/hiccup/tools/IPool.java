package com.hiccup.tools;

import java.io.Closeable;

/**
 * 可池化对象. <br>
 * @author chen
 */
public interface IPool extends Closeable{

    boolean isBusy();

    void setBusy(boolean busy);
    
}
