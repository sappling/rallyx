package org.appling.rallyx;

import com.google.gson.*;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import org.apache.commons.cli.*;
import org.appling.rallyx.rally.RallyQueryFactory;
import org.xmind.core.CoreException;
import org.xmind.core.ITopic;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Created by sappling on 9/5/2016.
 */
public class Main {
    private static final String OPTION_NOPROXY = "noproxy";
    private static final String OPTION_INIT = "i";
    private static final String OPTION_RELEASE = "r";
    private static final String OPTION_OUTFILE = "outfile";
    private static final String OPTION_HELP = "help";

    private static Options options = setupOptions();


    static public void main(String args[]) {
        CommandLineParser parser = new DefaultParser();
        boolean useProxy = true;
        CommandLine line = null;
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

        if (line.hasOption(OPTION_NOPROXY)) {
            useProxy = false;
        }

        if (line.hasOption(OPTION_RELEASE)) {
            String release = line.getOptionValue(OPTION_RELEASE);
        }

        String rally_key = System.getenv("RALLY_KEY");
        if (rally_key == null) {
            System.err.println("Error:  environment variable RALLY_KEY not defined.  This must be set to the Rally API-Key for read only web services.");
            System.exit(-1);
        }

        String initiativeID = null;
        if (line.hasOption(OPTION_INIT)) {
            initiativeID = line.getOptionValue(OPTION_INIT);
        }

        String outName = null;
        String[] remainingargs = line.getArgs();
        if (remainingargs.length > 0) {
            outName = remainingargs[0];
        }

        XMindWriter hwriter = null;
        try {
            RallyRestApi restApi = new RallyRestApi(new URI("https://rally1.rallydev.com"), rally_key);
            if (useProxy) {
                restApi.setProxy(new URI("http://servproxy.utc.com:8080"), "foo", "bar");
            }

            if (line.hasOption(OPTION_RELEASE)) {
                String releaseName = line.getOptionValue(OPTION_RELEASE);
                QueryResponse response = restApi.query(RallyQueryFactory.findStoriesInRelease(releaseName));
                if (response.wasSuccessful()) {
                    JsonArray results = response.getResults();
                    for (JsonElement result : results) {
                        JsonObject obj = result.getAsJsonObject();
                        String id = obj.get("ObjectID").getAsString();
                        /*
                        System.out.format("%s - %s\n",
                                id,
                                obj.get("Name").getAsString());
                        */
                        releaseStories.put(id, obj);
                    }

                }
            }

            if (outName != null && initiativeID!=null) {
                QueryResponse queryResponse = restApi.query(RallyQueryFactory.findInitiative(initiativeID));

                if (queryResponse.wasSuccessful()) {
                    JsonArray resultArray = queryResponse.getResults();
                    JsonElement jsonInitiative = resultArray.get(0);

                    hwriter = new XMindWriter(outName);
                    RallySortedTreeWalker walker = new RallySortedTreeWalker(restApi, releaseStories);

                    walker.walk(jsonInitiative.getAsJsonObject(), null, hwriter, 1);
                } else {
                    System.out.println("Error:");
                    String[] errors = queryResponse.getErrors();
                    for (String error : errors) {
                        System.out.println(error);
                    }
                }
            }

            System.out.format("Found %d user stores not under the iteration %s:\n", releaseStories.size(), initiativeID);
            if (hwriter != null) {
                hwriter.addOrphans(releaseStories.values());
            }
            /*
            for (JsonObject object : releaseStories.values()) {
                String formattedID = object.get("FormattedID").getAsString();
                String name = object.get("Name").getAsString();
                System.out.format("%s - %s\n", formattedID, name);
            }
            */
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (hwriter != null) {
                try {
                    hwriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static String prettyPrintJSON(JsonElement element) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(element);
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("rallyx", options, true);
    }

    private static Options setupOptions() {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_INIT).longOpt("initiative").desc("Initiative ID (like I203) - REQUIRED")
                .numberOfArgs(1).optionalArg(false).argName("id").build());
        options.addOption(Option.builder(OPTION_RELEASE).longOpt("release")
                .desc("Release (like \"some release\") - REQUIRED").required().numberOfArgs(1)
                .optionalArg(false).argName("name").build());
        options.addOption(OPTION_NOPROXY, false, "disable proxy use (defaults to UTC proxy)");
        options.addOption(OPTION_HELP, false, "display help");
        return options;
    }
}
