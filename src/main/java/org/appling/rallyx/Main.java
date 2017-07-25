package org.appling.rallyx;

import com.google.gson.*;
import com.rallydev.rest.RallyRestApi;
import org.apache.commons.cli.*;
import org.appling.rallyx.excel.ExcelWriter;
import org.appling.rallyx.rally.*;
import org.appling.rallyx.xmind.XMindWriter;
import org.xmind.core.CoreException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sappling on 9/5/2016.
 */
public class Main {
    private static final String OPTION_NOPROXY = "noproxy";
    private static final String OPTION_INIT = "i";
    private static final String OPTION_RELEASE = "r";
    private static final String OPTION_TYPE = "type";
    private static final String OPTION_FILE = "f";
    private static final String OPTION_HELP = "help";

    private static final String ENV_APIKEY = "RALLY_KEY";
    private static final String ENV_PROXYURL = "PROXYURL";
    private static final String ENV_PROXYUSER = "PROXYUSER";
    private static final String ENV_PROXYPASS = "PROXYPASS";

    private static Options options = setupOptions();


    static public void main(String args[]) {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        String outType = null;
        HashMap<String, JsonObject> releaseStories = new HashMap<>();


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

        if (line.hasOption(OPTION_TYPE)) {
            outType = line.getOptionValue(OPTION_TYPE);
        }


        String rally_key = System.getenv(ENV_APIKEY);
        String proxy_url = System.getenv(ENV_PROXYURL);
        String proxy_user = System.getenv(ENV_PROXYUSER);
        String proxy_pass = System.getenv(ENV_PROXYPASS);


        if (rally_key == null) {
            System.err.println("Error:  environment variable RALLY_KEY not defined.  This must be set to the Rally API-Key for read only web services.");
            System.exit(-1);
        }

        String initiativeID = null;

        String outName = null;
        if (line.hasOption(OPTION_FILE)) {
            outName = line.getOptionValue(OPTION_FILE);
        }
        /*
        String[] remainingargs = line.getArgs();
        if (remainingargs.length > 0) {
            outName = remainingargs[0];
        }
        */

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

            if (line.hasOption(OPTION_RELEASE)) {
                String releaseName = line.getOptionValue(OPTION_RELEASE);
                UserStoryFinder finder = new UserStoryFinder(restApi);
                finder.setRelease(releaseName);

                storiesInReleaseList = finder.getStories();
            }

            if (line.hasOption(OPTION_INIT)) {
                initiativeID = line.getOptionValue(OPTION_INIT);
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

    static String prettyPrintJSON(JsonElement element) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(element);
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("rallyx", options, true);
        String envHelp = "\nAlso uses the following environment variables:\n" +
                "RALLY_KEY     Set to the Rally API Key   - REQUIRED\n" +
                "PROXYURL      URL of proxy (if needed) like http://myproxy.my.com:8080\n" +
                "PROXYUSER     username of authenticated proxy\n" +
                "PROXYPASS     password for authenticated proxy\n";
        System.out.print(envHelp);
    }

    private static Options setupOptions() {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_INIT).longOpt("initiative").desc("Initiative ID (like I203)")
                .required().numberOfArgs(1).optionalArg(false).argName("id").build());
        options.addOption(Option.builder(OPTION_RELEASE).longOpt("release")
                .desc("Release (like \"some release\") - REQUIRED").required().numberOfArgs(1)
                .optionalArg(false).argName("name").build());
        options.addOption(Option.builder(OPTION_TYPE).longOpt("type").desc("type of output (xmind, excel, word)")
                .numberOfArgs(1).optionalArg(false).argName("filetype").build());
        options.addOption(OPTION_NOPROXY, false, "disable proxy use even if env var set");
        options.addOption(Option.builder(OPTION_FILE).longOpt("file").desc("output filename")
                .numberOfArgs(1).optionalArg(false).argName("filename").build());
        options.addOption(OPTION_HELP, false, "display help");
        return options;
    }
}
