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

import io.github.kbiakov.codeview.highlight.prettify.lang.Lang;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangAppollo;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangBasic;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangClj;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangCss;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangDart;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangErlang;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangGo;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangHs;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangKotlin;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangLasso;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangLisp;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangLlvm;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangLogtalk;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangLua;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangMatlab;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangMd;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangMl;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangMumps;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangN;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangPascal;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangR;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangRd;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangScala;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangSql;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangTcl;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangTex;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangVb;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangVhdl;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangWiki;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangXq;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangYaml;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangEx;
import io.github.kbiakov.codeview.highlight.prettify.lang.LangSwift;

/**
 * This is similar to the prettify.js in JavaScript Prettify.
 * 
 * All comments are adapted from the JavaScript Prettify.
 * 
 * <p>
 * Some functions for browser-side pretty printing of code contained in html.
 * </p>
 *
 * <p>
 * For a fairly comprehensive set of languages see the
 * <a href="http://google-code-prettify.googlecode.com/svn/trunk/README.html#langs">README</a>
 * file that came with this source.  At a minimum, the lexer should work on a
 * number of languages including C and friends, Java, Python, Bash, SQL, HTML,
 * XML, CSS, Javascript, and Makefiles.  It works passably on Ruby, PHP and Awk
 * and a subset of Perl, but, because of commenting conventions, doesn't work on
 * Smalltalk, Lisp-like, or CAML-like languages without an explicit lang class.
 * <p>
 * Usage: <ol>
 * <li> include this source file in an html page via
 *   {@code <script type="text/javascript" src="/path/to/prettify.js"></script>}
 * <li> define style rules.  See the example page for examples.
 * <li> mark the {@code <pre>} and {@code <code>} tags in your source with
 *    {@code class=prettyprint.}
 *    You can also use the (html deprecated) {@code <xmp>} tag, but the pretty
 *    printer needs to do more substantial DOM manipulations to support that, so
 *    some css styles may not be preserved.
 * </ol>
 * That's it.  I wanted to keep the API as simple as possible, so there's no
 * need to specify which language the code is in, but if you wish, you can add
 * another class to the {@code <pre>} or {@code <code>} element to specify the
 * language, as in {@code <pre class="prettyprint lang-java">}.  Any class that
 * starts with "lang-" followed by a file extension, specifies the file type.
 * See the "lang-*.js" files in this directory for code that implements
 * per-language file handlers.
 * <p>
 * Change log:<br>
 * cbeust, 2006/08/22
 * <blockquote>
 *   Java annotations (start with "@") are now captured as literals ("lit")
 * </blockquote>
 */
public class Prettify {

