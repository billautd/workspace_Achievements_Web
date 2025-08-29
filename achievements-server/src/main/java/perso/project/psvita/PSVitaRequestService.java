package perso.project.psvita;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
public class PSVitaRequestService {
	@Inject
	MainModel model;

	@Inject
	@ConfigProperty(name = "psvita.games.path")
	private Path psVitaGamesPath;

	@Inject
	@ConfigProperty(name = "psvita.beaten.path")
	private Path psVitaBeatenPath;

	@Inject
	@ConfigProperty(name = "psvita.mastered.path")
	private Path psVitaMasteredPath;

	ObjectMapper mapper;

	public PSVitaRequestService() {
		setupMapper();
	}

	ObjectMapper setupMapper() {
		mapper = new ObjectMapper();

		final JavaTimeModule module = new JavaTimeModule();
		mapper.registerModule(module);

		return mapper;
	}

	public List<ConsoleData> getConsoleIds() {
		ConsoleData psvitaConsoleData;
		if (!model.getConsoleDataMap().containsKey(Model.PSVITA_CONSOLE_ID)) {
			Log.info("Getting PSVita console data");
			psvitaConsoleData = new ConsoleData();
			psvitaConsoleData.setActive(true);
			psvitaConsoleData.setGameSystem(true);
			psvitaConsoleData.setId(Model.PSVITA_CONSOLE_ID);
			psvitaConsoleData.setName("PlayStation Vita");
			psvitaConsoleData.setSource(ConsoleSourceEnum.PSVITA);
			model.getConsoleDataMap().put(psvitaConsoleData.getId(), psvitaConsoleData);
		} else {
			psvitaConsoleData = model.getConsoleDataMap().get(Model.PSVITA_CONSOLE_ID);
		}
		return List.of(psvitaConsoleData);
	}

	public void getOwnedGames(final Path path) {
		Log.info("Getting all PSVita owned games");

		try (final XSSFWorkbook gamesWorkbook = new XSSFWorkbook(new FileInputStream(path.toFile()))) {
			for (final Row row : gamesWorkbook.getSheetAt(0)) {
				final String gameName = ExcelUtils.getCellAsString(row.getCell(0));
				final int gameId = ExcelUtils.getCellAsInt(row.getCell(1));
				final GameData gameData = new GameData();
				gameData.setTitle(gameName);
				gameData.setId(gameId);
				gameData.setConsoleId(Model.PSVITA_CONSOLE_ID);
				gameData.setConsoleName("PlayStation Vita");
				model.getConsoleDataMap().get(Model.PSVITA_CONSOLE_ID).getGameDataMap().put(gameId, gameData);
			}
		} catch (IOException e) {
			Log.error("Cannot read PSVita games file at " + path);
		}
	}

	public void getPSVitaGames_Beaten(final Path path) {
		Log.info("Reading " + path);
		try (final XSSFWorkbook beatenWorkbook = new XSSFWorkbook(new FileInputStream(path.toFile()))) {
			for (final Row row : beatenWorkbook.getSheetAt(0)) {
				final String gameName = ExcelUtils.getCellAsString(row.getCell(0));
				final int gameId = ExcelUtils.getCellAsInt(row.getCell(1));
				final GameData gameData = model.getConsoleDataMap().get(Model.PSVITA_CONSOLE_ID).getGameDataMap()
						.get(gameId);
				if (gameData == null) {
					Log.error(gameName + " (" + gameId + ") does not exist for PSVita");
					continue;
				}
				gameData.setCompletionStatus(CompletionStatusEnum.BEATEN);
				Log.info(gameName + " (" + gameId + ") for PSVita is Beaten");
			}
		} catch (IOException e) {
			Log.error("Cannot read PSVita beaten file at " + path);
		}
	}

	public void getPSVitaGames_Mastered(final Path path) {
		Log.info("Reading " + path);
		try (final XSSFWorkbook masteredWorkbook = new XSSFWorkbook(new FileInputStream(path.toFile()))) {
			for (final Row row : masteredWorkbook.getSheetAt(0)) {
				final String gameName = ExcelUtils.getCellAsString(row.getCell(0));
				final int gameId = ExcelUtils.getCellAsInt(row.getCell(1));
				final GameData gameData = model.getConsoleDataMap().get(Model.PSVITA_CONSOLE_ID).getGameDataMap()
						.get(gameId);
				if (gameData == null) {
					Log.error(gameName + " (" + gameId + ") does not exist for PSVita");
					continue;
				}
				gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
				Log.info(gameName + " (" + gameId + ") for PSVita is Mastered");
			}
		} catch (IOException e) {
			Log.error("Cannot read PSVita mastered file at " + path);
		}
	}

	public List<GameData> getAllData() {
		Log.info("Getting all PSVita games");
		getOwnedGames(psVitaGamesPath);
		getPSVitaGames_Beaten(psVitaBeatenPath);
		getPSVitaGames_Mastered(psVitaBeatenPath);
		final List<GameData> gameData = model.getConsoleDataMap().get(Model.PSVITA_CONSOLE_ID).getGameDataMap().values()
				.stream().toList();
		Log.info("Processing " + gameData.size() + " PSVita games");
		return gameData;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}
}
