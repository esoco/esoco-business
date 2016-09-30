//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.lib.comm;

import java.io.IOException;

import java.net.URI;

import java.nio.charset.StandardCharsets;

import java.util.Objects;

import org.obrel.core.RelationType;

import static de.esoco.lib.comm.CommunicationRelationTypes.ENCRYPTED_CONNECTION;

import static org.obrel.core.RelationTypeModifier.PRIVATE;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * An endpoint implementation for sending messages to a Graylog server. A
 * graylog endpoint can be created by providing a URL to the standard factory
 * method {@link Endpoint#at(String)}. The format of the URL must be like this:
 *
 * <p><code>graylog://host[:port]?protocol</code></p>
 *
 * <p>Where protocol stands for one of the enum constants in {@link Protocol}.
 * For an encrypted connection the scheme <code>graylogs</code> can be used
 * instead.</p>
 *
 * @author eso
 */
public class GraylogEndpoint extends Endpoint
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the supported Graylog communication protocols.
	 */
	public enum Protocol { UDP, TCP }

	//~ Static fields/initializers ---------------------------------------------

	private static final RelationType<Connection> GRAYLOG_SERVER_CONNECTION =
		newType(PRIVATE);

	private static final RelationType<CommunicationMethod<byte[], ?>> GRAYLOG_SERVER_METHOD =
		newType(PRIVATE);

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method that creates a new communication method for sending
	 * Graylog messages.
	 *
	 * @return A new communication method
	 */
	public static SendGraylogMessage sendMessage()
	{
		return new SendGraylogMessage();
	}

	/***************************************
	 * Builds a Graylog endpoint URL from the given parameters.
	 *
	 * @param  sHost      The host name or address
	 * @param  nPort      The port to connect to
	 * @param  eProtocol  The communication protocol
	 * @param  bEncrypted TRUE for an encrypted connection
	 *
	 * @return The resulting endpoint URL
	 */
	@SuppressWarnings("boxing")
	public static String url(String   sHost,
							 int	  nPort,
							 Protocol eProtocol,
							 boolean  bEncrypted)
	{
		return String.format("%s://%s:%d?%s",
							 bEncrypted ? "graylogs" : "graylog",
							 sHost,
							 nPort,
							 eProtocol);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void closeConnection(Connection rConnection) throws IOException
	{
		rConnection.get(GRAYLOG_SERVER_CONNECTION).close();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void initConnection(Connection rConnection) throws IOException
	{
		URI		 rUri	   = rConnection.getUri();
		String   sHost     = rUri.getHost();
		int		 nPort     = rUri.getPort();
		String   sProtocol = rUri.getQuery();
		Protocol eProtocol = null;

		if (sProtocol != null)
		{
			eProtocol = Protocol.valueOf(sProtocol);
		}

		Objects.requireNonNull(eProtocol, "Graylog protocol missing");

		if (eProtocol == Protocol.TCP)
		{
			String sSocketAddress =
				SocketEndpoint.url(sHost, nPort, hasFlag(ENCRYPTED_CONNECTION));

			rConnection.set(GRAYLOG_SERVER_CONNECTION,
							Endpoint.at(sSocketAddress).connect(rConnection));
			rConnection.set(GRAYLOG_SERVER_METHOD,
							SocketEndpoint.binaryRequest(null, null));
		}
		else
		{
			throw new CommunicationException("Protocol not implemented: " +
											 eProtocol);
		}
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * Sends a message over a Graylog connection.
	 *
	 * @author eso
	 */
	public static class SendGraylogMessage
		extends CommunicationMethod<GraylogMessage, Void>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		public SendGraylogMessage()
		{
			super("SendGraylogMessage(%)", null);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public Void doOn(Connection rConnection, GraylogMessage rMessage)
		{
			rConnection.get(GRAYLOG_SERVER_METHOD)
					   .doOn(rConnection.get(GRAYLOG_SERVER_CONNECTION),
							 rMessage.toJson()
							 .getBytes(StandardCharsets.UTF_8));

			return null;
		}
	}
}
