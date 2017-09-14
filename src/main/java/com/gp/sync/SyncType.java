package com.gp.sync;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.gp.common.GeneralConstants;

public class SyncType {
	
	private List<String> options = new ArrayList<String>();
	
	public SyncType(String type) {
		Iterable<String> opts = Splitter.on(GeneralConstants.OPTS_SEPARATOR).split(type);
		for(String opt: opts) {
			options.add(opt);
		}
	}
	
	public SyncType(String ...opts) {
		for(String opt: opts) {
			options.add(opt);
		}
	}
	
	/**
	 * Check the option existence 
	 **/
	public boolean hasOption(String option) {
		return options.contains(option);
	}
	
	/**
	 * Get the options 
	 **/
	public List<String> getOptions(){
		return this.options;
	}
	
	@Override
	public String toString() {
		
		return Joiner.on(GeneralConstants.OPTS_SEPARATOR).join(options);
	}
}
