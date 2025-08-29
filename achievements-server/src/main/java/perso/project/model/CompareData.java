package perso.project.model;

import perso.project.model.enums.CompareDataStatusEnum;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;

public class CompareData {
	private ConsoleSourceEnum source = null;

	private String consoleName = "";
	private int consoleId = 0;

	private String name = "";

	private CompareDataStatusEnum status = null;

	private CompletionStatusEnum playniteStatus = null;
	private CompletionStatusEnum databaseStatus = null;

	public CompareData(final String consoleName, final int consoleId, final String name, final ConsoleSourceEnum source,
			final CompareDataStatusEnum status) {
		this.consoleName = consoleName;
		this.consoleId = consoleId;
		this.name = name;
		this.source = source;
		this.status = status;
	}

	public CompareData(final String consoleName, final int consoleId, final String name, final ConsoleSourceEnum source,
			final CompareDataStatusEnum status, final CompletionStatusEnum playniteStatus,
			final CompletionStatusEnum databaseStatus) {
		this(consoleName, consoleId, name, source, status);
		this.databaseStatus = databaseStatus;
		this.playniteStatus = playniteStatus;
	}

	public CompletionStatusEnum getPlayniteStatus() {
		return playniteStatus;
	}

	public void setPlayniteStatus(CompletionStatusEnum playniteStatus) {
		this.playniteStatus = playniteStatus;
	}

	public CompletionStatusEnum getDatabaseStatus() {
		return databaseStatus;
	}

	public void setDatabaseStatus(CompletionStatusEnum databaseStatus) {
		this.databaseStatus = databaseStatus;
	}

	public CompareDataStatusEnum getStatus() {
		return status;
	}

	public void setStatus(CompareDataStatusEnum status) {
		this.status = status;
	}

	public ConsoleSourceEnum getSource() {
		return source;
	}

	public void setSource(ConsoleSourceEnum source) {
		this.source = source;
	}

	public String getConsoleName() {
		return consoleName;
	}

	public void setConsoleName(String consoleName) {
		this.consoleName = consoleName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getConsoleId() {
		return consoleId;
	}

	public void setConsoleId(int consoleId) {
		this.consoleId = consoleId;
	}
}
