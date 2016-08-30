// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package io.github.kbiakov.codeview.highlight.prettify.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is similar to the combinePrefixPattern.js in JavaScript Prettify.
 * 
 * All comments are adapted from the JavaScript Prettify.
 * 
 * @author mikesamuel@gmail.com
 */
public class CombinePrefixPattern {

  protected int capturedGroupIndex = 0;
  protected boolean needToFoldCase = false;

  public CombinePrefixPattern() {
  }

  /**
   * Given a group of {@link java.util.regex.Pattern}s, returns a {@code RegExp} that globally
   * matches the union of the sets of strings matched by the input RegExp.
   * Since it matches globally, if the input strings have a start-of-input
   * anchor (/^.../), it is ignored for the purposes of unioning.
   * @param regexs non multiline, non-global regexs.
   * @return Pattern a global regex.
   */
  public Pattern combinePrefixPattern(List<Pattern> regexs) throws Exception {
    boolean ignoreCase = false;

    for (int i = 0, n = regexs.size(); i < n; ++i) {
      Pattern regex = regexs.get(i);
      if ((regex.flags() & Pattern.CASE_INSENSITIVE) != 0) {
        ignoreCase = true;
      } else if (Util.test(Pattern.compile("[a-z]", Pattern.CASE_INSENSITIVE), regex.pattern().replaceAll("\\\\[Uu][0-9A-Fa-f]{4}|\\\\[Xx][0-9A-Fa-f]{2}|\\\\[^UuXx]", ""))) {
        needToFoldCase = true;
        ignoreCase = false;
        break;
      }
    }

    List<String> rewritten = new ArrayList<String>();
    for (int i = 0, n = regexs.size(); i < n; ++i) {
      Pattern regex = regexs.get(i);
      if ((regex.flags() & Pattern.MULTILINE) != 0) {
        throw new Exception(regex.pattern());
      }
      rewritten.add("(?:" + allowAnywhereFoldCaseAndRenumberGroups(regex) + ")");
    }

    return ignoreCase ? Pattern.compile(Util.join(rewritten, "|"), Pattern.CASE_INSENSITIVE) : Pattern.compile(Util.join(rewritten, "|"));
  }
  protected static final Map<Character, Integer> escapeCharToCodeUnit = new HashMap<Character, Integer>();

  static {
    escapeCharToCodeUnit.put('b', 8);
    escapeCharToCodeUnit.put('t', 9);
    escapeCharToCodeUnit.put('n', 0xa);
    escapeCharToCodeUnit.put('v', 0xb);
    escapeCharToCodeUnit.put('f', 0xc);
    escapeCharToCodeUnit.put('r', 0xf);
  }

  protected static int decodeEscape(String charsetPart) {
    Integer cc0 = charsetPart.codePointAt(0);
    if (cc0 != 92 /* \\ */) {
      return cc0;
    }
    char c1 = charsetPart.charAt(1);
    cc0 = escapeCharToCodeUnit.get(c1);
    if (cc0 != null) {
      return cc0;
    } else if ('0' <= c1 && c1 <= '7') {
      return Integer.parseInt(charsetPart.substring(1), 8);
    } else if (c1 == 'u' || c1 == 'x') {
      return Integer.parseInt(charsetPart.substring(2), 16);
    } else {
      return charsetPart.codePointAt(1);
    }
  }

  protected static String encodeEscape(int charCode) {
    if (charCode < 0x20) {
      return (charCode < 0x10 ? "\\x0" : "\\x") + Integer.toString(charCode, 16);
    }

    String ch = new String(Character.toChars(charCode));
    return (charCode == '\\' || charCode == '-' || charCode == ']' || charCode == '^')
            ? "\\" + ch : ch;
  }

