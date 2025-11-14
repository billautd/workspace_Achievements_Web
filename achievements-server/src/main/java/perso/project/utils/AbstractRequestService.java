package perso.project.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.MainModel;

public abstract class AbstractRequestService {
	protected ObjectMapper mapper;
	protected XmlMapper xmlMapper;

	@Inject
	protected MainModel model;

	protected AbstractRequestService() {
		setupMapper();
	}

	ObjectMapper setupMapper() {
		mapper = JsonMapper.builder().addModule(new JavaTimeModule())
				.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true).build();
		xmlMapper = new XmlMapper();
		return mapper;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

	public XmlMapper getXmlMapper() {
		return xmlMapper;
	}

	/**
	 * @return Console data for source
	 */
	protected abstract List<ConsoleData> getConsoleIds();

	protected HttpResponse<String> requestHttpURI(final URI uri) {
		try {
			Log.debug("Creating request for URI : " + uri.toString());

			final HttpClient client = HttpClient.newBuilder().build();
			final HttpRequest request = HttpRequest.newBuilder(uri).build();
			return client.send(request, BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			Log.error("Error creating requesting uri " + uri.toString(), e);
			return null;
		}
	}

	protected void setGameAchievementPercent(final GameData gameData) {
		if (gameData.getTotalAchievements() == 0) {
			switch (gameData.getCompletionStatus()) {
			case BEATEN:
				gameData.setPercent(50d);
				break;
			case MASTERED:
				gameData.setPercent(100d);
				break;
			default:
				gameData.setPercent(0d);
				break;
			}
		} else {
			gameData.setPercent(Math.round(100 * gameData.getAwardedAchievements() / gameData.getTotalAchievements()));
		}
	}
}
