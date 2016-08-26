# CodeView (Android)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-codeview--android-green.svg?style=true)](https://android-arsenal.com/details/1/4216)
[![Release](https://jitpack.io/v/softwee/codeview-android.svg)](https://jitpack.io/#softwee/codeview-android)

CodeView helps to show code content with syntax highlighting in native way.

## Description
CodeView contains 3 core parts to implement necessary logic:<br>

1. <b>CodeClassifier</b> is trying to define what language presented in code snippet. It built upon <a href="https://github.com/ptnplanet/Java-Naive-Bayes-Classifier">Naive Bayes classifier</a>. There is no need to work with this class directly & you must just follow instructions below. (Experimental module, may not work properly!)<br>

2. For highlighting it uses <b>CodeHighlighter</b>, just highlights your code & returns formatted content. It based on Google Prettify and <a href="https://github.com/twalcari/java-prettify">their fork</a>.<br>

3. <b>CodeView</b> & related adapter.<br>

## Download
Add it in your root ```build.gradle``` at the end of repositories:
```groovy
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

Add the dependency:
```groovy
compile 'com.github.softwee:codeview-android:1.0.3'
```

## Usage
If you want to use code classifier to auto language recognizing just add to your ```Application.java```:
```java
// train classifier on app start
CodeProcessor.init(this);
```

Add view for your layout:
```xml
<io.github.kbiakov.codeview.CodeView
        android:id="@+id/code_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
```

Use chaining syntax when build view:
```java
CodeView codeView = (CodeView) findViewById(R.id.code_view);

codeView.highlightCode("js")
        .setColorTheme(ColorTheme.SOLARIZED_LIGHT.withBgContent(myColor))
        .setCodeContent(getString(R.string.listing_js));
```

And perform actions sequentially when view built:
```java
codeView.setCodeContent(getString(R.string.listing_java));
codeView.highlightCode("java");
```

You can use both forms for build & built view, but note: ```setCodeContent(String)``` is final step when you build your view, otherwise not. If you firstly highlight and then set code content, code will not be highlighted if view was not built yet. Instructions above helps you to avoid errors. View has state to handle this behavior.

## Customizing
Use implicit form to code highlighting:
```java
codeView.highlightCode();
```
or eplixit (see available extensions below):
```java
codeView.highlightCode("js"); // it will work fast!
```

Extend default color theme or create your own (don't forget to open PR with this stuff!):
```java
int myColor = ContextCompat.getColor(this, R.color.code_content_background);
codeView.setColorTheme(ColorTheme.SOLARIZED_LIGHT.withBgContent(myColor));
```
```java
codeView.setColorTheme(new ColorThemeData(new SyntaxColors(...)));
```

Handle user clicks on code lines:
```java
codeView.setCodeListener(new OnCodeLineClickListener() {
    @Override
    public void onCodeLineClicked(int n) {
      // your logic here
    }
});
```

## How it looks in app
See <a href="https://github.com/Softwee/codeview-android/blob/master/example/src/main/java/io/github/kbiakov/codeviewexample/ListingsActivity.java">example</a>.<br>

[![CodeView_Android_Screenshot.png](https://s10.postimg.org/ckzv9xmm1/Code_View_Android_Screenshot.png)](https://postimg.org/image/6wtkj1i9h/)

## List of available languages & their extensions
C/C++/Objective-C (```"c"```, ```"cc"```, ```"cpp"```, ```"cxx"```, ```"cyc"```, ```"m"```), C# (```"cs"```), Java (```"java"```),Bash (```"bash"```, ```"bsh"```, ```"csh"```, ```"sh"```), Python (```"cv"```, ```"py"```, ```"python"```), Perl (```"perl"```, ```"pl"```, ```"pm"```), Ruby (```"rb"```, ```"ruby"```), JavaScript (```"javascript"```, ```"js"```), CoffeeScript (```"coffee"```), Rust (```"rc"```, ```"rs"```, ```"rust"```), Appollo (```"apollo"```, ```"agc"```, ```"aea"```), Basic (```"basic"```, ```"cbm"```), Clojure (```"clj"```), Css (```"css"```), Dart (```"dart"```), Erlang (```"erlang"```, ```"erl"```), Go (```"go"```), Haskell (```"hs"```), Lisp (```"cl"```, ```"el"```, ```"lisp"```, ```"lsp"```, ```"scm"```, ```"ss"```, ```"rkt"```), Llvm (```"llvm"```, ```"ll"```), Lua (```"lua"```), Matlab (```"matlab"```), ML (OCaml, SML, F#, etc) (```"fs"```, ```"ml"```), Mumps (```"mumps"```), N (```"n"```, ```"nemerle"```), Pascal (```"pascal"```), R (```"r"```, ```"s"```, ```"R"```, ```"S"```, ```"Splus"```), Rd (```"Rd"```, ```"rd"```), Scala (```"scala"```), SQL (```"sql"```), Tex (```"latex"```, ```"tex"```), VB (```"vb"```, ```"vbs"```), VHDL (```"vhdl"```, ```"vhd"```), Tcl (```"tcl"```), Wiki (```"wiki.meta"```), XQuery (```"xq"```, ```"xquery"```), YAML (```"yaml"```, ```"yml"```), formats (```"json"```, ```"xml"```, ```"proto"```), ```"regex"```

Didn't found yours? Please, open issue to show your interest & I try to add this language in next releases.

## Author
### <a href="https://github.com/kbiakov">Kirill Biakov</a>

## License MIT
Copyright (c) 2016 Softwee

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
