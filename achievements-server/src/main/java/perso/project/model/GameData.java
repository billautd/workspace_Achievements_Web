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

	@JsonProperty("Percent")
	private double percent = 0;

	@JsonProperty("Points")
	private int totalPoints = 0;

	@JsonProperty("TruePoints")
	private int truePoints = 0;

	@JsonProperty("EarnedPoints")
	private int earnedPoints = 0;

	@JsonProperty("EarnedTruePoints")
	private int earnedTruePoints = 0;

	@JsonProperty("Ratio")
	private double ratio = 0d;

	@JsonProperty("EarnedRatio")
	private double earnedRatio = 0d;

	@JsonProperty("NumDistinctPlayersHardcore")
	private int totalPlayers = 0;

	@JsonProperty("HighestAwardKind")
	private String awardKind = "";

	@JsonProperty("CompletionStatus")
	private CompletionStatusEnum completionStatus = CompletionStatusEnum.NOT_PLAYED;

	@JsonProperty("AchievementData")
	private List<AchievementData> achievementData = new ArrayList<>();

	@JsonProperty("Image")
	private String imageURL = "";

	@JsonProperty("ImageBase64")
	private String imageBase64 = "";

	@JsonProperty("UUID")
	private String UUID = "";

	@Override
	public String toString() {
		return "Title : " + getTitle() + '\n' + "Id : " + getId() + '\n' + "Console id : " + getConsoleId() + '\n'
				+ "Console name : " + getConsoleName() + '\n' + "Total achievements : " + getTotalAchievements() + '\n'
				+ "Awarded achievements : " + getAwardedAchievements() + '\n' + "Total points : " + getTotalPoints()
				+ '\n' + "Completion status : " + getCompletionStatus() + '\n' + "Achievements number : "
				+ getAchievementData().size();
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

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}

	public int getTruePoints() {
		return truePoints;
	}

	public void setTruePoints(int truePoints) {
		this.truePoints = truePoints;
	}

	public int getEarnedPoints() {
		return earnedPoints;
	}

	public void setEarnedPoints(int earnedPoints) {
		this.earnedPoints = earnedPoints;
	}

	public int getEarnedTruePoints() {
		return earnedTruePoints;
	}

	public void setEarnedTruePoints(int earnedTruePoints) {
		this.earnedTruePoints = earnedTruePoints;
	}

	public int getTotalPlayers() {
		return totalPlayers;
	}

	public void setTotalPlayers(int totalPlayers) {
		this.totalPlayers = totalPlayers;
	}

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public String getAwardKind() {
		return awardKind;
	}

	public double getEarnedRatio() {
		return earnedRatio;
	}

	public void setEarnedRatio(double earnedRatio) {
		this.earnedRatio = earnedRatio;
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

	public List<AchievementData> getAchievementData() {
		return achievementData;
	}

	public void setAchievementData(List<AchievementData> achievementData) {
		this.achievementData = achievementData;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getImageBase64() {
		return imageBase64;
	}

	public void setImageBase64(String imageBase64) {
		this.imageBase64 = imageBase64;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
	}

}
