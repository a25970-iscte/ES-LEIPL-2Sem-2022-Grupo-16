/*
 * Copyright 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.result;

import com.google.zxing.Result;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses contact information formatted according to the VCard (2.1) format. This is not a complete
 * implementation but should parse information as commonly encoded in 2D barcodes.
 *
 * @author Sean Owen
 */
public final class VCardResultParser extends ResultParser {

  private static final Pattern BEGIN_VCARD = Pattern.compile("BEGIN:VCARD", Pattern.CASE_INSENSITIVE);
  private static final Pattern VCARD_LIKE_DATE = Pattern.compile("\\d{4}-?\\d{2}-?\\d{2}");
  static final Pattern CR_LF_SPACE_TAB = Pattern.compile("\r\n[ \t]");
  static final Pattern NEWLINE_ESCAPE = Pattern.compile("\\\\[nN]");
  static final Pattern VCARD_ESCAPES = Pattern.compile("\\\\([,;\\\\])");
  static final Pattern EQUALS = Pattern.compile("=");
  static final Pattern SEMICOLON = Pattern.compile(";");
  static final Pattern UNESCAPED_SEMICOLONS = Pattern.compile("(?<!\\\\);+");
  private static final Pattern COMMA = Pattern.compile(",");
  private static final Pattern SEMICOLON_OR_COMMA = Pattern.compile("[;,]");

  @Override
  public AddressBookParsedResult parse(Result result) {
    // Although we should insist on the raw text ending with "END:VCARD", there's no reason
    // to throw out everything else we parsed just because this was omitted. In fact, Eclair
    // is doing just that, and we can't parse its contacts without this leniency.
    String rawText = getMassagedText(result);
    Matcher m = BEGIN_VCARD.matcher(rawText);
    if (!m.find() || m.start() != 0) {
      return null;
    }
    List<List<String>> names = AddressBookParsedResult.matchVCardPrefixedField("FN", rawText, true, false);
    if (names == null) {
      // If no display names found, look for regular name fields and format them
      names = AddressBookParsedResult.matchVCardPrefixedField("N", rawText, true, false);
      formatNames(names);
    }
    List<String> nicknameString = matchSingleVCardPrefixedField("NICKNAME", rawText, true, false);
    String[] nicknames = nicknameString == null ? null : COMMA.split(nicknameString.get(0));
    List<List<String>> phoneNumbers = AddressBookParsedResult.matchVCardPrefixedField("TEL", rawText, true, false);
    List<List<String>> emails = AddressBookParsedResult.matchVCardPrefixedField("EMAIL", rawText, true, false);
    List<String> note = matchSingleVCardPrefixedField("NOTE", rawText, false, false);
    List<List<String>> addresses = AddressBookParsedResult.matchVCardPrefixedField("ADR", rawText, true, true);
    List<String> org = matchSingleVCardPrefixedField("ORG", rawText, true, true);
    List<String> birthday = matchSingleVCardPrefixedField("BDAY", rawText, true, false);
    if (birthday != null && !isLikeVCardDate(birthday.get(0))) {
      birthday = null;
    }
    List<String> title = matchSingleVCardPrefixedField("TITLE", rawText, true, false);
    List<List<String>> urls = AddressBookParsedResult.matchVCardPrefixedField("URL", rawText, true, false);
    List<String> instantMessenger = matchSingleVCardPrefixedField("IMPP", rawText, true, false);
    List<String> geoString = matchSingleVCardPrefixedField("GEO", rawText, true, false);
    String[] geo = geoString == null ? null : SEMICOLON_OR_COMMA.split(geoString.get(0));
    if (geo != null && geo.length != 2) {
      geo = null;
    }
    return new AddressBookParsedResult(AddressBookParsedResult.toPrimaryValues(names),
                                       nicknames,
                                       null, 
                                       AddressBookParsedResult.toPrimaryValues(phoneNumbers), 
                                       AddressBookParsedResult.toTypes(phoneNumbers),
                                       AddressBookParsedResult.toPrimaryValues(emails),
                                       AddressBookParsedResult.toTypes(emails),
                                       AddressBookParsedResult.toPrimaryValue(instantMessenger),
                                       AddressBookParsedResult.toPrimaryValue(note),
                                       AddressBookParsedResult.toPrimaryValues(addresses),
                                       AddressBookParsedResult.toTypes(addresses),
                                       AddressBookParsedResult.toPrimaryValue(org),
                                       AddressBookParsedResult.toPrimaryValue(birthday),
                                       AddressBookParsedResult.toPrimaryValue(title),
                                       AddressBookParsedResult.toPrimaryValues(urls),
                                       geo);
  }

