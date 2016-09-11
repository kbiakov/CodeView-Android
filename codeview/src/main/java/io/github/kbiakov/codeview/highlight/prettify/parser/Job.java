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

import java.util.ArrayList;
import java.util.List;

/**
 * This is the job object that similar to those in JavaScript Prettify.
 * 
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Job {

  /**
   * The starting point of the source code.
   */
  protected int basePos;
  /**
   * The source code.
   */
  protected String sourceCode;
  /**
   * The parsed results. n<sup>th</sup> items are starting position position, 
   * n+1<sup>th</sup> items are the three-letter style keyword, where n start 
   * from 0.
   */
  protected List<Object> decorations;

  /**
   * Constructor.
   */
  public Job() {
    this(0, "");
  }

  /**
   * Constructor.
   * 
   * @param basePos the starting point of the source code
   * @param sourceCode the source code
   */
  public Job(int basePos, String sourceCode) {
    if (sourceCode == null) {
      throw new NullPointerException("argument 'sourceCode' cannot be null");
    }
    this.basePos = basePos;
    this.sourceCode = sourceCode;
    decorations = new ArrayList<Object>();
  }

  /**
   * Set the starting point of the source code.
   * 
   * @return the position
   */
  public int getBasePos() {
    return basePos;
  }

  /**
   * Set the starting point of the source code.
   * 
   * @param basePos the position
   */
  public void setBasePos(int basePos) {
    this.basePos = basePos;
  }

  /**
   * Get the source code.
   * 
   * @return the source code
   */
  public String getSourceCode() {
    return sourceCode;
  }

  /**
   * Set the source code.
   * 
   * @param sourceCode the source code 
   */
  public void setSourceCode(String sourceCode) {
    if (sourceCode == null) {
      throw new NullPointerException("argument 'sourceCode' cannot be null");
    }
    this.sourceCode = sourceCode;
  }

  /**
   * Get the parsed results. see {@link #decorations}.
   * 
   * @return the parsed results
   */
  public List<Object> getDecorations() {
    return new ArrayList<Object>(decorations);
  }

  /**
   * Set the parsed results. see {@link #decorations}.
   * 
   * @param decorations the parsed results
   */
  public void setDecorations(List<Object> decorations) {
    if (decorations == null) {
      this.decorations = new ArrayList<Object>();
      return;
    }
    this.decorations = new ArrayList<Object>(decorations);
  }
}
