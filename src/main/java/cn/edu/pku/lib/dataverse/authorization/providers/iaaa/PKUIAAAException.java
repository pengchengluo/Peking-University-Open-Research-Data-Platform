/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.authorization.providers.iaaa;

/**
 *
 * @author luopc
 */
public class PKUIAAAException extends RuntimeException{
    
    private static final long serialVersionUID = -6379787170427198928L;
    
    public PKUIAAAException() {
        super();
    }

    public PKUIAAAException(String message) {
        super(message);
    }

    public PKUIAAAException(String message, Throwable cause) {
        super(message, cause);
    }

    public PKUIAAAException(Throwable cause) {
        super(cause);
    }

    protected PKUIAAAException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
