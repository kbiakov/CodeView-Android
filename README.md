# CodeView (Android)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-codeview--android-blue.svg)](https://android-arsenal.com/details/1/4216)
[![Release](https://jitpack.io/v/kbiakov/CodeView-android.svg)](https://jitpack.io/#kbiakov/CodeView-android)
[![Build Status](https://travis-ci.org/Softwee/codeview-android.svg?branch=master)](https://travis-ci.org/Softwee/codeview-android)

<b>CodeView</b> helps to show code content with syntax highlighting in native way.

## Description
<b>CodeView</b> contains 3 core parts to implement necessary logic:<br>

1. <b>CodeView</b> & related abstract adapter to provide options & customization (see below).<br>

2. For highlighting it uses <b>CodeHighlighter</b>, it highlights your code & returns formatted content. It's based on [Google Prettify](https://github.com/google/code-prettify) and their Java implementation & [fork](https://github.com/google/code-prettify).<br>

3. <b>CodeClassifier</b> is trying to define what language is presented in the code snippet. It's built using [Naive Bayes classifier](https://en.wikipedia.org/wiki/Naive_Bayes_classifier) upon found open-source [implementation](https://github.com/ptnplanet/Java-Naive-Bayes-Classifier), which I rewrote in Kotlin. There is no need to work with this class directly & you must just follow instructions below. (Experimental module, may not work properly!)<br>

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
compile 'com.github.kbiakov:CodeView-android:1.3.0'
```

## Usage
If you want to use code classifier to auto language recognizing just add to your ```Application.java```:
```java
// train classifier on app start
CodeProcessor.init(this);
```

Having done ones on app start you can classify language for different snippets even faster, because the algorithm needs time for training on sets for the presented listings of the languages which the library has.

Add view to your layout & bind as usual:
```xml
<io.github.kbiakov.codeview.CodeView
	android:id="@+id/code_view"
	android:layout_width="wrap_content"
	android:layout_height="wrap_content"/>
```
```java
CodeView codeView = (CodeView) findViewById(R.id.code_view);
```

So now you can set code using implicit form:
```java
// auto language recognition
codeView.setCode(getString(R.string.listing_js));
```

Or explicit (see available extensions below):
```java
// will work faster!
codeView.setCode(getString(R.string.listing_py), "py");
```

## Customization
When you call ```setCode(...)``` the view will be prepared with the default params if the view was not initialized before. So if you want some customization, it can be done using the options and/or adapter.

### Initialization
You can initialize the view with options:
```java
codeView.setOptions(Options.Default.get(this)
    .withLanguage("python")
    .withCode(R.string.listing_py)
    .withTheme(ColorTheme.MONOKAI));
```

Or using adapter (see <b>Adapter</b> or example for more details):
```java
final CustomAdapter myAdapter = new CustomAdapter(this, getString(R.string.listing_md));
codeView.setAdapter(myAdapter);
```

<b>Note:</b> Each <b>CodeView</b> has a adapter and each adapter has options. When calling ```setOptions(...)``` or ```setAdapter(...)``` the current adapter is "flushed" with the current options. If you want to save the state and just update options saving adapter or set adapter saving options you must call ```updateOptions(...)``` or ```updateAdapter(...)``` accordingly.

### Options
Options helps to easily set necessary params, such as code & language, color theme, font, format, shortcut params (max lines, note) and code line click listener. Some params are unnecessary.

When the view is initialized (options or adapter are set) you can manipulate the options in various ways:
```java
codeView.getOptions()
    .withCode(R.string.listing_java)
    .withLanguage("java")
    .withTheme(ColorTheme.MONOKAI);
```

### Color theme
There are some default themes (see full list below):
```java
codeView.getOptions().setTheme(ColorTheme.SOLARIZED_LIGHT);
```

But you can build your own from a existing one:
```java
ColorThemeData myTheme = ColorTheme.SOLARIZED_LIGHT.theme()
    .withBgContent(android.R.color.black)
    .withNoteColor(android.R.color.white);

codeView.getOptions().setTheme(myTheme);
```

Or create your own from scratch (don't forget to open PR with this stuff!):
```java
ColorThemeData customTheme = new ColorThemeData(new SyntaxColors(...), ...);
codeView.getOptions().setTheme(customTheme);
```

### Font
Set font for your code content:
```java
codeView.getOptions().withFont(Font.Consolas);
```

`Font.Consolas` is a font preset (see the list of available below).
To use your own font you can use similar method by providing `Typeface` or font path. Fonts are internally cached.

### Format
Manage the space that code line take. There are 3 types: `Compact`, `ExtraCompact` and `Medium`.
Setup is similar:
```kotlin
// Kotlin
codeView.getOptions().withFont(Font.Compact)
```
```java
// Java
codeView.getOptions().withFont(Format.Default.getCompact());
```
Also you can create custom `Format` by providing params such as `scaleFactor`, `lineHeight`, `borderHeight` (above first line and below last) and `fontSize`.

### Adapter
Sometimes you may want to take code lines under your control, and that's why you need a <b>Adapter</b>.

You can create your own implementation as follows:

1. Create your model to store data, for example some ```MyModel``` class.<br>
2. Extend ```AbstractCodeAdapter<MyModel>``` typed by your model class.<br>
3. Implement necessary methods in obtained ```MyCodeAdapter```:
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
        return /* your initialized view here */;
    }
}
```
<br>

4. Set custom adapter to your code view:
```java
final MyCodeAdapter adapter = new MyCodeAdapter(this, getString(R.string.listing_py));
codeView.setAdapter(diffsAdapter);
```
<br>

5. Init footer entities to provide mapper from your model to view:
```java
// it will add an addition diff to code line
adapter.addFooterEntity(16, new MyModel(getString(R.string.py_addition_16), true));
// and this a deletion diff
adapter.addFooterEntity(11, new MyModel(getString(R.string.py_deletion_11), false));
```
<br>

6. You can also add a multiple diff entities:
```java
AbstractCodeAdapter<MyModel>.addFooterEntities(HashMap<Int, List<MyModel>> myEntities)
```
Here you must provide a map from code line numbers (started from 0) to list of line entities. It will be mapped by adapter to specified footer views.
<br>

See [Github diff](https://github.com/Softwee/codeview-android/blob/master/codeview/src/main/java/io/github/kbiakov/codeview/adapters/CodeWithDiffsAdapter.kt) as example of my "best practice" implementation.

## How it looks in app
See <a href="https://github.com/Softwee/codeview-android/blob/master/example/src/main/java/io/github/kbiakov/codeviewexample/ListingsActivity.java">example</a>.<br>

[![CodeView_Android_Solarized_light](https://s10.postimg.org/vx3u6q0l5/Screen_Shot_2016_08_31_at_18_41_31.png)](https://s10.postimg.org/vx3u6q0l5/Screen_Shot_2016_08_31_at_18_41_31.png)
[![CodeView_Android_Monokau](https://s10.postimg.org/rmkkxe649/Screen_Shot_2016_08_31_at_18_45_05.png)](https://s10.postimg.org/rmkkxe649/Screen_Shot_2016_08_31_at_18_45_05.png)
[![CodeView_Android_Default](https://s10.postimg.org/u2meb8o6x/Screen_Shot_2016_08_31_at_18_49_33.png)](https://s10.postimg.org/u2meb8o6x/Screen_Shot_2016_08_31_at_18_49_33.png)

## List of available languages & their extensions
C/C++/Objective-C (```"c"```, ```"cc"```, ```"cpp"```, ```"cxx"```, ```"cyc"```, ```"m"```), C# (```"cs"```), Java (```"java"```), Bash (```"bash"```, ```"bsh"```, ```"csh"```, ```"sh"```), Python (```"cv"```, ```"py"```, ```"python"```), Perl (```"perl"```, ```"pl"```, ```"pm"```), Ruby (```"rb"```, ```"ruby"```), JavaScript (```"javascript"```, ```"js"```), CoffeeScript (```"coffee"```), Rust (```"rc"```, ```"rs"```, ```"rust"```), Appollo (```"apollo"```, ```"agc"```, ```"aea"```), Basic (```"basic"```, ```"cbm"```), Clojure (```"clj"```), Css (```"css"```), Dart (```"dart"```), Erlang (```"erlang"```, ```"erl"```), Go (```"go"```), Haskell (```"hs"```), Lisp (```"cl"```, ```"el"```, ```"lisp"```, ```"lsp"```, ```"scm"```, ```"ss"```, ```"rkt"```), Llvm (```"llvm"```, ```"ll"```), Lua (```"lua"```), Matlab (```"matlab"```), ML (OCaml, SML, F#, etc) (```"fs"```, ```"ml"```), Mumps (```"mumps"```), N (```"n"```, ```"nemerle"```), Pascal (```"pascal"```), R (```"r"```, ```"s"```, ```"R"```, ```"S"```, ```"Splus"```), Rd (```"Rd"```, ```"rd"```), Scala (```"scala"```), SQL (```"sql"```), Tex (```"latex"```, ```"tex"```), VB (```"vb"```, ```"vbs"```), VHDL (```"vhdl"```, ```"vhd"```), Tcl (```"tcl"```), Wiki (```"wiki.meta"```), XQuery (```"xq"```, ```"xquery"```), YAML (```"yaml"```, ```"yml"```), Markdown (```"md"```, ```"markdown"```), formats (```"json"```, ```"xml"```, ```"proto"```), ```"regex"```

Didn't found yours? Please, open issue to show your interest & I'll try to add this language in next releases.

## List of available themes
* Default (simple light theme).
* Solarized Light.
* Monokai.

## List of available fonts
* Consolas
* CourierNew
* DejaVuSansMono
* DroidSansMonoSlashed
* Inconsolata
* Monaco

## Contribute
1. You can add your theme (see [ColorTheme](https://github.com/Softwee/codeview-android/blob/master/codeview/src/main/java/io/github/kbiakov/codeview/highlight/CodeHighlighter.kt) class). Try to add some classic color themes or create your own if it looks cool. You can find many of them in different open-source text editors.<br>
2. If you are strong in regex, add missed language as shown [here](https://github.com/Softwee/codeview-android/blob/master/codeview/src/main/java/io/github/kbiakov/codeview/highlight/prettify/lang/LangScala.java). You can find existing regex for some language in different sources of libraries, which plays the same role.<br>
3. Various adapters also welcome.

## Author
### [Kirill Biakov](https://github.com/kbiakov)

## License MIT
```
Copyright (c) 2016 Kirill Biakov

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
```
