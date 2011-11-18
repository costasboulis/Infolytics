package gr.infolytics.models.mixtureOfExperts;

import java.util.HashMap;

/**
 * Parses and provides a convenient means of grabbing command line arguments.
 *
 * Format is standard unix-like:
 *    switches are -switch
 *    arguments are -arg="foo" or -arg=foo
 *
 * Typical usage:
 *       CommandLineParser parser = new CommandLineParser(args);
 *
 *       // optionally blow up if there were invalid switches
 *       if (parser.hasErrors()){
 *          failWithUsage();
 *       }
 *
 *       // optionally blow up if unknown switches were used
 *       // first arg is switches, second arg is parameters
 *       //
 *       // parameter names ending in ! are required.
 *       //
 *       if (parser.hasErrors(new String[]{"foo"},
 *       					  new String[]{"bar!"})){
 *       	failWithUsage();
 *       }
 *
 *       // grab a switch out, will throw if was entered as arg instead
 *       boolean foo = parser.getSwitch("foo")
 *
 *       // grab a parameter out, will throw if it was entered as switch instead
 *       String bar = parser.getParameter("bar");
 *
 * 
 */
public class CommandLineParser {

	/** Get an parameter, throwing an exception of the arg is required and not present.
	 * @param hash map of parameters
	 * @param name the name of the parameter
	 * @param required whether the parameter is required
	 * @return the parameter, or null of the parameter does not exist
	 * @throws IllegalArgumentException when the parameter is required and not present. */
	public static Object getParameter(HashMap<String, Object> parameters, String name, boolean required) throws IllegalArgumentException {
		Object object = parameters.get(name);
		if ((object == null) && required) {
			throw new IllegalArgumentException("Must define parameter " + name + ".");
		}

		return object;
	}

	/** Get a String parameter.
	 * @param hash map of parameters
	 * @param name the name of the parameter
	 * @param required whether the parameter is required
	 * @return the parameter, or null of the parameter does not exist
	 * @throws IllegalArgumentException when the parameter is required and not present, or if parameter is not a String.
	 **/
	public static String getStringParameter(HashMap<String, Object> parameters, String name, boolean required) throws IllegalArgumentException {
		Object object = getParameter(parameters, name, required);
		if (object == null) {
			return null;
		}

		// Now enforce String requirements
		if (!(object instanceof String)) {
			throw new IllegalArgumentException("Parameter " + name + " must be a String");
		}

		String arg = (String)object;
		if ((arg.length() == 0) && required) {
			throw new IllegalArgumentException("String parameter " + name + " is a required parameter and cannot be blank.");
		}

		return arg;
	}

	/** Get a Boolean parameter.
	 * @param hash map of parameters
	 * @param name the name of the parameter
	 * @param required whether the parameter is required
	 * @param defaultValue the default value, if parameter is not required and not present.
	 * @return the parameter, or null of the parameter does not exist
	 * @throws IllegalArgumentException when the parameter is required and not present, or if parameter is not a Boolean.
	 **/
	public static boolean getBooleanParameter(HashMap<String, Object> parameters, String name, boolean required, boolean defaultValue)  throws IllegalArgumentException {
		Object object = getParameter(parameters, name, required);
		if (object == null) {
			return defaultValue;
		}

		// Now enforce Boolean requirements
		if (!(object instanceof Boolean)) {
			throw new IllegalArgumentException("Parameter " + name + " must be a Boolean.");
		}

		return (Boolean)object;
	}

	/** Get an integer parameter.
	 * @param hash map of parameters
	 * @param name the name of the parameter
	 * @param required whether the parameter is required
	 * @param defaultValue the default value, if parameter is not required and not present.
	 * @return the parameter, or null of the parameter does not exist
	 * @throws IllegalArgumentException when the parameter is required and not present, or if parameter is not an integer.
	 **/
	public static int getIntegerParameter(HashMap<String, Object> parameters, String name, boolean required, int defaultValue) throws IllegalArgumentException {
		// Get the parameter as a String
		String stringValue = getStringParameter(parameters, name, required);
		if (stringValue == null) {
			return defaultValue;
		}

		// Now ensure it parses into an int
		int intValue = defaultValue;
		try {
			intValue = Integer.parseInt(stringValue);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Parameter " + name + " must be an integer", e);
		}

		return intValue;
	}

