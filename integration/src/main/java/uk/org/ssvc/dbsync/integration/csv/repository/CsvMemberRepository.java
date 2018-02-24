package uk.org.ssvc.dbsync.integration.csv.repository;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CsvMemberRepository implements MemberRepository {

    private final LinkedHashMap<String, Member> members = new LinkedHashMap<>();

    private final CsvMemberDataLoader csvMemberDataLoader;
    private final MemberParser memberParser;

    @Inject
    public CsvMemberRepository(CsvMemberDataLoader csvMemberDataLoader,
                               MemberParser memberParser) {
        this.csvMemberDataLoader = csvMemberDataLoader;
        this.memberParser = memberParser;
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(lazyLoadMembers().values());
    }

    @Override
    public Member findById(String id) {
        return lazyLoadMembers().get(id);
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

    private LinkedHashMap<String, Member> lazyLoadMembers() {
        if (members.isEmpty()) {
            try {
                log.info("Loading members from CSV format...");
                members.putAll(new CSVParser(
                    csvMemberDataLoader.loadData(),
                    CSVFormat.DEFAULT.withFirstRecordAsHeader())
                    .getRecords()
                    .stream()
                    .map(memberParser::parse)
                    .collect(Collectors.toMap(Member::getId, identity(), (u, v) -> {
                        throw new IllegalStateException(String.format("Duplicate key %s", u));
                    }, LinkedHashMap::new)));
                log.info("Parsed {} members", members.size());
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to parse member CSV", e);
            }
        }

        return members;
    }

}
