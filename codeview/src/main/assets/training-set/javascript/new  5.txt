'use strict';

var path = require('path'),
    fs = require('fs'),
    natural = require('natural'),
    lunr = require('lunr'),
    tokenizer = new natural.WordTokenizer(),
    loc = path.resolve(__dirname, 'content'),
    scraper = {
      title: /\[meta:title\]:\s<>\s\((.+?)\)(?!\))/,
      description: /\[meta:description\]:\s<>\s\((.+?)\)(?!\))/,
      firstlines: /^((.*\n){2}){1,3}/
    };

//
// ### @private function scrape()
// #### @content {String} document content
// #### @key {String} scraper key
// #### @n {Number} index of match that should be returned
// Scrapes the [key] from the content by Regular Epression
//
function scrape(content, key, n) {
  if (!content) return '';

  var match = content.match(scraper[key]);

  // Only return scraped content if there is a meta:[key].
  return match && match[n] ? match[n].trim() : '';
}

//
// ### @private function normalize()
// #### @file {String} file name
// Normalize the file name to resolve to a Markdown or index file.
//
function normalize(file) {
  if (!file) file = 'index.md';

  // Remove trailing slashes from paths
  if (file[file.length - 1] === '/') file = file.slice(0, -1);

  return ~file.indexOf('.md') ? file : file + '.md';
}

//
// ### @private function fileContent()
// #### @content {String} Document content
// Sugar content with additional properties from scraped content.
//
function fileContent(content) {
  return {
    content: content || '',
    description: scrape(content, 'description', 1) || scrape(content, 'firstlines', 0),
    title: scrape(content, 'title', 1),
    tags: tags(content, 10)
  };
}

//
// ### @private function frequency()
// #### @content {String} Document content
// Return list of words scored by Term Frequency-Inverse Document Frequency.
//
function frequency(content) {
  var tfidf = new natural.TfIdf(),
      processed = [],
      words = [];

  // Add the current content.
  content = content.toLowerCase();
  tfidf.addDocument(content);

  tokenizer.tokenize(content).forEach(function wordFrequency(word) {
    // Return early if word is processed, to short or only a number.
    if (+word || word.length < 3 || ~processed.indexOf(word)) return;

    words.push({
      word: word,
      score: tfidf.tfidf(word, 0)
    });

    // Add word to processed so tfidf is not called more than required.
    processed.push(word);
  });

  return words;
}

//
// ### @private function tags()
// #### @content {String} Document content
// #### @n {Number} number of tags
// Return n highest scoring tags as determined by term frequency.
//
function tags(content, n) {
  if (!content) return [];

  return frequency(content).sort(function sortByScore(a, b) {
    return b.score - a.score;
  }).slice(0, n).map(function extractWords(tag) {
    return tag.word;
  });
}

//
// ### @private function read()
// #### @file {String} Filename
// #### @callback {Function} Callback for file contents
// Returns file content by normalized path, if a callback is provided, content
// is returned asynchronously.
//
function read(file, callback) {
  file = path.resolve(loc, normalize(file));
  if (!callback) return fileContent(fs.readFileSync(file, 'utf8'));

  fs.readFile(file, 'utf8', function read(error, content) {
    callback.apply(this, [error, fileContent(content)]);
  });
}

