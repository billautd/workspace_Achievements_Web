package perso.project.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import perso.project.model.enums.ConsoleSourceEnum;

@JsonIgnoreProperties({ "IconURL", "gameDataMap" })
public class ConsoleData {
	@JsonProperty("ID")
	private int id = 0;

	@JsonProperty("Name")
	private String name = "";

	@JsonProperty("Active")
	private boolean isActive = false;

	@JsonProperty("IsGameSystem")
	private boolean isGameSystem = false;

	@JsonProperty("Source")
	private ConsoleSourceEnum source = null;

	private Map<Integer, GameData> gameDataMap = new HashMap<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isGameSystem() {
		return isGameSystem;
	}

	public void setGameSystem(boolean isGameSystem) {
		this.isGameSystem = isGameSystem;
	}

	public ConsoleSourceEnum getSource() {
		return source;
	}

	public void setSource(ConsoleSourceEnum source) {
		this.source = source;
	}

	public Map<Integer, GameData> getGameDataMap() {
		return gameDataMap;
	}

	@Override
	public String toString() {
		return "Id : " + getId() + '\n' + "Name : " + getName() + '\n' + "Is active : " + isActive() + '\n'
				+ "Is game system : " + isGameSystem() + '\n' + "Source : " + getSource() + '\n' + "Games number : "
				+ getGameDataMap().values().size();
	}
}
