package com.emlogis.common.security;

import java.math.BigInteger;

public class ShaUtils {

   private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

   private static final String BASE64_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz./";
   private static final char[] BASE64_CHARSET = BASE64_CHARS.toCharArray();

   private ShaUtils() {
      super();
   }

   public static String toString(byte[] ba) {
      return toString(ba, 0, ba.length);
   }

   public static final String toString(byte[] ba, int offset, int length) {
      char[] buf = new char[length * 2];
      for (int i = 0, j = 0, k; i < length; ) {
         k = ba[offset + i++];
         buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
         buf[j++] = HEX_DIGITS[ k        & 0x0F];
      }
      return new String(buf);
   }

   public static String toReversedString(byte[] ba) {
      return toReversedString(ba, 0, ba.length);
   }

   public static final String
   toReversedString(byte[] ba, int offset, int length) {
      char[] buf = new char[length * 2];
      for (int i = offset+length-1, j = 0, k; i >= offset; ) {
         k = ba[offset + i--];
         buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
         buf[j++] = HEX_DIGITS[ k        & 0x0F];
      }
      return new String(buf);
   }

   public static byte[] toBytesFromString(String s) {
      int limit = s.length();
      byte[] result = new byte[((limit + 1) / 2)];
      int i = 0, j = 0;
      if ((limit % 2) == 1) {
         result[j++] = (byte) fromDigit(s.charAt(i++));
      }
      while (i < limit) {
         result[j  ]  = (byte) (fromDigit(s.charAt(i++)) << 4);
         result[j++] |= (byte)  fromDigit(s.charAt(i++));
      }
      return result;
   }

   public static byte[] toReversedBytesFromString(String s) {
      int limit = s.length();
      byte[] result = new byte[((limit + 1) / 2)];
      int i = 0;
      if ((limit % 2) == 1) {
         result[i++] = (byte) fromDigit(s.charAt(--limit));
      }
      while (limit > 0) {
         result[i  ]  = (byte)  fromDigit(s.charAt(--limit));
         result[i++] |= (byte) (fromDigit(s.charAt(--limit)) << 4);
      }
      return result;
   }

   public static int fromDigit(char c) {
      if (c >= '0' && c <= '9') {
         return c - '0';
      } else if (c >= 'A' && c <= 'F') {
         return c - 'A' + 10;
      } else if (c >= 'a' && c <= 'f') {
         return c - 'a' + 10;
      } else
         throw new IllegalArgumentException("Invalid hexadecimal digit: " + c);
   }

   public static String toString(int n) {
      char[] buf = new char[8];
      for (int i = 7; i >= 0; i--) {
         buf[i] = HEX_DIGITS[n & 0x0F];
         n >>>= 4;
      }
      return new String(buf);
   }

   public static String toString(int[] ia) {
      int length = ia.length;
      char[] buf = new char[length * 8];
      for (int i = 0, j = 0, k; i < length; i++) {
         k = ia[i];
         buf[j++] = HEX_DIGITS[(k >>> 28) & 0x0F];
         buf[j++] = HEX_DIGITS[(k >>> 24) & 0x0F];
         buf[j++] = HEX_DIGITS[(k >>> 20) & 0x0F];
         buf[j++] = HEX_DIGITS[(k >>> 16) & 0x0F];
         buf[j++] = HEX_DIGITS[(k >>> 12) & 0x0F];
         buf[j++] = HEX_DIGITS[(k >>>  8) & 0x0F];
         buf[j++] = HEX_DIGITS[(k >>>  4) & 0x0F];
         buf[j++] = HEX_DIGITS[ k         & 0x0F];
      }
      return new String(buf);
   }

   public static String toString(long n) {
      char[] b = new char[16];
      for (int i = 15; i >= 0; i--) {
         b[i] = HEX_DIGITS[(int)(n & 0x0FL)];
         n >>>= 4;
      }
      return new String(b);
   }

   public static String toUnicodeString(byte[] ba) {
      return toUnicodeString(ba, 0, ba.length);
   }

   public static final String
   toUnicodeString(byte[] ba, int offset, int length) {
      StringBuffer sb = new StringBuffer();
      int i = 0;
      int j = 0;
      int k;
      sb.append('\n').append("\"");
      while (i < length) {
         sb.append("\\u");

         k = ba[offset + i++];
         sb.append(HEX_DIGITS[(k >>> 4) & 0x0F]);
         sb.append(HEX_DIGITS[ k        & 0x0F]);

         k = ba[offset + i++];
         sb.append(HEX_DIGITS[(k >>> 4) & 0x0F]);
         sb.append(HEX_DIGITS[ k        & 0x0F]);

         if ((++j % 8) == 0) {
            sb.append("\"+").append('\n').append("\"");
         }
      }
      sb.append("\"").append('\n');
      return sb.toString();
   }

   public static String toUnicodeString(int[] ia) {
      StringBuffer sb = new StringBuffer();
      int i = 0;
      int j = 0;
      int k;
      sb.append('\n').append("\"");
      while (i < ia.length) {
         k = ia[i++];
         sb.append("\\u");
         sb.append(HEX_DIGITS[(k >>> 28) & 0x0F]);
         sb.append(HEX_DIGITS[(k >>> 24) & 0x0F]);
         sb.append(HEX_DIGITS[(k >>> 20) & 0x0F]);
         sb.append(HEX_DIGITS[(k >>> 16) & 0x0F]);
         sb.append("\\u");
         sb.append(HEX_DIGITS[(k >>> 12) & 0x0F]);
         sb.append(HEX_DIGITS[(k >>>  8) & 0x0F]);
         sb.append(HEX_DIGITS[(k >>>  4) & 0x0F]);
         sb.append(HEX_DIGITS[ k         & 0x0F]);

         if ((++j % 4) == 0) {
            sb.append("\"+").append('\n').append("\"");
         }
      }
      sb.append("\"").append('\n');
      return sb.toString();
   }

