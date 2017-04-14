/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.model.application.AptGetPackages;
import ch.fhnw.lernstickwelcome.model.application.CombinedPackages;
import ch.fhnw.lernstickwelcome.model.application.WgetPackages;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;
import ch.fhnw.lernstickwelcome.model.WelcomeUtil;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import ch.fhnw.util.StorageTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//import org.freedesktop.dbus.exceptions.DBusException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XXX Change this class if applications should be load from file
 * 
 * @author sschw
 */
public class WelcomeModelFactory {
    private final static ProcessExecutor PROCESS_EXECUTOR = new ProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(WelcomeModelFactory.class.getName());
    private static StorageDevice SYSTEM_STORAGE_DEVICE;
    
    public static ProcessExecutor getProcessExecutor() {
        return PROCESS_EXECUTOR;
    }
    
    public static StorageDevice getSystemStorageDevice() {
        if(SYSTEM_STORAGE_DEVICE == null) {
            try {
                SYSTEM_STORAGE_DEVICE = StorageTools.getSystemStorageDevice();
            } catch (DBusException | IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return SYSTEM_STORAGE_DEVICE;
    }    
    
    /**
     * Creates an ApplicationGroupTask containing all the application with given tag.
     * @param tag tag to be searched for
     * @param title a title for the group task, can be anything
     * @param proxy
     * @return ApplicationGroupTask
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public static ApplicationGroupTask getApplicationGroupTask(String tag, String title, ProxyTask proxy) throws ParserConfigurationException, SAXException, IOException {
        List<ApplicationTask> apps = getApplicationTasks(tag);
        ApplicationGroupTask task = new ApplicationGroupTask(
                title, 
                proxy,
                apps
        );
        return task;
    }
    
    public static ProxyTask getProxyTask() {
        return new ProxyTask();
    }

    /**
     * Searches the application.xml for applications with the given tag.
     * @param name
     * @return List of ApplicationTasks
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public static List<ApplicationTask> getApplicationTasks(String tag) throws ParserConfigurationException, SAXException, IOException {
    	ArrayList<ApplicationTask> apps = new ArrayList<>();
    	File xmlFile = new File("applications.xml");
    	Document xmlDoc = WelcomeUtil.parseXmlFile(xmlFile);
    	NodeList applications = xmlDoc.getElementsByTagName("application");
    	for (int i = 0; i < applications.getLength(); i++) {
    		Node application = applications.item(i);
    		if (application.getNodeType() == Node.ELEMENT_NODE) {
    			Element app = (Element) application;
    			NodeList tags = app.getElementsByTagName("tag");
    			for (int j = 0; j < tags.getLength(); j++) {
    				Element t = ((Element)tags.item(j));
    				if (t.getTextContent().equals(tag)) {
    					apps.add(getApplicationTask(app));
    				}
    			}
    		}
    	}
    	return apps;
    }
    
    /**
     * Searches the application.xml for an application with the given name.
     * @param name
     * @return a Task for this specific application or null if no application was found.
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public static ApplicationTask getApplicationTask(String name) throws ParserConfigurationException, SAXException, IOException {
    	File xmlFile = new File("applications.xml");
    	Document xmlDoc = WelcomeUtil.parseXmlFile(xmlFile);
    	/*DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	Document xmlDoc = builder.parse(xmlFile);*/
    	NodeList applications = xmlDoc.getElementsByTagName("application");
    	for (int i = 0; i < applications.getLength(); i++) {
    		Node application = applications.item(i);
    		if (application.getNodeType() == Node.ELEMENT_NODE) {
    			Element app = (Element) application;
    			if (app.getAttribute("name").equals(name)) { // found application
        			return getApplicationTask(app);
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * Helper function to get ApplicationTask from XML application element.
     * @param app (XML Element)
     * @return ApplicationTask
     */
    private static ApplicationTask getApplicationTask(Element app) {
    	String name = app.getAttribute("name");
    	Node l = app.getElementsByTagName("description").item(0);
    	String description = app.getElementsByTagName("description").item(0).getTextContent();
		String icon = app.getElementsByTagName("icon").item(0).getTextContent();
		String helpPath = app.getElementsByTagName("help-path").item(0).getTextContent();
		List<String> aptgetPackages = new ArrayList<>();
		List<String> wgetPackages = new ArrayList<>();
		// XXX does really every wget package have the same fetchUrl and SaveDir?
		// In application.xml I defined it so every package can have different ones.
		// In class WgetPackages however, there is only one property for all packages.
		// So for now this function uses just the last ones of the properties.
		String wgetFetchUrl = null; 
		String wgetSaveDir = null;
		NodeList packages = app.getElementsByTagName("package");
		for (int j = 0; j < packages.getLength(); j++) {
			Element pkg = ((Element)packages.item(j));
			String type = pkg.getAttribute("type");
			String pkgName = pkg.getNodeValue();
			switch (type) {
			case "aptget":
				aptgetPackages.add(pkgName);
				break;
			case "wget":
				wgetPackages.add(pkgName);
				wgetFetchUrl = pkg.getAttribute("fetchUrl");
				wgetSaveDir = pkg.getAttribute("saveDir");
				break;
			default: break;
			}
		}
		CombinedPackages pkgs = new CombinedPackages(
			new AptGetPackages(aptgetPackages.toArray(new String[aptgetPackages.size()])), 
			new WgetPackages(wgetPackages.toArray(new String[wgetPackages.size()]), wgetFetchUrl, wgetSaveDir)
		);
		ApplicationTask task = new ApplicationTask(name, description, icon, helpPath, pkgs);
		return task;
    }
}