	/** Get a double parameter.
	 * @param hash map of parameters
	 * @param name the name of the parameter
	 * @param required whether the parameter is required
	 * @param defaultValue the default value, if parameter is not required and not present.
	 * @return the parameter, or null of the parameter does not exist
	 * @throws IllegalArgumentException when the parameter is required and not present, or if parameter is not an integer.
	 **/
	public static double getDoubleParameter(HashMap<String, Object> parameters, String name, boolean required, double defaultValue) throws IllegalArgumentException {
		// Get the parameter as a String
		String stringValue = getStringParameter(parameters, name, required);
		if (stringValue == null) {
			return defaultValue;
		}

		// Now ensure it parses into a double
		double intValue = defaultValue;
		try {
			intValue = Double.parseDouble(stringValue);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Parameter " + name + " must be an integer", e);
		}

		return intValue;
	}

	/**
	 * Creates a new command line parser with the passed in args.  Parsing
	 * takes place immediately.
	 * @param args The arguments to parse
	 */
	public CommandLineParser(String[] args){
		hasErrors = parseArgs(args, arguments);
	}
	/**
	 * Returns the error string for the first error we found.  Note that this will
	 * only be non-null if an error was found, and that might only occur if you call
	 * the strict hasErrors() method.
	 *
	 * @return An error message as to why our parsing wasn't perfect
	 */
	public String getErrorString(){
		return errorString;
	}

	/**
	 * Returns whether this parser ran into any erros
	 * @return whether this parser ran into any erros
	 */
	public boolean hasErrors(){
		return hasErrors;
	}

	/**
	 * Returns whether our parser had any errors.  This is a stricter test
	 * and will make sure that the usage was correct and no extra switches
	 * were specified.
	 *
	 * @param switches The names of the optional switches
	 * @param parameters The names of the parameters, required ones end with !
	 * @return Whether there are any errors
	 */
	public boolean hasErrors(String[] switches, String[] parameters){
		// If we already know we have errors, return
		if (hasErrors) return true;

		// we keep track of all found args so we can check for extra ones at
		// the end
		int foundArgs = 0;

		// check that all switches are valid
		for(String curSwitch : switches){
			try{
				boolean found = getSwitch(curSwitch);
				if (found) foundArgs++;
			} catch (IllegalArgumentException e){
				// this was a parameter instead of a switch, that's an error
				errorString = e.getMessage();
				return true;
			}
		}

		// check all parameters are valid
		for(String parameter : parameters){
			// is this parameter required?
			boolean required = parameter.endsWith("!");
			if (required){
				parameter = parameter.substring(0, parameter.length() - 1);
			}

			// Try to look it up
			try{
				String value = getParameter(parameter);

				// If we have a value, increment our count of found args
				if (value != null){
					foundArgs++;
				} else if (required){
					// if this is a required parameter and we have no value, that's an error
					errorString = "Missing required parameter: " + parameter;
					return true;
				}
			} catch (IllegalArgumentException e){
				// This was a switch instead of a parameter, fail
				errorString = e.getMessage();
				return true;
			}
		}

		// Final check is that our # of args is the same as the # we found, this
		// checks for extra arguments that weren't expected
		return (arguments.size() != foundArgs );
	}

	/**
	 * Returns all the parameters that were handed to the parser.
	 * @return A hashmap of key->value
	 */
	public HashMap<String,String> getParameters(){
		HashMap<String,String> parameters = new HashMap<String,String>();

		for(Object key: arguments.keySet()){
			// look up the value
			Object value = arguments.get(key);

			// if the value is a string, it's a parameter, add it to the map
			if (value instanceof String){
				parameters.put((String) key, (String) value);
			}
		}
		return parameters;
	}

