package perso.project.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import perso.project.model.enums.ConsoleSourceEnum;

public class Model {
	public static final int STEAM_CONSOLE_ID = 100000;
	public static final int PS3_CONSOLE_ID = 200000;
	public static final int PSVITA_CONSOLE_ID = 300000;

	private final Map<Integer, ConsoleData> consoleDataMap = new HashMap<>();

	private final Map<String, PlayniteGameData> playniteData = new HashMap<>();

	public Map<Integer, ConsoleData> getConsoleDataMap() {
		return consoleDataMap;
	}

	public Map<String, PlayniteGameData> getPlayniteData() {
		return playniteData;
	}

	public List<GameData> getGameDataForSources(final List<ConsoleSourceEnum> sources) {
		return consoleDataMap.values().stream().filter(c -> sources.contains(c.getSource()))
				.map(c -> c.getGameDataMap().values()).flatMap(Collection::stream).toList();
	}

	public List<PlayniteGameData> getPlayniteGameDataForSources(final List<ConsoleSourceEnum> sources) {
		return getPlayniteData().values().stream().filter(g -> g.getSource() != null && sources.contains(g.getSource()))
				.toList();
	}
}