  private static final Logger LOG = Logger.getLogger(Prettify.class.getName());
  // Keyword lists for various languages.
  public static final String FLOW_CONTROL_KEYWORDS = "break,continue,do,else,for,if,return,while";
  public static final String C_KEYWORDS = FLOW_CONTROL_KEYWORDS + "," + "auto,case,char,const,default,"
          + "double,enum,extern,float,goto,inline,int,long,register,short,signed,"
          + "sizeof,static,struct,switch,typedef,union,unsigned,void,volatile";
  public static final String COMMON_KEYWORDS = C_KEYWORDS + "," + "catch,class,delete,false,import,"
          + "new,operator,private,protected,public,this,throw,true,try,typeof";
  public static final String CPP_KEYWORDS = COMMON_KEYWORDS + "," + "alignof,align_union,asm,axiom,bool,"
          + "concept,concept_map,const_cast,constexpr,decltype,delegate,"
          + "dynamic_cast,explicit,export,friend,generic,late_check,"
          + "mutable,namespace,nullptr,property,reinterpret_cast,static_assert,"
          + "static_cast,template,typeid,typename,using,virtual,where";
  public static final String JAVA_KEYWORDS = COMMON_KEYWORDS + ","
          + "abstract,assert,boolean,byte,extends,final,finally,implements,import,"
          + "instanceof,interface,null,native,package,strictfp,super,synchronized,"
          + "throws,transient";
  public static final String RUST_KEYWORDS = FLOW_CONTROL_KEYWORDS + "," + "as,assert,const,copy,drop,"
          + "enum,extern,fail,false,fn,impl,let,log,loop,match,mod,move,mut,priv,"
          + "pub,pure,ref,self,static,struct,true,trait,type,unsafe,use";
  public static final String CSHARP_KEYWORDS = JAVA_KEYWORDS + ","
          + "as,base,by,checked,decimal,delegate,descending,dynamic,event,"
          + "fixed,foreach,from,group,implicit,in,internal,into,is,let,"
          + "lock,object,out,override,orderby,params,partial,readonly,ref,sbyte,"
          + "sealed,stackalloc,string,select,uint,ulong,unchecked,unsafe,ushort,"
          + "var,virtual,where";
  public static final String COFFEE_KEYWORDS = "all,and,by,catch,class,else,extends,false,finally,"
          + "for,if,in,is,isnt,loop,new,no,not,null,of,off,on,or,return,super,then,"
          + "throw,true,try,unless,until,when,while,yes";
  public static final String JSCRIPT_KEYWORDS = COMMON_KEYWORDS + ","
          + "debugger,eval,export,function,get,null,set,undefined,var,with,"
          + "Infinity,NaN";
  public static final String PERL_KEYWORDS = "caller,delete,die,do,dump,elsif,eval,exit,foreach,for,"
          + "goto,if,import,last,local,my,next,no,our,print,package,redo,require,"
          + "sub,undef,unless,until,use,wantarray,while,BEGIN,END";
  public static final String PYTHON_KEYWORDS = FLOW_CONTROL_KEYWORDS + "," + "and,as,assert,class,def,del,"
          + "elif,except,exec,finally,from,global,import,in,is,lambda,"
          + "nonlocal,not,or,pass,print,raise,try,with,yield,"
          + "False,True,None";
  public static final String RUBY_KEYWORDS = FLOW_CONTROL_KEYWORDS + "," + "alias,and,begin,case,class,"
          + "def,defined,elsif,end,ensure,false,in,module,next,nil,not,or,redo,"
          + "rescue,retry,self,super,then,true,undef,unless,until,when,yield,"
          + "BEGIN,END";
  public static final String SH_KEYWORDS = FLOW_CONTROL_KEYWORDS + "," + "case,done,elif,esac,eval,fi,"
          + "function,in,local,set,then,until";
  public static final String ALL_KEYWORDS = CPP_KEYWORDS + "," + CSHARP_KEYWORDS + "," + JSCRIPT_KEYWORDS + "," + PERL_KEYWORDS + ","
          + PYTHON_KEYWORDS + "," + RUBY_KEYWORDS + "," + SH_KEYWORDS;
  public static final Pattern C_TYPES = Pattern.compile("^(DIR|FILE|vector|(de|priority_)?queue|list|stack|(const_)?iterator|(multi)?(set|map)|bitset|u?(int|float)\\d*)\\b");
  // token style names.  correspond to css classes
  /**
   * token style for a string literal
   */
  public static final String PR_STRING = "str";
  /**
   * token style for a keyword
   */
  public static final String PR_KEYWORD = "kwd";
  /**
   * token style for a comment
   */
  public static final String PR_COMMENT = "com";
  /**
   * token style for a type
   */
  public static final String PR_TYPE = "typ";
  /**
   * token style for a literal value.  e.g. 1, null, true.
   */
  public static final String PR_LITERAL = "lit";
  /**
   * token style for a punctuation string.
   */
  public static final String PR_PUNCTUATION = "pun";
  /**
   * token style for a plain text.
   */
  public static final String PR_PLAIN = "pln";
  /**
   * token style for an sgml tag.
   */
  public static final String PR_TAG = "tag";
  /**
   * token style for a markup declaration such as a DOCTYPE.
   */
  public static final String PR_DECLARATION = "dec";
  /**
   * token style for embedded source.
   */
  public static final String PR_SOURCE = "src";
  /**
   * token style for an sgml attribute name.
   */
  public static final String PR_ATTRIB_NAME = "atn";
  /**
   * token style for an sgml attribute value.
   */
  public static final String PR_ATTRIB_VALUE = "atv";
  /**
   * A class that indicates a section of markup that is not code, e.g. to allow
   * embedding of line numbers within code listings.
   */
  public static final String PR_NOCODE = "nocode";
  /**
   * A set of tokens that can precede a regular expression literal in
   * javascript
   * http://web.archive.org/web/20070717142515/http://www.mozilla.org/js/language/js20/rationale/syntax.html
   * has the full list, but I've removed ones that might be problematic when
   * seen in languages that don't support regular expression literals.
   *
   * <p>Specifically, I've removed any keywords that can't precede a regexp
   * literal in a syntactically legal javascript program, and I've removed the
   * "in" keyword since it's not a keyword in many languages, and might be used
   * as a count of inches.
   *
   * <p>The link above does not accurately describe EcmaScript rules since
   * it fails to distinguish between (a=++/b/i) and (a++/b/i) but it works
   * very well in practice.
   */
  private static final String REGEXP_PRECEDER_PATTERN = "(?:^^\\.?|[+-]|[!=]=?=?|\\#|%=?|&&?=?|\\(|\\*=?|[+\\-]=|->|\\/=?|::?|<<?=?|>>?>?=?|,|;|\\?|@|\\[|~|\\{|\\^\\^?=?|\\|\\|?=?|break|case|continue|delete|do|else|finally|instanceof|return|throw|try|typeof)\\s*";
  // CAVEAT: this does not properly handle the case where a regular
  // expression immediately follows another since a regular expression may
  // have flags for case-sensitivity and the like.  Having regexp tokens
  // adjacent is not valid in any language I'm aware of, so I'm punting.
  // TODO: maybe style special characters inside a regexp as punctuation.

