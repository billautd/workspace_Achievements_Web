package perso.project.xbox360;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.AchievementData;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractXboxRequestService;
import perso.project.utils.GenericUtils;

@ApplicationScoped
public class Xbox360RequestService extends AbstractXboxRequestService {
	static final String PROFILE_INFO_ID = "00010000";
	static final String EXTENSION = "gpd";
	static final String XDBF_MAGIC = "58444246";
	static final String ACHIEVEMENT_MAGIC = "0000001C";
	static final int ENTRY_SIZE = 36;
	static final int FREE_ENTRY_SIZE = 16;
	static final String NULL_SEPARATION = "0000";
	static final int UNLOCKED_FLAG = 0x20000;

	int currentIndex = 0;

	@Inject
	@ConfigProperty(name = "xbox360.emulator.data")
	private Path xboxEmulatorData;

	@Inject
	@ConfigProperty(name = "xbox360.html.folder.path")
	private Path xbox360HTMLPath;

	@Inject
	@ConfigProperty(name = "xbox360.beaten.path")
	private Path xbox360BeatenPath;

	@Inject
	@ConfigProperty(name = "xbox360.mastered.path")
	private Path xbox360MasteredPath;

	@Override
	protected Path getHTMLPath() {
		return xbox360HTMLPath;
	}

	@Override
	protected Path getGamesBeatenPath() {
		return xbox360BeatenPath;
	}

	@Override
	protected Path getGamesMasteredPath() {
		return xbox360MasteredPath;
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.XBOX_360;
	}

	@Override
	protected int getId() {
		return Model.XBOX360_CONSOLE_ID;
	}

	@Override
	protected void parseAchievements(final List<GameData> gameData) {
		readStandaloneGamesByIds();
		// Take first profile
		final File profileFolder = xboxEmulatorData.toFile().listFiles()[0];
		// Find folder for profile info
		File profileInfoFolder = null;
		String profileId = "";
		for (final File profileSubFolder : profileFolder.listFiles()) {
			if (profileSubFolder.listFiles().length == 1
					&& profileSubFolder.listFiles()[0].getName().equals(PROFILE_INFO_ID)) {
				profileId = profileSubFolder.getName();
				profileInfoFolder = profileSubFolder.listFiles()[0].listFiles()[0];
			}
		}
		if (profileInfoFolder == null) {
			Log.error("Could not find Xenia profile");
			return;
		}

		for (final File gameDataFile : profileInfoFolder.listFiles()) {
			// Do not parse non GPD files
			if (!FilenameUtils.getExtension(gameDataFile.getName()).equals(EXTENSION)
					// Ignore profile file
					|| FilenameUtils.removeExtension(gameDataFile.getName()).equals(profileId)) {
				continue;
			}

			final String gameUUID = FilenameUtils.removeExtension(gameDataFile.getName());
			final String gameName = model.getStandaloneGamesByIds().get(gameUUID);
			if (gameName == null) {
				Log.error("No Xbox360 game found for UUID " + gameUUID);
				continue;
			}
			final Optional<GameData> gameDataOpt = model.getConsoleDataMap().get(Model.XBOX360_CONSOLE_ID)
					.getGameDataMap().values().stream().filter(g -> g.getTitle().equals(gameName)).findFirst();
			if (gameDataOpt.isEmpty()) {
				Log.error("No Xbox360 game found for name " + gameName + " and UUID " + gameUUID);
				continue;
			}
			// Link game to UUID
			gameDataOpt.get().setUUID(gameUUID);

			readAchievementsFile(gameDataOpt.get(), gameDataFile);
			parseAchievementData(gameDataOpt.get());
		}
	}

