# CodeView (Android)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-codeview--android-green.svg?style=true)](https://android-arsenal.com/details/1/4216)
[![Release](https://jitpack.io/v/softwee/codeview-android.svg)](https://jitpack.io/#softwee/codeview-android)

CodeView helps to show code content with syntax highlighting in native way.

## Description
CodeView contains 3 core parts to implement necessary logic:<br>

1. <b>CodeClassifier</b> is trying to define what language presented in code snippet. It built upon [Naive Bayes classifier](https://github.com/ptnplanet/Java-Naive-Bayes-Classifier). There is no need to work with this class directly & you must just follow instructions below. (Experimental module, may not work properly!)<br>

2. For highlighting it uses <b>CodeHighlighter</b>, just highlights your code & returns formatted content. It based on [Google Prettify](https://github.com/google/code-prettify) and their Java implementation & [fork](https://github.com/google/code-prettify).<br>

3. <b>CodeView</b> & related abstract adapter to provide customization (see below).<br>

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
compile 'com.github.softwee:codeview-android:1.1.1'
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

Extend default color theme:
```java
int myColor = ContextCompat.getColor(this, R.color.code_content_background);
codeView.setColorTheme(ColorTheme.SOLARIZED_LIGHT.withBgContent(myColor));
```
or provide your own (don't forget to open PR with this stuff!)
```java
codeView.setColorTheme(new ColorThemeData(new SyntaxColors(...), ...));
```

Handle user clicks on code lines:
```java
codeView.setCodeListener(new OnCodeLineClickListener() {
    @Override
    public void onCodeLineClicked(int n, @NotNull String line) {
        Log.i("ListingsActivity", "On " + (n + 1) + " line clicked");
    }
});
```

Enable shadows to hide scrolled content:
```java
codeView.setShadowsEnabled(true);
```

## Adapter customization
Sometimes you may want to add some content under line. You can create your own implementation as follows:

1. Create your model to store data, for example some ```MyModel``` class.<br>
2. Extend ```AbstractCodeAdapter<MyModel>``` typed by your model class.<br>
3. Implement necessary methods in obtained ```MyCodeAdapter<MyModel>```:
```kotlin
// Kotlin
class MyCodeAdapter : AbstractCodeAdapter<MyModel> {
    constructor(context: Context, content: String) : super(context, content)

    override fun createFooter(context: Context, entity: MyModel, isFirst: Boolean) =
        /* init & return your view here */
}
```
```java
// Java
public class MyCodeAdapter extends AbstractCodeAdapter<MyModel> {
    public CustomAdapter(@NotNull Context context, @NotNull String content) {
    	// @see params in AbstractCodeAdapter
        super(context, content, true, 10, context.getString(R.string.show_all), null);
    }

    @NotNull
    @Override
    public View createFooter(@NotNull Context context, CustomModel entity, boolean isFirst) {
        return /* init your view here */;
    }
}
```
<br>
4. Set custom adapter to your code view:
```java
final CodeWithDiffsAdapter diffsAdapter = new CodeWithDiffsAdapter(this, getString(R.string.listing_py));
codeView.setAdapter(diffsAdapter);
```
<br>
5. Init footer entities to provide mapper from your view to model:
```java
// it will add an addition diff to code line
diffsAdapter.addFooterEntity(16, new DiffModel(getString(R.string.py_addition_16), true));
// and this a deletion diff
diffsAdapter.addFooterEntity(11, new DiffModel(getString(R.string.py_deletion_11), false));
```
<br>
6. You can also add a multiple diff entities, see ```AbstractCodeAdapter<MyModel>.addFooterEntities(HashMap<Int, List<MyModel>>)``` method). Here you must provide a map from code line numbers (started from 0) to list of line entities. It will be mapped by adapter to specified footer views.
<br>

See [Github diff](https://github.com/Softwee/codeview-android/blob/master/codeview/src/main/java/io/github/kbiakov/codeview/adapters/CodeWithDiffsAdapter.kt) as example of my "best practice" implementation.

## How it looks in app
See <a href="https://github.com/Softwee/codeview-android/blob/master/example/src/main/java/io/github/kbiakov/codeviewexample/ListingsActivity.java">example</a>.<br>

[![CodeView_Android_Solarized_light](https://s10.postimg.org/vx3u6q0l5/Screen_Shot_2016_08_31_at_18_41_31.png)](https://s10.postimg.org/vx3u6q0l5/Screen_Shot_2016_08_31_at_18_41_31.png)
[![CodeView_Android_Monokau](https://s10.postimg.org/rmkkxe649/Screen_Shot_2016_08_31_at_18_45_05.png)](https://s10.postimg.org/rmkkxe649/Screen_Shot_2016_08_31_at_18_45_05.png)
[![CodeView_Android_Default](https://s10.postimg.org/u2meb8o6x/Screen_Shot_2016_08_31_at_18_49_33.png)](https://s10.postimg.org/u2meb8o6x/Screen_Shot_2016_08_31_at_18_49_33.png)

## List of available languages & their extensions
C/C++/Objective-C (```"c"```, ```"cc"```, ```"cpp"```, ```"cxx"```, ```"cyc"```, ```"m"```), C# (```"cs"```), Java (```"java"```),Bash (```"bash"```, ```"bsh"```, ```"csh"```, ```"sh"```), Python (```"cv"```, ```"py"```, ```"python"```), Perl (```"perl"```, ```"pl"```, ```"pm"```), Ruby (```"rb"```, ```"ruby"```), JavaScript (```"javascript"```, ```"js"```), CoffeeScript (```"coffee"```), Rust (```"rc"```, ```"rs"```, ```"rust"```), Appollo (```"apollo"```, ```"agc"```, ```"aea"```), Basic (```"basic"```, ```"cbm"```), Clojure (```"clj"```), Css (```"css"```), Dart (```"dart"```), Erlang (```"erlang"```, ```"erl"```), Go (```"go"```), Haskell (```"hs"```), Lisp (```"cl"```, ```"el"```, ```"lisp"```, ```"lsp"```, ```"scm"```, ```"ss"```, ```"rkt"```), Llvm (```"llvm"```, ```"ll"```), Lua (```"lua"```), Matlab (```"matlab"```), ML (OCaml, SML, F#, etc) (```"fs"```, ```"ml"```), Mumps (```"mumps"```), N (```"n"```, ```"nemerle"```), Pascal (```"pascal"```), R (```"r"```, ```"s"```, ```"R"```, ```"S"```, ```"Splus"```), Rd (```"Rd"```, ```"rd"```), Scala (```"scala"```), SQL (```"sql"```), Tex (```"latex"```, ```"tex"```), VB (```"vb"```, ```"vbs"```), VHDL (```"vhdl"```, ```"vhd"```), Tcl (```"tcl"```), Wiki (```"wiki.meta"```), XQuery (```"xq"```, ```"xquery"```), YAML (```"yaml"```, ```"yml"```), Markdown, (```"md"```, ```"markdown"```), formats (```"json"```, ```"xml"```, ```"proto"```), ```"regex"```

Didn't found yours? Please, open issue to show your interest & I try to add this language in next releases.

## List of available themes
1. Default (simple light theme)
2. Solarized Light
3. Monokai

## Contribute
1. You can add your theme (see [ColorTheme](https://github.com/Softwee/codeview-android/blob/master/codeview/src/main/java/io/github/kbiakov/codeview/highlight/CodeHighlighter.kt) class). Try to add some classic color themes or create your own if it looks cool. You can find many of them in different open-source text editors.<br>
2. If you are strong in a regex add missed language as shown [here](https://github.com/Softwee/codeview-android/blob/master/codeview/src/main/java/io/github/kbiakov/codeview/highlight/prettify/lang/LangScala.java). You can find existing regex for some language in different sources of js-libraries, etc, which plays the same role.<br>
3. Various adapters also welcome, many use cases are impossible to cover.

## Author
### [Kirill Biakov](https://github.com/kbiakov)

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
