package perso.project.xbox360;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractXboxRequestService;

@ApplicationScoped
public class Xbox360RequestService extends AbstractXboxRequestService {
	static final String PROFILE_INFO_ID = "00010000";
	static final String EXTENSION = "gpd";

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

		final List<String> gameIds = new ArrayList<>();
		File profileInfoFile = null;
		for (final File gameInfoFile : profileInfoFolder.listFiles()) {
			// Do not parse non GPD files
			if (!FilenameUtils.getExtension(gameInfoFile.getName()).equals(EXTENSION)) {
				continue;
			}
			if (FilenameUtils.removeExtension(gameInfoFile.getName()).equals(profileId)) {
				profileInfoFile = gameInfoFile;
			} else {
				gameIds.add(FilenameUtils.removeExtension(gameInfoFile.getName()));
			}
		}

		if (profileInfoFile == null) {
			Log.error("Could not find profile info file for id " + profileId);
			return;
		}
		parseProfileInfoFile(profileInfoFile, gameIds, gameData);
	}

	private void parseProfileInfoFile(final File profileInfoFile, final List<String> gameIds,
			final List<GameData> gameData) {
		String hexStr = "";
		try (final FileInputStream fis = new FileInputStream(profileInfoFile)) {
			final byte[] bytes = fis.readAllBytes();
			for (final byte b : bytes) {
				hexStr += String.format("%02x", b).toUpperCase();
			}
		} catch (final IOException e) {
			Log.error("Could not read " + profileInfoFile);
		}

		for (final String gameId : gameIds) {
			currentIndex = 0;
			final int gameIdIndex = hexStr.lastIndexOf(gameId);
			final String shortenedStr = hexStr.substring(gameIdIndex);
			// Read X_XDBF_GPD_TITLE_PLAYED struct from Xenia in gpd_info.h
			// Game id
			readBytes(shortenedStr, 8);
			// Achievement count
			readBytes(shortenedStr, 8);
			// Achievement unlocked
			final String achievementUnlockedStr = readBytes(shortenedStr, 8);
			// Gamerscore total
			readBytes(shortenedStr, 8);
			// Gamerscore earned
			readBytes(shortenedStr, 8);
			// Online achievement count
			readBytes(shortenedStr, 4);
			// Avatar awards
			readBytes(shortenedStr, 4);
			readBytes(shortenedStr, 4);
			readBytes(shortenedStr, 4);
			// Flags
			readBytes(shortenedStr, 8);
			// Filetime
			readBytes(shortenedStr, 16);
			// Game name
			final String gameNameStr = shortenedStr.substring(currentIndex).split("0000")[0].replace("00", "");
			String gameName;
			try {
				gameName = new String(Hex.decodeHex(gameNameStr.toCharArray()));
			} catch (DecoderException e) {
				Log.error("Could not parse hex string " + gameNameStr);
				continue;
			}

			final Optional<GameData> gameDataOpt = getGameForName(gameData, gameName);
			if (gameDataOpt.isEmpty()) {
				continue;
			}

			final GameData data = gameDataOpt.get();
			data.setAwardedAchievements(Integer.parseInt(achievementUnlockedStr, 16));
			parseCompletionStatus(data);
			Log.info("Found " + data.getAwardedAchievements() + " / " + data.getTotalAchievements()
					+ " achievements for " + data.getTitle() + " (" + data.getId() + ")");
		}
	}

	private Optional<GameData> getGameForName(final List<GameData> gameData, final String gameName) {
		final Optional<GameData> gameDataOpt = gameData.stream()
				.filter(g -> g.getTitle().toLowerCase().equals(gameName.toLowerCase())).findFirst();
		if (gameDataOpt.isEmpty()) {
			Log.error("Could not find game " + gameName + ".");
		} else {
			Log.info("Found game " + gameName);
		}
		return gameDataOpt;
	}

	private String readBytes(String str, final int nbr) {
		final String sub = str.substring(currentIndex, currentIndex + nbr);
		currentIndex += nbr;
		return sub;
	}
}
