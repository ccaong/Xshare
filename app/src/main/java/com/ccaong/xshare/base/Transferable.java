package com.ccaong.xshare.base;

/**
 * @author devel
 */
public interface Transferable {

    /**
     * @throws Exception
     */
    void init() throws Exception;


    /**
     * @throws Exception
     */
    void parseHeader() throws Exception;


    /**
     * @throws Exception
     */
    void parseBody() throws Exception;


    /**
     * @throws Exception
     */
    void finish() throws Exception;
}
