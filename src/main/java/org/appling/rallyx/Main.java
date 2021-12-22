package org.appling.rallyx;

import com.rallydev.rest.RallyRestApi;
import org.apache.commons.cli.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.appling.rallyx.excel.ExcelIssueWriter;
import org.appling.rallyx.excel.ExcelStoryWriter;
import org.appling.rallyx.html.HTMLWriter;
import org.appling.rallyx.miro.MiroUpdater;
import org.appling.rallyx.miro.MiroWriter;
import org.appling.rallyx.rally.*;
import org.appling.rallyx.word.WordWriter;
import org.appling.rallyx.xmind.XMindWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by sappling on 9/5/2016.
 */
public class Main {
    public static final String OPTION_PROPERTIES = "p";
    private static final String OPTION_NOPROXY = "noproxy";
    private static final String OPTION_INIT = "i";
    private static final String OPTION_PROJECT = "project";
    private static final String OPTION_RELEASE = "r";
    private static final String OPTION_TYPE = "t";
    private static final String OPTION_FILE = "f";
    private static final String OPTION_LIST = "l";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_INCOMPLETE = "incomplete";

    private static final String PROP_APIKEY = "rally_key";
    private static final String PROP_PROXYURL = "proxyurl";
    private static final String PROP_PROXYUSER = "proxyuser";
    private static final String PROP_PROXYPASS = "proxypass";
    private static final String PROP_INITIATIVE = "initiative";
    private static final String PROP_PROJECT = "project";
    private static final String PROP_RELEASE = "release";
    private static final String PROP_TYPE = "type";
    private static final String PROP_FILE = "file";
    private static final String PROP_MIRO_TOKEN = "miro.token";
    private static final String PROP_MIRO_BOARD = "miro.board";
    private static final String PROP_MIRO_FRAME = "miro.frame";
    private static final String PROP_MIRO_LINK = "miro.link";
    private static final String PROP_MIRO_UPDATE_LINK = "miro.update.link";
    private static final String PROP_MIRO_UPDATE_BOARD = "miro.update.board";
    private static final String PROP_MIRO_UPDATE_FRAME = "miro.update.frame";
    private static final String PROP_MIRO_CARD_SHOW = "miro.card.show";


    private static Options options = setupOptions();
    private static String proxy_url;
    private static String proxy_user;
    private static String proxy_pass;


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
        proxy_url = properties.getProperty(PROP_PROXYURL);
        proxy_user = properties.getProperty(PROP_PROXYUSER);
        proxy_pass = properties.getProperty(PROP_PROXYPASS);


        if (properties.containsKey(PROP_TYPE)) {
            outType = properties.getProperty(PROP_TYPE);
        }

        Optional<String> project = Optional.empty();
        if (properties.containsKey( PROP_PROJECT )) {
            project = Optional.of(properties.getProperty( PROP_PROJECT ));
        }

        boolean useProxy = (proxy_url != null);
        if (line.hasOption(OPTION_NOPROXY)) {
            useProxy = false;
        }

        String outName = null;
        if (properties.containsKey(PROP_FILE)) {
            outName = properties.getProperty(PROP_FILE);
        }



