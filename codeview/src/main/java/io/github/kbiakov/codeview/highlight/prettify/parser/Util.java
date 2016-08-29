// Copyright (C) 2011 Chan Wai Shing
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common Utilities.
 * Some of the functions are port from JavaScript.
 * 
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Util {

  protected Util() {
  }
  
  /**
   * Treat a variable as an boolean in JavaScript style. Note this function can
   * only handle string, integer and boolean currently. All other data type, if
   * null, return false, not null return true.
   *
   * @param var the variable to get value from
   * @return the boolean value
   */
  public static Boolean getVariableValueAsBoolean(Object var) {
    Boolean returnResult = null;

    if (var == null) {
      returnResult = false;
    } else if (var instanceof String) {
      returnResult = !((String) var).isEmpty();
    } else if (var instanceof Integer) {
      returnResult = ((Integer) var) != 0;
    } else if (var instanceof Boolean) {
      returnResult = (Boolean) var;
    } else {
      returnResult = true;
    }

    return returnResult;
  }

  /**
   * Treat a variable as an integer in JavaScript style. Note this function can
   * only handle integer and boolean currently.
   *
   * @param var the variable to get value from
   * @return the integer value
   * @throws IllegalArgumentException the data type of {@code var} is neither
   * integer nor boolean.
   */
  public static Integer getVariableValueAsInteger(Object var) {
    if (var == null) {
      throw new NullPointerException("argument 'var' cannot be null");
    }

    Integer returnResult = -1;

    if (var instanceof Integer) {
      returnResult = (Integer) var;
    } else if (var instanceof Boolean) {
      // Javascript treat true as 1
      returnResult = (Boolean) var ? 1 : 0;
    } else {
      throw new IllegalArgumentException("'var' is neither integer nor boolean");
    }

    return returnResult;
  }

  /**
   * Get all the matches for {@code string} compiled by {@code pattern}. If 
   * {@code isGlobal} is true, the return results will only include the 
   * group 0 matches. It is similar to string.match(regexp) in JavaScript.
   * 
   * @param pattern the regexp
   * @param string the string
   * @param isGlobal similar to JavaScript /g flag
   * 
   * @return all matches
   */
  public static String[] match(Pattern pattern, String string, boolean isGlobal) {
    if (pattern == null) {
      throw new NullPointerException("argument 'pattern' cannot be null");
    }
    if (string == null) {
      throw new NullPointerException("argument 'string' cannot be null");
    }

    List<String> matchesList = new ArrayList<String>();

    Matcher matcher = pattern.matcher(string);
    while (matcher.find()) {
      matchesList.add(matcher.group(0));
      if (!isGlobal) {
        for (int i = 1, iEnd = matcher.groupCount(); i <= iEnd; i++) {
          matchesList.add(matcher.group(i));
        }
      }
    }

    return matchesList.toArray(new String[matchesList.size()]);
  }

  /**
   * Test whether the {@code string} has at least one match by 
   * {@code pattern}.
   * 
   * @param pattern the regexp
   * @param string the string to test
   * 
   * @return true if at least one match, false if no match
   */
  public static boolean test(Pattern pattern, String string) {
    if (pattern == null) {
      throw new NullPointerException("argument 'pattern' cannot be null");
    }
    if (string == null) {
      throw new NullPointerException("argument 'string' cannot be null");
    }
    return pattern.matcher(string).find();
  }

  /**
   * Join the {@code strings} into one string.
   * 
   * @param strings the string list to join
   * 
   * @return the joined string
   */
  public static String join(List<String> strings) {
    if (strings == null) {
      throw new NullPointerException("argument 'strings' cannot be null");
    }
    return join(strings.toArray(new String[strings.size()]));
  }

  /**
   * Join the {@code strings} into one string with {@code delimiter} in 
   * between.
   * 
   * @param strings the string list to join
   * @param delimiter the delimiter
   * 
   * @return the joined string
   */
  public static String join(List<String> strings, String delimiter) {
    if (strings == null) {
      throw new NullPointerException("argument 'strings' cannot be null");
    }
    return join(strings.toArray(new String[strings.size()]), delimiter);
  }

  /**
   * Join the {@code strings} into one string.
   * 
   * @param strings the string list to join
   * 
   * @return the joined string
   */
  public static String join(String[] strings) {
    return join(strings, null);
  }

  /**
   * Join the {@code strings} into one string with {@code delimiter} in 
   * between. It is similar to RegExpObject.test(string) in JavaScript.
   * 
   * @param strings the string list to join
   * @param delimiter the delimiter
   * 
   * @return the joined string
   */
  public static String join(String[] strings, String delimiter) {
    if (strings == null) {
      throw new NullPointerException("argument 'strings' cannot be null");
    }

    StringBuilder sb = new StringBuilder();

    if (strings.length != 0) {
      sb.append(strings[0]);
      for (int i = 1, iEnd = strings.length; i < iEnd; i++) {
        if (delimiter != null) {
          sb.append(delimiter);
        }
        sb.append(strings[i]);
      }
    }

    return sb.toString();
  }

  /**
   * Remove identical adjacent tags from {@code decorations}.
   * 
   * @param decorations see {@link prettify.parser.Job#decorations}
   * @param source the source code
   * 
   * @return the {@code decorations} after treatment
   * 
   * @throws IllegalArgumentException the size of {@code decoration} is not
   * a multiple of 2
   */
  public static List<Object> removeDuplicates(List<Object> decorations, String source) {
    if (decorations == null) {
      throw new NullPointerException("argument 'decorations' cannot be null");
    }
    if (source == null) {
      throw new NullPointerException("argument 'source' cannot be null");
    }
    if ((decorations.size() & 0x1) != 0) {
      throw new IllegalArgumentException("the size of argument 'decorations' should be a multiple of 2");
    }

    List<Object> returnList = new ArrayList<Object>();

    // use TreeMap to remove entrys with same pos
    Map<Integer, Object> orderedMap = new TreeMap<Integer, Object>();
    for (int i = 0, iEnd = decorations.size(); i < iEnd; i += 2) {
      orderedMap.put((Integer) decorations.get(i), decorations.get(i + 1));
    }

    // remove adjacent style
    String previousStyle = null;
    for (Integer pos : orderedMap.keySet()) {
      String style = (String) orderedMap.get(pos);
      if (previousStyle != null && previousStyle.equals(style)) {
        continue;
      }
      returnList.add(pos);
      returnList.add(style);
      previousStyle = style;
    }

    // remove last zero length tag
    int returnListSize = returnList.size();
    if (returnListSize >= 4 && returnList.get(returnListSize - 2).equals(source.length())) {
      returnList.remove(returnListSize - 2);
      returnList.remove(returnListSize - 2);
    }

    return returnList;
  }
}
