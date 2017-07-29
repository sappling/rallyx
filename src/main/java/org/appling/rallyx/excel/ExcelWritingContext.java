package org.appling.rallyx.excel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Workbook;
import org.appling.rallyx.rally.RallyNode;
import org.appling.rallyx.reports.Issue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sappling on 7/29/2017.
 */
public class ExcelWritingContext {
    private int rank = 0;
    private CellStyle linkStyle;
    private CellStyle errorStyle;
    private RallyNode node;
    private List<Issue> issues = Collections.emptyList();
    private Workbook workbook;
    private CreationHelper creationHelper;

    public ExcelWritingContext(Workbook workbook) {
        creationHelper = workbook.getCreationHelper();
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public CellStyle getLinkStyle() {
        return linkStyle;
    }

    public void setLinkStyle(CellStyle linkStyle) {
        this.linkStyle = linkStyle;
    }

    public CellStyle getErrorStyle() {
        return errorStyle;
    }

    public void setErrorStyle(CellStyle errorStyle) {
        this.errorStyle = errorStyle;
    }

    @Nullable
    public RallyNode getNode() {
        return node;
    }

    public void setNode(RallyNode node) {
        this.node = node;
    }

    @NotNull
    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public void setSingleIssue(Issue issue) {
        issues = new ArrayList<>();
        issues.add(issue);
    }

    public Hyperlink createHyperlink() {
        return creationHelper.createHyperlink(HyperlinkType.URL);
    }
}
