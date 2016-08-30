// Copyright (C) 2009 Google Inc.
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
 * This is similar to the lang-hs.js in JavaScript Prettify.
 * 
 * All comments are adapted from the JavaScript Prettify.
 * 
 * <p>
 * Registers a language handler for Haskell.
 *
 *
 * To use, include prettify.js and this file in your HTML page.
 * Then put your code in an HTML tag like
 *      <pre class="prettyprint lang-hs">(my lisp code)</pre>
 * The lang-cl class identifies the language as common lisp.
 * This file supports the following language extensions:
 *     lang-cl - Common Lisp
 *     lang-el - Emacs Lisp
 *     lang-lisp - Lisp
 *     lang-scm - Scheme
 *
 *
 * I used http://www.informatik.uni-freiburg.de/~thiemann/haskell/haskell98-report-html/syntax-iso.html
 * as the basis, but ignore the way the ncomment production nests since this
 * makes the lexical grammar irregular.  It might be possible to support
 * ncomments using the lookbehind filter.
 *
 *
 * @author mikesamuel@gmail.com
 */
public class LangHs extends Lang {

  public LangHs() {
    List<List<Object>> _shortcutStylePatterns = new ArrayList<List<Object>>();
    List<List<Object>> _fallthroughStylePatterns = new ArrayList<List<Object>>();

    // Whitespace
    // whitechar    ->    newline | vertab | space | tab | uniWhite
    // newline      ->    return linefeed | return | linefeed | formfeed
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^[\\t\\n\\x0B\\x0C\\r ]+"), null, "\t\n" + Character.toString((char) 0x0B) + Character.toString((char) 0x0C) + "\r "}));
    // Single line double and single-quoted strings.
    // char         ->    ' (graphic<' | \> | space | escape<\&>) '
    // string       ->    " {graphic<" | \> | space | escape | gap}"
    // escape       ->    \ ( charesc | ascii | decimal | o octal
    //                        | x hexadecimal )
    // charesc      ->    a | b | f | n | r | t | v | \ | " | ' | &
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\\"(?:[^\\\"\\\\\\n\\x0C\\r]|\\\\[\\s\\S])*(?:\\\"|$)"), null, "\""}));
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_STRING, Pattern.compile("^\\'(?:[^\\'\\\\\\n\\x0C\\r]|\\\\[^&])\\'?"), null, "'"}));
    // decimal      ->    digit{digit}
    // octal        ->    octit{octit}
    // hexadecimal  ->    hexit{hexit}
    // integer      ->    decimal
    //               |    0o octal | 0O octal
    //               |    0x hexadecimal | 0X hexadecimal
    // float        ->    decimal . decimal [exponent]
    //               |    decimal exponent
    // exponent     ->    (e | E) [+ | -] decimal
    _shortcutStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_LITERAL, Pattern.compile("^(?:0o[0-7]+|0x[\\da-f]+|\\d+(?:\\.\\d+)?(?:e[+\\-]?\\d+)?)", Pattern.CASE_INSENSITIVE), null, "0123456789"}));
    // Haskell does not have a regular lexical grammar due to the nested
    // ncomment.
    // comment      ->    dashes [ any<symbol> {any}] newline
    // ncomment     ->    opencom ANYseq {ncomment ANYseq}closecom
    // dashes       ->    '--' {'-'}
    // opencom      ->    '{-'
    // closecom     ->    '-}'
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_COMMENT, Pattern.compile("^(?:(?:--+(?:[^\\r\\n\\x0C]*)?)|(?:\\{-(?:[^-]|-+[^-\\}])*-\\}))")}));
    // reservedid   ->    case | class | data | default | deriving | do
    //               |    else | if | import | in | infix | infixl | infixr
    //               |    instance | let | module | newtype | of | then
    //               |    type | where | _
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_KEYWORD, Pattern.compile("^(?:case|class|data|default|deriving|do|else|if|import|in|infix|infixl|infixr|instance|let|module|newtype|of|then|type|where|_)(?=[^a-zA-Z0-9\\']|$)"), null}));
    // qvarid       ->    [ modid . ] varid
    // qconid       ->    [ modid . ] conid
    // varid        ->    (small {small | large | digit | ' })<reservedid>
    // conid        ->    large {small | large | digit | ' }
    // modid        ->    conid
    // small        ->    ascSmall | uniSmall | _
    // ascSmall     ->    a | b | ... | z
    // uniSmall     ->    any Unicode lowercase letter
    // large        ->    ascLarge | uniLarge
    // ascLarge     ->    A | B | ... | Z
    // uniLarge     ->    any uppercase or titlecase Unicode letter
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PLAIN, Pattern.compile("^(?:[A-Z][\\w\\']*\\.)*[a-zA-Z][\\w\\']*")}));
    // matches the symbol production
    _fallthroughStylePatterns.add(Arrays.asList(new Object[]{Prettify.PR_PUNCTUATION, Pattern.compile("^[^\\t\\n\\x0B\\x0C\\r a-zA-Z0-9\\'\\\"]+")}));

    setShortcutStylePatterns(_shortcutStylePatterns);
    setFallthroughStylePatterns(_fallthroughStylePatterns);
  }

  public static List<String> getFileExtensions() {
    return Arrays.asList(new String[]{"hs"});
  }
}
