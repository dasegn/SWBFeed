/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cap.apps;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.Resource;

/**
 *
 * @author daniel.martinez
 */
public class SWBFeedTemplates {
    private static Logger log = SWBUtils.getLogger(SWBFeedTemplates.class);  
    private static PrintWriter out = null;  
    
    public static void buildTemplate(HttpServletResponse response, VelocityContext ctx, String tplName, Resource base){
        StringWriter sw = new StringWriter();
        try {
            out = response.getWriter();
            ctx.put("webPath", SWBFeedUtils.getWebPath(base));
            Template tmpl = prepareTemplate(tplName + ".vm",base);
            tmpl.merge(ctx, sw);            
            out.println(sw); 
            out.close();
        } catch(IOException e){
            log.error("Ocurrió un error durante la ejecución de la vista "+ tplName +"  \n "+e.getMessage()); 
            e.printStackTrace();            
        }
    }
        
    private static Template prepareTemplate(String name, Resource base){
        Template tmpl = null;
        try {
            VelocityEngine ve = new VelocityEngine();
            Properties p = new Properties();
            p.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,"org.apache.velocity.runtime.log.Log4JLogChute" );
            p.setProperty("runtime.log.logsystem.log4j.logger","SWBFeedTemplates.class");
            p.setProperty("resource.loader", "file");
            p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            p.setProperty("file.resource.loader.path", SWBFeedUtils.getWPath(base) + "templates");
            p.setProperty("file.resource.loader.cache", "false");
            ve.init(p);
            tmpl = ve.getTemplate(name, "UTF-8");                   
        } catch(Exception e){
            log.error("Ocurrió un error en el armado de la plantilla:\n "+e.getMessage());
            log.error("Path de template: " + SWBFeedUtils.getWPath(base) + "templates");
            e.getStackTrace();
        }
        return tmpl;
    }
    
    private SWBFeedTemplates(){}
}
