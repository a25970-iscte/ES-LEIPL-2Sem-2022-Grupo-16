/*
 * Copyright 2007 ZXing authors
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a parsed result that encodes contact information, like that in an address book
 * entry.
 *
 * @author Sean Owen
 */
public final class AddressBookParsedResult extends ParsedResult {

  private final String[] names;
  private final String[] nicknames;
  private final String pronunciation;
  private final String[] phoneNumbers;
  private final String[] phoneTypes;
  private final String[] emails;
  private final String[] emailTypes;
  private final String instantMessenger;
  private final String note;
  private final String[] addresses;
  private final String[] addressTypes;
  private final String org;
  private final String birthday;
  private final String title;
  private final String[] urls;
  private final String[] geo;

  private static final String[] EMPTY_STR_ARRAY = new String[0];

  public AddressBookParsedResult(String[] names,
                                 String[] phoneNumbers,
                                 String[] phoneTypes,
                                 String[] emails,
                                 String[] emailTypes,
                                 String[] addresses,
                                 String[] addressTypes) {
    this(names,
         null,
         null,
         phoneNumbers,
         phoneTypes,
         emails,
         emailTypes,
         null,
         null,
         addresses,
         addressTypes,
         null,
         null,
         null,
         null,
         null);
  }

  public AddressBookParsedResult(String[] names,
                                 String[] nicknames,
                                 String pronunciation,
                                 String[] phoneNumbers,
                                 String[] phoneTypes,
                                 String[] emails,
                                 String[] emailTypes,
                                 String instantMessenger,
                                 String note,
                                 String[] addresses,
                                 String[] addressTypes,
                                 String org,
                                 String birthday,
                                 String title,
                                 String[] urls,
                                 String[] geo) {
    super(ParsedResultType.ADDRESSBOOK);
    if (phoneNumbers != null && phoneTypes != null && phoneNumbers.length != phoneTypes.length) {
      throw new IllegalArgumentException("Phone numbers and types lengths differ");
    }
    if (emails != null && emailTypes != null && emails.length != emailTypes.length) {
      throw new IllegalArgumentException("Emails and types lengths differ");
    }
    if (addresses != null && addressTypes != null && addresses.length != addressTypes.length) {
      throw new IllegalArgumentException("Addresses and types lengths differ");
    }
    this.names = names;
    this.nicknames = nicknames;
    this.pronunciation = pronunciation;
    this.phoneNumbers = phoneNumbers;
    this.phoneTypes = phoneTypes;
    this.emails = emails;
    this.emailTypes = emailTypes;
    this.instantMessenger = instantMessenger;
    this.note = note;
    this.addresses = addresses;
    this.addressTypes = addressTypes;
    this.org = org;
    this.birthday = birthday;
    this.title = title;
    this.urls = urls;
    this.geo = geo;
  }

  public String[] getNames() {
    return names;
  }

  public String[] getNicknames() {
    return nicknames;
  }

  /**
   * In Japanese, the name is written in kanji, which can have multiple readings. Therefore a hint
   * is often provided, called furigana, which spells the name phonetically.
   *
   * @return The pronunciation of the getNames() field, often in hiragana or katakana.
   */
  public String getPronunciation() {
    return pronunciation;
  }

  public String[] getPhoneNumbers() {
    return phoneNumbers;
  }

  /**
   * @return optional descriptions of the type of each phone number. It could be like "HOME", but,
   *  there is no guaranteed or standard format.
   */
  public String[] getPhoneTypes() {
    return phoneTypes;
  }

  public String[] getEmails() {
    return emails;
  }

  /**
   * @return optional descriptions of the type of each e-mail. It could be like "WORK", but,
   *  there is no guaranteed or standard format.
   */
  public String[] getEmailTypes() {
    return emailTypes;
  }
  
  public String getInstantMessenger() {
    return instantMessenger;
  }

  public String getNote() {
    return note;
  }

  public String[] getAddresses() {
    return addresses;
  }

  /**
   * @return optional descriptions of the type of each e-mail. It could be like "WORK", but,
   *  there is no guaranteed or standard format.
   */
  public String[] getAddressTypes() {
    return addressTypes;
  }

  public String getTitle() {
    return title;
  }

  public String getOrg() {
    return org;
  }

  public String[] getURLs() {
    return urls;
  }

  /**
   * @return birthday formatted as yyyyMMdd (e.g. 19780917)
   */
  public String getBirthday() {
    return birthday;
  }

  /**
   * @return a location as a latitude/longitude pair
   */
  public String[] getGeo() {
    return geo;
  }

  @Override
  public String getDisplayResult() {
    StringBuilder result = new StringBuilder(100);
    maybeAppend(names, result);
    maybeAppend(nicknames, result);
    maybeAppend(pronunciation, result);
    maybeAppend(title, result);
    maybeAppend(org, result);
    maybeAppend(addresses, result);
    maybeAppend(phoneNumbers, result);
    maybeAppend(emails, result);
    maybeAppend(instantMessenger, result);
    maybeAppend(urls, result);
    maybeAppend(birthday, result);
    maybeAppend(geo, result);
    maybeAppend(note, result);
    return result.toString();
  }

  static String[] toTypes(Collection<List<String>> lists) {
    if (lists == null || lists.isEmpty()) {
      return null;
    }
    List<String> result = new ArrayList<>(lists.size());
    for (List<String> list : lists) {
      String value = list.get(0);
      if (value != null && !value.isEmpty()) {
        String type = null;
        for (int i = 1; i < list.size(); i++) {
          String metadatum = list.get(i);
          int equals = metadatum.indexOf('=');
          if (equals < 0) {
            // take the whole thing as a usable label
            type = metadatum;
            break;
          }
          if ("TYPE".equalsIgnoreCase(metadatum.substring(0, equals))) {
            type = metadatum.substring(equals + 1);
            break;
          }
        }
        result.add(type);
      }
    }
    return result.toArray(EMPTY_STR_ARRAY);
  }

