package perso.project.standalone;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.MainModel;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.ExcelUtils;

public abstract class AbstractStandaloneRequestService {
	@Inject
	MainModel model;

	ObjectMapper mapper;

	protected AbstractStandaloneRequestService() {
		setupMapper();
	}

	protected abstract Path getGamesPath();

	protected abstract Path getGamesBeatenPath();

	protected abstract Path getGamesMasteredPath();

	protected abstract ConsoleSourceEnum getSource();

	protected abstract int getId();

	ObjectMapper setupMapper() {
		mapper = new ObjectMapper();

		final JavaTimeModule module = new JavaTimeModule();
		mapper.registerModule(module);

		return mapper;
	}

	public List<ConsoleData> getConsoleIds() {
		ConsoleData saConsoleData;
		if (!model.getConsoleDataMap().containsKey(getId())) {
			Log.info("Getting " + getSource() + " console data");
			saConsoleData = new ConsoleData();
			saConsoleData.setActive(true);
			saConsoleData.setGameSystem(true);
			saConsoleData.setId(getId());
			saConsoleData.setName(getSource().getName());
			saConsoleData.setSource(getSource());
			model.getConsoleDataMap().put(saConsoleData.getId(), saConsoleData);
		} else {
			saConsoleData = model.getConsoleDataMap().get(getId());
		}
		return List.of(saConsoleData);
	}

	public void getOwnedGames(final Path path) {
		Log.info("Getting all " + getSource() + " owned games");

		try (final XSSFWorkbook gamesWorkbook = new XSSFWorkbook(new FileInputStream(path.toFile()))) {
			for (final Row row : gamesWorkbook.getSheetAt(0)) {
				final String gameName = ExcelUtils.getCellAsString(row.getCell(0));
				final int gameId = ExcelUtils.getCellAsInt(row.getCell(1));
				final GameData gameData = new GameData();
				gameData.setTitle(gameName);
				gameData.setId(gameId);
				gameData.setConsoleId(getId());
				gameData.setConsoleName(getSource().getName());
				model.getConsoleDataMap().get(getId()).getGameDataMap().put(gameId, gameData);
			}
		} catch (IOException e) {
			Log.error("Cannot read " + getSource() + " games file at " + path);
		}
	}

	public void getGames_Beaten(final Path path) {
		Log.info("Reading " + path);
		try (final XSSFWorkbook beatenWorkbook = new XSSFWorkbook(new FileInputStream(path.toFile()))) {
			for (final Row row : beatenWorkbook.getSheetAt(0)) {
				final String gameName = ExcelUtils.getCellAsString(row.getCell(0));
				final int gameId = ExcelUtils.getCellAsInt(row.getCell(1));
				final GameData gameData = model.getConsoleDataMap().get(getId()).getGameDataMap().get(gameId);
				if (gameData == null) {
					Log.error(gameName + " (" + gameId + ") does not exist for " + getSource());
					continue;
				}
				gameData.setCompletionStatus(CompletionStatusEnum.BEATEN);
				Log.info(gameName + " (" + gameId + ") for " + getSource() + " is Beaten");
			}
		} catch (IOException e) {
			Log.error("Cannot read " + getSource() + " beaten file at " + path);
		}
	}

	public void getGames_Mastered(final Path path) {
		Log.info("Reading " + path);
		try (final XSSFWorkbook masteredWorkbook = new XSSFWorkbook(new FileInputStream(path.toFile()))) {
			for (final Row row : masteredWorkbook.getSheetAt(0)) {
				final String gameName = ExcelUtils.getCellAsString(row.getCell(0));
				final int gameId = ExcelUtils.getCellAsInt(row.getCell(1));
				final GameData gameData = model.getConsoleDataMap().get(getId()).getGameDataMap().get(gameId);
				if (gameData == null) {
					Log.error(gameName + " (" + gameId + ") does not exist for " + getSource());
					continue;
				}
				gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
				Log.info(gameName + " (" + gameId + ") for " + getSource() + " is Mastered");
			}
		} catch (IOException e) {
			Log.error("Cannot read " + getSource() + " mastered file at " + path);
		}
	}

	public List<GameData> getAllData() {
		Log.info("Getting all " + getSource() + " games");
		getOwnedGames(getGamesPath());
		getGames_Beaten(getGamesBeatenPath());
		getGames_Mastered(getGamesMasteredPath());
		final List<GameData> gameData = model.getConsoleDataMap().get(getId()).getGameDataMap().values().stream()
				.toList();
		Log.info("Processing " + gameData.size() + " " + getSource() + " games");
		return gameData;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}
}
