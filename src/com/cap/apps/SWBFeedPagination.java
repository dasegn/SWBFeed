/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cap.apps;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceURL;

/**
 *
 * @author daniel.martinez
 */
public class SWBFeedPagination {
    private static String previous = "#";
    private static String next = "#";
    
    public static LinkedHashMap<String,String> build(SWBParamRequest paramsRequest, SWBFeedPageable pg) {
        LinkedHashMap<String,String> urls = new LinkedHashMap<String,String>();        
        setPrevious(paramsRequest, pg);
        
        //All Pages
        for(int i=1; i <= pg.getMaxPageRange();i++){
            SWBResourceURL url = paramsRequest.getRenderUrl();
            url.setParameter("page", Integer.toString(i));
            urls.put(Integer.toString(i), url.toString());
        }        
        setNext(paramsRequest, pg);        
        return urls;
    }    
    
    private static void setPrevious(SWBParamRequest paramsRequest, SWBFeedPageable pg){
        //Previous Page
        SWBResourceURL prevURL = paramsRequest.getRenderUrl();
        if(pg.getPreviousPage() != 0){
            prevURL.setParameter("page", Integer.toString(pg.getPreviousPage()));
            previous = prevURL.toString();
        } else {
            previous = prevURL.toString() + "#";
        }   
    }
    
    private static void setNext(SWBParamRequest paramsRequest, SWBFeedPageable pg){
        //Next Page
        SWBResourceURL nextURL = paramsRequest.getRenderUrl();        
        if(pg.getNextPage() != 0){
            nextURL.setParameter("page", Integer.toString(pg.getNextPage()));
            next = nextURL.toString();
        } else {
            next = nextURL.toString() + "#";
        }        
    }
    
    public static String getPrevious(){    
        return previous;
    }
    public static String getNext(){
        return next;
    }
    
}
