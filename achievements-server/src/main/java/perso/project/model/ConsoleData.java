package perso.project.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties("IconURL")
public class ConsoleData {
	@JsonProperty("ID")
	private int id = 0;

	@JsonProperty("Name")
	private String name = "";

	@JsonProperty("Active")
	private boolean isActive = false;

	@JsonProperty("IsGameSystem")
	private boolean isGameSystem = false;

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

	public Map<Integer, GameData> getGameDataMap() {
		return gameDataMap;
	}

	@Override
	public String toString() {
		return "Console data : \n" + "\tId : " + getId() + "\n\tName : " + getName() + "\n\tIs active : " + isActive()
				+ "\n\tIs game system : " + isGameSystem();
	}
}
