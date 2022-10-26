package com.emlogis.common.security.encoding;

public class PasswordEncoder {

	public static boolean isPasswordValid(String encPass, String rawPass, String salt){
		ShaPasswordEncoder passwordEncoder=new ShaPasswordEncoder();
		String saltedPass = passwordEncoder.getEncryptedString(rawPass + salt);
		return saltedPass.equals(encPass);
	}
	public static void main(String[] args) {
		ShaPasswordEncoder passwordEncoder=new ShaPasswordEncoder();
		System.out.println("admin:" + passwordEncoder.getEncryptedString("admin"));
		System.out.println("guest:" + passwordEncoder.getEncryptedString("guest"));
	}

}