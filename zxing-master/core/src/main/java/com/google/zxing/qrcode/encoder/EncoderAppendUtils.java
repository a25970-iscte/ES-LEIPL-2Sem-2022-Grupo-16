package com.google.zxing.qrcode.encoder;

import java.nio.charset.Charset;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.common.StringUtils;
import com.google.zxing.qrcode.decoder.Mode;
import com.google.zxing.qrcode.decoder.Version;

public class EncoderAppendUtils {

    static void appendECI(CharacterSetECI eci, BitArray bits) {
      bits.appendBits(Mode.ECI.getBits(), 4);
      // This is correct for values up to 127, which is all we need now.
      bits.appendBits(eci.getValue(), 8);
    }

    static void appendKanjiBytes(String content, BitArray bits) throws WriterException {
      byte[] bytes = content.getBytes(StringUtils.SHIFT_JIS_CHARSET);
      if (bytes.length % 2 != 0) {
        throw new WriterException("Kanji byte size not even");
      }
      int maxI = bytes.length - 1; // bytes.length must be even
      for (int i = 0; i < maxI; i += 2) {
        int byte1 = bytes[i] & 0xFF;
        int byte2 = bytes[i + 1] & 0xFF;
        int code = (byte1 << 8) | byte2;
        int subtracted = -1;
        if (code >= 0x8140 && code <= 0x9ffc) {
          subtracted = code - 0x8140;
        } else if (code >= 0xe040 && code <= 0xebbf) {
          subtracted = code - 0xc140;
        }
        if (subtracted == -1) {
          throw new WriterException("Invalid byte sequence");
        }
        int encoded = ((subtracted >> 8) * 0xc0) + (subtracted & 0xff);
        bits.appendBits(encoded, 13);
      }
    }

    static void append8BitBytes(String content, BitArray bits, Charset encoding) {
      byte[] bytes = content.getBytes(encoding);
      for (byte b : bytes) {
        bits.appendBits(b, 8);
      }
    }

    static void appendAlphanumericBytes(CharSequence content, BitArray bits) throws WriterException {
      int length = content.length();
      int i = 0;
      while (i < length) {
        int code1 = Encoder.getAlphanumericCode(content.charAt(i));
        if (code1 == -1) {
          throw new WriterException();
        }
        if (i + 1 < length) {
          int code2 = Encoder.getAlphanumericCode(content.charAt(i + 1));
          if (code2 == -1) {
            throw new WriterException();
          }
          // Encode two alphanumeric letters in 11 bits.
          bits.appendBits(code1 * 45 + code2, 11);
          i += 2;
        } else {
          // Encode one alphanumeric letter in six bits.
          bits.appendBits(code1, 6);
          i++;
        }
      }
    }

    static void appendNumericBytes(CharSequence content, BitArray bits) {
      int length = content.length();
      int i = 0;
      Encoder.bitsChecker(content, bits, length, i);
    }

    /**
     * Append "bytes" in "mode" mode (encoding) into "bits". On success, store the result in "bits".
     */
    static void appendBytes(String content,
                            Mode mode,
                            BitArray bits,
                            Charset encoding) throws WriterException {
      switch (mode) {
        case NUMERIC:
          appendNumericBytes(content, bits);
          break;
        case ALPHANUMERIC:
          appendAlphanumericBytes(content, bits);
          break;
        case BYTE:
          append8BitBytes(content, bits, encoding);
          break;
        case KANJI:
          appendKanjiBytes(content, bits);
          break;
        default:
          throw new WriterException("Invalid mode: " + mode);
      }
    }

    /**
     * Append length info. On success, store the result in "bits".
     */
    static void appendLengthInfo(int numLetters, Version version, Mode mode, BitArray bits) throws WriterException {
      int numBits = mode.getCharacterCountBits(version);
      if (numLetters >= (1 << numBits)) {
        throw new WriterException(numLetters + " is bigger than " + ((1 << numBits) - 1));
      }
      bits.appendBits(numLetters, numBits);
    }

    /**
     * Append mode info. On success, store the result in "bits".
     */
    static void appendModeInfo(Mode mode, BitArray bits) {
      bits.appendBits(mode.getBits(), 4);
    }
    
}
