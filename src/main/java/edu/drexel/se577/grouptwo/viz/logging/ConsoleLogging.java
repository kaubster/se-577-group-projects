package edu.drexel.se577.grouptwo.viz.logging;

import edu.drexel.se577.grouptwo.viz.logging.Logging;

public class ConsoleLogging implements Logging {

	public void LogCritical(String message) {
		System.out.println("(Critical) " + message);
	}

	public void LogDebug(String message) {
		System.out.println("(Debug) " + message);
	}

	public void LogError(String message) {
		System.out.println("(Error) " + message);
	}
}