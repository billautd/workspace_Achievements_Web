package perso.project.psvita;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import perso.project.utils.GenericUtils;

@ApplicationScoped
public class PSVitaRequestService extends AbstractPSNRequestService {
	static final String USER_TROPHY_DATA_NAME = "TROPUSR.DAT";
	static final String GAME_TROPHY_DATA_NAME = "TROP.SFM";
	static final String GAME_IMAGE_NAME = "ICON0.PNG";
	static final String TROPHY_ICON_PREFIX = "TROP";
	static final String TROPHY_ICON_SUFFIX = ".png";
	static final String BASE64_PREFIX = "data:image/png;base64,";

	@Inject
	@ConfigProperty(name = "psvita.emulator.game.data")
	private Path psVitaEmulatorGameData;

	@Inject
	@ConfigProperty(name = "psvita.emulator.user.data")
	private Path psVitaEmulatorUserData;

	@Inject
	@ConfigProperty(name = "psvita.html.folder.path")
	private Path psVitaHTMLPath;

	@Inject
	@ConfigProperty(name = "psvita.beaten.path")
	private Path psVitaBeatenPath;

	@Inject
	@ConfigProperty(name = "psvita.mastered.path")
	private Path psVitaMasteredPath;

	@Override
	protected Path getHTMLPath() {
		return psVitaHTMLPath;
	}

	@Override
	protected Path getGamesBeatenPath() {
		return psVitaBeatenPath;
	}

	@Override
	protected Path getGamesMasteredPath() {
		return psVitaMasteredPath;
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.PSVITA;
	}

	@Override
	protected int getId() {
		return Model.PSVITA_CONSOLE_ID;
	}

	@Override
	protected void parseAchievements(final List<GameData> gameData) {
		readStandaloneGamesByIds();
		for (final File appFolder : psVitaEmulatorGameData.toFile().listFiles()) {
			final String gameUUID = appFolder.getName();
			// Game data file
			final Path gameDataFile = getGameDataFile(gameUUID);
			if (!gameDataFile.toFile().exists()) {
				Log.error("No game data file for UUID " + gameUUID);
				continue;
			}

			// User data file
			final Path userDataFile = getUserDataFile(gameUUID);
			if (!userDataFile.toFile().exists()) {
				Log.error("No user data file for UUID2 " + gameUUID);
				continue;
			}

			final Optional<GameData> gameDataOpt = readGameDataFile(gameDataFile.toFile(), gameUUID);
			if (gameDataOpt.isEmpty()) {
				continue;
			}
			readAchievementsFile(gameDataOpt.get(), userDataFile.toFile());
			parseAchievementData(gameDataOpt.get());
		}
	}

	public GameData getFullGameData(final int gameId) {
		Log.info("Getting full game data for PSVita game " + gameId);
		final GameData existingGameData = model.getConsoleDataMap().get(Model.PSVITA_CONSOLE_ID).getGameDataMap()
				.get(gameId);
		if (existingGameData == null) {
			Log.error("No PSVita game found for id " + gameId);
			return null;
		}

		final String gameUUID = existingGameData.getUUID();

		// Game data file
		final Path gameDataFile = getGameDataFile(gameUUID);
		if (!gameDataFile.toFile().exists()) {
			Log.error("No game data file for UUID " + gameUUID);
			return existingGameData;
		}

		// User data file
		final Path userDataFile = getUserDataFile(gameUUID);
		if (!userDataFile.toFile().exists()) {
			Log.error("No user data file for UUID2 " + gameUUID);
			return existingGameData;
		}

		readGameDataFile(gameDataFile.toFile(), existingGameData.getUUID());
		readAchievementsFile(existingGameData, userDataFile.toFile());
		parseAchievementData(existingGameData);
		parseImages(existingGameData);

		return existingGameData;
	}

	private Optional<GameData> readGameDataFile(final File gameDataFile, final String gameUUID) {
		final String gameName = model.getStandaloneGamesByIds().get(gameUUID);
		if (gameName == null) {
			Log.error("Cannot find PSVita game for UUID " + gameUUID);
			return Optional.empty();
		}

		try {
			final Optional<GameData> gameDataOpt = model.getConsoleDataMap().get(Model.PSVITA_CONSOLE_ID)
					.getGameDataMap().values().stream().filter(g -> g.getTitle().equals(gameName)).findFirst();
			if (gameDataOpt.isEmpty()) {
				Log.error("Could not find PSVita game " + gameName + " for UUID " + gameUUID);
				return gameDataOpt;
			}
			Log.info("Found PSVita game " + gameName + " for UUID " + gameUUID);

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
			// Parse file
			// Find string with format 0e000000000000000000000
			// Each trophy is a 1 in binary
			// Always read by groups of 2 bytes to then reverse the groups of 8 binary
			// values
			final int bytesToRead = (int) (2 * Math.ceil(gameData.getAchievementData().size() / 8d));
			final String trophyProgressString = hexStr.substring(8, 8 + bytesToRead);
			final String binaryProgressString = GenericUtils.hexToBin(trophyProgressString);
			String reversedProgressString = "";
			for (int i = 0; i < bytesToRead / 2; i++) {
				final String substringI = binaryProgressString.substring(8 * i, 8 * (i + 1));
				reversedProgressString += new StringBuffer(substringI).reverse();
			}
			// reversedProgressString contains a list of binary values ordered by ids of
			// achievements
			// 1 is unlocked, 0 is locked
			for (final AchievementData ach : gameData.getAchievementData()) {
				final boolean isEarned = reversedProgressString.substring(ach.getId(), ach.getId() + 1).equals("1");
				ach.setAchieved(isEarned);
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
			gameData.setRatio(1);
		}

		// Parse earned achievement data
		gameData.setEarnedPoints(gameData.getAchievementData().stream().filter(AchievementData::isAchieved)
				.mapToInt(AchievementData::getPoints).sum());
		gameData.setEarnedTruePoints(gameData.getAchievementData().stream().filter(AchievementData::isAchieved)
				.mapToInt(AchievementData::getRealPoints).sum());
		if (gameData.getEarnedPoints() != 0) {
			gameData.setEarnedRatio((double) gameData.getEarnedTruePoints() / gameData.getEarnedPoints());
		} else {
			gameData.setEarnedRatio(1);
		}

		Log.info(gameData.getTitle() + " (" + gameData.getId() + ") for PSVita is " + gameData.getCompletionStatus()
				+ " with " + gameData.getAwardedAchievements() + " / " + gameData.getTotalAchievements()
				+ " achievements and " + gameData.getTotalPoints() + " (" + gameData.getTruePoints() + ") points");
		return gameData;
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
		return Path.of(psVitaEmulatorGameData + "\\" + gameUUID + "\\" + GAME_TROPHY_DATA_NAME);
	}

	private Path getUserDataFile(final String gameUUID) {
		return Path.of(psVitaEmulatorUserData + "\\" + gameUUID + "\\" + USER_TROPHY_DATA_NAME);
	}

	private Path getGameImagePath(final String gameUUID) {
		return Path.of(psVitaEmulatorGameData + "\\" + gameUUID + "\\" + GAME_IMAGE_NAME);
	}

	private Path getTrophyIconPath(final String gameUUID, final int achievementId) {
		return Path.of(psVitaEmulatorGameData + "\\" + gameUUID + "\\" + TROPHY_ICON_PREFIX
				+ String.format("%03d", achievementId) + TROPHY_ICON_SUFFIX);
	}
}
