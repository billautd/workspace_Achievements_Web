package perso.project.standalone;

import java.io.File;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.quarkus.logging.Log;
import perso.project.model.GameData;

public abstract class AbstractXboxRequestService extends AbstractStandaloneRequestService {

	@Override
	protected void parseDocument(final File htmlFile, final List<GameData> gameData, Document document) {
		final Element gameList = document.select("section.section").selectFirst("ul.list");
		final Elements gameLis = gameList.select("li");

		for (final Element gameLi : gameLis) {
			final GameData data = new GameData();
			data.setConsoleId(getId());
			data.setConsoleName(getSource().getName());
			parseGameDataFromLI(data, gameLi);
			// Add region to JP games
			if (htmlFile.getName().startsWith("JP")) {
				data.setTitle(data.getTitle() + " (JP)");
			}
			// Ignore duplicates for Xbox, no way to differentiate
			if (gameData.stream()
					.anyMatch(existing -> existing.getTitle().toLowerCase().equals(data.getTitle().toLowerCase()))) {
				Log.warn("Game " + data.getTitle() + " (" + data.getId() + ") is duplicate for " + getSource().getName()
						+ ". Ignoring.");
				continue;
			}
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
		data.setTruePoints(points);

		Log.debug("Getting " + getSource() + " game " + data.getTitle() + " (" + data.getId() + ") with "
				+ data.getTotalAchievements() + " trophies");
		return data;
	}
}
