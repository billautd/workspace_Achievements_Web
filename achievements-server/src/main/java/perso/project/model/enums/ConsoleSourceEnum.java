package perso.project.model.enums;

public enum ConsoleSourceEnum {
	RETRO_ACHIEVEMENTS("Retro Achievements"), STEAM("Steam"), PS3("PlayStation 3"), PSVITA("PlayStation Vita"),
	XBOX_360("Xbox 360");

	private final String name;

	private ConsoleSourceEnum(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
