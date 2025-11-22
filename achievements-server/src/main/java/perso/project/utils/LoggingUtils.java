package perso.project.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;

public class LoggingUtils {
	private LoggingUtils() {

	}

	public static void prettyPrintDebug(final ObjectMapper mapper, final String resBody) {
		try {
			Log.debug("Body : " + mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(mapper.readValue(resBody, Object.class)));
		} catch (JsonProcessingException e) {
			Log.error("Cannot pretty print " + resBody, e);
		}
	}

	public static void prettyPrint(final ObjectMapper mapper, final String resBody) {
		try {
			Log.trace("Body : " + mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(mapper.readValue(resBody, Object.class)));
		} catch (JsonProcessingException e) {
			Log.error("Cannot pretty print " + resBody, e);
		}
	}
}
