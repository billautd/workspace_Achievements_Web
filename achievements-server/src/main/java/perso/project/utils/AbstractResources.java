package perso.project.utils;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import perso.project.model.CompareData;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.MainModel;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.playnite.PlayniteService;
import perso.project.steam.SteamRequestService;

public abstract class AbstractResources {
	@Inject
	PlayniteService playniteService;

	@Inject
	DatabaseService databaseService;

	@Inject
	SteamRequestService rService;

	@Inject
	MainModel model;

	@Inject
	@ConfigProperty(name = "playnite.data.path")
	java.nio.file.Path playniteDataPath;

	protected abstract ConsoleSourceEnum getSource();

	protected String getConsoleIds(final AbstractRequestService requestService) throws JsonProcessingException {
		final List<ConsoleData> data = requestService.getConsoleIds();
		Log.info("Returning " + getSource() + " console data. Found " + data.size() + " consoles");
		return requestService.getMapper().writeValueAsString(data);
	}

	protected String getCompareData(final AbstractRequestService requestService,
			final AbstractCompareService compareService) throws JsonProcessingException {
		playniteService.getPlayniteData(playniteDataPath);
		final List<CompareData> data = compareService.getCompareData();
		Log.info("Returning " + getSource() + " " + data.size() + " compare data");
		return requestService.getMapper().writeValueAsString(data);
	}

	protected String getExistingData(final AbstractRequestService requestService, final Path databasePath)
			throws JsonProcessingException {
		// Read database file
		databaseService.readDatabase(requestService, databasePath);

		final List<GameData> data = model.getGameDataForSources(List.of(getSource()));
		Log.info("Returning " + data.size() + " existing " + getSource() + " games");
		return requestService.getMapper().writeValueAsString(data);
	}

	protected String writeDatabase(final Path path) {
		Log.info("Writing database for " + getSource());
		databaseService.writeDatabase(getSource(), path);
		return "{}";
	}
}
