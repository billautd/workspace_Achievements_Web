package perso.project.ps3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractPSNRequestService;

@ApplicationScoped
public class PS3RequestService extends AbstractPSNRequestService {
	static final String TROPHY_SPLIT_1 = "0000000400000050000000";
	static final String TROPHY_SPLIT_2 = "0000000600000060000000";
	static final String UNLOCKED_STRING = "00000001";
	static final int MIN_LENGTH = 58;
	static final String USER_TROPHY_DATA_NAME = "TROPUSR.DAT";
	static final String GAME_TROPHY_DATA_NAME = "TROPCONF.SFM";

	@Inject
	@ConfigProperty(name = "ps3.html.folder.path")
	private Path ps3HTMLPath;

	@Inject
	@ConfigProperty(name = "ps3.emulator.data")
	private Path ps3EmulatorData;

	@Inject
	@ConfigProperty(name = "ps3.beaten.path")
	private Path ps3BeatenPath;

	@Inject
	@ConfigProperty(name = "ps3.mastered.path")
	private Path ps3MasteredPath;

	@Override
	protected Path getHTMLPath() {
		return ps3HTMLPath;
	}

	@Override
	protected Path getGamesBeatenPath() {
		return ps3BeatenPath;
	}

	@Override
	protected Path getGamesMasteredPath() {
		return ps3MasteredPath;
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.PS3;
	}

	@Override
	protected int getId() {
		return Model.PS3_CONSOLE_ID;
	}

	/**
	 * Code based on<br>
	 * https://github.com/Lacro59/playnite-successstory-plugin/blob/master/source/Clients/Rpcs3Achievements.cs
	 */
	@Override
	protected void parseAchievements(final List<GameData> gameData) {
		for (final File trophyFolder : ps3EmulatorData.toFile().listFiles()) {
			final File[] trophyFolderFiles = trophyFolder.listFiles();
			File gameDataFile = null;
			for (final File trophyFolderFile : trophyFolderFiles) {
				if (trophyFolderFile.getName().equals(GAME_TROPHY_DATA_NAME)) {
					gameDataFile = trophyFolderFile;
					break;
				}
			}
			if (gameDataFile == null) {
				Log.error("Could not find " + GAME_TROPHY_DATA_NAME + " in folder " + trophyFolder.getName());
				continue;
			}

			File userDataFile = null;
			for (final File trophyFolderFile : trophyFolderFiles) {
				if (trophyFolderFile.getName().equals(USER_TROPHY_DATA_NAME)) {
					userDataFile = trophyFolderFile;
					break;
				}
			}
			if (userDataFile == null) {
				Log.error("Could not find " + USER_TROPHY_DATA_NAME + " in folder " + trophyFolder.getName());
				continue;
			}

			final Optional<GameData> gameDataOpt = getGameForFolder(gameData, gameDataFile);
			if (gameDataOpt.isEmpty()) {
				continue;
			}

			parseAchievementsData(gameDataOpt.get(), userDataFile);
		}
	}

	private Optional<GameData> getGameForFolder(final List<GameData> gameData, final File gameDataFile) {
		final XmlMapper xmlMapper = new XmlMapper();
		try (final FileInputStream fis = new FileInputStream(gameDataFile)) {
			final JsonNode node = xmlMapper.reader().readTree(fis);
			final String gameText = node.get("title-name").asText();
			final Optional<GameData> gameDataOpt = gameData.stream()
					.filter(g -> g.getTitle().toLowerCase().equals(gameText.toLowerCase())).findFirst();
			if (gameDataOpt.isEmpty()) {
				Log.error("Could not find game " + gameText);
			}
			return gameDataOpt;
		} catch (IOException e) {
			Log.error("Error reading " + gameDataFile.getName(), e);
			return Optional.empty();
		}
	}

	private GameData parseAchievementsData(final GameData gameData, final File userDataFile) {
		String hexStr = "";
		try (final FileInputStream fis = new FileInputStream(userDataFile)) {
			final byte[] bytes = fis.readAllBytes();
			for (final byte b : bytes) {
				hexStr += String.format("%02x", b);
			}
			System.out.println();
			List<String> allSplits = new ArrayList<>();

			final String[] splits1 = hexStr.split(TROPHY_SPLIT_1);
			for (final String str1 : splits1) {
				final String[] splits2 = str1.split(TROPHY_SPLIT_2);
				for (final String str2 : splits2) {
					allSplits.add(str2);
				}
			}

			int unlockedCount = 0;
			for (final String hexData : allSplits) {
				if (hexData.length() < MIN_LENGTH) {
					continue;
				}
				final String unlockedStr = hexData.substring(18, 26);
				if (unlockedStr.equals(UNLOCKED_STRING)) {
					unlockedCount++;
				}
			}
			gameData.setAwardedAchievements(unlockedCount);
			// If beaten or mastered, already set by other methods
			if (CompletionStatusEnum.NOT_PLAYED.equals(gameData.getCompletionStatus())) {
				if (gameData.getTotalAchievements() == 0) {
					gameData.setCompletionStatus(CompletionStatusEnum.NO_ACHIEVEMENTS);
				} else if (gameData.getAwardedAchievements() == gameData.getTotalAchievements()) {
					gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
				} else if (gameData.getAwardedAchievements() > 0) {
					gameData.setCompletionStatus(CompletionStatusEnum.TRIED);
				}
			}
			Log.info("Found " + unlockedCount + " / " + gameData.getTotalAchievements() + " achievements for "
					+ gameData.getTitle() + " (" + gameData.getId() + ")");
		} catch (final Exception e) {
			Log.error("Error reading " + userDataFile.getName(), e);
		}
		return gameData;
	}

}
