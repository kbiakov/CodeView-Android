// Copyright (c) 2012 Chan Wai Shing
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package io.github.kbiakov.codeview.highlight.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * The parser parsed result.
 * 
 * This class include the information needed to highlight the syntax. 
 * Information includes where the content located in the document (offset and 
 * length) and what style(s) should be applied on that segment of content.
 * 
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ParseResult {

  /**
   * The start position of the content.
   */
  protected int offset;
  /**
   * The length of the content.
   */
  protected int length;
  /**
   * The style keys of the content. The style at higher index of the list will 
   * override the style of the lower index.
   */
  protected List<String> styleKeys;

  /**
   * Constructor.
   * 
   * @param offset the start position of the content
   * @param length the length of the content
   * @param styleKeys the style keys of the content
   */
  public ParseResult(int offset, int length, List<String> styleKeys) {
    this.offset = offset;
    this.length = length;
    this.styleKeys = new ArrayList<String>(styleKeys);
  }

  /**
   * The start position of the content.
   * @return the start position of the content
   */
  public int getOffset() {
    return offset;
  }

  /**
   * The start position of the content.
   * @param offset the start position of the content
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /**
   * The length of the content.
   * @return the length of the content
   */
  public int getLength() {
    return length;
  }

  /**
   * The length of the content.
   * @param length the length of the content
   */
  public void setLength(int length) {
    this.length = length;
  }

  /**
   * Get the style keys represented by one string key, see 
   * {@link Theme#getStylesAttributeSet(String)}.
   * @return the style keys of the content
   */
  public String getStyleKeysString() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0, iEnd = styleKeys.size(); i < iEnd; i++) {
      if (i != 0) {
        sb.append(" ");
      }
      sb.append(styleKeys.get(i));
    }
    return sb.toString();
  }

  /**
   * The style keys of the content.
   * @param styleKeys the style keys of the content
   */
  public void setStyleKeys(List<String> styleKeys) {
    this.styleKeys = new ArrayList<String>(styleKeys);
  }

  /**
   * The style keys of the content.
   * @param styleKey the style key
   * @return see the return value of {@link List#add(Object)}
   */
  public boolean addStyleKey(String styleKey) {
    return styleKeys.add(styleKey);
  }

  /**
   * The style keys of the content.
   * @param styleKey the style key
   * @return see the return value of {@link List#remove(Object)}
   */
  public boolean removeStyleKey(String styleKey) {
    return styleKeys.remove(styleKey);
  }

  /**
   * The style keys of the content.
   */
  public void clearStyleKeys() {
    styleKeys.clear();
  }

  /**
   * The style keys for this matched result, see {@link syntaxhighlighter.theme}.
   * @return the style keys
   */
  public List<String> getStyleKeys() {
    return new ArrayList<String>(styleKeys);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("[");
    sb.append(offset);
    sb.append("; ");
    sb.append(length);
    sb.append("; ");
    for (int i = 0, iEnd = styleKeys.size(); i < iEnd; i++) {
      if (i != 0) {
        sb.append(", ");
      }
      sb.append(styleKeys.get(i));
    }
    sb.append("]");

    return sb.toString();
  }
}