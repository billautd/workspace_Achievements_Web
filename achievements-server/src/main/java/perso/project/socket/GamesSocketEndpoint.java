package perso.project.socket;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/games_socket")
@ApplicationScoped
public class GamesSocketEndpoint {
	private final List<Session> sessions = new ArrayList<>();

	@OnOpen
	public void onOpen(final Session session) {
		Log.info("Socket games open");
		sessions.add(session);
	}

	@OnClose
	public void onClose(final Session session) {
		Log.info("Socket games closed");
		sessions.remove(session);
	}

	@OnError
	public void onError(final Session session, final Throwable error) {
		Log.error("Socket games error", error);
	}

	@OnMessage
	public void onMessage(final String message) {
		Log.info("Received message " + message);
	}

	public void sendStringDataBroadcast(final String message) {
		Log.info("Sending message to " + sessions.size() + " sessions");
		sessions.forEach(session -> {
			session.getAsyncRemote().sendText(message);
		});
	}
}
