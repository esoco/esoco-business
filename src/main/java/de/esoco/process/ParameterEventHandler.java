package de.esoco.process;

/********************************************************************
 * An event handler interface for parameter value updates.
 *
 * @author eso
 */
public interface ParameterEventHandler<T>
{
	//~ Methods ------------------------------------------------------------

	/***************************************
	 * Will be invoked if an event occurred for the parameter.
	 *
	 * @param rParamValue The current parameter value
	 */
	public void handleParameterUpdate(T rParamValue);
}