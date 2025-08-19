package perso.project.model.enums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "AwardedAt", "AwardData", "AwardDataExtra", "DisplayOrder", "Flags", "ImageIcon" })
public class UserAwardData {
	@JsonProperty("ConsoleID")
	private int consoleId = 0;

	@JsonProperty("AwardType")
	private String awardType = "";

	@JsonProperty("Title")
	private String title = "";

	@JsonProperty("ConsoleName")
	private String consoleName = "";

	@Override
	public String toString() {
		return "Title : " + getTitle() + '\n' + "Console id : " + getConsoleId() + '\n' + "Console name : "
				+ getConsoleName() + '\n' + '\n' + "Award type : " + getAwardType();
	}

	public int getConsoleId() {
		return consoleId;
	}

	public void setConsoleId(int consoleId) {
		this.consoleId = consoleId;
	}

	public String getAwardType() {
		return awardType;
	}

	public void setAwardType(String awardType) {
		this.awardType = awardType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getConsoleName() {
		return consoleName;
	}

	public void setConsoleName(String consoleName) {
		this.consoleName = consoleName;
	}
}
