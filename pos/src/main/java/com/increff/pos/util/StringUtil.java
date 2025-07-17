package com.increff.pos.util;

public class StringUtil {

	public static boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	public static String toLowerCase(String s) {
		return s == null ? null : s.trim().toLowerCase();
	}

	public static String toUpperCase(String s) {
		return s == null ? null : s.trim().toUpperCase();
	}

	public static String normalize(String input) {
		if (input == null) {
			return null;
		}
		return input.trim();
	}

	public static String extractFieldFromValidationError(String errorMessage) {
		if (errorMessage.contains("Barcode")) return "barcode";
		if (errorMessage.contains("Client ID") || errorMessage.contains("Client id")) return "client_id";
		if (errorMessage.contains("Product name") || errorMessage.contains("name")) return "name";
		if (errorMessage.contains("MRP") || errorMessage.contains("mrp")) return "mrp";
		return "general";
	}
}