  static String[] toPrimaryValues(Collection<List<String>> lists) {
  if (lists == null || lists.isEmpty()) {
    return null;
  }
  List<String> result = new ArrayList<>(lists.size());
  for (List<String> list : lists) {
    String value = list.get(0);
    if (value != null && !value.isEmpty()) {
      result.add(value);
    }
  }
  return result.toArray(EMPTY_STR_ARRAY);
}

static String toPrimaryValue(List<String> list) {
    return list == null || list.isEmpty() ? null : list.get(0);
  }

  static List<List<String>> matchVCardPrefixedField(CharSequence prefix,
                                                  String rawText,
                                                  boolean trim,
                                                  boolean parseFieldDivider) {
  List<List<String>> matches = null;
  int i = 0;
  int max = rawText.length();

  while (i < max) {

    // At start or after newline, match prefix, followed by optional metadata 
    // (led by ;) ultimately ending in colon
    Matcher matcher = Pattern.compile("(?:^|\n)" + prefix + "(?:;([^:]*))?:",
                                      Pattern.CASE_INSENSITIVE).matcher(rawText);
    if (i > 0) {
      i--; // Find from i-1 not i since looking at the preceding character
    }
    if (!matcher.find(i)) {
      break;
    }
    i = matcher.end(0); // group 0 = whole pattern; end(0) is past final colon

    String metadataString = matcher.group(1); // group 1 = metadata substring
    List<String> metadata = null;
    boolean quotedPrintable = false;
    String quotedPrintableCharset = null;
    String valueType = null;
    if (metadataString != null) {
      for (String metadatum : VCardResultParser.SEMICOLON.split(metadataString)) {
        if (metadata == null) {
          metadata = new ArrayList<>(1);
        }
        metadata.add(metadatum);
        String[] metadatumTokens = VCardResultParser.EQUALS.split(metadatum, 2);
        if (metadatumTokens.length > 1) {
          String key = metadatumTokens[0];
          String value = metadatumTokens[1];
          if ("ENCODING".equalsIgnoreCase(key) && "QUOTED-PRINTABLE".equalsIgnoreCase(value)) {
            quotedPrintable = true;
          } else if ("CHARSET".equalsIgnoreCase(key)) {
            quotedPrintableCharset = value;
          } else if ("VALUE".equalsIgnoreCase(key)) {
            valueType = value;
          }
        }
      }
    }

    int matchStart = i; // Found the start of a match here

    i = VCardResultParser.followedByTabOrSpace(rawText, i, quotedPrintable);

    if (i < 0) {
      // No terminating end character? uh, done. Set i such that loop terminates and break
      i = max;
    } else if (i > matchStart) {
      // found a match
      if (matches == null) {
        matches = new ArrayList<>(1); // lazy init
      }
      if (i >= 1 && rawText.charAt(i - 1) == '\r') {
        i--; // Back up over \r, which really should be there
      }
      String element = rawText.substring(matchStart, i);
      if (trim) {
        element = element.trim();
      }
      if (quotedPrintable) {
        element = VCardResultParser.decodeQuotedPrintable(element, quotedPrintableCharset);
        if (parseFieldDivider) {
          element = VCardResultParser.UNESCAPED_SEMICOLONS.matcher(element).replaceAll("\n").trim();
        }
      } else {
        if (parseFieldDivider) {
          element = VCardResultParser.UNESCAPED_SEMICOLONS.matcher(element).replaceAll("\n").trim();
        }
        element = VCardResultParser.CR_LF_SPACE_TAB.matcher(element).replaceAll("");
        element = VCardResultParser.NEWLINE_ESCAPE.matcher(element).replaceAll("\n");
        element = VCardResultParser.VCARD_ESCAPES.matcher(element).replaceAll("$1");
      }
      // Only handle VALUE=uri specially
      if ("uri".equals(valueType)) {
        // Don't actually support dereferencing URIs, but use scheme-specific part not URI
        // as value, to support tel: and mailto:
        try {
          element = URI.create(element).getSchemeSpecificPart();
        } catch (IllegalArgumentException iae) {
          // ignore
        }
      }
      if (metadata == null) {
        List<String> match = new ArrayList<>(1);
        match.add(element);
        matches.add(match);
      } else {
        metadata.add(0, element);
        matches.add(metadata);
      }
      i++;
    } else {
      i++;
    }

  }

  return matches;
}

static String parseName(String name) {
  int comma = name.indexOf(',');
  if (comma >= 0) {
    // Format may be last,first; switch it around
    return name.substring(comma + 1) + ' ' + name.substring(0, comma);
  }
  return name;
}

static String[] buildPhoneNumbers(String number1,
                                            String number2,
                                            String number3) {
    List<String> numbers = new ArrayList<>(3);
    if (number1 != null) {
      numbers.add(number1);
    }
    if (number2 != null) {
      numbers.add(number2);
    }
    if (number3 != null) {
      numbers.add(number3);
    }
    int size = numbers.size();
    if (size == 0) {
      return null;
    }
    return numbers.toArray(new String[size]);
  }

  static String buildName(String firstName, String lastName) {
    if (firstName == null) {
      return lastName;
    } else {
      return lastName == null ? firstName : firstName + ' ' + lastName;
    }
  }

}
