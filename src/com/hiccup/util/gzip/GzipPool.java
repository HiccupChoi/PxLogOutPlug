package com.hiccup.util.gzip;

import com.hiccup.tools.DataPool;
import com.hiccup.util.IOTool;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author chen
 */
public class GzipPool {

    private final DataPool<GZIPOutput> gout;
    private final DataPool<GZIPInput> gin;
    private boolean end = false;

    public GzipPool(int maxSize) {
        gout = new DataPool<GZIPOutput>(false, new GZIPOutput[maxSize]) {
            @Override
            protected GZIPOutput newInstance() {
                return new GZIPOutput();
            }
        };
        gin = new DataPool<GZIPInput>(false, new GZIPInput[maxSize]) {
            @Override
            protected GZIPInput newInstance() {
                return new GZIPInput();
            }
        };
    }

    public GZIPOutput getOut(OutputStream ao) {
        if (end)
        {
            return null;
        }
        GZIPOutput out = this.gout.getData();
        if (out != null)
        {
            try
            {
                out.setOut(ao);
            } catch (Throwable e)
            {
                out.setBusy(false);
                return null;
            }
            return out;
        }
        return null;
    }

    public synchronized GZIPInput geIn(InputStream in) {
        if (end)
        {
            return null;
        }
        GZIPInput gzin = this.gin.getData();
        if (gzin != null)
        {
            try
            {
                gzin.setIn(in);
            } catch (Throwable e)
            {
                gzin.setBusy(false);
                return null;
            }
            return gzin;
        }
        return null;
    }


}