//
// ### @private function walk()
// #### @dir {String} Directory path to crawl
// #### @result {Object} Append content to current results
// #### @callback {Function} Callback for sub directory
// #### @sub {Boolean} Is directory subdirectory of dir
// Recusive walk of directory by using asynchronous functions, returns
// a collection of markdown files in each folder.
//
function walk(dir, callback, result, sub) {
  var current = sub ? path.basename(dir) : 'index';

  // Prepare containers.
  result = result || {};
  result[current] = {};

  // Read the current directory
  fs.readdir(dir, function readDir(error, list) {
    if (error) return callback(error);

    var pending = list.length;
    if (!pending) return callback(null, result);

    list.forEach(function loopFiles(file) {
      file = dir + '/' + file;

      fs.stat(file, function statFile(error, stat) {
        var name, ref;

        if (stat && stat.isDirectory()) {
          walk(file, function dirDone() {
            if (!--pending) callback(null, result);
          }, result, true);
        } else {
          // Only get markdown files from the directory content.
          if (path.extname(file) !== '.md') return;

          ref = path.basename(file, '.md');
          name = ['/' + path.basename(dir), ref];

          // Only append the name of the file if not index
          if (ref === 'index') name.pop();

          // Get the tile of the file.
          read(file, function getFile(err, file) {
            result[current][ref] = {
              href: sub ? name.join('/') + '/' : '/',
              title: file.title,
              description: file.description,
              path: dir
            };

            if (!--pending) callback(null, result);
          });
        }
      });
    });
  });
}

//
// ### @private function walkSync()
// #### @dir {String} Directory path to crawl
// #### @result {Object} Append content to current results
// #### @sub {Boolean} Is directory subdirectory of dir
// Recusive walk of directory by using synchronous functions, returns
// a collection of markdown files in each folder.
//
function walkSync(dir, result, sub) {
  var current = sub ? path.basename(dir) : 'index';

  // Prepare containers.
  result = result || {};
  result[current] = {};

  // Read the current directory
  fs.readdirSync(dir).forEach(function loopDir(file) {
    var stat, name, ref;

    file = dir + '/' + file;
    stat = fs.statSync(file);

    // If directory initiate another walk.
    if (stat && stat.isDirectory()) {
      walkSync(file, result, true);
    } else {
      // Only get markdown files from the directory content.
      if (path.extname(file) !== '.md') return;

      ref = path.basename(file, '.md');
      name = ['/' + path.basename(dir), ref];

      // Only append the name of the file if not index
      if (ref === 'index') name.pop();

      // Append file information to current container.
      file = read(file);
      result[current][ref] = {
        href: sub ? name.join('/') + '/' : '/',
        title: file.title,
        description: file.description,
        path: dir
      };
    }
  });

  return result;
}

//
// ### function Handbook()
// Constructor for easy access to Handbook content. On constructuing handbook
// synchronously prepare the search index. Listening to a search index ready
// event is not required thus easing the flow.
//
function Handbook() {
  var toc = this.index = walkSync(loc),
      cache = this.cache = {},
      idx = this.idx = lunr(function setupLunr() {
        this.field('title', { boost: 10 });
        this.field('body');
      });

  Object.keys(toc).forEach(function loopSections(section) {
    Object.keys(toc[section]).forEach(function loopPages(page) {
      var document = read((section !== 'index' ? section + '/' : '') + page)
        , id = section + '/' + page;

      idx.add({
        id: id,
        title: document.title,
        body: document.content
      });

      //
      // Keep cached reference of all documents, for quick external access.
      //
      cache[id] = document;
    });
  });
}

//
// ### function get()
// #### @file {String} Filename
// #### @callback {Function} Callback for file contents
// Proxy method to private async method read. This method can be called
// synchronously by omitting the callback.
//
Handbook.prototype.get = function get(file, callback) {
  return read.apply(this, arguments);
};

//
// ### function catalog()
// #### @callback {Function} Callback for catalog/TOC
// Returns a catalog by parsing the content directory, if a callback is provided
// content is returned asynchronously.
//
Handbook.prototype.catalog = function catalog(callback) {
  if (!callback) return walkSync(loc);

  walk(loc, callback);
};

//
// ### function search()
// #### @query {String} Query to search against
// Proxy to the search method of Lunr, returns a list of documents, this must
// be called in Lunr scope.
//
Handbook.prototype.search = function (query) {
  return this.idx.search.call(this.idx, query);
};

//
// Expose the 301 routes for the handbook.
//
Handbook.redirect = require('./301.json');

// Expose public functions.
module.exports = Handbook;
