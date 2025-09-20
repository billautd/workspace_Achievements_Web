package perso.project.utils;

import org.apache.poi.ss.usermodel.Cell;

import io.quarkus.logging.Log;

public class ExcelUtils {
	private ExcelUtils() {
		// Static methods only
	}

	public static String getCellAsString(final Cell cell) {
		switch (cell.getCellType()) {
		case STRING:
			return cell.toString();
		case NUMERIC:
			return Integer.toString((int) Double.parseDouble(cell.toString()));
		default:
			Log.error("Cell type " + cell.getCellType() + " not managed. Value is " + cell.toString());
			return "";
		}
	}

	public static int getCellAsInt(final Cell cell) {
		switch (cell.getCellType()) {
		case STRING:
			return (int) Double.parseDouble(cell.toString());
		case NUMERIC:
			return (int) cell.getNumericCellValue();
		default:
			Log.error("Cell type " + cell.getCellType() + " not managed. Value is " + cell.toString());
			return 0;
		}
	}
}
