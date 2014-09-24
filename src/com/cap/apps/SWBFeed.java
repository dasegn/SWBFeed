/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cap.apps;
import com.google.gson.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.semanticwb.Logger;
import org.semanticwb.SWBException;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.Resource;
import org.semanticwb.model.ResourceType;
import org.semanticwb.model.User;
import org.semanticwb.model.WebPage;
import org.semanticwb.model.WebSite;
import org.semanticwb.portal.api.GenericAdmResource;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.api.SWBResourceURL;

/**
 *
 * @author daniel.martinez
 */
public class SWBFeed extends GenericAdmResource {
    private static Logger log = SWBUtils.getLogger(SWBFeed.class);     
    private PrintWriter out = null;  
    
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramsRequest) throws SWBResourceException {
        try {
            out = response.getWriter();  
            Resource base = paramsRequest.getResourceBase();
            String urlRSS = base.getAttribute("urlRSS", "");
            out.println(SWBFeedReader.readRSS(urlRSS).toString());            
            out.flush();
            out.close();            
        } catch (IOException e){
            log.error("Ocurrió un error en la construcción de la vista del rescurso:\n "+e.getMessage());
        }
    }
    @Override
    public void doAdmin(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramReq) {    
        SWBResourceURL url = paramReq.getActionUrl();
        try {            
            VelocityContext context = new VelocityContext();
            context.put("actionURL", url);            
            context.put("msg", request.getParameter("msg"));
            context.put("urlRSS", this.getResourceBase().getAttribute("urlRSS",""));      
            runTemplate(response, context, "SWBFeedAdmin");          
        } catch(Exception e){
            log.error("Ocurrió un error durante la construcción de la vista de administración. "+e.getMessage()); 
            e.printStackTrace();
        }        
    }

    @Override
    public void processAction(HttpServletRequest request, SWBActionResponse response) throws SWBResourceException, IOException {
        Resource base = getResourceBase();
        try {
            Enumeration names = request.getParameterNames();
            while (names.hasMoreElements()){
                String name = (String) names.nextElement();
                base.setAttribute(name, request.getParameter(name));
            }
            base.updateAttributesToDB();
            response.setRenderParameter("msg", "true");            
        } catch(SWBException e){
            response.setRenderParameter("msg", "false");            
            log.error(e);
        }
    }    
    public void runTemplate(HttpServletResponse response, VelocityContext ctx, String tplName){
        StringWriter sw = new StringWriter();
        try {
            out = response.getWriter();
            ctx.put("webPath", getWebPath());
            Template tmpl = prepareTemplate(tplName + ".vm");
            tmpl.merge(ctx, sw);            
            out.println(sw); 
            out.close();
        } catch(IOException e){
            log.error("Ocurrió un error durante la ejecución de la vista "+ tplName +"  \n "+e.getMessage()); 
            e.printStackTrace();            
        }
    }
        
    public Template prepareTemplate(String name){
        Template tmpl = null;
        try {
            VelocityEngine ve = new VelocityEngine();
            Properties p = new Properties();
            p.setProperty("resource.loader", "file");
            p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            p.setProperty("file.resource.loader.path", getWPath() + "templates");
            p.setProperty("file.resource.loader.cache", "false");
            ve.init(p);
            tmpl = ve.getTemplate(name, "UTF-8");                   
        } catch(Exception e){
            log.error("Ocurrió un error en el armado de la plantilla:\n "+e.getMessage());
            e.getStackTrace();
        }
        return tmpl;
    }
    @Override
    public void install(ResourceType resourceType) throws SWBResourceException {  
        String path = SWBPortal.getWorkPath()+resourceType.getWorkPath();

        // Estableciendo parametros de la instancia
        resourceType.setTitle("SWBFeed");
        resourceType.setDescription("Recurso que administra fuentes RSS y parsea su información. ");
        //resourceType.get
        boolean mkDir = false;
        
        try {            
            mkDir = SWBUtils.IO.createDirectory(path);            
        } catch (Exception e){
            log.error("Error intentando crear directorio base o copiando archivos de trabajo para el recurso SWBFeed ", e);
        }        
        if(mkDir){
            try {            
                JarFile thisJar = SWBFeedUtils.getJarName(SWBFeed.class);
                if(thisJar != null){
                    try {
                        SWBFeedUtils.copyResourcesToDirectory(thisJar, "com/cap/apps/swbfeed/assets", path);
                    } catch (IOException e){
                        log.error("Error intentando exportar el directorio assets. ", e);
                    }
                }            
            } catch(Exception e){         
                log.error("Error intentando definir el path del archivo jar de trabajo o exportando directorio de assets. ", e);            
            }
        }
    }
    
   @Override
    public void uninstall(ResourceType resourceType) throws SWBResourceException {
        String path = SWBPortal.getWorkPath() + resourceType.getWorkPath();
        try {
            boolean deleteDirectory = SWBUtils.IO.removeDirectory(path);
        } catch (Exception e){
            log.error("Error intentando eliminar directorio de trabajo para el recurso. ", e);
        }    
   }  
   
    @Override
    public void setResourceBase(Resource base) {
        try {                                          
            super.setResourceBase(base);
            Iterator<String> it = base.getAttributeNames();
            
            while(it.hasNext()) {
                String attname = it.next();
                String attval = base.getAttribute(attname);                
                if(attname.startsWith("path") && attval != null){
                    attval = attval.replaceAll("(\\r|\\n)","");
                    base.setAttribute(attname, attval);
                }                
            }
            try {
                base.updateAttributesToDB();
            } catch(Exception e) {
                log.error(e);
            }                       
        } catch(Exception e) { 
            log.error("Error while setting resource base: "+base.getId() +"-"+ base.getTitle(), e);
        }
    }
    
    public String getWPath(){
        String base = this.getResourceBase().getResourceType().getWorkPath();
        return SWBPortal.getWorkPath().replace("//", "/") + base+"/";
    }
    public String getWebPath(){
        String base = this.getResourceBase().getResourceType().getWorkPath();
        return SWBPortal.getWebWorkPath() + base+ "/";
    }
    protected String getStack(Exception e){
        StringBuilder stck = new StringBuilder();
        stck.append("Mensaje: "+e.getMessage()+"\n");
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement element : trace) {
          stck.append("----------------------------------\n");
          stck.append("Clase: ").append(element.getClassName()).append("\n");
          stck.append("Metodo: ").append(element.getMethodName()).append("\n");
          stck.append("Archivo: ").append(element.getFileName()).append("\n");
          stck.append("Linea: ").append(element.getLineNumber()).append("\n");
          stck.append("----------------------------------");          
        }
        return stck.toString();
    }     
}
