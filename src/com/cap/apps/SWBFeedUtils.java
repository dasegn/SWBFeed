/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cap.apps;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.servlet.http.HttpServletResponse;
import org.semanticwb.Logger;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.Resource;
import org.semanticwb.model.WebPage;


/**
 *
 * @author daniel.martinez
 */
public class SWBFeedUtils {
    private static Logger log = SWBUtils.getLogger(SWBFeedUtils.class);
    
    public static JarFile getJarName(Class<?> clase){
        String path = "/" + clase.getName().replace('.', '/') + ".class";
        URL jarUrl = clase.getResource(path);
        
        if(jarUrl == null) {
            return null;
        }            
        String url = null;
        try {
            url = URLDecoder.decode(jarUrl.toString(), "UTF-8");
        } catch(UnsupportedEncodingException e){
            log.error("Error codificando ruta de archivo jar a UTF-8", e); 
        }
        int bang = url.indexOf("!");
        String JAR_URI_PREFIX = "jar:file:";
        
        if(url.startsWith(JAR_URI_PREFIX) && bang !=-1){
            try {                
                return new JarFile(url.substring(JAR_URI_PREFIX.length(), bang));
            } catch (IOException e){
                throw new IllegalStateException("Error convirtiendo Path de archivo jar a JarFile",e);  
            }
        }
        
        return null;
    }
    
    public static void copyResourcesToDirectory(JarFile fromJar, String jarDir, String destDir) throws IOException {        
        for(Enumeration<JarEntry> entries = fromJar.entries(); entries.hasMoreElements();){
            JarEntry entry = entries.nextElement();
            if(entry.getName().startsWith(jarDir+"/") && !entry.isDirectory()){
                File dest = new File(destDir + "/" + entry.getName().substring(jarDir.length() + 1));
                File parent = dest.getParentFile();
                if(parent != null){
                    parent.mkdirs();
                }
                
                FileOutputStream out = new FileOutputStream(dest);
                InputStream in = fromJar.getInputStream(entry);
                
                try {
                    byte[] buffer = new byte[8*1024];
                    int s = 0;
                    while((s = in.read(buffer)) > 0){
                        out.write(buffer, 0, s);
                    }
                } catch (IOException e){
                    log.error("No se puede copiar assets de archivo JAR", e);
                } finally {
                    try {
                        in.close();
                    } catch(IOException ignored) {}
                    try {
                        out.close();
                    } catch(IOException ignored) {}
                }
                
            }
        }    
    }
    
    public static String getPageChilds(WebPage page, String indent){
        StringBuilder sb = new StringBuilder();
        Iterator<WebPage>  it = page.listVisibleChilds(null);         
        
        if(it.hasNext()){
            while(it.hasNext()) {
                WebPage tp = it.next();                
                if( null != tp ){
                    sb.append(",{ 'value': '"+tp.getUrl() + "', 'name':'"+ indent + tp.getDisplayName() + "'}"); 
                    if(tp.listVisibleChilds(null).hasNext()){
                        sb.append(getPageChilds(tp, indent + "- "));                      
                    }
                }
            }
        }        
                                                     
        return sb.toString();        
    }   
    
    public static String getWPath(Resource res){
        String base = res.getResourceType().getWorkPath();
        return SWBPortal.getWorkPath().replace("//", "/") + base+"/";
    }
    public static String getWebPath(Resource res){
        String base = res.getResourceType().getWorkPath();
        return SWBPortal.getWebWorkPath() + base+ "/";
    }
    
    
    public static boolean isNumber(String string){
        try {
            Long.parseLong(string);
        } catch (Exception e){
            return false;
        }
        return true;
    }
    
    // Haciendo la clase no instanciable
    private SWBFeedUtils() { }
}