  public Prettify() {
    try {
      Map<String, Object> decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", ALL_KEYWORDS);
      decorateSourceMap.put("hashComments", true);
      decorateSourceMap.put("cStyleComments", true);
      decorateSourceMap.put("multiLineStrings", true);
      decorateSourceMap.put("regexLiterals", true);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"default-code"}));

      List<List<Object>> shortcutStylePatterns, fallthroughStylePatterns;

      shortcutStylePatterns = new ArrayList<List<Object>>();
      fallthroughStylePatterns = new ArrayList<List<Object>>();
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_PLAIN, Pattern.compile("^[^<?]+")}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_DECLARATION, Pattern.compile("^<!\\w[^>]*(?:>|$)")}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_COMMENT, Pattern.compile("^<\\!--[\\s\\S]*?(?:-\\->|$)")}));
      // Unescaped content in an unknown language
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-", Pattern.compile("^<\\?([\\s\\S]+?)(?:\\?>|$)")}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-", Pattern.compile("^<%([\\s\\S]+?)(?:%>|$)")}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_PUNCTUATION, Pattern.compile("^(?:<[%?]|[%?]>)")}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-", Pattern.compile("^<xmp\\b[^>]*>([\\s\\S]+?)<\\/xmp\\b[^>]*>", Pattern.CASE_INSENSITIVE)}));
      // Unescaped content in javascript.  (Or possibly vbscript).
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-js", Pattern.compile("^<script\\b[^>]*>([\\s\\S]*?)(<\\/script\\b[^>]*>)", Pattern.CASE_INSENSITIVE)}));
      // Contains unescaped stylesheet content
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-css", Pattern.compile("^<style\\b[^>]*>([\\s\\S]*?)(<\\/style\\b[^>]*>)", Pattern.CASE_INSENSITIVE)}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-in.tag", Pattern.compile("^(<\\/?[a-z][^<>]*>)", Pattern.CASE_INSENSITIVE)}));
      registerLangHandler(new CreateSimpleLexer(shortcutStylePatterns, fallthroughStylePatterns), Arrays.asList(new String[]{"default-markup", "htm", "html", "mxml", "xhtml", "xml", "xsl"}));

      shortcutStylePatterns = new ArrayList<List<Object>>();
      fallthroughStylePatterns = new ArrayList<List<Object>>();
      shortcutStylePatterns.add(Arrays.asList(new Object[]{PR_PLAIN, Pattern.compile("^[\\s]+"), null, " \t\r\n"}));
      shortcutStylePatterns.add(Arrays.asList(new Object[]{PR_ATTRIB_VALUE, Pattern.compile("^(?:\\\"[^\\\"]*\\\"?|\\'[^\\']*\\'?)"), null, "\"'"}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_TAG, Pattern.compile("^^<\\/?[a-z](?:[\\w.:-]*\\w)?|\\/?>$", Pattern.CASE_INSENSITIVE)}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_ATTRIB_NAME, Pattern.compile("^(?!style[\\s=]|on)[a-z](?:[\\w:-]*\\w)?", Pattern.CASE_INSENSITIVE)}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-uq.val", Pattern.compile("^=\\s*([^>\\'\\\"\\s]*(?:[^>\\'\\\"\\s\\/]|\\/(?=\\s)))", Pattern.CASE_INSENSITIVE)}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_PUNCTUATION, Pattern.compile("^[=<>\\/]+")}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-js", Pattern.compile("^on\\w+\\s*=\\s*\\\"([^\\\"]+)\\\"", Pattern.CASE_INSENSITIVE)}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-js", Pattern.compile("^on\\w+\\s*=\\s*\\'([^\\']+)\\'", Pattern.CASE_INSENSITIVE)}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-js", Pattern.compile("^on\\w+\\s*=\\s*([^\\\"\\'>\\s]+)", Pattern.CASE_INSENSITIVE)}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-css", Pattern.compile("^style\\s*=\\s*\\\"([^\\\"]+)\\\"", Pattern.CASE_INSENSITIVE)}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-css", Pattern.compile("^style\\s*=\\s*\\'([^\\']+)\\'", Pattern.CASE_INSENSITIVE)}));
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-css", Pattern.compile("^style\\s*=\\s\\*([^\\\"\\'>\\s]+)", Pattern.CASE_INSENSITIVE)}));
      registerLangHandler(new CreateSimpleLexer(shortcutStylePatterns, fallthroughStylePatterns), Arrays.asList(new String[]{"in.tag"}));

      shortcutStylePatterns = new ArrayList<List<Object>>();
      fallthroughStylePatterns = new ArrayList<List<Object>>();
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_ATTRIB_VALUE, Pattern.compile("^[\\s\\S]+")}));
      registerLangHandler(new CreateSimpleLexer(shortcutStylePatterns, fallthroughStylePatterns), Arrays.asList(new String[]{"uq.val"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", CPP_KEYWORDS);
      decorateSourceMap.put("hashComments", true);
      decorateSourceMap.put("cStyleComments", true);
      decorateSourceMap.put("types", C_TYPES);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"c", "cc", "cpp", "cxx", "cyc", "m"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", "null,true,false");
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"json"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", CSHARP_KEYWORDS);
      decorateSourceMap.put("hashComments", true);
      decorateSourceMap.put("cStyleComments", true);
      decorateSourceMap.put("verbatimStrings", true);
      decorateSourceMap.put("types", C_TYPES);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"cs"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", JAVA_KEYWORDS);
      decorateSourceMap.put("cStyleComments", true);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"java"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", SH_KEYWORDS);
      decorateSourceMap.put("hashComments", true);
      decorateSourceMap.put("multiLineStrings", true);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"bash", "bsh", "csh", "sh"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", PYTHON_KEYWORDS);
      decorateSourceMap.put("hashComments", true);
      decorateSourceMap.put("multiLineStrings", true);
      decorateSourceMap.put("tripleQuotedStrings", true);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"cv", "py", "python"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", PERL_KEYWORDS);
      decorateSourceMap.put("hashComments", true);
      decorateSourceMap.put("multiLineStrings", true);
      decorateSourceMap.put("regexLiterals", 2);   // multiline regex literals
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"perl", "pl", "pm"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", RUBY_KEYWORDS);
      decorateSourceMap.put("hashComments", true);
      decorateSourceMap.put("multiLineStrings", true);
      decorateSourceMap.put("regexLiterals", true);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"rb", "ruby"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", JSCRIPT_KEYWORDS);
      decorateSourceMap.put("cStyleComments", true);
      decorateSourceMap.put("regexLiterals", true);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"javascript", "js"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", COFFEE_KEYWORDS);
      decorateSourceMap.put("hashComments", 3); // ### style block comments
      decorateSourceMap.put("cStyleComments", true);
      decorateSourceMap.put("multilineStrings", true);
      decorateSourceMap.put("tripleQuotedStrings", true);
      decorateSourceMap.put("regexLiterals", true);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"coffee"}));

      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", RUST_KEYWORDS);
      decorateSourceMap.put("cStyleComments", true);
      decorateSourceMap.put("multilineStrings", true);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"rc", "rs", "rust"}));

      shortcutStylePatterns = new ArrayList<List<Object>>();
      fallthroughStylePatterns = new ArrayList<List<Object>>();
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_STRING, Pattern.compile("^[\\s\\S]+")}));
      registerLangHandler(new CreateSimpleLexer(shortcutStylePatterns, fallthroughStylePatterns), Arrays.asList(new String[]{"regex"}));

      /**
       * Registers a language handler for Protocol Buffers as described at
       * http://code.google.com/p/protobuf/.
       *
       * Based on the lexical grammar at
       * http://research.microsoft.com/fsharp/manual/spec2.aspx#_Toc202383715
       *
       * @author mikesamuel@gmail.com
       */
      decorateSourceMap = new HashMap<String, Object>();
      decorateSourceMap.put("keywords", "bytes,default,double,enum,extend,extensions,false,"
              + "group,import,max,message,option,"
              + "optional,package,repeated,required,returns,rpc,service,"
              + "syntax,to,true");
      decorateSourceMap.put("types", Pattern.compile("^(bool|(double|s?fixed|[su]?int)(32|64)|float|string)\\b"));
      decorateSourceMap.put("cStyleComments", true);
      registerLangHandler(sourceDecorator(decorateSourceMap), Arrays.asList(new String[]{"proto"}));

      register(LangAppollo.class);
      register(LangBasic.class);
      register(LangClj.class);
      register(LangCss.class);
      register(LangDart.class);
      register(LangErlang.class);
      register(LangGo.class);
      register(LangHs.class);
      register(LangLisp.class);
      register(LangLlvm.class);
      register(LangLua.class);
      register(LangMatlab.class);
      register(LangMd.class);
      register(LangMl.class);
      register(LangMumps.class);
      register(LangN.class);
      register(LangPascal.class);
      register(LangR.class);
      register(LangRd.class);
      register(LangScala.class);
      register(LangSql.class);
      register(LangTex.class);
      register(LangVb.class);
      register(LangVhdl.class);
      register(LangTcl.class);
      register(LangWiki.class);
      register(LangXq.class);
      register(LangYaml.class);
      register(LangEx.class);
      register(LangSwift.class);
      register(LangKotlin.class);
      register(LangLogtalk.class);
      register(LangLasso.class);
    } catch (Exception ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Apply the given language handler to sourceCode and add the resulting
   * decorations to out.
   * @param basePos the index of sourceCode within the chunk of source
   *    whose decorations are already present on out.
   */
  protected static void appendDecorations(int basePos, String sourceCode, CreateSimpleLexer langHandler, List<Object> out) {
    if (sourceCode == null) {
      throw new NullPointerException("argument 'sourceCode' cannot be null");
    }
    Job job = new Job();
    job.setSourceCode(sourceCode);
    job.setBasePos(basePos);
    langHandler.decorate(job);
    out.addAll(job.getDecorations());
  }

  public class CreateSimpleLexer {

    protected List<List<Object>> fallthroughStylePatterns;
    protected Map<Character, List<Object>> shortcuts = new HashMap<Character, List<Object>>();
    protected Pattern tokenizer;
    protected int nPatterns;

    /** Given triples of [style, pattern, context] returns a lexing function,
     * The lexing function interprets the patterns to find token boundaries and
     * returns a decoration list of the form
     * [index_0, style_0, index_1, style_1, ..., index_n, style_n]
     * where index_n is an index into the sourceCode, and style_n is a style
     * constant like PR_PLAIN.  index_n-1 <= index_n, and style_n-1 applies to
     * all characters in sourceCode[index_n-1:index_n].
     *
     * The stylePatterns is a list whose elements have the form
     * [style : string, pattern : RegExp, DEPRECATED, shortcut : string].
     *
     * Style is a style constant like PR_PLAIN, or can be a string of the
     * form 'lang-FOO', where FOO is a language extension describing the
     * language of the portion of the token in $1 after pattern executes.
     * E.g., if style is 'lang-lisp', and group 1 contains the text
     * '(hello (world))', then that portion of the token will be passed to the
     * registered lisp handler for formatting.
     * The text before and after group 1 will be restyled using this decorator
     * so decorators should take care that this doesn't result in infinite
     * recursion.  For example, the HTML lexer rule for SCRIPT elements looks
     * something like ['lang-js', /<[s]cript>(.+?)<\/script>/].  This may match
     * '<script>foo()<\/script>', which would cause the current decorator to
     * be called with '<script>' which would not match the same rule since
     * group 1 must not be empty, so it would be instead styled as PR_TAG by
     * the generic tag rule.  The handler registered for the 'js' extension would
     * then be called with 'foo()', and finally, the current decorator would
     * be called with '<\/script>' which would not match the original rule and
     * so the generic tag rule would identify it as a tag.
     *
     * Pattern must only match prefixes, and if it matches a prefix, then that
     * match is considered a token with the same style.
     *
     * Context is applied to the last non-whitespace, non-comment token
     * recognized.
     *
     * Shortcut is an optional string of characters, any of which, if the first
     * character, gurantee that this pattern and only this pattern matches.
     *
     * @param shortcutStylePatterns patterns that always start with
     *   a known character.  Must have a shortcut string.
     * @param fallthroughStylePatterns patterns that will be tried in
     *   order if the shortcut ones fail.  May have shortcuts.
     */
    protected CreateSimpleLexer(List<List<Object>> shortcutStylePatterns, List<List<Object>> fallthroughStylePatterns) throws Exception {
      this.fallthroughStylePatterns = fallthroughStylePatterns;

      List<List<Object>> allPatterns = new ArrayList<List<Object>>(shortcutStylePatterns);
      allPatterns.addAll(fallthroughStylePatterns);
      List<Pattern> allRegexs = new ArrayList<Pattern>();
      Map<String, Object> regexKeys = new HashMap<String, Object>();
      for (int i = 0, n = allPatterns.size(); i < n; ++i) {
        List<Object> patternParts = allPatterns.get(i);
        String shortcutChars = patternParts.size() > 3 ? (String) patternParts.get(3) : null;
        if (shortcutChars != null) {
          for (int c = shortcutChars.length(); --c >= 0;) {
            shortcuts.put(shortcutChars.charAt(c), patternParts);
          }
        }
        Pattern regex = (Pattern) patternParts.get(1);
        String k = regex.pattern();
        if (regexKeys.get(k) == null) {
          allRegexs.add(regex);
          regexKeys.put(k, new Object());
        }
      }
      allRegexs.add(Pattern.compile("[\0-\\uffff]"));
      tokenizer = new CombinePrefixPattern().combinePrefixPattern(allRegexs);

      nPatterns = fallthroughStylePatterns.size();
    }

    /**
     * Lexes job.sourceCode and produces an output array job.decorations of
     * style classes preceded by the position at which they start in
     * job.sourceCode in order.
     *
     * @param job an object like <pre>{
     *    sourceCode: {string} sourceText plain text,
     *    basePos: {int} position of job.sourceCode in the larger chunk of
     *        sourceCode.
     * }</pre>
     */
    public void decorate(Job job) {
      String sourceCode = job.getSourceCode();
      int basePos = job.getBasePos();
      /** Even entries are positions in source in ascending order.  Odd enties
       * are style markers (e.g., PR_COMMENT) that run from that position until
       * the end.
       * @type {Array.<number|string>}
       */
      List<Object> decorations = new ArrayList<Object>(Arrays.asList(new Object[]{basePos, PR_PLAIN}));
      int pos = 0;  // index into sourceCode
      String[] tokens = Util.match(tokenizer, sourceCode, true);
      Map<String, String> styleCache = new HashMap<String, String>();

      for (int ti = 0, nTokens = tokens.length; ti < nTokens; ++ti) {
        String token = tokens[ti];
        String style = styleCache.get(token);
        String[] match = null;

        boolean isEmbedded;
        if (style != null) {
          isEmbedded = false;
        } else {
          List<Object> patternParts = shortcuts.get(token.charAt(0));
          if (patternParts != null) {
            match = Util.match((Pattern) patternParts.get(1), token, false);
            style = (String) patternParts.get(0);
          } else {
            for (int i = 0; i < nPatterns; ++i) {
              patternParts = fallthroughStylePatterns.get(i);
              match = Util.match((Pattern) patternParts.get(1), token, false);
              if (match.length != 0) {
                style = (String) patternParts.get(0);
                break;
              }
            }

            if (match.length == 0) {  // make sure that we make progress
              style = PR_PLAIN;
            }
          }

          isEmbedded = style != null && style.length() >= 5 && style.startsWith("lang-");
          if (isEmbedded && !(match.length > 1 && match[1] != null)) {
            isEmbedded = false;
            style = PR_SOURCE;
          }

          if (!isEmbedded) {
            styleCache.put(token, style);
          }
        }

        int tokenStart = pos;
        pos += token.length();

        if (!isEmbedded) {
          decorations.add(basePos + tokenStart);
          decorations.add(style);
        } else {  // Treat group 1 as an embedded block of source code.
          String embeddedSource = match[1];
          int embeddedSourceStart = token.indexOf(embeddedSource);
          int embeddedSourceEnd = embeddedSourceStart + embeddedSource.length();
          if (match.length > 2 && match[2] != null) {
            // If embeddedSource can be blank, then it would match at the
            // beginning which would cause us to infinitely recurse on the
            // entire token, so we catch the right context in match[2].
            embeddedSourceEnd = token.length() - match[2].length();
            embeddedSourceStart = embeddedSourceEnd - embeddedSource.length();
          }
          String lang = style.substring(5);
          // Decorate the left of the embedded source
          appendDecorations(basePos + tokenStart,
                  token.substring(0, embeddedSourceStart),
                  this, decorations);
          // Decorate the embedded source
          appendDecorations(basePos + tokenStart + embeddedSourceStart,
                  embeddedSource,
                  langHandlerForExtension(lang, embeddedSource),
                  decorations);
          // Decorate the right of the embedded section
          appendDecorations(basePos + tokenStart + embeddedSourceEnd,
                  token.substring(embeddedSourceEnd),
                  this, decorations);
        }
      }

      job.setDecorations(Util.removeDuplicates(decorations, job.getSourceCode()));
    }
  }

  /** returns a function that produces a list of decorations from source text.
   *
   * This code treats ", ', and ` as string delimiters, and \ as a string
   * escape.  It does not recognize perl's qq() style strings.
   * It has no special handling for double delimiter escapes as in basic, or
   * the tripled delimiters used in python, but should work on those regardless
   * although in those cases a single string literal may be broken up into
   * multiple adjacent string literals.
   *
   * It recognizes C, C++, and shell style comments.
   *
   * @param options a set of optional parameters.
   * @return a function that examines the source code
   *     in the input job and builds the decoration list.
   */
  protected CreateSimpleLexer sourceDecorator(Map<String, Object> options) throws Exception {
    List<List<Object>> shortcutStylePatterns = new ArrayList<List<Object>>();
    List<List<Object>> fallthroughStylePatterns = new ArrayList<List<Object>>();
    if (Util.getVariableValueAsBoolean(options.get("tripleQuotedStrings"))) {
      // '''multi-line-string''', 'single-line-string', and double-quoted
      shortcutStylePatterns.add(Arrays.asList(new Object[]{PR_STRING,
                Pattern.compile("^(?:\\'\\'\\'(?:[^\\'\\\\]|\\\\[\\s\\S]|\\'{1,2}(?=[^\\']))*(?:\\'\\'\\'|$)|\\\"\\\"\\\"(?:[^\\\"\\\\]|\\\\[\\s\\S]|\\\"{1,2}(?=[^\\\"]))*(?:\\\"\\\"\\\"|$)|\\'(?:[^\\\\\\']|\\\\[\\s\\S])*(?:\\'|$)|\\\"(?:[^\\\\\\\"]|\\\\[\\s\\S])*(?:\\\"|$))"),
                null,
                "'\""}));
    } else if (Util.getVariableValueAsBoolean(options.get("multiLineStrings"))) {
      // 'multi-line-string', "multi-line-string"
      shortcutStylePatterns.add(Arrays.asList(new Object[]{PR_STRING,
                Pattern.compile("^(?:\\'(?:[^\\\\\\']|\\\\[\\s\\S])*(?:\\'|$)|\\\"(?:[^\\\\\\\"]|\\\\[\\s\\S])*(?:\\\"|$)|\\`(?:[^\\\\\\`]|\\\\[\\s\\S])*(?:\\`|$))"),
                null,
                "'\"`"}));
    } else {
      // 'single-line-string', "single-line-string"
      shortcutStylePatterns.add(Arrays.asList(new Object[]{PR_STRING,
                Pattern.compile("^(?:\\'(?:[^\\\\\\'\r\n]|\\\\.)*(?:\\'|$)|\\\"(?:[^\\\\\\\"\r\n]|\\\\.)*(?:\\\"|$))"),
                null,
                "\"'"}));
    }
    if (Util.getVariableValueAsBoolean(options.get("verbatimStrings"))) {
      // verbatim-string-literal production from the C# grammar.  See issue 93.
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_STRING,
                Pattern.compile("^@\\\"(?:[^\\\"]|\\\"\\\")*(?:\\\"|$)"),
                null}));
    }
    Object hc = options.get("hashComments");
    if (Util.getVariableValueAsBoolean(hc)) {
      if (Util.getVariableValueAsBoolean(options.get("cStyleComments"))) {
        if ((hc instanceof Integer) && (Integer) hc > 1) {  // multiline hash comments
          shortcutStylePatterns.add(Arrays.asList(new Object[]{PR_COMMENT,
                    Pattern.compile("^#(?:##(?:[^#]|#(?!##))*(?:###|$)|.*)"),
                    null,
                    "#"}));
        } else {
          // Stop C preprocessor declarations at an unclosed open comment
          shortcutStylePatterns.add(Arrays.asList(new Object[]{PR_COMMENT,
                    Pattern.compile("^#(?:(?:define|e(?:l|nd)if|else|error|ifn?def|include|line|pragma|undef|warning)\\b|[^\r\n]*)"),
                    null,
                    "#"}));
        }
        // #include <stdio.h>
        fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_STRING,
                  Pattern.compile("^<(?:(?:(?:\\.\\.\\/)*|\\/?)(?:[\\w-]+(?:\\/[\\w-]+)+)?[\\w-]+\\.h(?:h|pp|\\+\\+)?|[a-z]\\w*)>"),
                  null}));
      } else {
        shortcutStylePatterns.add(Arrays.asList(new Object[]{PR_COMMENT,
                  Pattern.compile("^#[^\r\n]*"),
                  null,
                  "#"}));
      }
    }
    if (Util.getVariableValueAsBoolean(options.get("cStyleComments"))) {
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_COMMENT,
                Pattern.compile("^\\/\\/[^\r\n]*"),
                null}));

      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_COMMENT,
                Pattern.compile("^\\/\\*[\\s\\S]*?(?:\\*\\/|$)"),
                null}));
    }
    Object regexLiterals = options.get("regexLiterals");
    if (Util.getVariableValueAsBoolean(regexLiterals)) {
      /**
       * @const
       */
      // Javascript treat true as 1
      String regexExcls = Util.getVariableValueAsInteger(regexLiterals) > 1
              ? "" // Multiline regex literals
              : "\n\r";
      /**
       * @const
       */
      String regexAny = !regexExcls.isEmpty() ? "." : "[\\S\\s]";
      /**
       * @const
       */
      String REGEX_LITERAL =
              // A regular expression literal starts with a slash that is
              // not followed by * or / so that it is not confused with
              // comments.
              "/(?=[^/*" + regexExcls + "])"
              // and then contains any number of raw characters,
              + "(?:[^/\\x5B\\x5C" + regexExcls + "]"
              // escape sequences (\x5C),
              + "|\\x5C" + regexAny
              // or non-nesting character sets (\x5B\x5D);
              + "|\\x5B(?:[^\\x5C\\x5D" + regexExcls + "]"
              + "|\\x5C" + regexAny + ")*(?:\\x5D|$))+"
              // finally closed by a /.
              + "/";
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{"lang-regex",
                Pattern.compile("^" + REGEXP_PRECEDER_PATTERN + "(" + REGEX_LITERAL + ")")}));
    }

    Pattern types = (Pattern) options.get("types");
    if (Util.getVariableValueAsBoolean(types)) {
      fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_TYPE, types}));
    }

    String keywords = (String) options.get("keywords");
    if (keywords != null) {
      keywords = keywords.replaceAll("^ | $", "");
      if (keywords.length() != 0) {
        fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_KEYWORD,
                  Pattern.compile("^(?:" + keywords.replaceAll("[\\s,]+", "|") + ")\\b"),
                  null}));
      }
    }

    shortcutStylePatterns.add(Arrays.asList(new Object[]{PR_PLAIN,
              Pattern.compile("^\\s+"),
              null,
              " \r\n\t" + Character.toString((char) 0xA0)
            }));

    // TODO(mikesamuel): recognize non-latin letters and numerals in idents
    fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_LITERAL,
              Pattern.compile("^@[a-z_$][a-z_$@0-9]*", Pattern.CASE_INSENSITIVE),
              null}));
    fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_TYPE,
              Pattern.compile("^(?:[@_]?[A-Z]+[a-z][A-Za-z_$@0-9]*|\\w+_t\\b)"),
              null}));
    fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_PLAIN,
              Pattern.compile("^[a-z_$][a-z_$@0-9]*", Pattern.CASE_INSENSITIVE),
              null}));
    fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_LITERAL,
              Pattern.compile("^(?:"
              // A hex number
              + "0x[a-f0-9]+"
              // or an octal or decimal number,
              + "|(?:\\d(?:_\\d+)*\\d*(?:\\.\\d*)?|\\.\\d\\+)"
              // possibly in scientific notation
              + "(?:e[+\\-]?\\d+)?"
              + ')'
              // with an optional modifier like UL for unsigned long
              + "[a-z]*", Pattern.CASE_INSENSITIVE),
              null,
              "0123456789"}));
    // Don't treat escaped quotes in bash as starting strings.
    // See issue 144.
    fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_PLAIN,
              Pattern.compile("^\\\\[\\s\\S]?"),
              null}));

    // The Bash man page says

    // A word is a sequence of characters considered as a single
    // unit by GRUB. Words are separated by metacharacters,
    // which are the following plus space, tab, and newline: { }
    // | & $ ; < >
    // ...

    // A word beginning with # causes that word and all remaining
    // characters on that line to be ignored.

    // which means that only a '#' after /(?:^|[{}|&$;<>\s])/ starts a
    // comment but empirically
    // $ echo {#}
    // {#}
    // $ echo \$#
    // $#
    // $ echo }#
    // }#

    // so /(?:^|[|&;<>\s])/ is more appropriate.

    // http://gcc.gnu.org/onlinedocs/gcc-2.95.3/cpp_1.html#SEC3
    // suggests that this definition is compatible with a
    // default mode that tries to use a single token definition
    // to recognize both bash/python style comments and C
    // preprocessor directives.

    // This definition of punctuation does not include # in the list of
    // follow-on exclusions, so # will not be broken before if preceeded
    // by a punctuation character.  We could try to exclude # after
    // [|&;<>] but that doesn't seem to cause many major problems.
    // If that does turn out to be a problem, we should change the below
    // when hc is truthy to include # in the run of punctuation characters
    // only when not followint [|&;<>].
    String punctuation = "^.[^\\s\\w.$@'\"`/\\\\]*";
    if (Util.getVariableValueAsBoolean(options.get("regexLiterals"))) {
        punctuation += "(?!\\s*/)";
    }
    fallthroughStylePatterns.add(Arrays.asList(new Object[]{PR_PUNCTUATION,
              Pattern.compile(punctuation),
              null}));

    return new CreateSimpleLexer(shortcutStylePatterns, fallthroughStylePatterns);
  }
  /** Maps language-specific file extensions to handlers. */
  protected Map<String, Object> langHandlerRegistry = new HashMap<String, Object>();

  /** Register a language handler for the given file extensions.
   * @param handler a function from source code to a list
   *      of decorations.  Takes a single argument job which describes the
   *      state of the computation.   The single parameter has the form
   *      {@code {
   *        sourceCode: {string} as plain text.
   *        decorations: {Array.<number|string>} an array of style classes
   *                     preceded by the position at which they start in
   *                     job.sourceCode in order.
   *                     The language handler should assigned this field.
   *        basePos: {int} the position of source in the larger source chunk.
   *                 All positions in the output decorations array are relative
   *                 to the larger source chunk.
   *      } }
   * @param fileExtensions
   */
  protected void registerLangHandler(CreateSimpleLexer handler, List<String> fileExtensions) throws Exception {
    for (int i = fileExtensions.size(); --i >= 0;) {
      String ext = fileExtensions.get(i);
      if (langHandlerRegistry.get(ext) == null) {
        langHandlerRegistry.put(ext, handler);
      } else {
        throw new Exception("cannot override language handler " + ext);
      }
    }
  }

  /**
   * Register language handler. The clazz will not be instantiated
   * @param clazz the class of the language
   * @throws Exception cannot instantiate the object using the class,
   * or language handler with specified extension exist already
   */
  public void register(Class<? extends Lang> clazz) throws Exception {
    if (clazz == null) {
      throw new NullPointerException("argument 'clazz' cannot be null");
    }
    List<String> fileExtensions = getFileExtensionsFromClass(clazz);
    for (int i = fileExtensions.size(); --i >= 0;) {
      String ext = fileExtensions.get(i);
      if (langHandlerRegistry.get(ext) == null) {
        langHandlerRegistry.put(ext, clazz);
      } else {
        throw new Exception("cannot override language handler " + ext);
      }
    }
  }

  protected List<String> getFileExtensionsFromClass(Class<? extends Lang> clazz) throws Exception {
    Method getExtensionsMethod = clazz.getMethod("getFileExtensions", (Class<?>[]) null);
    return (List<String>) getExtensionsMethod.invoke(null, null);
  }

  /**
   * Get the parser for the extension specified. 
   * @param extension the file extension, if null, default parser will be returned
   * @param source the source code
   * @return the parser
   */
  public CreateSimpleLexer langHandlerForExtension(String extension, String source) {
    if (!(extension != null && langHandlerRegistry.get(extension) != null)) {
      // Treat it as markup if the first non whitespace character is a < and
      // the last non-whitespace character is a >.
      extension = Util.test(Pattern.compile("^\\s*<"), source)
              ? "default-markup"
              : "default-code";
    }

    Object handler = langHandlerRegistry.get(extension);
    if (handler instanceof CreateSimpleLexer) {
      return (CreateSimpleLexer) handler;
    } else {
      CreateSimpleLexer _simpleLexer;
      try {
        Lang _lang = ((Class<Lang>) handler).newInstance();
        _simpleLexer = new CreateSimpleLexer(_lang.getShortcutStylePatterns(), _lang.getFallthroughStylePatterns());

        List<Lang> extendedLangs = _lang.getExtendedLangs();
        for (Lang _extendedLang : extendedLangs) {
          register(_extendedLang.getClass());
        }

        List<String> fileExtensions = getFileExtensionsFromClass((Class<Lang>) handler);
        for (String _extension : fileExtensions) {
          langHandlerRegistry.put(_extension, _simpleLexer);
        }
      } catch (Exception ex) {
        LOG.log(Level.SEVERE, null, ex);
        return null;
      }

      return _simpleLexer;
    }
  }
}
