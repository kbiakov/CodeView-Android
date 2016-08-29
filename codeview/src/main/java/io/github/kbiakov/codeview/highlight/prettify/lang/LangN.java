// Copyright (C) 2011 Zimin A.V.
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
 * This is similar to the lang-n.js in JavaScript Prettify.
 * 
 * All comments are adapted from the JavaScript Prettify.
 * 
 * <p>
 * Registers a language handler for the Nemerle language.
 * http://nemerle.org
 * @author Zimin A.V.
 */
public class LangN extends Lang {

  protected static String keywords = "abstract|and|as|base|catch|class|def|delegate|enum|event|extern|false|finally|"
          + "fun|implements|interface|internal|is|macro|match|matches|module|mutable|namespace|new|"
          + "null|out|override|params|partial|private|protected|public|ref|sealed|static|struct|"
          + "syntax|this|throw|true|try|type|typeof|using|variant|virtual|volatile|when|where|with|"
          + "assert|assert2|async|break|checked|continue|do|else|ensures|for|foreach|if|late|lock|new|nolate|"
          + "otherwise|regexp|repeat|requires|return|surroundwith|unchecked|unless|using|while|yield";

  public LangN() {
    List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
    List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^(?:\\'(?:[^\\\\\\'\\r\\n]|\\\\.)*\\'|\\\"(?:[^\\\\\\\"\\r\\n]|\\\\.)*(?:\\\"|$))"), null, "\""}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^#(?:(?:define|elif|else|endif|error|ifdef|include|ifndef|line|pragma|undef|warning)\\b|[^\\r\\n]*)"), null, "#"}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^\\s+"), null, " \r\n\t" + Character.toString((char) 0xA0)}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^@\\\"(?:[^\\\"]|\\\"\\\")*(?:\\\"|$)"), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^<#(?:[^#>])*(?:#>|$)"), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^<(?:(?:(?:\\.\\.\\/)*|\\/?)(?:[\\w-]+(?:\\/[\\w-]+)+)?[\\w-]+\\.h|[a-z]\\w*)>"), null,}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^\\/\\/[^\\r\\n]*"), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^\\/\\*[\\s\\S]*?(?:\\*\\/|$)"), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^(?:" + keywords + ")\\\\b"), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TYPE, Pattern.compile("^(?:array|bool|byte|char|decimal|double|float|int|list|long|object|sbyte|short|string|ulong|uint|ufloat|ulong|ushort|void)\\b"), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^@[a-z_$][a-z_$@0-9]*", Pattern.CASE_INSENSITIVE), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_TYPE, Pattern.compile("^@[A-Z]+[a-z][A-Za-z_$@0-9]*"), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^'?[A-Za-z_$][a-z_$@0-9]*", Pattern.CASE_INSENSITIVE), null}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:"
              // A hex number
              + "0x[a-f0-9]+"
              // or an octal or decimal number,
              + "|(?:\\\\d(?:_\\\\d+)*\\\\d*(?:\\\\.\\\\d*)?|\\\\.\\\\d\\\\+)"
              // possibly in scientific notation
              + "(?:e[+\\\\-]?\\\\d+)?"
              + ")"
              // with an optional modifier like UL for unsigned long
              + "[a-z]*", Pattern.CASE_INSENSITIVE), null, "0123456789"}));
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^.[^\\s\\w\\.$@\\'\\\"\\`\\/\\#]*"), null}));

    setShortcutStylePatterns(_shortcutStylePatterns);
    setFallthroughStylePatterns(_fallthroughStylePatterns);
  }

  public static List<String> getFileExtensions() {
    return Arrays.asList(new String[]{"n", "nemerle"});
  }
}
