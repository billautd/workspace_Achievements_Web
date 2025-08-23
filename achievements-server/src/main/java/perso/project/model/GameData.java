package perso.project.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import perso.project.model.enums.CompletionStatusEnum;

@JsonIgnoreProperties({
		// Retro achievements
		"DateModified", "NumLeaderboards", "ImageIcon", "ForumTopicID", "PctWon", "HardcoreMode", "NumAwarded",
		"MostRecentAwardedDate", "HighestAwardDate",
		// Steam
		"playtime_forever", "img_icon_url", "playtime_windows_forever", "playtime_mac_forever",
		"playtime_linux_forever", "playtime_deck_forever", "rtime_last_played", "content_descriptorids",
		"playtime_disconnected", "has_community_visible_stats", "has_leaderboards", "playtime_2weeks" })
public class GameData {
	@JsonProperty("ID")
	@JsonAlias({ "GameID", "appid" })
	private int id = 0;

	@JsonProperty("Title")
	@JsonAlias("name")
	private String title = "";

	@JsonProperty("ConsoleID")
	private int consoleId = 0;

	@JsonProperty("ConsoleName")
	private String consoleName = "";

	@JsonProperty("MaxPossible")
	@JsonAlias("NumAchievements")
	private int totalAchievements = 0;

	@JsonProperty("NumAwardedHardcore")
	private int awardedAchievements = 0;

	@JsonProperty("Points")
	private int totalPoints = 0;

	@JsonProperty("HighestAwardKind")
	private String awardKind = "";

	@JsonProperty("CompletionStatus")
	private CompletionStatusEnum completionStatus = CompletionStatusEnum.NOT_PLAYED;

	@JsonProperty("AchievementData")
	private List<SteamAchievementData> steamAchievementData = new ArrayList<>();

	public void setGameData(final GameData data) {
		setTitle(data.getTitle());
		setConsoleName(data.getConsoleName());
		setTotalAchievements(data.getTotalAchievements());
		setAwardedAchievements(data.getAwardedAchievements());
		setTotalPoints(data.getTotalPoints());
		setCompletionStatus(data.getCompletionStatus());
		setSteamAchievementData(data.getSteamAchievementData());
	}

	@Override
	public String toString() {
		return "Title : " + getTitle() + '\n' + "Id : " + getId() + '\n' + "Console id : " + getConsoleId() + '\n'
				+ "Console name : " + getConsoleName() + '\n' + "Total achievements : " + getTotalAchievements() + '\n'
				+ "Awarded achievements : " + getAwardedAchievements() + '\n' + "Total points : " + getTotalPoints()
				+ '\n' + "Completion status : " + getCompletionStatus() + '\n' + "Steam achievements number : "
				+ getSteamAchievementData().size();
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

	public int getTotalAchievements() {
		return totalAchievements;
	}

	public void setTotalAchievements(int totalAchievements) {
		this.totalAchievements = totalAchievements;
	}

	public int getAwardedAchievements() {
		return awardedAchievements;
	}

	public void setAwardedAchievements(int awardedAchievements) {
		this.awardedAchievements = awardedAchievements;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}

	public String getAwardKind() {
		return awardKind;
	}

	public void setAwardKind(String awardKind) {
		this.awardKind = awardKind;
	}

	public CompletionStatusEnum getCompletionStatus() {
		return completionStatus;
	}

	public void setCompletionStatus(CompletionStatusEnum completionStatus) {
		this.completionStatus = completionStatus;
	}

	public List<SteamAchievementData> getSteamAchievementData() {
		return steamAchievementData;
	}

	public void setSteamAchievementData(List<SteamAchievementData> steamAchievementData) {
		this.steamAchievementData = steamAchievementData;
	}
}
