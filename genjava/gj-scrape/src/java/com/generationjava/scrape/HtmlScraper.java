/*
 * Copyright (c) 2003, Henri Yandell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of Genjava-Core nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.generationjava.scrape;

import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedList;

import java.text.Format;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import com.generationjava.web.HtmlW;
import com.generationjava.web.XmlW;

/// simple system in. need to now consider the move methods.
/// then need to make sure the get method obeys internal 
/// rules. ie) if given td.a and u find td.td, should then look 
/// at that td? I guess so. Just as long as a tag doesn't span 
/// another that it's not allowed to.

// need to be able to say: move to b.class=blah and b=foo

/// Add a moveback(String tag) method
/// Add a getContent(int i) method to get untagged blocks of text
    /**
        HtmlScraper scraper = new HtmlScraper();
        scraper.scrape(UrlW.openUrlStream("http://www.yandell.org"));
        System.err.println(scraper.get("TITLE").trim());
        System.err.println(scraper.get("HEAD.TITLE").trim());
        System.err.println(scraper.get("HTML.HEAD.TITLE").trim());
        System.err.println(scraper.get("HTML.HEAD.META[name]"));
        while(scraper.move("LI")) {
            System.err.println(scraper.get("A[HREF]"));
        }
        System.err.println(scraper.get("LI.I"));
    */
public class HtmlScraper {

    // The entire page
    private String page;
    // Lowercase version of the entire page
    private String lcPage;
    // Position at which the string-scraper has reached
    private int currentIndex;

    // Whether it has made the first move. 
    // A hack to handle the semantics of wanting to 
    // move to the tag that starts the text.
    private boolean firstMove;

    public HtmlScraper() {
    }

    public int getIndex() {
        return this.currentIndex;
    }

    public void scrape(String text) {
        if(text.startsWith("http://")) {
            throw new RuntimeException("Text starts with http://. This could be bad. ");
        }
        
        this.page = text;
        this.lcPage = text.toLowerCase();
        reset();
    }

    public HtmlScraper scrapeTag(String tag) {
        HtmlScraper scraper = new HtmlScraper();
        scraper.scrape(get(tag));
        return scraper;
    }

    /**
     * Move back to the start of the page.
     */
    public void reset() {
        this.currentIndex = 0;
    }

    /**
     * Move to the specified tag. 
     */
     // This needs to be case-insensitive
    public boolean move(String tag) {

        int lastIdx = this.currentIndex + 1;

        // HACK: We expect move(String) to work the first time, even if it starts with that tag
        if(firstMove == false && this.currentIndex == 0 && page.startsWith("<") ) {
            firstMove = true;
            lastIdx--;
        }

        int idx = XmlW.getIndexOpeningTag(lcPage, tag.toLowerCase(), lastIdx);
        if(idx == -1) {
            return false;
        } else {
            this.currentIndex = idx;
            return true;
        }
    }

    /**
     * Helper method.
     * Move a number of tags.
     */
    public boolean move(String tag, int sz) {
        for(int i=0; i< sz; i++) {
            if(!move(tag)) {
                return false;
            }
        }
        return true;
    }

    // finds any tag with name=value attribute. Need to be able 
    // to specify the tag really, and also the same for a value.
    // really we need a generic search method? :)
    // get this value, is it this. if not, find next.

    // Tricky. name needs to be case-insensitive, value needs 
    // to not be.
    public boolean moveToTagWith(String name, String value) {
        int idx = this.page.indexOf(name+"=\""+value+"\"", this.currentIndex);
        if(idx == -1) {
            idx = this.page.indexOf(name+"="+value, this.currentIndex);
        }
        if(idx == -1) {
            idx = this.page.indexOf(name+"='"+value+"'", this.currentIndex);
        }
        if(idx == -1) {
            return false;
        } else {
            idx = this.page.lastIndexOf("<", idx);
            if(idx == this.currentIndex) {
                // then we just found the previous one, so we need to bump things 
                // along a little bit
                int nextTag = this.page.indexOf("<", this.currentIndex + 1);
                if(nextTag == -1) {
                    return false;
                } else {
                    this.currentIndex = nextTag;
                    boolean found = moveToTagWith(name, value);
                    if(!found) {
                        // reset the currentIndex
                        this.currentIndex = idx;
                    }
                    return found;
                }
            }
            this.currentIndex = idx;
            return true;
        }
    }

    //   moveTo a[href], www.yandell.org
    // TODO: Kill the moveToTagWith method and have:  *[href], www.foo.com
    public boolean moveTo(String get, String value) {
        HtmlScraper scraper = new HtmlScraper();
        scraper.scrape(this.page.substring(this.currentIndex));
        int count = 1;
        while(true) {
            boolean found = scraper.move(get);
            if(!found) {
                return false;
            }
            String chunk = scraper.get(get);
            if( (chunk == null) || (chunk.equals("")) ) {
                return false;
            }
            if(chunk.equals(value)) {
                move(get, count);
                break;
            } else {
                count++;
            }
        }
        return true;
    }

    /**
     * Move to a specified piece of text. 
     */
    public boolean moveToText(String text) {
        int idx = this.page.indexOf(text, this.currentIndex);
        if(idx == -1) {
            return false;
        } else {
            this.currentIndex = idx;
            return true;
        }
    }

