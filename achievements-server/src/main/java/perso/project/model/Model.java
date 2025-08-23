package perso.project.model;

import java.util.HashMap;
import java.util.Map;

public class Model {
	public static final int STEAM_CONSOLE_ID = 100000;
	public static final int PS3_CONSOLE_ID = 200000;
	public static final int PSVITA_CONSOLE_ID = 300000;

	private final Map<Integer, ConsoleData> consoleDataMap = new HashMap<>();

	public Map<Integer, ConsoleData> getConsoleDataMap() {
		return consoleDataMap;
	}
}
