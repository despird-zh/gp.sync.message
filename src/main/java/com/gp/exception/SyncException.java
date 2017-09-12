package com.gp.exception;


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.gp.core.DictionaryFacade;
import com.gp.exception.BaseException;
import com.gp.validate.ValidateMessage;

/**
 * CoreException wrap the exception occurs in core package.
 * It support the database side message pattern from dictionary table.
 * 
 * @author gary diao
 * @version 0.1 2015-1-2
 * 
 **/
public class SyncException extends BaseException{

	private static final long serialVersionUID = 6149095030747094149L;
	
	private static Map<Locale, ResourceBundle> sync_bundles = new HashMap<Locale, ResourceBundle>();
	
	/**
	 * Constructor with error code and parameters
	 **/
	public SyncException(String errorcode,Object ...param){
		this(Locale.getDefault(),errorcode, param);
	}

	/**
	 * Constructor with error code, cause and parameters
	 **/
    public SyncException(String errorcode, Throwable cause,Object ...param) {
        this(Locale.getDefault(), errorcode, cause, param);
    }

	/**
	 * Constructor with error code and parameters
	 **/
	public SyncException(Locale locale, String errorcode, Object... param) {
		super(locale, errorcode, param);
	}

	/**
	 * Constructor with error code, cause and parameters
	 **/
    public SyncException(Locale locale, String errorcode, Throwable cause,Object ...param) {
        super(locale, errorcode, cause, param);
    }

	/**
	 * Constructor with cause
	 **/
    public SyncException(Throwable cause) {
        super(cause);
        this.locale = Locale.getDefault();
    }
    	
    @Override
	protected String findMessage(Locale locale, String errorcode,Object ... param){
		
		ResourceBundle rb = sync_bundles.get(locale);
		if(rb == null){
			rb = loadResourceBundle(locale, WebException.class);
			sync_bundles.put(locale, rb);
		}
		String messagePattern = null;
		if(rb == null || !rb.containsKey(errorcode)) {
			matched = false;
			return super.findMessage(locale, errorcode, param);
		} else{
			matched = true;
			messagePattern = rb.getString(errorcode);
		}

		return MessageFormat.format(messagePattern, param);
	}

}
