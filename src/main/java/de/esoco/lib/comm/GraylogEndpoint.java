//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import org.obrel.core.RelationType;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static de.esoco.lib.comm.CommunicationRelationTypes.ENCRYPTION;
import static org.obrel.core.RelationTypeModifier.PRIVATE;
import static org.obrel.core.RelationTypes.newType;

/**
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
public class GraylogEndpoint extends Endpoint {

	/**
	 * Enumeration of the supported Graylog communication protocols.
	 */
	public enum Protocol {UDP, TCP}

	private static final RelationType<Connection> GRAYLOG_SERVER_CONNECTION =
		newType(PRIVATE);

	private static final RelationType<CommunicationMethod<byte[], ?>>
		GRAYLOG_SERVER_METHOD = newType(PRIVATE);

	/**
	 * Factory method that creates a new communication method for sending
	 * Graylog messages.
	 *
	 * @return A new communication method
	 */
	public static SendGraylogMessage sendMessage() {
		return new SendGraylogMessage();
	}

	/**
	 * Builds a Graylog endpoint URL from the given parameters.
	 *
	 * @param host      The host name or address
	 * @param port      The port to connect to
	 * @param protocol  The communication protocol
	 * @param encrypted TRUE for an encrypted connection
	 * @return The resulting endpoint URL
	 */
	@SuppressWarnings("boxing")
	public static String url(String host, int port, Protocol protocol,
		boolean encrypted) {
		return String.format("%s://%s:%d?%s",
			encrypted ? "graylogs" : "graylog", host, port, protocol);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void closeConnection(Connection connection) throws IOException {
		connection.get(GRAYLOG_SERVER_CONNECTION).close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initConnection(Connection connection) {
		URI uri = connection.getUri();
		String host = uri.getHost();
		int port = uri.getPort();
		String proto = uri.getQuery();
		Protocol protocol = null;

		if (proto != null) {
			protocol = Protocol.valueOf(proto);
		} else
			throw new IllegalArgumentException("Graylog protocol missing");

		if (protocol == Protocol.TCP) {
			String socketAddress =
				SocketEndpoint.url(host, port, hasFlag(ENCRYPTION));

			connection.set(GRAYLOG_SERVER_CONNECTION,
				Endpoint.at(socketAddress).connect(connection));
			connection.set(GRAYLOG_SERVER_METHOD,
				SocketEndpoint.binaryRequest(null, null));
		} else {
			throw new CommunicationException(
				"Protocol not implemented: " + protocol);
		}
	}

	/**
	 * Sends a message over a Graylog connection.
	 *
	 * @author eso
	 */
	public static class SendGraylogMessage
		extends CommunicationMethod<GraylogMessage, Void> {

		/**
		 * Creates a new instance.
		 */
		public SendGraylogMessage() {
			super("SendGraylogMessage(%)", null);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Void doOn(Connection connection, GraylogMessage message)
			throws Exception {
			connection
				.get(GRAYLOG_SERVER_METHOD)
				.doOn(connection.get(GRAYLOG_SERVER_CONNECTION),
					message.toJson().getBytes(StandardCharsets.UTF_8));

			return null;
		}
	}
}