   public static byte[] toBytesFromUnicode(String s) {
      int limit = s.length() * 2;
      byte[] result = new byte[limit];
      char c;
      for (int i = 0; i < limit; i++) {
         c = s.charAt(i >>> 1);
         result[i] = (byte)(((i & 1) == 0) ? c >>> 8 : c);
      }
      return result;
   }

   public static String dumpString(byte[] data, int offset, int length, String m) {
      if (data == null) {
         return m + "null\n";
      }
      StringBuffer sb = new StringBuffer(length * 3);
      if (length > 32) {
         sb.append(m).append("Hexadecimal dump of ").append(length).append(" bytes...\n");
      }

      int end = offset + length;
      String s;
      int l = Integer.toString(length).length();
      if (l < 4) {
         l = 4;
      }
      for ( ; offset < end; offset += 32) {
         if (length > 32) {
            s = "         " + offset;
            sb.append(m).append(s.substring(s.length()-l)).append(": ");
         }
         int i = 0;
         for ( ; i < 32 && offset + i + 7 < end; i += 8) {
            sb.append(toString(data, offset + i, 8)).append(' ');
         }
         if (i < 32) {
            for ( ; i < 32 && offset + i < end; i++) {
               sb.append(byteToString(data[offset + i]));
            }
         }
         sb.append('\n');
      }
      return sb.toString();
   }

   public static String dumpString(byte[] data) {
      return (data == null) ? "null\n" : dumpString(data, 0, data.length, "");
   }

   public static String dumpString(byte[] data, String m) {
      return (data == null) ? "null\n" : dumpString(data, 0, data.length, m);
   }

   public static String dumpString(byte[] data, int offset, int length) {
      return dumpString(data, offset, length, "");
   }

   public static String byteToString(int n) {
      char[] buf = { HEX_DIGITS[(n >>> 4) & 0x0F], HEX_DIGITS[n & 0x0F] };
      return new String(buf);
   }

   public static final String toBase64(byte[] buffer) {
      int len = buffer.length, pos = len % 3;
      byte b0 = 0, b1 = 0, b2 = 0;
      switch (pos) {
      case 1:
         b2 = buffer[0];
         break;
      case 2:
         b1 = buffer[0];
         b2 = buffer[1];
         break;
      }
      StringBuffer sb = new StringBuffer();
      int c;
      boolean notleading = false;
      do {
         c = (b0 & 0xFC) >>> 2;
         if (notleading || c != 0) {
           sb.append(BASE64_CHARSET[c]);
           notleading = true;
         }
         c = ((b0 & 0x03) << 4) | ((b1 & 0xF0) >>> 4);
         if (notleading || c != 0) {
           sb.append(BASE64_CHARSET[c]);
           notleading = true;
         }
         c = ((b1 & 0x0F) << 2) | ((b2 & 0xC0) >>> 6);
         if (notleading || c != 0) {
           sb.append(BASE64_CHARSET[c]);
           notleading = true;
         }
         c = b2 & 0x3F;
         if (notleading || c != 0) {
           sb.append(BASE64_CHARSET[c]);
           notleading = true;
         }
         if (pos >= len) {
           break;
         } else {
           try {
             b0 = buffer[pos++];
             b1 = buffer[pos++];
             b2 = buffer[pos++];
           } catch (ArrayIndexOutOfBoundsException x) {
             break;
           }
         }
      } while (true);

      if (notleading) {
        return sb.toString();
      }
      return "0";
   }

   public static final byte[] fromBase64(String str) {
      int len = str.length();
      if (len == 0) {
         throw new NumberFormatException("Empty string");
      }
      byte[] a = new byte[len + 1];
      int i, j;
      for (i = 0; i < len; i++) {
         try {
            a[i] = (byte) BASE64_CHARS.indexOf(str.charAt(i));
         } catch (ArrayIndexOutOfBoundsException x) {
            throw new NumberFormatException("Illegal character at #"+i);
         }
      }
      i = len - 1;
      j = len;
      try {
         while (true) {
            a[j] = a[i];
            if (--i < 0) {
               break;
            }
            a[j] |= (a[i] & 0x03) << 6;
            j--;
            a[j] = (byte)((a[i] & 0x3C) >>> 2);
            if (--i < 0) {
               break;
            }
            a[j] |= (a[i] & 0x0F) << 4;
            j--;
            a[j] = (byte)((a[i] & 0x30) >>> 4);
            if (--i < 0) {
               break;
            }
            a[j] |= (a[i] << 2);
            j--;
            a[j] = 0;
            if (--i < 0) {
               break;
            }
         }
      } catch (Exception ignored) {
      }

      try {
         while(a[j] == 0) {
            j++;
         }
      } catch (Exception x) {
         return new byte[1];
      }
      byte[] result = new byte[len - j + 1];
      System.arraycopy(a, j, result, 0, len - j + 1);
      return result;
   }

   public static final byte[] trim(BigInteger n) {
      byte[] in = n.toByteArray();
      if (in.length == 0 || in[0] != 0) {
         return in;
      }
      int len = in.length;
      int i = 1;
      while (in[i] == 0 && i < len) {
         ++i;
      }
      byte[] result = new byte[len - i];
      System.arraycopy(in, i, result, 0, len - i);
      return result;
   }

   public static final String dump(BigInteger x) {
      return dumpString(trim(x));
   }
}