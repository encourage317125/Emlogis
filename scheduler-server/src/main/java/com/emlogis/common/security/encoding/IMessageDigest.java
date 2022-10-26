package com.emlogis.common.security.encoding;

public interface IMessageDigest {
	   String name();

	   int hashSize();

	   int blockSize();

	   void update(byte b);

	   void update(byte[] in, int offset, int length);

	   byte[] digest();

	   void reset();

	   String getEncryptedString(String strToEncrypt);

	   Object clone();

}
