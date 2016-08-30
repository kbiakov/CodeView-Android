// Copyright (C) 2011 Kitware Inc.
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
 * This is similar to the lang-mumps.js in JavaScript Prettify.
 * <p/>
 * All comments are adapted from the JavaScript Prettify.
 * <p/>
 * <p/>
 * To use, include prettify.js and this file in your HTML page.
 * Then put your code in an HTML tag like
 * <pre class="prettyprint lang-mumps">(my SQL code)</pre>
 * <p/>
 * Commands, intrinsic functions and variables taken from ISO/IEC 11756:1999(E)
 *
 * @author chris.harris@kitware.com
 *         <p/>
 *         Known issues:
 *         <p/>
 *         - Currently can't distinguish between keywords and local or global variables having the same name
 *         for exampe SET IF="IF?"
 *         - m file are already used for MatLab hence using mumps.
 */
public class LangMumps extends Lang {

    public LangMumps() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

        final String commands = "B|BREAK|" +
                "C|CLOSE|" +
                "D|DO|" +
                "E|ELSE|" +
                "F|FOR|" +
                "G|GOTO|" +
                "H|HALT|" +
                "H|HANG|" +
                "I|IF|" +
                "J|JOB|" +
                "K|KILL|" +
                "L|LOCK|" +
                "M|MERGE|" +
                "N|NEW|" +
                "O|OPEN|" +
                "Q|QUIT|" +
                "R|READ|" +
                "S|SET|" +
                "TC|TCOMMIT|" +
                "TRE|TRESTART|" +
                "TRO|TROLLBACK|" +
                "TS|TSTART|" +
                "U|USE|" +
                "V|VIEW|" +
                "W|WRITE|" +
                "X|XECUTE";

        final String intrinsicVariables = "D|DEVICE|" +
                "EC|ECODE|" +
                "ES|ESTACK|" +
                "ET|ETRAP|" +
                "H|HOROLOG|" +
                "I|IO|" +
                "J|JOB|" +
                "K|KEY|" +
                "P|PRINCIPAL|" +
                "Q|QUIT|" +
                "ST|STACK|" +
                "S|STORAGE|" +
                "SY|SYSTEM|" +
                "T|TEST|" +
                "TL|TLEVEL|" +
                "TR|TRESTART|" +
                "X|" +
                "Y|" +
                "Z[A-Z]*|";

        final String intrinsicFunctions = "A|ASCII|" +
                "C|CHAR|" +
                "D|DATA|" +
                "E|EXTRACT|" +
                "F|FIND|" +
                "FN|FNUMBER|" +
                "G|GET|" +
                "J|JUSTIFY|" +
                "L|LENGTH|" +
                "NA|NAME|" +
                "O|ORDER|" +
                "P|PIECE|" +
                "QL|QLENGTH|" +
                "QS|QSUBSCRIPT|" +
                "Q|QUERY|" +
                "R|RANDOM|" +
                "RE|REVERSE|" +
                "S|SELECT|" +
                "ST|STACK|" +
                "T|TEXT|" +
                "TR|TRANSLATE|" +
                "V|VIEW|" +
                "Z[A-Z]*|";

        final String intrinsic = intrinsicVariables + intrinsicFunctions;

        // Whitespace
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[\t\n\r \\xA0]+"), null, "\t\n\r " + Character.toString((char) 0xA0)}));
        // A double or single quoted, possibly multi-line, string.
        _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^(?:\"(?:[^\"]|\\\\.)*\")"), null, "\""}));

        // A line comment that starts with ;
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^;[^\\r\\n]*"), null, ";"}));
        // Add intrinsic variables and functions as declarations, there not really but it mean
        // they will hilighted differently from commands.
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_DECLARATION, Pattern.compile("^(?:\\$(?:" + intrinsic + "))\\b", Pattern.CASE_INSENSITIVE), null}));
        // Add commands as keywords
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^(?:[^\\$]" + commands + ")\\b", Pattern.CASE_INSENSITIVE), null}));
        // A number is a decimal real literal or in scientific notation.
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^[+-]?(?:(?:\\.\\d+|\\d+(?:\\.\\d*)?)(?:E[+\\-]?\\d+)?)", Pattern.CASE_INSENSITIVE)}));
        // An identifier
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[a-z][a-zA-Z0-9]*", Pattern.CASE_INSENSITIVE)}));
        // Exclude $ % and ^
        _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[^\\w\\t\\n\\r\\xA0\\\"\\$;%\\^]|_")}));

        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Arrays.asList(new String[]{"mumps"});
    }
}
