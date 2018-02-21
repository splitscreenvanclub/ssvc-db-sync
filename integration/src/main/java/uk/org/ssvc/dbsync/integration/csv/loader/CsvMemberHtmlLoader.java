package uk.org.ssvc.dbsync.integration.csv.loader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import uk.org.ssvc.dbsync.integration.service.MemberDatabaseHtmlService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Singleton
public class CsvMemberHtmlLoader implements CsvMemberDataLoader {

    private final MemberDatabaseHtmlService memberDatabaseHtmlService;

    @Inject
    public CsvMemberHtmlLoader(MemberDatabaseHtmlService memberDatabaseHtmlService) {
        this.memberDatabaseHtmlService = memberDatabaseHtmlService;
    }

    @Override
    public Reader loadData() {
        StringBuilder csvContent = new StringBuilder();

        String activeMembersHtml = memberDatabaseHtmlService.fetchHtmlForActiveMembersPage();
        String lapsedMembersHtml = memberDatabaseHtmlService.fetchHtmlForLapsedMembersPage();

        List<String> activeLines = memberCsvLines(activeMembersHtml);
        List<String> lapsedLines = memberCsvLines(lapsedMembersHtml);

        // Remove header (that should be same as active):
        lapsedLines.remove(0);

        activeLines.forEach(csvContent::append);
        lapsedLines.forEach(csvContent::append);

        return new StringReader(csvContent.toString());
    }

    private List<String> memberCsvLines(String html) {
        List<String> csvLines = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        doc.select("table[width=\"100%\"] tr").forEach(rowElement -> {
            StringBuilder line = new StringBuilder();

            rowElement.select("td").forEach(cellElement ->
                line.append(escapeValue(cellElement.text())).append(","));

            csvLines.add(line.append("\n").toString());
        });

        return csvLines;
    }

    public String escapeValue(String value) {
        return isBlank(value) ? value : "\"" + value.replaceAll("\"", "\"\"") + "\"";
    }

}
