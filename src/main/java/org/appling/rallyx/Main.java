package org.appling.rallyx;

import com.google.gson.*;
import com.rallydev.rest.RallyRestApi;
import org.apache.commons.cli.*;
import org.appling.rallyx.rally.InitiativeNodeFinder;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.RallyNodeWalker;
import org.appling.rallyx.rally.UserStoryFinder;
import org.xmind.core.CoreException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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


        String rally_key = System.getenv("RALLY_KEY");
        if (rally_key == null) {
            System.err.println("Error:  environment variable RALLY_KEY not defined.  This must be set to the Rally API-Key for read only web services.");
            System.exit(-1);
        }

        String initiativeID = null;

        String outName = null;
        String[] remainingargs = line.getArgs();
        if (remainingargs.length > 0) {
            outName = remainingargs[0];
        }

        List<RallyNode> storiesInReleaseList = null;
        RallyNode initiative = null;
        List<RallyNode> storiesUnderInitiativeList = null;

        XMindWriter hwriter = null;
        try {
            RallyRestApi restApi = new RallyRestApi(new URI("https://rally1.rallydev.com"), rally_key);
            if (useProxy) {
                restApi.setProxy(new URI("http://servproxy.utc.com:8080"), "foo", "bar");
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

            Set<RallyNode> storiesInReleaseSet = new HashSet<>();
            Set<RallyNode> storiesUnderInitiativeSet = new HashSet<>();
            Set<RallyNode> storiesNotInInitiative = new HashSet<>();
            Set<RallyNode> storiesNotInRelease = new HashSet<>();
            Set<RallyNode> storiesInNoRelease = new HashSet<>();
            Set<RallyNode> allStories = new HashSet<>();

            if (storiesInReleaseList != null) {
                storiesInReleaseSet = new HashSet<>(storiesInReleaseList);
            }
            if (storiesUnderInitiativeList != null) {
                storiesUnderInitiativeSet = new HashSet<>(storiesUnderInitiativeList);
            }

            // find all stories in the release that are not in the initiative
            storiesNotInInitiative = new HashSet<>(storiesInReleaseSet);
            storiesNotInInitiative.removeAll(storiesUnderInitiativeSet);

            // find all stories in the initiative that are not in the release
            storiesNotInRelease = new HashSet<>(storiesUnderInitiativeSet);
            storiesNotInRelease.removeAll(storiesInReleaseSet);
            storiesNotInRelease = removeParents(storiesNotInRelease);

            // find stories in no release
            storiesInNoRelease = storiesNotInRelease.stream()
                .filter(s -> s.getRelease().isEmpty())
                .collect(Collectors.toSet());

            // all stories
            allStories = new HashSet<>(storiesUnderInitiativeSet);
            allStories.addAll(storiesNotInInitiative);

            //statistics
            System.out.format("%d stories total\n", allStories.size());
            System.out.format("%d stories not in initiative\n", storiesNotInInitiative.size());
            System.out.format("%d stories not in specified release\n", storiesNotInRelease.size());
            System.out.format("%d stories in no release\n", storiesInNoRelease.size());

            storiesInNoRelease.forEach(System.out::println);

            if (outName != null && initiative != null ) {
                XMindWriter writer = new XMindWriter(outName, storiesInReleaseSet);
                RallyNodeWalker walker = new RallyNodeWalker(writer);
                walker.walk(initiative, null, 1);
                writer.addOrphans(storiesNotInInitiative);
                try {
                    writer.close();
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }

                        /*

            if (hwriter != null) {
                hwriter.addOrphans(releaseStories.values());
            }
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

    private static Set<RallyNode> removeParents(Set<RallyNode> set) {
        HashSet<RallyNode> results = new HashSet<>();
        for (RallyNode rallyNode : set) {
            if (!rallyNode.hasChildren()) {
                results.add(rallyNode);
            }
        }
        return results;
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
