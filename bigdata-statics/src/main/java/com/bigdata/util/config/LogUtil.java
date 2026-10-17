/*

Copyright (C) SYSTAP, LLC DBA Blazegraph 2006-2016.  All rights reserved.

Contact:
     SYSTAP, LLC DBA Blazegraph
     2501 Calvert ST NW #106
     Washington, DC 20008
     licenses@blazegraph.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

package com.bigdata.util.config;

import java.net.URL;
import java.io.File;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Utility class that provides a set of static convenience methods related to
 * the initialization and configuration of the logging mechanism(s) employed by
 * the components of the system. The methods of this class can be useful both in
 * Jini configuration files, as well as in the system components themselves.
 * <p>
 * This class uses the <code>log4j2.configurationFile</code> property when it
 * is set.
 * <p>
 * If that property is not set, this class searches the
 * CLASSPATH for a log4j configuration. While this is a change from the
 * historical, searching the CLASSPATH is necessary for webapp deployments.
 * <p>
 * This class looks for Log4j 2 configuration files with either the
 * <code>.properties</code> or <code>.xml</code> extension. If the configuration
 * property is not defined, if the resource identified by the
 * property can not be located, or if a log4j configuration resource can not be
 * located in the default location along the class path then this class will
 * will a message on <em>stderr</em>.
 * <p>
 * The logging configuration is loaded from the selected location.
 * <p>
 * This class cannot be instantiated.
 * 
 * @see https://sourceforge.net/apps/trac/bigdata/ticket/394
 */
public class LogUtil {

    /**
     * Examine the various log4j configuration properties and return the name of
     * the log4j configuration resource if one was configured.
     * 
     * @return The log4j configuration resource -or- <code>null</code> if the
     *         resource was not configured properly.
     */
    static String getConfigPropertyValue() {

        return System
                .getProperty("log4j2.configurationFile");
        
    }
    
    /**
     * Attempt to resolve the resources with the following names in the given
     * order and return the {@link URL} of the first such resource which is
     * found and <code>null</code> if none of the resources are found:
     * <ol>
     * <li>log4j2.properties</li>
     * <li>log4j2.xml</li>
     * </ol>
     * 
     * @return The {@link URL} of the first such resource which was found.
     */
    static URL getConfigPropertyValueUrl() {

        URL url = LogUtil.class.getResource("/log4j2.properties");

        if (url == null)
            url = LogUtil.class.getResource("/log4j2.xml");

        return url;

    }
    
    public interface Options {

        /**
         * This may be used to suppress the banner text. 
         */
        String QUIET = "com.bigdata.util.config.LogUtil.quiet";
        
    }
    
    // Static initialization block that retrieves and initializes 
    // the log4j logger configuration for the given VM in which this
    // class resides. Note that this block is executed only once
    // during the life of the associated VM.
    static {

        final boolean quiet = Boolean.getBoolean(Options.QUIET);
        
        /*
         * First, attempt to resolve the configuration property.
         */
        final String log4jConfig = getConfigPropertyValue();
        
        if (log4jConfig != null) {

            configure(log4jConfig);
            if (!quiet) System.out.println("INFO: " + LogUtil.class.getName()
                    + ": Configure: " + log4jConfig);

        } else {
            
            /*
             * Then attempt to resolve the resource to a URL.
             */
            
            final URL log4jUrl = getConfigPropertyValueUrl();
            
            if (log4jUrl != null) {

                configure(log4jUrl);
                if (!quiet) System.out.println("INFO: " + LogUtil.class.getName()
                        + ": Configure: " + log4jUrl);

            } else {
                
                /*
                 * log4j was not explicitly configured and the log4j resource
                 * could not be located on the CLASSPATH.
                 */

                System.err.println("ERROR: " + LogUtil.class.getName()
                     + " : Could not initialize Log4J logging utility.\n"
                     + "Set system property "
                     +"'-Dlog4j2.configurationFile=file:<path-to-log4j2-configuration>'");
        
            }
            
        }
        
    }

    private static void configure(String location) {
        URI uri;
        if (location.matches("^[A-Za-z]:[\\\\/].*")
                || !location.matches("^[A-Za-z][A-Za-z0-9+.-]*:.*")) {
            uri = new File(location).toURI();
        } else {
            uri = URI.create(location);
        }
        Configurator.reconfigure(uri);
    }

    private static void configure(URL location) {
        try {
            Configurator.reconfigure(location.toURI());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Invalid logging configuration URL: " + location, e);
        }
    }

    public static Logger getLog4jLogger(String componentName) {
        return LogManager.getLogger(componentName);
    }

    public static Logger getLog4jLogger(Class componentClass) {
        return LogManager.getLogger(componentClass);
    }

    public static Logger getLog4jRootLogger() {
        return LogManager.getRootLogger();
    }
    
}
