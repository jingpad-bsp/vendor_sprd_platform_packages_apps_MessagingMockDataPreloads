package com.sprd.preload;

/**
 * Created by elena.guo on 2016/2/5.
 */
public interface IPrepare {
    int   begin(Object obj);
    int   process(Object obj);
    int   end(Object obj);
}
