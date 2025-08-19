package perso.project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "unlocktime" })
public class SteamAchievementData {
	@JsonProperty("apiname")
	private String name = "";

	@JsonProperty("achieved")
	private boolean achieved = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAchieved() {
		return achieved;
	}

	public void setAchieved(boolean achieved) {
		this.achieved = achieved;
	}
}
