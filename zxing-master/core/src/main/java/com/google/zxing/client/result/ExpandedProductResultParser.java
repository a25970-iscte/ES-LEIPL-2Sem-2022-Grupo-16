/*
 * Copyright (C) 2010 ZXing authors
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

/*
 * These authors would like to acknowledge the Spanish Ministry of Industry,
 * Tourism and Trade, for the support in the project TSI020301-2008-2
 * "PIRAmIDE: Personalizable Interactions with Resources on AmI-enabled
 * Mobile Dynamic Environments", led by Treelogic
 * ( http://www.treelogic.com/ ):
 *
 *   http://www.piramidepse.com/
 */

package com.google.zxing.client.result;

import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

/**
 * Parses strings of digits that represent a RSS Extended code.
 * 
 * @author Antonio Manuel Benjumea Conde, Servinform, S.A.
 * @author Agustín Delgado, Servinform, S.A.
 */
public final class ExpandedProductResultParser extends ResultParser {

  private String rawText;
  private String productID = null;
  private String sscc = null;
  private String lotNumber = null;
  private String productionDate = null;
  private String packagingDate = null;
  private String bestBeforeDate = null;
  private String expirationDate = null;
  private String weight = null;
  private String weightType = null;
  private String weightIncrement = null;
  private String price = null;
  private String priceIncrement = null;
  private String priceCurrency = null;
  private Map<String, String> uncommonAIs = new HashMap<>();

  @Override
  public ExpandedProductParsedResult parse(Result result) {
    BarcodeFormat format = result.getBarcodeFormat();
    if (format != BarcodeFormat.RSS_EXPANDED) {
      // ExtendedProductParsedResult NOT created. Not a RSS Expanded barcode
      return null;
    }
    rawText = getMassagedText(result);

    int i = 0;

    while (i < rawText.length()) {
      String ai = findAIvalue(i, rawText);
      if (ai == null) {
        // Error. Code doesn't match with RSS expanded pattern
        // ExtendedProductParsedResult NOT created. Not match with RSS Expanded pattern
        return null;
      }
      i += ai.length() + 2;
      String value = findValue(i, rawText);
      i += value.length();

      switch (ai) {
        case "00":
          sscc = value;
          break;
        case "01":
          productID = value;
          break;
        case "10":
          lotNumber = value;
          break;
        case "11":
          productionDate = value;
          break;
        case "13":
          packagingDate = value;
          break;
        case "15":
          bestBeforeDate = value;
          break;
        case "17":
          expirationDate = value;
          break;
        case "3109":
          weight = value;
          weightType = ExpandedProductParsedResult.KILOGRAM;
          weightIncrement = ai.substring(3);
          break;
        case "3209":
          weight = value;
          weightType = ExpandedProductParsedResult.POUND;
          weightIncrement = ai.substring(3);
          break;
        case "3923":
          price = value;
          priceIncrement = ai.substring(3);
          break;
        case "3933":
          if (value.length() < 4) {
            // The value must have more of 3 symbols (3 for currency and
            // 1 at least for the price)
            // ExtendedProductParsedResult NOT created. Not match with RSS Expanded pattern
            return null;
          }
          price = value.substring(3);
          priceCurrency = value.substring(0, 3);
          priceIncrement = ai.substring(3);
          break;
        default:
          // No match with common AIs
          String aisome = ai;
          String[] strAr = { "3100", "3101", "3102", "3103", "3104", "3105", "3106", "3107", "3108", "3200", "3201",
              "3202", "3203", "3204", "3205", "3206", "3207", "3208", "3920", "3921", "3922", "3930", "3931", "3932" };
          for (int index = 0; index < strAr.length; index++) {
            if (!aisome.equals(strAr[index]))
              uncommonAIs.put(aisome, value);
          }
          break;
      }
    }

    return new ExpandedProductParsedResult(rawText,
        productID,
        sscc,
        lotNumber,
        productionDate,
        packagingDate,
        bestBeforeDate,
        expirationDate,
        weight,
        weightType,
        weightIncrement,
        price,
        priceIncrement,
        priceCurrency,
        uncommonAIs);
  }

  private static String findAIvalue(int i, String rawText) {
    char c = rawText.charAt(i);
    // First character must be a open parenthesis.If not, ERROR
    if (c != '(') {
      return null;
    }

    CharSequence rawTextAux = rawText.substring(i + 1);

    StringBuilder buf = new StringBuilder();
    for (int index = 0; index < rawTextAux.length(); index++) {
      char currentChar = rawTextAux.charAt(index);
      if (currentChar == ')') {
        return buf.toString();
      }
      if (currentChar < '0' || currentChar > '9') {
        return null;
      }
      buf.append(currentChar);
    }
    return buf.toString();
  }

  private static String findValue(int i, String rawText) {
    StringBuilder buf = new StringBuilder();
    String rawTextAux = rawText.substring(i);

    for (int index = 0; index < rawTextAux.length(); index++) {
      char c = rawTextAux.charAt(index);
      if (c == '(') {
        // We look for a new AI. If it doesn't exist (ERROR), we continue
        // with the iteration
        if (findAIvalue(index, rawTextAux) != null) {
          break;
        }
        buf.append('(');
      } else {
        buf.append(c);
      }
    }
    return buf.toString();
  }
}
