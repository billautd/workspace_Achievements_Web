package perso.project.standalone;

import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.quarkus.logging.Log;
import perso.project.model.GameData;

public abstract class AbstractXboxRequestService extends AbstractStandaloneRequestService {

	static final String BULLET_CHARACTER = " • ";

	@Override
	protected void parseDocument(final List<GameData> gameData, Document document) {
		final Element gameList = document.select("section.section").selectFirst("ul.list");
		final Elements gameLis = gameList.select("li");
		for (final Element gameLi : gameLis) {
			final GameData data = new GameData();
			data.setConsoleId(getId());
			data.setConsoleName(getSource().getName());
			parseGameDataFromLI(data, gameLi);
			gameData.add(data);
			model.getConsoleDataMap().get(getId()).getGameDataMap().put(data.getId(), data);
		}
	}

	private GameData parseGameDataFromLI(final GameData data, final Element gameTr) {
		// Title
		final Element gameText = gameTr.selectFirst("h4.h-5");
		data.setTitle(gameText.text());

		// Id
		data.setId(id++);

		// Trophies total
		final Element trophyCount = gameTr.selectFirst("div.achievements");
		final String[] splitStr = trophyCount.text().split(" / ");
		final int total = Integer.parseInt(splitStr[0]);
		data.setTotalAchievements(total);

		// Points
		final int points = Integer.parseInt(splitStr[1]);
		data.setTotalPoints(points);

		Log.debug("Getting " + getSource() + " game " + data.getTitle() + " (" + data.getId() + ") with "
				+ data.getTotalAchievements() + " trophies");
		return data;
	}
}
