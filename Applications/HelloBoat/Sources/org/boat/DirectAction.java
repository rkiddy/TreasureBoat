// Generated by the WOLips Templateengine Plug-in at Apr 23, 2014 3:28:12 PM
package org.boat;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WODirectAction;

import org.boat.ui.Main;

public class DirectAction extends WODirectAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	public WOActionResults defaultAction() {
		return pageWithName(Main.class.getName());
	}
}