  static int followedByTabOrSpace(String rawText, int i, boolean quotedPrintable) {
    while ((i = rawText.indexOf('\n', i)) >= 0) { // Really, end in \r\n
      if (i < rawText.length() - 1 &&           // But if followed by tab or space,
          (rawText.charAt(i + 1) == ' ' ||        // this is only a continuation
           rawText.charAt(i + 1) == '\t')) {
        i += 2; // Skip \n and continutation whitespace
      } else if (quotedPrintable &&             // If preceded by = in quoted printable
                 ((i >= 1 && rawText.charAt(i - 1) == '=') || // this is a continuation
                  (i >= 2 && rawText.charAt(i - 2) == '='))) {
        i++; // Skip \n
      } else {
        break;
      }
    }
    return i;
  }

  static String decodeQuotedPrintable(CharSequence value, String charset) {
    int length = value.length();
    StringBuilder result = new StringBuilder(length);
    ByteArrayOutputStream fragmentBuffer = new ByteArrayOutputStream();
    for (int i = 0; i < length; i++) {
      char c = value.charAt(i);
      switch (c) {
        case '\r':
        case '\n':
          break;
        case '=':
          if (i < length - 2) {
            char nextChar = value.charAt(i + 1);
            if (nextChar != '\r' && nextChar != '\n') {
              char nextNextChar = value.charAt(i + 2);
              int firstDigit = parseHexDigit(nextChar);
              int secondDigit = parseHexDigit(nextNextChar);
              if (firstDigit >= 0 && secondDigit >= 0) {
                fragmentBuffer.write((firstDigit << 4) + secondDigit);
              } // else ignore it, assume it was incorrectly encoded
              i += 2;
            }
          }
          break;
        default:
          maybeAppendFragment(fragmentBuffer, charset, result);
          result.append(c);
      }
    }
    maybeAppendFragment(fragmentBuffer, charset, result);
    return result.toString();
  }

  private static void maybeAppendFragment(ByteArrayOutputStream fragmentBuffer,
                                          String charset,
                                          StringBuilder result) {
    if (fragmentBuffer.size() > 0) {
      byte[] fragmentBytes = fragmentBuffer.toByteArray();
      String fragment;
      if (charset == null) {
        fragment = new String(fragmentBytes, StandardCharsets.UTF_8);
      } else {
        try {
          fragment = new String(fragmentBytes, charset);
        } catch (UnsupportedEncodingException e) {
          fragment = new String(fragmentBytes, StandardCharsets.UTF_8);
        }
      }
      fragmentBuffer.reset();
      result.append(fragment);
    }
  }

  static List<String> matchSingleVCardPrefixedField(CharSequence prefix,
                                                    String rawText,
                                                    boolean trim,
                                                    boolean parseFieldDivider) {
    List<List<String>> values = AddressBookParsedResult.matchVCardPrefixedField(prefix, rawText, trim, parseFieldDivider);
    return values == null || values.isEmpty() ? null : values.get(0);
  }
  
  private static boolean isLikeVCardDate(CharSequence value) {
    return value == null || VCARD_LIKE_DATE.matcher(value).matches();
  }

  /**
   * Formats name fields of the form "Public;John;Q.;Reverend;III" into a form like
   * "Reverend John Q. Public III".
   *
   * @param names name values to format, in place
   */
  private static void formatNames(Iterable<List<String>> names) {
    if (names != null) {
      for (List<String> list : names) {
        String name = list.get(0);
        String[] components = new String[5];
        int start = 0;
        int end;
        int componentIndex = 0;
        while (componentIndex < components.length - 1 && (end = name.indexOf(';', start)) >= 0) {
          components[componentIndex] = name.substring(start, end);
          componentIndex++;
          start = end + 1;
        }
        components[componentIndex] = name.substring(start);
        StringBuilder newName = new StringBuilder(100);
        maybeAppendComponent(components, 3, newName);
        maybeAppendComponent(components, 1, newName);
        maybeAppendComponent(components, 2, newName);
        maybeAppendComponent(components, 0, newName);
        maybeAppendComponent(components, 4, newName);
        list.set(0, newName.toString().trim());
      }
    }
  }

  private static void maybeAppendComponent(String[] components, int i, StringBuilder newName) {
    if (components[i] != null && !components[i].isEmpty()) {
      if (newName.length() > 0) {
        newName.append(' ');
      }
      newName.append(components[i]);
    }
  }

}
