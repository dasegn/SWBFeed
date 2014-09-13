/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cap.apps;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author daniel.martinez
 */
public class SWBFeedReader {
    public static StringBuilder readRSS(String url) {
        URL feedSource;
        StringBuilder sb = new StringBuilder();
        try {
            feedSource = new URL(url);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));
            List listEntries =feed.getEntries();
            Iterator it= listEntries.iterator();
            while(it.hasNext()){
                SyndEntry entrada = (SyndEntry)it.next();
                String title=(entrada.getTitle() );
                String author=(entrada.getAuthor());
                Date date=(entrada.getPublishedDate());
                String date2= ((date!=null)?date.toString():null);
                sb.append("TITLE= "+title +" | AUTHOR= "+author+" DATE= "+date2+" <br/>");
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
        return sb;
    }
    
}
