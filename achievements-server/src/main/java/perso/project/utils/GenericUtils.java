package perso.project.utils;

import java.util.ArrayList;
import java.util.List;

public class GenericUtils {
	private GenericUtils() {
		// Nothing
	}

	public static final List<Integer> indexesOf(final String value, final String toSearch) {
		final List<Integer> indexes = new ArrayList<>();
		int index = value.indexOf(toSearch);
		while (index >= 0) {
			indexes.add(index);
			index = value.indexOf(toSearch, index + 1);
		}
		return indexes;
	}

	public static String hexToBin(String hex) {
		hex = hex.replaceAll("0", "0000");
		hex = hex.replaceAll("1", "0001");
		hex = hex.replaceAll("2", "0010");
		hex = hex.replaceAll("3", "0011");
		hex = hex.replaceAll("4", "0100");
		hex = hex.replaceAll("5", "0101");
		hex = hex.replaceAll("6", "0110");
		hex = hex.replaceAll("7", "0111");
		hex = hex.replaceAll("8", "1000");
		hex = hex.replaceAll("9", "1001");
		hex = hex.replaceAll("A", "1010");
		hex = hex.replaceAll("a", "1010");
		hex = hex.replaceAll("B", "1011");
		hex = hex.replaceAll("b", "1011");
		hex = hex.replaceAll("C", "1100");
		hex = hex.replaceAll("c", "1100");
		hex = hex.replaceAll("D", "1101");
		hex = hex.replaceAll("d", "1101");
		hex = hex.replaceAll("E", "1110");
		hex = hex.replaceAll("e", "1110");
		hex = hex.replaceAll("F", "1111");
		hex = hex.replaceAll("f", "1111");
		return hex;
	}

	public static String hexToAscii(final String hex) {
		String output = "";

		for (int i = 0; i < hex.length(); i += 2) {
			String str = hex.substring(i, i + 2);
			output += (char) Integer.parseInt(str, 16);
		}

		return output;
	}
}
