package perso.project.ps3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.AchievementData;
import perso.project.model.GameData;
import perso.project.model.Model;
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
	static final String GAME_IMAGE_NAME = "ICON0.PNG";
	static final String TROPHY_ICON_PREFIX = "TROP";
	static final String TROPHY_ICON_SUFFIX = ".png";
	static final String BASE64_PREFIX = "data:image/png;base64,";

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
		readStandaloneGamesByIds();
		for (final File trophyFolder : ps3EmulatorData.toFile().listFiles()) {
			final String gameUUID = trophyFolder.getName();
			// Game data file
			final Path gameDataFile = getGameDataFile(gameUUID);
			if (!gameDataFile.toFile().exists()) {
				Log.error("No game data file for UUID " + gameUUID);
				continue;
			}
			// User data file
			final Path userDataFile = getUserDataFile(gameUUID);
			if (!userDataFile.toFile().exists()) {
				Log.error("No user data file for UUID " + gameUUID);
				continue;
			}

			final Optional<GameData> gameDataOpt = readGameDataFile(gameDataFile.toFile(), gameUUID);
			if (gameDataOpt.isEmpty()) {
				Log.error("Game not found for UUID " + gameUUID);
				continue;
			}
			readAchievementsFile(gameDataOpt.get(), userDataFile.toFile());
			parseAchievementData(gameDataOpt.get());
		}
	}

	private Optional<GameData> readGameDataFile(final File gameDataFile, final String gameUUID) {
		final String gameName = model.getStandaloneGamesByIds().get(gameUUID);
		if (gameName == null) {
			Log.error("Cannot find PSVita game for UUID " + gameUUID);
			return Optional.empty();
		}

		try {
			final Optional<GameData> gameDataOpt = model.getConsoleDataMap().get(Model.PS3_CONSOLE_ID).getGameDataMap()
					.values().stream().filter(g -> g.getTitle().equals(gameName)).findFirst();
			if (gameDataOpt.isEmpty()) {
				Log.error("Could not find PS3 game " + gameName + " for UUID " + gameUUID);
				return gameDataOpt;
			}
			Log.info("Found PS3 game " + gameName + " for UUID " + gameUUID);

			final JsonNode node = xmlMapper.readTree(gameDataFile);
			// Link UUID to game found
			final GameData gameData = gameDataOpt.get();
			gameData.setUUID(gameUUID);

			// Read achievement descriptions
			gameData.getAchievementData().clear();
			final JsonNode trophyNodes = node.get("trophy");
			trophyNodes.forEach(t -> {
				gameData.getAchievementData().add(readAchievementDescription(t));
			});

			return gameDataOpt;
		} catch (IOException e) {
			Log.error("Error reading " + gameDataFile.getName(), e);
			return Optional.empty();
		}
	}

	private AchievementData readAchievementDescription(final JsonNode trophyNode) {
		final AchievementData ach = new AchievementData();
		ach.setId(trophyNode.get("id").asInt());
		ach.setDisplayOrder(ach.getId());
		ach.setDisplayName(trophyNode.get("name").asText());
		ach.setDescription(trophyNode.get("detail").asText());
		// Set points
		ach.setPoints(switch (trophyNode.get("ttype").asText()) {
		case "B" -> 15;
		case "S" -> 30;
		case "G" -> 90;
		case "P" -> 300;
		default -> 0;
		});
		// Ratio is 1, no data for unlock percentage
		ach.setRealPoints(ach.getPoints());

		return ach;
	}

	private GameData readAchievementsFile(final GameData gameData, final File userDataFile) {
		String hexStr = "";
		try (final FileInputStream fis = new FileInputStream(userDataFile)) {
			final byte[] bytes = fis.readAllBytes();
			for (final byte b : bytes) {
				hexStr += String.format("%02x", b);
			}
			List<String> allSplits = new ArrayList<>();

			final String[] splits1 = hexStr.split(TROPHY_SPLIT_1);
			for (final String str1 : splits1) {
				final String[] splits2 = str1.split(TROPHY_SPLIT_2);
				for (final String str2 : splits2) {
					allSplits.add(str2);
				}
			}

			for (final String hexData : allSplits) {
				if (hexData.length() < MIN_LENGTH) {
					continue;
				}
				final int achievementId = Integer.parseInt(hexData.substring(0, 2), 16);
				final Optional<AchievementData> achOpt = gameData.getAchievementData().stream()
						.filter(ach -> ach.getId() == achievementId).findAny();
				if (achOpt.isEmpty()) {
					Log.error("Game " + gameData.getTitle() + " : Achievement " + achievementId + " not found");
					continue;
				}

				final String unlockedStr = hexData.substring(18, 26);
				achOpt.get().setAchieved(UNLOCKED_STRING.equals(unlockedStr));
			}

		} catch (final Exception e) {
			Log.error("Error reading " + userDataFile.getName(), e);
		}
		return gameData;
	}

	private GameData parseAchievementData(final GameData gameData) {
		gameData.setTotalAchievements(gameData.getAchievementData().size());
		gameData.setAwardedAchievements(
				(int) gameData.getAchievementData().stream().filter(ach -> ach.isAchieved()).count());

		// Parse completion status
		parseCompletionStatus(gameData);

		// Parse achievements percentage and points
		setGameAchievementPercent(gameData);

		// Parse total achievement data
		gameData.setTotalPoints(gameData.getAchievementData().stream().mapToInt(AchievementData::getPoints).sum());
		gameData.setTruePoints(gameData.getAchievementData().stream().mapToInt(AchievementData::getRealPoints).sum());
		if (gameData.getTotalPoints() != 0) {
			gameData.setRatio((double) gameData.getTruePoints() / gameData.getTotalPoints());
		} else {
			gameData.setRatio(0);
		}

		// Parse earned achievement data
		gameData.setEarnedPoints(gameData.getAchievementData().stream().filter(AchievementData::isAchieved)
				.mapToInt(AchievementData::getPoints).sum());
		gameData.setEarnedTruePoints(gameData.getAchievementData().stream().filter(AchievementData::isAchieved)
				.mapToInt(AchievementData::getRealPoints).sum());
		if (gameData.getEarnedPoints() != 0) {
			gameData.setEarnedRatio((double) gameData.getEarnedTruePoints() / gameData.getEarnedPoints());
		} else {
			gameData.setEarnedRatio(0);
		}

		Log.info(gameData.getTitle() + " (" + gameData.getId() + ") for PS3 is " + gameData.getCompletionStatus()
				+ " with " + gameData.getAwardedAchievements() + " / " + gameData.getTotalAchievements()
				+ " achievements and " + gameData.getTotalPoints() + " (" + gameData.getTruePoints() + ") points");
		return gameData;
	}

	public GameData getFullGameData(final int gameId) {
		Log.info("Getting full game data for PS3 game " + gameId);
		final GameData existingGameData = model.getConsoleDataMap().get(Model.PS3_CONSOLE_ID).getGameDataMap()
				.get(gameId);
		if (existingGameData == null) {
			Log.error("No PS3 game found for id " + gameId);
			return null;
		}

		parseAchievements(
				model.getConsoleDataMap().get(Model.PS3_CONSOLE_ID).getGameDataMap().values().stream().toList());
		// Set local images as base64
		parseImages(existingGameData);

		return existingGameData;
	}

	private GameData parseImages(final GameData gameData) {
		// Game image
		try {
			final byte[] imageBytes = Files.readAllBytes(getGameImagePath(gameData.getUUID()));
			final String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
			gameData.setImageURL(BASE64_PREFIX + imageBase64);
		} catch (IOException e) {
			Log.error("Cannot convert game image to Base64 for game " + gameData.getTitle());
			return gameData;
		}

		// Achievement icons
		gameData.getAchievementData().forEach(ach -> {
			try {
				final byte[] iconBytes = Files.readAllBytes(getTrophyIconPath(gameData.getUUID(), ach.getId()));
				final String iconBase64 = Base64.getEncoder().encodeToString(iconBytes);
				ach.setIconLockedURL(BASE64_PREFIX + iconBase64);
				ach.setIconUnlockedURL(BASE64_PREFIX + iconBase64);
			} catch (IOException e) {
				Log.error("Cannot convert game image to Base64 for game " + gameData.getTitle());
			}
		});

		return gameData;
	}

	private Path getGameDataFile(final String gameUUID) {
		return Path.of(ps3EmulatorData + "\\" + gameUUID + "\\" + GAME_TROPHY_DATA_NAME);
	}

	private Path getUserDataFile(final String gameUUID) {
		return Path.of(ps3EmulatorData + "\\" + gameUUID + "\\" + USER_TROPHY_DATA_NAME);
	}

	private Path getGameImagePath(final String gameUUID) {
		return Path.of(ps3EmulatorData + "\\" + gameUUID + "\\" + GAME_IMAGE_NAME);
	}

	private Path getTrophyIconPath(final String gameUUID, final int achievementId) {
		return Path.of(ps3EmulatorData + "\\" + gameUUID + "\\" + TROPHY_ICON_PREFIX
				+ String.format("%03d", achievementId) + TROPHY_ICON_SUFFIX);
	}

}
