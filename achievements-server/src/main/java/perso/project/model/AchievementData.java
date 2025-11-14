package perso.project.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import perso.project.model.enums.AchievementTypeEnum;

@JsonIgnoreProperties({
		// Steam
		"unlocktime", "defaultvalue", "hidden",
		// Retro Achievements
		"NumAwarded", "Author", "AuthorULID", "DateModified", "DateCreated", "MemAddr", "DateEarned" })
public class AchievementData {
	@JsonProperty("apiname")
	@JsonAlias({ "name" })
	private String name = "";

	@JsonProperty("ID")
	private int id = -1;

	@JsonProperty("DisplayOrder")
	private int displayOrder = 0;

	@JsonProperty("achieved")
	private boolean achieved = false;

	@JsonProperty("displayName")
	@JsonAlias("Title")
	private String displayName = "";

	@JsonProperty("description")
	@JsonAlias("Description")
	private String description = "";

	@JsonProperty("percent")
	private double unlockPercentage = 0d;

	@JsonProperty("NumAwardedHardcore")
	private int numAwarded = 0;

	@JsonProperty("Points")
	private int points = 0;

	@JsonProperty("TrueRatio")
	private int realPoints = 0;

	@JsonProperty("icon")
	private String iconUnlockedURL = "";

	@JsonProperty("icongray")
	private String iconLockedURL = "";

	@JsonProperty("BadgeName")
	private int badgeId = 0;

	@JsonProperty("Type")
	@JsonAlias("type")
	private AchievementTypeEnum type = null;

	@JsonProperty("DateEarnedHardcore")
	private String dateEarned = "";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public boolean isAchieved() {
		return achieved;
	}

	public void setAchieved(boolean achieved) {
		this.achieved = achieved;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getUnlockPercentage() {
		return unlockPercentage;
	}

	public void setUnlockPercentage(double unlockPercentage) {
		this.unlockPercentage = unlockPercentage;
	}

	public int getNumAwarded() {
		return numAwarded;
	}

	public void setNumAwarded(int numAwarded) {
		this.numAwarded = numAwarded;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getRealPoints() {
		return realPoints;
	}

	public void setRealPoints(int realPoints) {
		this.realPoints = realPoints;
	}

	public String getIconLockedURL() {
		return iconLockedURL;
	}

	public void setIconLockedURL(String iconLockedURL) {
		this.iconLockedURL = iconLockedURL;
	}

	public String getIconUnlockedURL() {
		return iconUnlockedURL;
	}

	public void setIconUnlockedURL(String iconUnlockedURL) {
		this.iconUnlockedURL = iconUnlockedURL;
	}

	public int getBadgeId() {
		return badgeId;
	}

	public void setBadgeId(int badgeId) {
		this.badgeId = badgeId;
	}

	public AchievementTypeEnum getType() {
		return type;
	}

	public void setType(AchievementTypeEnum type) {
		this.type = type;
	}

	public String getDateEarned() {
		return dateEarned;
	}

	public void setDateEarned(String dateEarned) {
		this.dateEarned = dateEarned;
	}
}
