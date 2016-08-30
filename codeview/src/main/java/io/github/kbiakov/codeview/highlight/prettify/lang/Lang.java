// Copyright (C) 2011 Chan Wai Shing
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
import java.util.List;

/**
 * Lang class for Java Prettify.
 * Note that the method {@link #getFileExtensions()} should be overridden.
 * 
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public abstract class Lang {

  /**
   * Similar to those in JavaScript prettify.js.
   */
  protected List<List<Object>> shortcutStylePatterns;
  /**
   * Similar to those in JavaScript prettify.js.
   */
  protected List<List<Object>> fallthroughStylePatterns;
  /**
   * See {@link io.github.kbiakov.codeview.highlight.prettify.lang.LangCss} for example.
   */
  protected List<Lang> extendedLangs;

  /**
   * Constructor.
   */
  public Lang() {
    shortcutStylePatterns = new ArrayList<List<Object>>();
    fallthroughStylePatterns = new ArrayList<List<Object>>();
    extendedLangs = new ArrayList<Lang>();
  }

  /**
   * This method should be overridden by the child class.
   * This provide the file extensions list to help the parser to determine which 
   * {@link Lang} to use. See JavaScript prettify.js.
   * 
   * @return the list of file extensions
   */
  public static List<String> getFileExtensions() {
    return new ArrayList<String>();
  }

  public List<List<Object>> getShortcutStylePatterns() {
    List<List<Object>> returnList = new ArrayList<List<Object>>();
    for (List<Object> shortcutStylePattern : shortcutStylePatterns) {
      returnList.add(new ArrayList<Object>(shortcutStylePattern));
    }
    return returnList;
  }

  public void setShortcutStylePatterns(List<List<Object>> shortcutStylePatterns) {
    if (shortcutStylePatterns == null) {
      this.shortcutStylePatterns = new ArrayList<List<Object>>();
      return;
    }
    List<List<Object>> cloneList = new ArrayList<List<Object>>();
    for (List<Object> shortcutStylePattern : shortcutStylePatterns) {
      cloneList.add(new ArrayList<Object>(shortcutStylePattern));
    }
    this.shortcutStylePatterns = cloneList;
  }

  public List<List<Object>> getFallthroughStylePatterns() {
    List<List<Object>> returnList = new ArrayList<List<Object>>();
    for (List<Object> fallthroughStylePattern : fallthroughStylePatterns) {
      returnList.add(new ArrayList<Object>(fallthroughStylePattern));
    }
    return returnList;
  }

  public void setFallthroughStylePatterns(List<List<Object>> fallthroughStylePatterns) {
    if (fallthroughStylePatterns == null) {
      this.fallthroughStylePatterns = new ArrayList<List<Object>>();
      return;
    }
    List<List<Object>> cloneList = new ArrayList<List<Object>>();
    for (List<Object> fallthroughStylePattern : fallthroughStylePatterns) {
      cloneList.add(new ArrayList<Object>(fallthroughStylePattern));
    }
    this.fallthroughStylePatterns = cloneList;
  }

  /**
   * Get the extended languages list.
   * @return the list
   */
  public List<Lang> getExtendedLangs() {
    return new ArrayList<Lang>(extendedLangs);
  }

  /**
   * Set extended languages. Because we cannot register multiple languages 
   * within one {@link prettify.lang.Lang}, so it is used as an solution. See 
   * {@link prettify.lang.LangCss} for example.
   * 
   * @param extendedLangs the list of {@link prettify.lang.Lang}s
   */
  public void setExtendedLangs(List<Lang> extendedLangs) {
    this.extendedLangs = new ArrayList<Lang>(extendedLangs);
  }
}
