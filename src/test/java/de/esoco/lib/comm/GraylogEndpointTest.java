//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 3.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-3.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.lib.comm;

import de.esoco.lib.comm.GraylogEndpoint.Protocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/********************************************************************
 * Test for {@link GraylogEndpoint}
 *
 * @author eso
 */
public class GraylogEndpointTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Tests the functionality of {@link GraylogEndpoint#url(String, int,
	 * Protocol, boolean)}.
	 */
	@Test
	public void testGraylogUrl()
	{
		assertEquals("graylog://1.2.3.4:5?TCP",
					 GraylogEndpoint.url("1.2.3.4", 5, Protocol.TCP, false));
		assertEquals("graylogs://host:123?TCP",
					 GraylogEndpoint.url("host", 123, Protocol.TCP, true));
		assertEquals("graylog://1.2.3.4:5?UDP",
					 GraylogEndpoint.url("1.2.3.4", 5, Protocol.UDP, false));
	}
}
