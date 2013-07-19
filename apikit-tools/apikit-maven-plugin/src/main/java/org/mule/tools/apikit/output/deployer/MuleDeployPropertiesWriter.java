package org.mule.tools.apikit.output.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MuleDeployPropertiesWriter {

    private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";

    public static void write(MuleDeployProperties deployProperties, File propertiesFile) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(propertiesFile);
            writer.println("#" + formatNow());
            printProperty(writer, MuleDeployPropertiesParser.PROPERTY_ENCODING, deployProperties.getEncoding());
            printProperty(writer, MuleDeployPropertiesParser.PROPERTY_DOMAIN, deployProperties.getDomain());
            printProperty(writer, MuleDeployPropertiesParser.PROPERTY_REDEPLOYMENT_ENABLED, deployProperties.isRedeploymentEnabled());
            if(!deployProperties.getConfigResources().isEmpty()) {
                printProperty(writer, MuleDeployPropertiesParser.PROPERTY_CONFIG_RESOURCES, stringify(deployProperties.getConfigResources()));
            }
            printProperty(writer, MuleDeployPropertiesParser.PROPERTY_CONFIG_BUILDER, deployProperties.getConfigurationBuilder());
            printProperty(writer, MuleDeployPropertiesParser.PROPERTY_LOADER_OVERRIDE, deployProperties.getLoaderOverride());
            printProperty(writer, MuleDeployPropertiesParser.PROPERTY_SCAN_PACKAGES, deployProperties.getPackagesToScan());
            writer.close();

        } catch(FileNotFoundException fne) {
            // Do nothing

        } finally {
            if(writer != null) {
                writer.close();
            }
        }
    }

    private static String formatNow() {
        // Format example: #Tue Mar 26 18:49:42 EDT 2013
        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        return format.format(now.getTime());
    }

    private static void printProperty(PrintWriter writer, String propertyName, Object propertyValue) {
        if(propertyValue != null) {
            writer.println(propertyName + "=" + propertyValue);
        }
    }

    private static String stringify(List<String> configResources) {
        StringBuilder builder = new StringBuilder("");
        for(String configResource : configResources) {
            if(builder.length() > 0) {
                builder.append(",");
            }
            builder.append(configResource);
        }
        return builder.toString();
    }

}
