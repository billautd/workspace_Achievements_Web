package perso.project.model.back;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "DateModified", "NumLeaderboards", "ImageIcon", "ForumTopicID", "MaxPossible", "PctWon",
		"HardcoreMode" })
public class GameData {
	@JsonProperty("ID")
	@JsonAlias("GameID")
	private int id = 0;

	@JsonProperty("Title")
	private String title = "";

	@JsonProperty("ConsoleID")
	private int consoleId = 0;

	@JsonProperty("ConsoleName")
	private String consoleName = "";

	@JsonProperty("NumAchievements")
	private int numAchievements = 0;

	@JsonProperty("NumAwarded")
	private int numAchievementsAwarded = 0;

	@JsonProperty("Points")
	private int totalPoints = 0;

	public void setGameData(final GameData data) {
		setTitle(data.getTitle());
		setConsoleName(data.getConsoleName());
		setNumAchievements(data.getNumAchievements());
		setNumAchievementsAwarded(data.getNumAchievementsAwarded());
		setTotalPoints(data.getTotalPoints());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getConsoleId() {
		return consoleId;
	}

	public void setConsoleId(int consoleId) {
		this.consoleId = consoleId;
	}

	public String getConsoleName() {
		return consoleName;
	}

	public void setConsoleName(String consoleName) {
		this.consoleName = consoleName;
	}

	public int getNumAchievements() {
		return numAchievements;
	}

	public void setNumAchievements(int numAchievements) {
		this.numAchievements = numAchievements;
	}

	public int getNumAchievementsAwarded() {
		return numAchievementsAwarded;
	}

	public void setNumAchievementsAwarded(int numAchievementsAwarded) {
		this.numAchievementsAwarded = numAchievementsAwarded;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}
}
