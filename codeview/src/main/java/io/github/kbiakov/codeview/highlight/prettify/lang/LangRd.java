// Copyright (C) 2012 Jeffrey Arnold
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

import io.github.kbiakov.codeview.highlight.prettify.parser.Prettify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This is similar to the lang-rd.js in JavaScript Prettify.
 * <p/>
 * Support for R documentation (Rd) files
 * <p/>
 * Minimal highlighting or Rd files, basically just highlighting
 * macros. It does not try to identify verbatim or R-like regions of
 * macros as that is too complicated for a lexer.  Descriptions of the
 * Rd format can be found
 * http://cran.r-project.org/doc/manuals/R-exts.html and
 * http://developer.r-project.org/parseRd.pdf.
 *
 * @author Jeffrey Arnold
 */
public class LangRd extends Lang {

    public LangRd() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

        // whitespace
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[\\t\\n\\r \\xA0]+"), null, "\t\n\r " + Character.toString((char) 0xA0)}));
        // all comments begin with '%'
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^%[^\\r\\n]*"), null, "%"}));

        // special macros with no args
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^\\\\(?:cr|l?dots|R|tab)\\b")}));
        // macros
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^\\\\[a-zA-Z@]+")}));
        // highlighted as macros, since technically they are
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^#(?:ifn?def|endif)")}));
        // catch escaped brackets
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^\\\\[{}]")}));
        // punctuation
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[{}()\\[\\]]+")}));

        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"Rd", "rd"});
    }
}
