//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.Socket;

import org.obrel.core.RelationType;

import org.w3c.dom.Document;

import static org.obrel.core.RelationTypeModifier.PRIVATE;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * An endpoint implementation for the communication with domain providers that
 * use the EPP protocol.
 *
 * @author eso
 */
public class EppEndpoint extends SocketEndpoint
{
	//~ Static fields/initializers ---------------------------------------------

	/** Parameter: the EPP login XML command. */
	public static final RelationType<Document> EPP_LOGIN_COMMAND = newType();

	/** Parameter: the EPP logout XML command. */
	public static final RelationType<Document> EPP_LOGOUT_COMMAND = newType();

	/**
	 * An internal relation type to the data input stream for the socket input
	 * stream of an EPP endpoint.
	 */
	private static final RelationType<DataInputStream> EPP_DATA_INPUT_STREAM =
		newType(PRIVATE);

	/**
	 * An internal relation type to the data output stream for the socket output
	 * stream of an EPP endpoint.
	 */
	private static final RelationType<DataOutputStream> EPP_DATA_OUTPUT_STREAM =
		newType(PRIVATE);

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see SocketEndpoint#closeConnection(Connection)
	 */
	@Override
	protected void closeConnection(Connection rConnection) throws IOException
	{
		super.closeConnection(rConnection);
	}

	/***************************************
	 * @see SocketEndpoint#initConnection(Connection)
	 */
	@Override
	protected void initConnection(Connection rConnection) throws IOException
	{
		super.initConnection(rConnection);

		Socket rSocket = getEndpointSocket(rConnection);

		DataOutputStream aDataOut =
			new DataOutputStream(rSocket.getOutputStream());
		DataInputStream  aDataIn  =
			new DataInputStream(rSocket.getInputStream());

		rConnection.set(EPP_DATA_OUTPUT_STREAM, aDataOut);
		rConnection.set(EPP_DATA_INPUT_STREAM, aDataIn);

		String sLoginCommand = rConnection.get(EPP_LOGIN_COMMAND).toString();

		// ignore initial response, then login
		readEppResponse(aDataIn);
		writeEppCommand(aDataOut, sLoginCommand);

		// TODO: check response for correct login
		readEppResponse(aDataIn);
	}

	/***************************************
	 * Reads an EPP XML response from an input stream.
	 *
	 * @param  rDataIn The input stream to read the response from
	 *
	 * @return A byte array containing the full response
	 *
	 * @throws IOException If reading the response fails
	 */
	private byte[] readEppResponse(DataInputStream rDataIn) throws IOException
	{
		int    nLength = rDataIn.readInt() - 4;
		byte[] aBytes  = new byte[nLength];

		rDataIn.readFully(aBytes, 0, nLength);

		return aBytes;
	}

	/***************************************
	 * Writes an EPP command to an output stream.
	 *
	 * @param  rDataOut The output stream
	 * @param  sCommand The EPP command
	 *
	 * @throws IOException If writing the command fails
	 */
	private void writeEppCommand(DataOutputStream rDataOut, String sCommand)
		throws IOException
	{
		byte[] rCommandBytes = sCommand.getBytes("UTF-8");

		// buffer length + 4 bytes for size integer
		rDataOut.writeInt(rCommandBytes.length + 4);
		rDataOut.write(rCommandBytes);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * Implementation of an EPP request communication method that sends and
	 * receives XML documents.
	 *
	 * @author eso
	 */
	public static class EppRequest extends SocketRequest<Document, Document>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rDefaultRequest The default request document
		 */
		protected EppRequest(Document rDefaultRequest)
		{
			super("EppRequest", rDefaultRequest);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected Document readResponse(
			Connection  rConnection,
			InputStream rInputStream) throws Exception
		{
			return null;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected void writeRequest(Connection   rConnection,
									OutputStream rOutputStream,
									Document	 rRequest) throws Exception
		{
		}
	}
}
