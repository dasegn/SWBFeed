/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cap.apps;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author daniel.martinez
 */
public class SWBFeedReader {
    // Class Properties
    private static boolean cropText = false;
    private static int cropLimit = 0;
    
    public static List readRSS(String url) {
        URL feedSource;
        List listEntries = new ArrayList();
        try {
            feedSource = new URL(url);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));
            listEntries =feed.getEntries();
            Iterator it= listEntries.iterator();
            while(it.hasNext()){
                SyndEntry entrada = (SyndEntry)it.next();
                String title=(entrada.getTitle());
                String author=(entrada.getAuthor());
                Date date=(entrada.getPublishedDate());
                if(cropText){
                    SyndContent description = cropText(entrada.getDescription());             
                    entrada.setDescription(description);
                }
                String date2= ((date!=null)?date.toString():null);
                
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FeedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return listEntries;
    }
    
    public static void setCropText(boolean crop, int limit){
        cropText = crop;
        cropLimit = limit - 1;
    }
    
    private static SyndContent cropText(SyndContent entry){
        SyndContent description = new SyndContentImpl();
        description.setType("text/plain");
        String value = entry.getValue();
        value = (value.length() > cropLimit) ? value.substring(0, cropLimit) : value;
        description.setValue(value); 
        return description;
    }
    
}
