package perso.project.utils;

public class SleepUtils {
	private SleepUtils() {

	}

	public static void sleep(final long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