    /**
     * Move to a specified comment. The parameter should 
     * not contain the HTML comment syntax.
     */
    public boolean moveToComment(String comment) {
        int idx = this.page.indexOf(comment, this.currentIndex);
        if(idx == -1) {
            return false;
        } else {
            idx = page.lastIndexOf("<!", idx);
            if(idx == -1) {
                return false;
            }
            this.currentIndex = idx;
            return true;
        }
    }

    /**
     * Does the piece of text exist in this page?
     */
    public boolean textExists(String text) {
        int idx = this.page.indexOf(text, this.currentIndex);
        return (idx != -1);
    }

    /**
     * Get the content from the current point to the
     * specified text. 
     */
    public String getContentToText(String text) {
        int idx = this.page.indexOf(text, this.currentIndex);
        if(idx == -1) {
            return "";
        } else {
            return this.page.substring(this.currentIndex, idx);
        }
    }

    // find a parent tag, and get all of a child tag from it.
    public String[] getChildren(String parent, String child) {
         LinkedList list = new LinkedList();
         HtmlScraper scraper = new HtmlScraper();
         String chunk = get(parent);
         scraper.scrape(chunk);
         while(scraper.move(child)) {
             list.add(scraper.get(child));
         }
         return (String[])list.toArray(new String[0]);
    }

    /**
     * Get a value from the page. Dot-notation is used to indicate 
     * child-tags, while [] notation is used for attributes.
     */
    public String get(String tag) {
        String lcText = this.lcPage.substring(this.currentIndex);
        String text = this.page.substring(this.currentIndex);
        String[] strs = StringUtils.split(tag, ".");
        for(int i=0; i<strs.length; i++) {
            String attr = null;
            int idx = strs[i].indexOf('[');
            if(idx != -1) {
                attr = StringUtils.getNestedString(strs[i], "[", "]");
                strs[i] = strs[i].substring(0, idx);
                int start = HtmlW.getIndexOpeningTag(lcText, strs[i]);
                return HtmlW.getAttribute(text, attr, start, lcText);
            } else {
//                System.out.println("HS: "+strs[i]+" --- "+text);
                text = HtmlW.getContent(text, strs[i], lcText);
            }
        }
        return text;
    }

    /**
     * Prints out the current position in the scraper until the end of the page. 
     */
    public String toString() {
        return this.page.substring(this.currentIndex);
    }

    // From GenJavaCore's UrlW
    static private Object getContent(InputStream in) throws IOException {
        String str = loadFile(in);
        str = StringUtils.chopNewline(str);
        str = StringUtils.chopNewline(str);
        return str;
    }

    /**
     * Load the contents of a given stream. Return null if the 
     * file is not loadable.
     */
     // From GenJavaCore's FileW
    static private String loadFile(InputStream in) {
        try {
            int ptr = 0;
            StringBuffer buffer = new StringBuffer();
            while( (ptr = in.read()) != -1 ) {
                buffer.append((char)ptr);
            }
            return buffer.toString();
        } catch(IOException ioe) {
            return null;
        }
    }



        /*
             DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");         
             Object[] pattern = new Object[] {
             		Number.class, String.class, String.class, 
                    Number.class, Number.class, Number.class, 
                    String.class, df, df, 
                    String.class, String.class, Number.class};
            
             collection = scraper.scrapeTable(pattern);                         
        */
     
    public Object[] scrapeTable() {
        return scrapeTable(null);
    }
    public Object[] scrapeTable(Object[] pattern) {
        List rowData = new LinkedList();
        
LABEL:  while(this.move("tr")) {
        	String tableRowData = this.get("tr");

            HtmlScraper rowScraper = new HtmlScraper();
        	rowScraper.scrape(tableRowData);
            
            int n = 0;
            List cells = new ArrayList();

            // TODO: Look for "th"?
            
            while (rowScraper.move("td")) {
            	String cellContent = rowScraper.get("td").trim();
                
                if(pattern == null) {
                    cells.add(cellContent);
                    continue;
                }

                Object target = null;
                   
                try {
                	target = pattern[n];
                } catch (ArrayIndexOutOfBoundsException ae) {                    
                	continue LABEL;
                }
                                                
                if (Format.class.isAssignableFrom(target.getClass())) {
                    try {
                        Object formattedObject = ((Format) target).parseObject(cellContent);
                        cells.add(formattedObject);
                    } catch (ParseException pe) {
                        throw new RuntimeException("Error parsing cell contents into Object: ", pe);
                    }
                } else if (target instanceof Class) {
                    Class clazz = (Class) target;                  
                	if (String.class.equals(clazz)) {                		
                		cells.add(cellContent);
                	} else if (Number.class.isAssignableFrom(clazz)) {
                		String s = cellContent.replaceAll(",", "");                           
                		try {
                			cells.add(NumberUtils.createNumber(s));
                        } catch (NumberFormatException nfe) {
                        	throw new RuntimeException("Unable to create a number from input: " + cellContent, nfe);
                        }
                	} else {
                        throw new RuntimeException("Unrecognized class type for target");                		
                    }
                } else {
                	throw new RuntimeException("Target was neither a Format object or a Class object: " + target.getClass().getName());
                }
                 
                n++;
            }
            
            // Does the number of cells match the pattern length?  If not, skip.
            /*
            if (pattern != null && cells.size() != pattern.length) {
            	continue;
            }           
            */
            
            rowData.add(cells.toArray());
        }
                    
        return rowData.toArray();
    }    

}
