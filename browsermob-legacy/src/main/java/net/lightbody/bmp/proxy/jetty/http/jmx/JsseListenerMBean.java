// ========================================================================
// $Id: JsseListenerMBean.java,v 1.3 2004/05/09 20:32:14 gregwilkins Exp $
// Copyright 2002-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package net.lightbody.bmp.proxy.jetty.http.jmx;

import javax.management.MBeanException;


/* ------------------------------------------------------------ */

/**
 * @author Greg Wilkins (gregw)
 * @version $Revision: 1.3 $
 */
public class JsseListenerMBean extends SocketListenerMBean {
	/* ------------------------------------------------------------ */

	/**
	 * Constructor.
	 *
	 * @throws MBeanException
	 */
	public JsseListenerMBean()
			throws MBeanException {
		super();
	}

	/* ------------------------------------------------------------ */
	protected void defineManagedResource() {
		super.defineManagedResource();
		defineAttribute("needClientAuth");
	}
}
