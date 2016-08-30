// Copyright (C) 2010 ribrdb @ code.google.com
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
package io.github.kbiakov.codeview.highlight.prettify.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

/**
 * This is similar to the lang-yaml.js in JavaScript Prettify.
 * 
 * All comments are adapted from the JavaScript Prettify.
 * 
 * <p>
 * Registers a language handler for YAML.
 *
 * @author ribrdb
 */
public class LangYaml extends Lang {

  public LangYaml() {
    List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
    List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[:|>?]+"), null, ":|>?"}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_DECLARATION, Pattern.compile("^%(?:YAML|TAG)[^#\\r\\n]+"), null, "%"}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TYPE, Pattern.compile("^[&]\\S+"), null, "&"}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TYPE, Pattern.compile("^!\\S*"), null, "!"}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\"(?:[^\\\\\"]|\\\\.)*(?:\"|$)"), null, "\""}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^'(?:[^']|'')*(?:'|$)"), null, "'"}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^#[^\\r\\n]*"), null, "#"}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^\\s+"), null, " \t\r\n"}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_DECLARATION, Pattern.compile("^(?:---|\\.\\.\\.)(?:[\\r\\n]|$)")}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^-")}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^\\w+:[ \\r\\n]")}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^\\w+")}));

    setShortcutStylePatterns(_shortcutStylePatterns);
    setFallthroughStylePatterns(_fallthroughStylePatterns);
  }

  public static List<String> getFileExtensions() {
    return Arrays.asList(new String[]{"yaml", "yml"});
  }
}
