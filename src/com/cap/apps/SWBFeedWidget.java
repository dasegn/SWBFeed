/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cap.apps;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.VelocityContext;
import org.semanticwb.Logger;
import org.semanticwb.SWBException;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.Resource;
import org.semanticwb.model.ResourceType;
import org.semanticwb.portal.api.GenericAdmResource;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.api.SWBResourceURL;

/**
 *
 * @author daniel.martinez
 */
public class SWBFeedWidget extends GenericAdmResource {
    private static Logger log = SWBUtils.getLogger(SWBFeedWidget.class);     
    private PrintWriter out = null;     

    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramsRequest) throws SWBResourceException {
        try {
            VelocityContext context = new VelocityContext();
            Resource base = paramsRequest.getResourceBase();
            String urlRSS = base.getAttribute("urlRSS", "");
            String urlFull = base.getAttribute("urlFull", "");
            int pageItems = SWBFeedUtils.toInteger(base.getAttribute("pageItems"), "10");        
            int limitText = SWBFeedUtils.toInteger(base.getAttribute("limitText"), "0");

            
            if(!urlRSS.isEmpty()){
                if(limitText != 0)
                    SWBFeedReader.setCropText(true, limitText);
                List feeds = SWBFeedReader.readRSS(urlRSS);
                if( feeds.size() > 0 ){ 
                    context.put("entries", feeds.subList(0, (pageItems > feeds.size()) ? feeds.size() : pageItems ));
                }
            }
            context.put("urlRSS", urlRSS);
            context.put("urlFull", urlFull);
            context.put("newWindow", base.getAttribute("newWindow","0"));
            SWBFeedTemplates.buildTemplate(response, context, "SWBFeedWidget", base);
            
        } catch (Exception e){
            log.error("Ocurrió un error en la construcción de la vista del recurso:\n "+e.getMessage());
            log.error(SWBFeed.getStack(e));
            e.printStackTrace();
        }
    }
    
    @Override
    public void doAdmin(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramReq) {    
        SWBResourceURL url = paramReq.getActionUrl();
        Resource base = getResourceBase(); 
        
        try {            
            VelocityContext context = new VelocityContext();
            context.put("actionURL", url);            
            context.put("msg", request.getParameter("msg"));
            context.put("urlRSS", paramReq.getResourceBase().getAttribute("urlRSS","")); 
            context.put("urlFull", paramReq.getResourceBase().getAttribute("urlFull",""));
            context.put("pageItems", paramReq.getResourceBase().getAttribute("pageItems","10")); 
            context.put("limitText", paramReq.getResourceBase().getAttribute("limitText","0")); 
            context.put("newWindow", base.getAttribute("newWindow","0"));
            SWBFeedTemplates.buildTemplate(response, context, "SWBFeedWidgetAdmin", base);          
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
    
    @Override
    public void install(ResourceType resourceType) throws SWBResourceException {  
        String path = SWBPortal.getWorkPath()+resourceType.getWorkPath();

        // Estableciendo parametros de la instancia
        resourceType.setTitle("SWBFeedWidget");
        resourceType.setDescription("Recurso que inserta un widget para mostrar información de una fuente RSS. ");
        //resourceType.get
        boolean mkDir = false;
        
        try {            
            mkDir = SWBUtils.IO.createDirectory(path);            
        } catch (Exception e){
            log.error("Error intentando crear directorio base o copiando archivos de trabajo para el recurso SWBFeedWidget ", e);
        }        
        if(mkDir){
            try {            
                JarFile thisJar = SWBFeedUtils.getJarName(SWBFeedWidget.class);
                if(thisJar != null){
                    try {
                        SWBFeedUtils.copyResourcesToDirectory(thisJar, "com/cap/apps/swbfeed/assets", path);
                        SWBFeedUtils.copyResourcesToDirectory(thisJar, "com/cap/apps/swbfeedw", path);
                    } catch (IOException e){
                        log.error("Error intentando exportar el directorio assets. ", e);
                    }
                }            
            } catch(Exception e){         
                log.error("Error intentando definir el path del archivo jar de trabajo o exportando directorio de assets. ", e);            
            }
        }
    }   
}

