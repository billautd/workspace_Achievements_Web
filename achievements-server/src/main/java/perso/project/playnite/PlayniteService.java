package perso.project.playnite;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvException;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.Model;
import perso.project.model.PlayniteGameData;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;

@ApplicationScoped
public class PlayniteService {
	@Inject
	Model model;

	public void getPlayniteData(final Path path) {
		model.getPlayniteData().clear();
		final RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
		try (final FileReader fileReader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
			final CSVReader reader = new CSVReaderBuilder(fileReader).withCSVParser(rfc4180Parser).build();
			// Read headers
			reader.readNextSilently();
			final List<String[]> stringList = reader.readAll();
			for (final String[] str : stringList) {
				final PlayniteGameData playniteData = new PlayniteGameData();
				playniteData.setName(str[0]);
				playniteData.setGameId(str[1]);
				playniteData.setPlatform(str[3]);
				mapSource(playniteData, str[4]);
				mapCompletionStatus(playniteData, str[2]);
				model.getPlayniteData().put(playniteData.getGameId(), playniteData);
			}
			Log.info("Found Playnite data with " + model.getPlayniteData().size() + " games");
		} catch (final IOException | CsvException e) {
			Log.error("Error reading file at " + path);
		}
	}

	private PlayniteGameData mapCompletionStatus(final PlayniteGameData data, final String playniteStatus) {
		switch (playniteStatus) {
		case "1 - Playing":
			data.setCompletionStatus(CompletionStatusEnum.PLAYING);
			break;
		case "2 - Not Played":
			data.setCompletionStatus(CompletionStatusEnum.NOT_PLAYED);
			break;
		case "3 - Tried":
			data.setCompletionStatus(CompletionStatusEnum.TRIED);
			break;
		case "4 - Beaten":
			data.setCompletionStatus(CompletionStatusEnum.BEATEN);
			break;
		case "5 - Mastered":
			data.setCompletionStatus(CompletionStatusEnum.MASTERED);
			break;
		case "6 - No Achievements & Not Interested":
			data.setCompletionStatus(CompletionStatusEnum.NO_ACHIEVEMENTS);
			break;
		case "7 - Cannot Play":
			data.setCompletionStatus(CompletionStatusEnum.CANNOT_PLAY);
			break;
		default:
			Log.error("Playnite completion status " + playniteStatus + " not managed");
			return data;
		}
		Log.debug(
				"Playnite game " + data.getName() + " for " + data.getPlatform() + " is " + data.getCompletionStatus());
		return data;
	}

	private PlayniteGameData mapSource(final PlayniteGameData data, final String playniteSource) {
		if ("Steam".equals(playniteSource)) {
			data.setSource(ConsoleSourceEnum.STEAM);
		} else if ("RetroAchievements".equals(playniteSource)) {
			data.setSource(ConsoleSourceEnum.RETRO_ACHIEVEMENTS);
		} else if ("Standalone".equals(playniteSource)) {
			if ("PlayStation 3".equals(data.getPlatform())) {
				data.setSource(ConsoleSourceEnum.PS3);
			} else if ("PlayStation Vita".equals(data.getPlatform())) {
				data.setSource(ConsoleSourceEnum.PSVITA);
			}
		} else {
			Log.error("Playnite source " + playniteSource + " not managed");
			return data;
		}
		Log.debug("Playnite game " + data.getName() + " for " + data.getPlatform() + " is from " + data.getSource());
		return data;
	}
}
