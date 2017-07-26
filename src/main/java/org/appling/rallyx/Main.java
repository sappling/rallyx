package org.appling.rallyx;

import com.google.gson.*;
import com.rallydev.rest.RallyRestApi;
import org.apache.commons.cli.*;
import org.appling.rallyx.excel.ExcelWriter;
import org.appling.rallyx.rally.*;
import org.appling.rallyx.xmind.XMindWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by sappling on 9/5/2016.
 */
public class Main {
    public static final String OPTION_PROPERTIES = "p";
    private static final String OPTION_NOPROXY = "noproxy";
    private static final String OPTION_INIT = "i";
    private static final String OPTION_RELEASE = "r";
    private static final String OPTION_TYPE = "t";
    private static final String OPTION_FILE = "f";
    private static final String OPTION_HELP = "help";

    private static final String PROP_APIKEY = "rally_key";
    private static final String PROP_PROXYURL = "proxyurl";
    private static final String PROP_PROXYUSER = "proxyuser";
    private static final String PROP_PROXYPASS = "proxypass";
    private static final String PROP_INITIATIVE = "initiative";
    private static final String PROP_RELEASE = "release";
    private static final String PROP_TYPE = "type";
    private static final String PROP_FILE = "file";


    private static Options options = setupOptions();


    static public void main(String args[]) {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        String outType = null;

        try {
            line = parser.parse(options, args);
        } catch (MissingOptionException me) {
            System.out.println("Missing required option.");
            printHelp();
            System.exit(-1);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        if (line.hasOption(OPTION_HELP)) {
            printHelp();
            System.exit(0);
        }

        Properties properties = getOptions(line);

        if (properties.containsKey(PROP_TYPE)) {
            outType = properties.getProperty(PROP_TYPE);
        }


        String rally_key = properties.getProperty(PROP_APIKEY);
        String proxy_url = properties.getProperty(PROP_PROXYURL);
        String proxy_user = properties.getProperty(PROP_PROXYUSER);
        String proxy_pass = properties.getProperty(PROP_PROXYPASS);


        if (rally_key == null) {
            System.err.println("Error:  environment variable RALLY_KEY not defined.  This must be set to the Rally API-Key for read only web services.");
            System.exit(-1);
        }

        String initiativeID = null;

        String outName = null;
        if (properties.containsKey(PROP_FILE)) {
            outName = properties.getProperty(PROP_FILE);
        }

        List<RallyNode> storiesInReleaseList = null;
        RallyNode initiative = null;
        List<RallyNode> storiesUnderInitiativeList = null;

        boolean useProxy = (proxy_url != null);
        if (line.hasOption(OPTION_NOPROXY)) {
            useProxy = false;
        }

        try {
            RallyRestApi restApi = new RallyRestApi(new URI("https://rally1.rallydev.com"), rally_key);
            if (useProxy) {
                if (proxy_user != null && proxy_pass != null) {
                    restApi.setProxy(new URI(proxy_url), proxy_user, proxy_pass);
                }  {
                    restApi.setProxy(new URI(proxy_url));
                }
            }

            if (properties.containsKey(PROP_RELEASE)) {
                String releaseName = properties.getProperty(PROP_RELEASE);
                UserStoryFinder finder = new UserStoryFinder(restApi);
                finder.setRelease(releaseName);

                storiesInReleaseList = finder.getStories();
            }

            if (properties.containsKey(PROP_INITIATIVE)) {
                initiativeID = properties.getProperty(PROP_INITIATIVE);
                InitiativeNodeFinder walker = new InitiativeNodeFinder(restApi);
                initiative = walker.getInitiativeTree(initiativeID);
                storiesUnderInitiativeList = walker.getStories();
            }

            // statistics
            StoryStats stats = new StoryStats(storiesInReleaseList, storiesUnderInitiativeList, initiative);
            stats.printStats();

            if (outType != null) {
                if (outType.equalsIgnoreCase("xmind") && (initiative!=null)) {
                    XMindWriter xwriter = new XMindWriter(outName, stats.getStoriesInRelease());
                    RallyNodeWalker walker = new RallyNodeWalker(xwriter);
                    walker.walk(initiative, null, 1);
                    xwriter.addOrphans(stats.getStoriesNotInInitiative());
                    xwriter.save();
                } else if (outType.equalsIgnoreCase("excel")) {
                    ExcelWriter excelWriter = new ExcelWriter(stats);
                    excelWriter.write(outName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties getOptions(CommandLine line) {
        Properties prop = new Properties();
        setPropFromEnv(prop, PROP_APIKEY);
        setPropFromEnv(prop, PROP_PROXYURL);
        setPropFromEnv(prop, PROP_PROXYUSER);
        setPropFromEnv(prop, PROP_PROXYPASS);

        Properties propFromFile = new Properties();
        if (line.hasOption(OPTION_PROPERTIES)) {
            String propFileName = line.getOptionValue(OPTION_PROPERTIES);
            File propFile = new File(propFileName);
            if (!propFile.exists()) {
                System.err.println("Error: Property file '"+propFileName+"' does not exist!");
                System.exit(-1);
            }
            try {
                propFromFile.load(new FileInputStream(propFile));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        Set<String> keys = propFromFile.stringPropertyNames();
        for (String key : keys) {
            prop.setProperty(key, propFromFile.getProperty(key));
        }


        if (line.hasOption(OPTION_INIT)) {
            prop.setProperty(PROP_INITIATIVE, line.getOptionValue(OPTION_INIT));
        }

        if (line.hasOption(OPTION_RELEASE)) {
            prop.setProperty(PROP_RELEASE, line.getOptionValue(OPTION_RELEASE));
        }
        if (line.hasOption(OPTION_TYPE)) {
            prop.setProperty(PROP_TYPE, line.getOptionValue(OPTION_TYPE));
        }

        if (line.hasOption(OPTION_FILE)) {
            prop.setProperty(PROP_FILE, line.getOptionValue(OPTION_FILE));
        }

        return prop;
    }

    private static void setPropFromEnv(Properties prop, String propname) {
        String env = System.getenv(propname);
        if (env != null && env.length()>0) {
            prop.setProperty(propname, env);
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("rallyx", options, true);
        String envHelp = "\nAlso uses the following environment variables if not set in properties file:\n" +
                "RALLY_KEY     Set to the Rally API Key   - REQUIRED\n" +
                "PROXYURL      URL of proxy (if needed) like http://myproxy.my.com:8080\n" +
                "PROXYUSER     username of authenticated proxy\n" +
                "PROXYPASS     password for authenticated proxy\n";
        System.out.print(envHelp);
    }

    private static Options setupOptions() {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_INIT).longOpt(PROP_INITIATIVE).desc("Initiative ID (like I203)")
                .numberOfArgs(1).optionalArg(false).argName("id").build());
        options.addOption(Option.builder(OPTION_RELEASE).longOpt(PROP_RELEASE)
                .desc("Release (like \"some release\") - REQUIRED").numberOfArgs(1)
                .optionalArg(false).argName("name").build());
        options.addOption(Option.builder(OPTION_TYPE).longOpt(PROP_TYPE).desc("type of output (xmind, excel, word)")
                .numberOfArgs(1).optionalArg(false).argName("filetype").build());
        options.addOption(OPTION_NOPROXY, false, "disable proxy use even if env var set");
        options.addOption(Option.builder(OPTION_FILE).longOpt(PROP_FILE).desc("output filename")
                .numberOfArgs(1).optionalArg(false).argName("filename").build());
        options.addOption(Option.builder(OPTION_PROPERTIES).longOpt("properties").desc("properties file with options")
                .numberOfArgs(1).optionalArg(false).argName("propfile").build());
        options.addOption(OPTION_HELP, false, "display help");
        return options;
    }
}
