package perso.project.model;

import perso.project.model.enums.CompletionStatusEnum;

public class LocalGameData {
	private String name = "";
	private CompletionStatusEnum status = CompletionStatusEnum.NOT_PLAYED;

	public LocalGameData() {

	}

	public LocalGameData(final String name, final CompletionStatusEnum status) {
		this.name = name;
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CompletionStatusEnum getStatus() {
		return status;
	}

	public void setStatus(CompletionStatusEnum status) {
		this.status = status;
	}
}
