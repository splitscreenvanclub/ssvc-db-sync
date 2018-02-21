package uk.org.ssvc.dbsync.integration.csv.repository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import uk.org.ssvc.core.domain.model.member.Member;
import uk.org.ssvc.core.domain.model.member.search.MemberFilterCriteria;
import uk.org.ssvc.core.domain.repository.MemberRepository;
import uk.org.ssvc.dbsync.integration.csv.loader.CsvMemberDataLoader;
import uk.org.ssvc.dbsync.integration.csv.parser.MemberParser;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Singleton
public class CsvMemberRepository implements MemberRepository {

    private final LinkedHashMap<String, Member> members;

    @Inject
    public CsvMemberRepository(CsvMemberDataLoader csvMemberDataLoader,
                               MemberParser memberParser) {
        try {
            members = new CSVParser(
                csvMemberDataLoader.loadData(),
                CSVFormat.DEFAULT.withFirstRecordAsHeader())
                    .getRecords()
                    .stream()
                    .map(memberParser::parse)
                    .collect(Collectors.toMap(Member::getId, identity(), (u, v) -> {
                        throw new IllegalStateException(String.format("Duplicate key %s", u));
                    }, LinkedHashMap::new));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse member CSV", e);
        }
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(members.values());
    }

    @Override
    public Member findById(String id) {
        return members.get(id);
    }

    @Override
    public List<Member> findByCriteria(MemberFilterCriteria filterCriteria) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Member member) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAll(Collection<Member> members) {
        throw new UnsupportedOperationException();
    }

}
