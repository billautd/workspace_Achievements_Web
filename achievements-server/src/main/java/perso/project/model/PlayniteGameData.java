package perso.project.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;

@JsonPropertyOrder({ "Name", "Game Id", "Completion Status", "Platforms", "Sources" })
public class PlayniteGameData {

	private String name = "";

	private String gameId = "";

	private CompletionStatusEnum completionStatus = null;

	private String platform = "";

	private ConsoleSourceEnum source = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public CompletionStatusEnum getCompletionStatus() {
		return completionStatus;
	}

	public void setCompletionStatus(CompletionStatusEnum completionStatus) {
		this.completionStatus = completionStatus;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public ConsoleSourceEnum getSource() {
		return source;
	}

	public void setSource(ConsoleSourceEnum source) {
		this.source = source;
	}

}
