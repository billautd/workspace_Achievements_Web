package perso.project.model.front;

import perso.project.model.back.GameData;

public class GameDataFront {
	private int id = 0;
	private String name = "";

	public GameDataFront(final GameData gameData) {
		setId(gameData.getId());
		setName(gameData.getTitle());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
