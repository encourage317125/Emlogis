package com.emlogis.test.providers;

import java.util.Locale;

public class TestLocaleValues {
	
	public static void main(String[] args) {
		for (String lang : Locale.getISOLanguages()) {
			System.out.println(lang);
		}
	}

}