	private GameData readAchievementsFile(final GameData gameData, final File gameDataFile) {
		String hexStr = "";
		try (final FileInputStream fis = new FileInputStream(gameDataFile)) {
			final byte[] bytes = fis.readAllBytes();
			for (final byte b : bytes) {
				hexStr += String.format("%02x", b).toUpperCase();
			}
		} catch (final IOException e) {
			Log.error("Could not read " + gameDataFile);
		}
		currentIndex = 0;
		// Read XdbfHeader struct from Xenia in xdbf_io.h
		final String magic = readBytes(hexStr, 8);
		if (!XDBF_MAGIC.equals(magic)) {
			Log.error("Incorrect XDBF magic " + magic);
			return gameData;
		}
		// Version, unused
		readBytes(hexStr, 8);
		final int entryCount = Integer.parseInt(readBytes(hexStr, 8), 16);
		// Entry used, unused. We only use total entry count
		readBytes(hexStr, 8);
		final int freeCount = Integer.parseInt(readBytes(hexStr, 8), 16);
		// Free used, unused. We only use total free count
		readBytes(hexStr, 8);

		// Now, since we have the number of entries in first part of DBF file, we can
		// skip ahead to achievement data
		readBytes(hexStr, entryCount * ENTRY_SIZE);
		readBytes(hexStr, freeCount * FREE_ENTRY_SIZE);

		final String achievementsHexStr = hexStr.substring(currentIndex);
		currentIndex = 0;
		// We now are at the start of the achievement data
		readAchievementsDataFromFile(gameData, achievementsHexStr);

		return gameData;
	}

	private GameData readAchievementsDataFromFile(final GameData gameData, final String hexStr) {
		gameData.getAchievementData().clear();
		String achievementMagic = readBytes(hexStr, 8);
		while (ACHIEVEMENT_MAGIC.equals(achievementMagic)) {
			final AchievementData ach = new AchievementData();
			final int achievementId = Integer.parseInt(readBytes(hexStr, 8), 16);
			// Image id, unused because images are in game file, not emulator
			readBytes(hexStr, 8);
			final int points = Integer.parseInt(readBytes(hexStr, 8), 16);
			final int flags = Integer.parseInt(readBytes(hexStr, 8), 16);
			// Unlock time, unused
			readBytes(hexStr, 16);
			final String title = readUntilNull(hexStr);
			// Unlocked description, unused
			readUntilNull(hexStr);
			final String description = readUntilNull(hexStr);

			ach.setId(achievementId);
			ach.setDisplayOrder(ach.getId());
			ach.setAchieved((flags & UNLOCKED_FLAG) == UNLOCKED_FLAG);
			ach.setPoints(points);
			ach.setRealPoints(ach.getPoints());
			ach.setDisplayName(GenericUtils.hexToAscii(title));
			ach.setDescription(GenericUtils.hexToAscii(description));

			gameData.getAchievementData().add(ach);

			achievementMagic = readBytes(hexStr, 8);
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

		Log.info(gameData.getTitle() + " (" + gameData.getId() + ") for Xbox 360 is " + gameData.getCompletionStatus()
				+ " with " + gameData.getAwardedAchievements() + " / " + gameData.getTotalAchievements()
				+ " achievements and " + gameData.getTotalPoints() + " (" + gameData.getTruePoints() + ") points");
		return gameData;
	}

	public GameData getFullGameData(final int gameId) {
		Log.info("Getting full game data for Xbox 360 game " + gameId);
		final GameData existingGameData = model.getConsoleDataMap().get(Model.XBOX360_CONSOLE_ID).getGameDataMap()
				.get(gameId);
		if (existingGameData == null) {
			Log.error("No Xbox 360 game found for id " + gameId);
			return null;
		}

		parseAchievements(
				model.getConsoleDataMap().get(Model.XBOX360_CONSOLE_ID).getGameDataMap().values().stream().toList());
		// Images for Xenia games are located within the game itself, so we can't get
		// images
		parseImages(existingGameData);
		return existingGameData;
	}

	private GameData parseImages(final GameData gameData) {
		gameData.setImageURL(Model.DEFAULT_GAME_IMAGE);
		gameData.getAchievementData().forEach(ach -> {
			ach.setIconLockedURL(Model.DEFAULT_LOCKED_ICON);
			ach.setIconUnlockedURL(Model.DEFAULT_UNLOCKED_ICON);
		});

		return gameData;
	}

	private String readBytes(final String str, final int nbr) {
		final String sub = str.substring(currentIndex, currentIndex + nbr);
		currentIndex += nbr;
		return sub;
	}

	private String readUntilNull(final String string) {
		// String read is in format 004800560057
		// We read 4 bytes to check for null separato but ignore the 00 ones
		String totalRead = "";
		String byteRead = readBytes(string, 4);
		while (!NULL_SEPARATION.equals(byteRead)) {
			totalRead += byteRead.substring(2);
			byteRead = readBytes(string, 4);
		}
		return totalRead;
	}
}
