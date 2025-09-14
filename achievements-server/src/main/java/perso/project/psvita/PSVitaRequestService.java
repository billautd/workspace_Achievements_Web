package perso.project.psvita;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractPSNRequestService;

@ApplicationScoped
public class PSVitaRequestService extends AbstractPSNRequestService {
	static final String START_TITLE_NAME = "<title-name>";
	static final String END_TITLE_NAME = "</title-name>";
	static final String USER_TROPHY_DATA_NAME = "TROPUSR.DAT";
	static final String GAME_TROPHY_DATA_NAME = "TROPHY.TRP";

	@Inject
	@ConfigProperty(name = "psvita.emulator.user.data")
	private Path psVitaEmulatorUserData;

	@Inject
	@ConfigProperty(name = "psvita.emulator.app.data")
	private Path psVitaEmulatorAppData;

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

	/**
	 * Code based on<br>
	 * https://github.com/Lacro59/playnite-successstory-plugin/blob/master/source/Clients/Rpcs3Achievements.cs
	 */
	@Override
	protected void parseAchievements(final List<GameData> gameData) {
		for (final File appFolder : psVitaEmulatorAppData.toFile().listFiles()) {
			// Get first id in folder (for example : NPWR06103_00)
			final File trophyFolder = new File(appFolder.toPath() + "\\sce_sys\\trophy").listFiles()[0];
			File gameDataFile = null;
			for (final File trophyFolderFile : trophyFolder.listFiles()) {
				if (trophyFolderFile.getName().equals(GAME_TROPHY_DATA_NAME)) {
					gameDataFile = trophyFolderFile;
					break;
				}
			}
			if (gameDataFile == null) {
				Log.error("Could not find " + GAME_TROPHY_DATA_NAME + " in folder " + appFolder.getName());
				continue;
			}

			File userDataFile = null;
			final File userDataFolder = new File(psVitaEmulatorUserData.toString() + "\\" + trophyFolder.getName());
			for (final File trophyFolderFile : userDataFolder.listFiles()) {
				if (trophyFolderFile.getName().equals(USER_TROPHY_DATA_NAME)) {
					userDataFile = trophyFolderFile;
					break;
				}
			}
			if (userDataFile == null) {
				Log.error("Could not find " + USER_TROPHY_DATA_NAME + " in folder " + trophyFolder.getName());
				continue;
			}

			final String region = parseRegion(appFolder.getName());
			final Optional<GameData> gameDataOpt = getGameForFolder(gameData, gameDataFile, region);
			if (gameDataOpt.isEmpty()) {
				continue;
			}

			parseAchievementsData(gameDataOpt.get(), userDataFile);
		}
	}

	private Optional<GameData> getGameForFolder(final List<GameData> gameData, final File gameDataFile,
			final String region) {
		try (final FileInputStream fis = new FileInputStream(gameDataFile)) {
			final String fileText = new String(fis.readAllBytes());
			final String gameText = fileText.split(START_TITLE_NAME)[1].split(END_TITLE_NAME)[0];
			final String gameTextWithRegion = gameText + " (" + region + ")";
			Optional<GameData> gameDataOpt = gameData.stream()
					.filter(g -> g.getTitle().toLowerCase().equals(gameText.toLowerCase())).findFirst();
			if (gameDataOpt.isEmpty()) {
				Log.error("Could not find game " + gameText + ". Trying to find by region");
				gameDataOpt = gameData.stream()
						.filter(g -> g.getTitle().toLowerCase().equals(gameTextWithRegion.toLowerCase())).findFirst();
				if (gameDataOpt.isEmpty()) {
					Log.error("Could not find game with region for text " + gameTextWithRegion);
					return Optional.empty();
				} else {
					Log.info("Found game " + gameDataOpt.get().getTitle());
				}
			} else {
				Log.info("Found game " + gameDataOpt.get().getTitle());
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
			int unlockedCount = 0;
			// Parse file
			// Find string with format 0e000000000000000000000
			// Each trophy is a 1 in binary
			final String trophyProgressString = hexStr.substring(8, 72);
			final String binaryTrophyProgressString = new BigInteger(trophyProgressString, 16).toString(2);
			unlockedCount = (int) binaryTrophyProgressString.chars().filter(c -> c == '1').count();
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

	private String parseRegion(final String folderName) {
		if (folderName.startsWith("PCSE") || folderName.startsWith("PCSA")) {
			return "NA";
		}
		if (folderName.startsWith("PCSF") || folderName.startsWith("PCSB")) {
			return "EU";
		}
		if (folderName.startsWith("PCSH")) {
			return "AS";
		}
		Log.error("Region for " + folderName + " not found");
		return "";
	}

}