  protected static String caseFoldCharset(String charSet) {
    String[] charsetParts = Util.match(Pattern.compile("\\\\u[0-9A-Fa-f]{4}"
            + "|\\\\x[0-9A-Fa-f]{2}"
            + "|\\\\[0-3][0-7]{0,2}"
            + "|\\\\[0-7]{1,2}"
            + "|\\\\[\\s\\S]"
            + "|-"
            + "|[^-\\\\]"), charSet.substring(1, charSet.length() - 1), true);
    List<List<Integer>> ranges = new ArrayList<List<Integer>>();
    boolean inverse = charsetParts[0] != null && charsetParts[0].equals("^");

    List<String> out = new ArrayList<String>(Arrays.asList(new String[]{"["}));
    if (inverse) {
      out.add("^");
    }

    for (int i = inverse ? 1 : 0, n = charsetParts.length; i < n; ++i) {
      String p = charsetParts[i];
      if (Util.test(Pattern.compile("\\\\[bdsw]", Pattern.CASE_INSENSITIVE), p)) {  // Don't muck with named groups.
        out.add(p);
      } else {
        int start = decodeEscape(p);
        int end;
        if (i + 2 < n && "-".equals(charsetParts[i + 1])) {
          end = decodeEscape(charsetParts[i + 2]);
          i += 2;
        } else {
          end = start;
        }
        ranges.add(Arrays.asList(new Integer[]{start, end}));
        // If the range might intersect letters, then expand it.
        // This case handling is too simplistic.
        // It does not deal with non-latin case folding.
        // It works for latin source code identifiers though.
        if (!(end < 65 || start > 122)) {
          if (!(end < 65 || start > 90)) {
            ranges.add(Arrays.asList(new Integer[]{Math.max(65, start) | 32, Math.min(end, 90) | 32}));
          }
          if (!(end < 97 || start > 122)) {
            ranges.add(Arrays.asList(new Integer[]{Math.max(97, start) & ~32, Math.min(end, 122) & ~32}));
          }
        }
      }
    }

    // [[1, 10], [3, 4], [8, 12], [14, 14], [16, 16], [17, 17]]
    // -> [[1, 12], [14, 14], [16, 17]]
    Collections.sort(ranges, new Comparator<List<Integer>>() {

      @Override
      public int compare(List<Integer> a, List<Integer> b) {
        return a.get(0) != b.get(0) ? (a.get(0) - b.get(0)) : (b.get(1) - a.get(1));
      }
    });
    List<List<Integer>> consolidatedRanges = new ArrayList<List<Integer>>();
//        List<Integer> lastRange = Arrays.asList(new Integer[]{0, 0});
    List<Integer> lastRange = new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 0}));
    for (int i = 0; i < ranges.size(); ++i) {
      List<Integer> range = ranges.get(i);
      if (lastRange.get(1) != null && range.get(0) <= lastRange.get(1) + 1) {
        lastRange.set(1, Math.max(lastRange.get(1), range.get(1)));
      } else {
        // reference of lastRange is added
        consolidatedRanges.add(lastRange = range);
      }
    }

    for (int i = 0; i < consolidatedRanges.size(); ++i) {
      List<Integer> range = consolidatedRanges.get(i);
      out.add(encodeEscape(range.get(0)));
      if (range.get(1) > range.get(0)) {
        if (range.get(1) + 1 > range.get(0)) {
          out.add("-");
        }
        out.add(encodeEscape(range.get(1)));
      }
    }
    out.add("]");

    return Util.join(out);
  }

  protected String allowAnywhereFoldCaseAndRenumberGroups(Pattern regex) {
    // Split into character sets, escape sequences, punctuation strings
    // like ('(', '(?:', ')', '^'), and runs of characters that do not
    // include any of the above.
    String[] parts = Util.match(Pattern.compile("(?:"
            + "\\[(?:[^\\x5C\\x5D]|\\\\[\\s\\S])*\\]" // a character set
            + "|\\\\u[A-Fa-f0-9]{4}" // a unicode escape
            + "|\\\\x[A-Fa-f0-9]{2}" // a hex escape
            + "|\\\\[0-9]+" // a back-reference or octal escape
            + "|\\\\[^ux0-9]" // other escape sequence
            + "|\\(\\?[:!=]" // start of a non-capturing group
            + "|[\\(\\)\\^]" // start/end of a group, or line start
            + "|[^\\x5B\\x5C\\(\\)\\^]+" // run of other characters
            + ")"), regex.pattern(), true);
    int n = parts.length;

    // Maps captured group numbers to the number they will occupy in
    // the output or to -1 if that has not been determined, or to
    // undefined if they need not be capturing in the output.
    Map<Integer, Integer> capturedGroups = new HashMap<Integer, Integer>();

    // Walk over and identify back references to build the capturedGroups
    // mapping.
    for (int i = 0, groupIndex = 0; i < n; ++i) {
      String p = parts[i];
      if (p.equals("(")) {
        // groups are 1-indexed, so max group index is count of '('
        ++groupIndex;
      } else if ('\\' == p.charAt(0)) {
        try {
          int decimalValue = Math.abs(Integer.parseInt(p.substring(1)));
          if (decimalValue <= groupIndex) {
            capturedGroups.put(decimalValue, -1);
          } else {
            // Replace with an unambiguous escape sequence so that
            // an octal escape sequence does not turn into a backreference
            // to a capturing group from an earlier regex.
            parts[i] = encodeEscape(decimalValue);
          }
        } catch (NumberFormatException ex) {
        }
      }
    }

    // Renumber groups and reduce capturing groups to non-capturing groups
    // where possible.
    for (int i : capturedGroups.keySet()) {
      if (-1 == capturedGroups.get(i)) {
        capturedGroups.put(i, ++capturedGroupIndex);
      }
    }
    for (int i = 0, groupIndex = 0; i < n; ++i) {
      String p = parts[i];
      if (p.equals("(")) {
        ++groupIndex;
        if (capturedGroups.get(groupIndex) == null) {
          parts[i] = "(?:";
        }
      } else if ('\\' == p.charAt(0)) {
        try {
          int decimalValue = Math.abs(Integer.parseInt(p.substring(1)));
          if (decimalValue <= groupIndex) {
            parts[i] = "\\" + capturedGroups.get(decimalValue);
          }
        } catch (NumberFormatException ex) {
        }
      }
    }

    // Remove any prefix anchors so that the output will match anywhere.
    // ^^ really does mean an anchored match though.
    for (int i = 0; i < n; ++i) {
      if ("^".equals(parts[i]) && !"^".equals(parts[i + 1])) {
        parts[i] = "";
      }
    }

    // Expand letters to groups to handle mixing of case-sensitive and
    // case-insensitive patterns if necessary.
    if ((regex.flags() & Pattern.CASE_INSENSITIVE) != 0 && needToFoldCase) {
      for (int i = 0; i < n; ++i) {
        String p = parts[i];
        char ch0 = p.length() > 0 ? p.charAt(0) : 0;
        if (p.length() >= 2 && ch0 == '[') {
          parts[i] = caseFoldCharset(p);
        } else if (ch0 != '\\') {
          // TODO: handle letters in numeric escapes.
          StringBuffer sb = new StringBuffer();
          Matcher _matcher = Pattern.compile("[a-zA-Z]").matcher(p);
          while (_matcher.find()) {
            int cc = _matcher.group(0).codePointAt(0);
            _matcher.appendReplacement(sb, "");
            sb.append("[").append(Character.toString((char) (cc & ~32))).append(Character.toString((char) (cc | 32))).append("]");
          }
          _matcher.appendTail(sb);
          parts[i] = sb.toString();
        }
      }
    }

    return Util.join(parts);
  }
}
