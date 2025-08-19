package perso.project.model;

import java.util.HashMap;
import java.util.Map;

public class Model {
	private final Map<Integer, ConsoleData> consoleDataMap = new HashMap<>();

	public Map<Integer, ConsoleData> getConsoleDataMap() {
		return consoleDataMap;
	}
}
