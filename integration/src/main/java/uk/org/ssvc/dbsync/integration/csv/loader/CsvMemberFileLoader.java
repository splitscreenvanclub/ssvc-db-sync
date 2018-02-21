package uk.org.ssvc.dbsync.integration.csv.loader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

@Singleton
public class CsvMemberFileLoader implements CsvMemberDataLoader {

    private final String filePath;

    @Inject
    public CsvMemberFileLoader(@Named("member.csv.file") String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Reader loadData() {
        try {
            return new InputStreamReader(new FileInputStream(filePath));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to read CSV", e);
        }
    }

}
