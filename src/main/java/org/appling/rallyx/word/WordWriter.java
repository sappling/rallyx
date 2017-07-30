package org.appling.rallyx.word;

import org.appling.rallyx.WalkAction;
import org.appling.rallyx.html.HTMLWriter;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.rally.StoryStats;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sappling on 7/30/2017.
 */
public class WordWriter implements WalkAction {
    private HTMLWriter htmlWriter;
    private StringWriter html;
    private String outName;

    public WordWriter(String outName, StoryStats stats) {
        html = new StringWriter();
        this.htmlWriter = new HTMLWriter(html, stats);
        this.outName = ensureExtention(outName, "Report.docx");
    }

    @Override
    public Object act(RallyNode node, Object parentNative, int depth) {
        return htmlWriter.act(node, parentNative, depth);
    }

    public void save() throws IOException {
        htmlWriter.close();

        InputStream stream = WordWriter.class.getResourceAsStream("/styles.docx");
        WordprocessingMLPackage wordMLPackage = null;
        try {
            wordMLPackage = Docx4J.load(stream);
            MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
            documentPart.addAltChunk(AltChunkType.Html, html.toString().getBytes());
            wordMLPackage.save(new File(outName));

        } catch (Docx4JException e) {
            throw new IOException(e);
        }

    }

    protected String ensureExtention(String outName, String defaultName) {
        String result = outName;
        if (outName == null || outName.length()==0) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd-");
            result = fmt.format(new Date())+defaultName;
        } else if (!outName.endsWith(".docx")) {
            result += ".docx";
        }
        return result;
    }
}