	/**
	 * Returns whether we have the parameter with the passed in name, throws if the
	 * parameter is a switch, not a parameter
	 * @param name The name of the parameter to check for
	 * @return Whether we have that parameter
	 */
	public boolean hasParameter(String name){
		Object value = arguments.get(name);

		// If it's null, return false
		if (value != null){
			if (!(value instanceof String)){
				throw new IllegalArgumentException("Invalid args.  " + name + " is a switch not a parameter.");
			}
			return true;
		}

		return false;
	}

	/**
	 * Returns all the arguments passed to this CommandLineParser.  Switches will have
	 * 'Boolean' values, while parameters will be standard key/value pairs.
	 *
	 * @return Our HashMap of arguments
	 */
	public HashMap<String, Object> getArguments(){
		return arguments;
	}

	/**
	 * Returns the value for the passed in parameter.  Returns null
	 * if the parameter was not specified, and throws if it was specified
	 * as a switch instead of a parameter.
	 *
	 * @param name The name of the parameter to get
	 * @return The value of the parameter, or null if it wasn't specified
	 */
	public String getParameter(String name){
		Object value = arguments.get(name);

		// If we have a switch or parameter by that name
		if (value != null){
			// If it's a parameter, return the value
			if (value instanceof String){
				return (String) value;
			} else {
				throw new IllegalArgumentException("Invalid args.  " + name + " is a switch not a parameter.");
			}
		}
		return null;
	}

	/**
	 * Returns whether the switch with the passed in name is set, throwing if
	 * it was specified as parameter instead.
	 * @param name The name of the switch to check
	 * @return Whether it was entered on the command line
	 */
	public boolean getSwitch(String name){
		Object value = arguments.get(name);

		// If we have a switch or parameter by that name
		if (value != null){
			// If it's a swith, return the value
			if (value instanceof Boolean){
				return ((Boolean)value).booleanValue();
			} else {
				throw new IllegalArgumentException("Invalid args.  " + name + " is a switch not a parameter.");
			}
		}
		return false;
	}

	/**
	 * Given a set of command line arguments, puts all valid values in the
	 * HashMap passed in, returning whether any errors were found.
	 * @param args The arguments to parse off the command line
	 * @param arguments The final arguments
	 * @return Whether any errors were found
	 */
	private boolean parseArgs(String[] args, HashMap arguments){
		boolean hasErrors = false;

		// for each element in our arg list
		for(String arg : args){
			// if this arg doesn't start with a switch, it's an error
			// mark it as so and move on
			if (!arg.startsWith("-")){
				if (!hasErrors){
					errorString = "Invalid argument: " + arg;
				}

				hasErrors = true;
			}
			// otherwise, so far so good, let's read the rest
			else {
				arg = arg.substring(1);

				// If there is no '=' then we are a switch
				int equalIndex = arg.indexOf('=');
				if (equalIndex < 0){
					arguments.put(arg, new Boolean(true));
				}
				// otherwise, we are a parameter
				else {
					String name = arg.substring(0, equalIndex);
					String value = arg.substring(equalIndex+1);

					// if the value starts and ends with double quotes, remove them
					if ( (value.startsWith("\"") && value.endsWith("\"")) ||
					     (value.startsWith("\'") && value.endsWith("\'"))){
						value = value.substring(1, value.length() - 1);
					}

					arguments.put(name, value);
				}
			}
		}

		// return whether we saw any errors
		return hasErrors;
	}

	/** Whether there were any errors during parsing */
	private boolean hasErrors;

	/** Our error string */
	private String errorString;

	/** Our arguments as given on the command line, minus errors */
	private HashMap<String, Object> arguments = new HashMap<String, Object>();
}
