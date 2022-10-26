package com.emlogis.common.encrypt;

public class EncryptDecryptPass {

	public static void main(String[] args) {
		try {
			String password = "MuJtpJmpaqUb8TQy59UYdqaq/SQMgr+aBV9XAGyR";
			System.out.println("plain pass=" + password);
			String encryptedPassword = Crypto.encrypt(password);
			System.out.println("encrypted pass=" + encryptedPassword);
			String decryptedPassword = Crypto.decrypt(encryptedPassword);
			System.out.println("decrypted pass=" + decryptedPassword);
		} catch (Exception e) {
			System.out.println("bug" + e.getMessage());
		}
	}

}
