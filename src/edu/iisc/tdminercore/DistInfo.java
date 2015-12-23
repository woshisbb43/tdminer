/*
 * DistInfo.java
 *
 * Created on July 25, 2007, 10:56 AM
 *
 */

package edu.iisc.tdminercore;

import java.io.File;

/**
 * This class provides static functions providing information 
 * about the installation.
 *
 * @author phreed@gmail.com
 */
public class DistInfo
{
    
    public static String getFolder()
    {
        String property = System.getProperty("java.class.path");
        File file = new File(".");
        String path = file.getAbsolutePath();
        DistInfo that = new DistInfo();
        String path2 = that.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        //java.net.URL url =  that.class.
                
        return path2;
    }
    
    public static boolean isLoaded() { return true; }
    
}