        try {

            if (outType != null) {
                if (outType.equalsIgnoreCase("xmind")) {
                    StoryStats stats = getStoryStats(properties, project, useProxy);
                    stats.printStats();
                    if (stats.getInitiative()!=null) {
                        XMindWriter xwriter = new XMindWriter(outName, stats.getStoriesInRelease());
                        RallyNodeWalker walker = new RallyNodeWalker(xwriter);
                        walker.walk(stats.getInitiative(), null, 1);
                        xwriter.addOrphans(stats.getStoriesNotInInitiative());
                        xwriter.save();
                    }
                } else if (outType.equalsIgnoreCase("html")) {
                    StoryStats stats = getStoryStats(properties, project, useProxy);
                    stats.printStats();

                    HTMLWriter htmlWriter = new HTMLWriter(new FileWriter(HTMLWriter.ensureExtention(outName, "Report.html")), stats);
                    RallyNodeWalker walker = new RallyNodeWalker(htmlWriter);
                    walker.walk(stats.getInitiative(), Boolean.TRUE, 1);
                    htmlWriter.close();
                } else if (outType.equalsIgnoreCase("word")) {
                    StoryStats stats = getStoryStats(properties, project, useProxy);
                    stats.printStats();

                    WordWriter wordWriter = new WordWriter(outName, stats);
                    RallyNodeWalker walker = new RallyNodeWalker(wordWriter);
                    walker.walk(stats.getInitiative(), Boolean.TRUE, 1);
                    wordWriter.save();
                } else if (outType.equalsIgnoreCase("excel")) {
                    StoryStats stats = getStoryStats(properties, project, useProxy);
                    stats.printStats();

                    ExcelStoryWriter excelStoryWriter = new ExcelStoryWriter(stats);
                    excelStoryWriter.write(outName);
                } else if (outType.equalsIgnoreCase("check")) {
                    StoryStats stats = getStoryStats(properties, project, useProxy);
                    stats.printStats();

                    ExcelIssueWriter issueWriter = new ExcelIssueWriter(stats);
                    issueWriter.write(outName);
                } else if (outType.equalsIgnoreCase("miro")) {
                    StoryStats stats = getStoryStats(properties, project, useProxy);
                    stats.printStats();

                    if (properties.containsKey(PROP_MIRO_UPDATE_BOARD) && properties.containsKey(PROP_MIRO_UPDATE_FRAME)) {
                        MiroUpdater updater = new MiroUpdater(stats, properties.getProperty( PROP_MIRO_TOKEN ),
                              properties.getProperty( PROP_MIRO_UPDATE_BOARD),
                              properties.getProperty( PROP_MIRO_UPDATE_FRAME ),
                              properties.getProperty( PROP_MIRO_CARD_SHOW ));
                        if (proxy_url != null) {
                            updater.setProxy(proxy_url, proxy_user, proxy_pass);
                        }
                        updater.update();


                        MiroWriter cardWriter = new MiroWriter(stats, properties.getProperty( PROP_MIRO_TOKEN ),
                              properties.getProperty( PROP_MIRO_BOARD),
                              properties.getProperty( PROP_MIRO_FRAME ),
                              properties.getProperty( PROP_MIRO_CARD_SHOW ), updater.getUpdatedNodes());
                        if (proxy_url != null) {
                            cardWriter.setProxy(proxy_url, proxy_user, proxy_pass);
                        }

                        cardWriter.writeAllInOrder();

                    } else {
                        MiroWriter cardWriter = new MiroWriter(stats, properties.getProperty( PROP_MIRO_TOKEN ),
                              properties.getProperty( PROP_MIRO_BOARD),
                              properties.getProperty( PROP_MIRO_FRAME ),
                              properties.getProperty( PROP_MIRO_CARD_SHOW ), new HashSet<>());
                        if (proxy_url != null) {
                            cardWriter.setProxy(proxy_url, proxy_user, proxy_pass);
                        }

                        cardWriter.writeAllInOrder();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static StoryStats getStoryStats(Properties properties, Optional<String> project, boolean useProxy) throws IOException, URISyntaxException {
        String rally_key = properties.getProperty(PROP_APIKEY);


        if (rally_key == null) {
            System.err.println("Error:  environment variable RALLY_KEY not defined.  This must be set to the Rally API-Key for read only web services.");
            System.exit(-1);
        }

        String initiativeID = null;

        List<RallyNode> storiesInReleaseList = null;
        List<RallyNode> defectsInReleaseList = null;
        RallyNode initiative = null;
        List<RallyNode> storiesUnderInitiativeList = null;
        List<RallyNode> defectsUnderInitiativeList = null;



        RallyRestApi restApi = new RallyRestApi(new URI("https://rally1.rallydev.com"), rally_key);
        if (useProxy) {
            if (proxy_user != null && proxy_pass != null) {
                restApi.setProxy(new URI(proxy_url), proxy_user, proxy_pass);
            }  {
                restApi.setProxy(new URI(proxy_url));
            }
        }

        String releaseName = "";
        if (properties.containsKey(PROP_RELEASE)) {
            releaseName = properties.getProperty(PROP_RELEASE);
            UserStoryFinder finder = new UserStoryFinder(restApi);
            finder.setFindComplete(!properties.containsKey(OPTION_INCOMPLETE));
            finder.setRelease(releaseName);
            finder.setProject( project );

            storiesInReleaseList = finder.getStories();
            defectsInReleaseList = finder.getDefects();
        }

        if (properties.containsKey(PROP_INITIATIVE)) {
            initiativeID = properties.getProperty(PROP_INITIATIVE);
            InitiativeNodeFinder walker = new InitiativeNodeFinder(restApi);
            walker.setFindComplete(!properties.containsKey(OPTION_INCOMPLETE));
            walker.setProject( project, "miro".equals(properties.getProperty(PROP_TYPE)));  // Todo - add an option for includeNodesOutOfProject
            initiative = walker.getInitiativeTree(initiativeID);
            storiesUnderInitiativeList = walker.getStories();
            defectsUnderInitiativeList = walker.getDefects();
        }

        /*
        if (line.hasOption(OPTION_LIST)) {
            QueryResponse response = restApi.query(RallyQueryFactory.getProjects());
            if (response.wasSuccessful()) {
                JsonArray jsonElements = response.getResults();
                for (JsonElement element : jsonElements) {
                    String name = element.getAsJsonObject().get("Name").getAsString();
                    String state = element.getAsJsonObject().get("State").getAsString();
                    String description = element.getAsJsonObject().get("Description").getAsString();
                    JsonElement parentEl = element.getAsJsonObject().get("Parent");
                    String parentName = parentEl.isJsonNull() ? "" : parentEl.getAsJsonObject().get("Name").getAsString();
                    int childCount = element.getAsJsonObject().get("Children").getAsJsonObject().get("Count").getAsInt();
                    System.out.format("%s - %s (%d) : %s\n", state, name, childCount, parentName);
                }
            }
        }
        */

            // statistics
        return new StoryStats(storiesInReleaseList, storiesUnderInitiativeList, defectsInReleaseList, defectsUnderInitiativeList, initiative, releaseName);
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

            String miroLink = propFromFile.getProperty( PROP_MIRO_LINK );
            String miroUpdateLink = propFromFile.getProperty( PROP_MIRO_UPDATE_LINK );
            extractBoardAndFrame( prop, miroLink, miroUpdateLink );
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

        if (line.hasOption( OPTION_PROJECT )) {
            prop.setProperty( PROP_PROJECT, line.getOptionValue(OPTION_PROJECT) );
        }

        return prop;
    }

    private static void extractBoardAndFrame( Properties prop, String writeLink, String updateLink )
    {
        if ( writeLink != null ) {
            List< String > segments = URLEncodedUtils.parsePathSegments( writeLink );
            List< NameValuePair > queries = URLEncodedUtils.parse( segments.get(segments.size()-1), Charset.defaultCharset() );

            if (segments.size() >= 6) {
                prop.put(PROP_MIRO_BOARD, segments.get( 5 ));
            }
            if (queries.size() >= 2) {
                NameValuePair pair = queries.get( 0 );
                prop.put(PROP_MIRO_FRAME, pair.getValue());
            }
        }
        if ( updateLink != null ) {
            List< String > segments = URLEncodedUtils.parsePathSegments( updateLink );
            List< NameValuePair > queries = URLEncodedUtils.parse( segments.get(segments.size()-1), Charset.defaultCharset() );

            if (segments.size() >= 6) {
                prop.put(PROP_MIRO_UPDATE_BOARD, segments.get( 5 ));
            }
            if (queries.size() >= 2) {
                NameValuePair pair = queries.get( 0 );
                prop.put(PROP_MIRO_UPDATE_FRAME, pair.getValue());
            }
        }
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
        options.addOption(Option.builder(OPTION_TYPE).longOpt(PROP_TYPE).desc("type of output (xmind, excel, word, check, miro)")
                .numberOfArgs(1).optionalArg(false).argName("filetype").build());
        options.addOption(OPTION_NOPROXY, false, "disable proxy use even if env var set");
        options.addOption(Option.builder(OPTION_FILE).longOpt(PROP_FILE).desc("output filename")
                .numberOfArgs(1).optionalArg(false).argName("filename").build());
        options.addOption(Option.builder(OPTION_PROPERTIES).longOpt("properties").desc("properties file with options")
                .numberOfArgs(1).optionalArg(false).argName("propfile").build());
        options.addOption(Option.builder(OPTION_PROJECT).desc("only use User Stories in this project")
              .numberOfArgs(1).optionalArg(false).argName("projectName").build());
        options.addOption(OPTION_INCOMPLETE,false,"Only use incomplete stories and defects");
        // options.addOption(OPTION_LIST, false, "List releases");
        options.addOption(OPTION_HELP, false, "display help");
        return options;
    }
}
