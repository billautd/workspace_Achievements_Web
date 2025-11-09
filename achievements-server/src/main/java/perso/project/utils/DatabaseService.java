package perso.project.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;

@ApplicationScoped
public class DatabaseService {
	@Inject
	private Model model;

	private ObjectMapper mapper = new ObjectMapper();

	public void writeDatabase(final ConsoleSourceEnum source, final Path path) {
		final List<GameData> gameDataForSources = model.getGameDataForSources(List.of(source));
		try {
			Log.info("Write " + source + " source to database at " + path);
			mapper.writer(new DefaultPrettyPrinter()).writeValue(path.toFile(), gameDataForSources);
		} catch (IOException e) {
			Log.error("Cannot write source " + source + " data in " + path);
		}
	}

	public void readDatabase(final AbstractRequestService requestService, final Path path) {
		// Setup console ids
		requestService.getConsoleIds();
		try (final FileInputStream fis = new FileInputStream(path.toFile())) {
			final List<GameData> data = mapper.readValue(fis, new TypeReference<List<GameData>>() {
			});
			data.forEach(game -> {
				final ConsoleData console = model.getConsoleDataMap().get(game.getConsoleId());
				if (console == null) {
					Log.error(
							"No console found for game : " + game.getConsoleName() + " (" + game.getConsoleId() + ")");
					return;
				}
				console.getGameDataMap().put(game.getId(), game);
			});
		} catch (IOException e) {
			Log.error("Cannot read file " + path, e);
		}
	}
}
