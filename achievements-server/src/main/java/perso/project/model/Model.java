package perso.project.model;

import java.util.HashMap;
import java.util.Map;

public class Model {
	public static final int STEAM_CONSOLE_ID = 100000;
	// RetroAchievements keep their own id
	// Steam is
	private final Map<Integer, ConsoleData> consoleDataMap = new HashMap<>();

	public Map<Integer, ConsoleData> getConsoleDataMap() {
		return consoleDataMap;
	}
}
