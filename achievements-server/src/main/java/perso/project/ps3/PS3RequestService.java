package perso.project.ps3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.MainModel;
import perso.project.model.Model;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.ExcelUtils;

@ApplicationScoped
public class PS3RequestService {
	@Inject
	MainModel model;

	ObjectMapper mapper;

	public PS3RequestService() {
		setupMapper();
	}

	ObjectMapper setupMapper() {
		mapper = new ObjectMapper();

		final JavaTimeModule module = new JavaTimeModule();
		mapper.registerModule(module);

		return mapper;
	}

	public List<ConsoleData> getConsoleIds() {
		Log.info("Getting PS3 console data");
		final ConsoleData ps3ConsoleData = new ConsoleData();
		ps3ConsoleData.setActive(true);
		ps3ConsoleData.setGameSystem(true);
		ps3ConsoleData.setId(Model.PS3_CONSOLE_ID);
		ps3ConsoleData.setName("PlayStation 3");
		ps3ConsoleData.setSource(ConsoleSourceEnum.STANDALONE);
		model.getConsoleDataMap().put(ps3ConsoleData.getId(), ps3ConsoleData);

		return List.of(ps3ConsoleData);
	}

	public void getOwnedGames(final String path) {
		Log.info("Getting all PS3 owned games");

		try (final XSSFWorkbook gamesWorkbook = new XSSFWorkbook(new FileInputStream(new File(path)))) {
			for (final Row row : gamesWorkbook.getSheetAt(0)) {
				final String gameName = ExcelUtils.getCellAsString(row.getCell(0));
				final int gameId = ExcelUtils.getCellAsInt(row.getCell(1));
				final GameData gameData = new GameData();
				gameData.setTitle(gameName);
				gameData.setId(gameId);
				gameData.setConsoleId(Model.PS3_CONSOLE_ID);
				gameData.setConsoleName("PlayStation 3");
				model.getConsoleDataMap().get(Model.PS3_CONSOLE_ID).getGameDataMap().put(gameId, gameData);
			}
		} catch (IOException e) {
			Log.error("Cannot read PS3 games file at " + path);
		}
	}

	public void getPS3Games_Beaten(final String path) {
		Log.info("Reading " + path);
		try (final XSSFWorkbook beatenWorkbook = new XSSFWorkbook(new FileInputStream(new File(path)))) {
			for (final Row row : beatenWorkbook.getSheetAt(0)) {
				final String gameName = ExcelUtils.getCellAsString(row.getCell(0));
				final int gameId = ExcelUtils.getCellAsInt(row.getCell(1));
				final GameData gameData = model.getConsoleDataMap().get(Model.PS3_CONSOLE_ID).getGameDataMap()
						.get(gameId);
				if (gameData == null) {
					Log.error(gameName + " (" + gameId + ") does not exist for PS3");
					continue;
				}
				gameData.setCompletionStatus(CompletionStatusEnum.BEATEN);
				Log.info(gameName + " (" + gameId + ") for PS3 is Beaten");
			}
		} catch (IOException e) {
			Log.error("Cannot read PS3 beaten file at " + path);
		}
	}

	public void getPS3Games_Mastered(final String path) {
		Log.info("Reading " + path);
		try (final XSSFWorkbook masteredWorkbook = new XSSFWorkbook(new FileInputStream(new File(path)))) {
			for (final Row row : masteredWorkbook.getSheetAt(0)) {
				final String gameName = ExcelUtils.getCellAsString(row.getCell(0));
				final int gameId = ExcelUtils.getCellAsInt(row.getCell(1));
				final GameData gameData = model.getConsoleDataMap().get(Model.PS3_CONSOLE_ID).getGameDataMap()
						.get(gameId);
				if (gameData == null) {
					Log.error(gameName + " (" + gameId + ") does not exist for PS3");
					continue;
				}
				gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
				Log.info(gameName + " (" + gameId + ") for PS3 is Mastered");
			}
		} catch (IOException e) {
			Log.error("Cannot read PS3 mastered file at " + path);
		}
	}

	public List<GameData> getAllData() {
		Log.info("Getting all PS3 games");
		getOwnedGames("C:\\Users\\dbill\\Downloads\\PS3Games.xlsx");
		getPS3Games_Beaten("C:\\Users\\dbill\\Downloads\\PS3GamesBeaten.xlsx");
		getPS3Games_Mastered("C:\\Users\\dbill\\Downloads\\PS3GamesMastered.xlsx");
		final List<GameData> gameData = model.getConsoleDataMap().get(Model.PS3_CONSOLE_ID).getGameDataMap().values()
				.stream().toList();
		Log.info("Processing " + gameData.size() + " PS3 games");
		return gameData;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}
}
