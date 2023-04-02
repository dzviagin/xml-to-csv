package docdvz.xmlparser;


import org.apache.commons.cli.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    private static final boolean DEBUG_PRINT = false;

    public static void main(String[] args) {
        Options options = options();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            String target = cmd.getOptionValue("t");
            String path = cmd.getOptionValue("f");
            write(new File(path), target);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options);
            System.exit(0);
        }
    }

    private static Options options() {
        Options options = new Options();
        options.addOption(Option.builder("f")
                        .argName("file")
                        .longOpt("file")
                        .hasArg()
                        .required()
                        .desc("Path to XML file")
                .build());
        options.addOption(Option.builder("t")
                        .argName("target")
                        .longOpt("target")
                        .hasArg()
                        .required()
                        .desc("Xml node name to extract")
                .build());
        return options;
    }

    private static void write(File file, String tag) throws Exception {
        BillingParser parser = new BillingParser(file, tag);
        parser.parse();
        List<String> fields = new ArrayList<>(parser.getFields());
        fields.sort(String::compareTo);
        List<Map<String, String>> data = parser.getResult();
        log(fields);
        String fileName = file.getName().split("\\.")[0] + "_" + tag + ".csv";
        File csvOutputFile = new File(fileName);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.println(String.join(";", fields));
            for (var d : data) {
                StringBuilder sb = new StringBuilder();
                for (var field : fields) {
                    sb.append(d.get(field)).append(";");
                }
                pw.println(sb);
                log(sb);
            }
        }
    }

    private static void log(Object s) {
        if (DEBUG_PRINT) {
            System.out.println(s.toString());
        }
    }
}
