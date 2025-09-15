package perso.project.standalone;

import java.io.File;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.quarkus.logging.Log;
import perso.project.model.GameData;

public abstract class AbstractPSNRequestService extends AbstractStandaloneRequestService {
	static final String BULLET_CHARACTER = " • ";

	@Override
	protected void parseDocument(final File htmlFile, final List<GameData> gameData, final Document document) {
		final Element gameList = document.selectFirst("table#game_list");
		final Elements gameTrs = gameList.select("tr");
		for (final Element gameTr : gameTrs) {
			final GameData data = new GameData();
			data.setConsoleId(getId());
			data.setConsoleName(getSource().getName());
			parseGameDataFromTR(data, gameTr);
			// Ignore duplicates for PSN, no way to differentiate
			if (gameData.stream()
					.anyMatch(existing -> existing.getTitle().toLowerCase().equals(data.getTitle().toLowerCase()))) {
				Log.warn("Game " + data.getTitle() + " (" + data.getId() + ") is duplicate for " + getSource().getName()
						+ ". Ignoring.");
				continue;
			}
			model.getConsoleDataMap().get(getId()).getGameDataMap().put(data.getId(), data);
			gameData.add(data);
		}
	}

	private GameData parseGameDataFromTR(final GameData data, final Element gameTr) {
		// Title
		final Element gameDiv = gameTr.selectFirst("div.ellipsis");
		final Element gameSpan = gameDiv.selectFirst("span");
		final String fullText = gameSpan.text();
		final String[] splitText = fullText.split(BULLET_CHARACTER);
		if (splitText.length == 1) {
			data.setTitle(splitText[0]);
		} else if (splitText.length == 2) {
			data.setTitle(splitText[0] + " (" + splitText[1] + ")");
		} else {
			Log.error("Cannot parse game title with full text " + fullText);
			data.setTitle("");
		}

		// Id
		final String gameHref = gameSpan.selectFirst("a").attr("href");
		// Value is /trophies/1234-gametitle
		final int id = Integer.parseInt(gameHref.split("/")[2].split("-")[0]);
		data.setId(id);

		// Trophies total
		final Element trophyCount = gameTr.selectFirst("div.trophy-count");
		final int trophiesTotal = Integer.parseInt(trophyCount.selectFirst("span.small-info").selectFirst("b").text());
		data.setTotalAchievements(trophiesTotal);

		// Points
		final int points = Integer
				.parseInt(trophyCount.selectFirst("span.small-info").select("b").last().text().replace(",", ""));
		data.setTotalPoints(points);

		Log.debug("Getting " + getSource().getName() + " game " + data.getTitle() + " (" + data.getId() + ") with "
				+ data.getTotalAchievements() + " trophies");
		return data;
	}

}
