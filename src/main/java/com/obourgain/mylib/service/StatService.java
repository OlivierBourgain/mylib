package com.obourgain.mylib.service;

import com.obourgain.mylib.util.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatService {
    private static final Logger log = LoggerFactory.getLogger(StatService.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StatService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Return a Map containing all stats for the library.
     * <p>
     * It contains :
     * - booksByTag and pagesByTag
     * - booksByAuthor and pagesByAuthor
     * - booksByYear and pagesByYear
     * - booksByMonth and pagesByMonth
     *
     * @param userId        The user Id
     * @param showDiscarded if true, includes discarded books.
     * @param year          The year selected, or null.
     */
    public Map<String, List<StatData>> getAllStat(String userId, Boolean showDiscarded, Integer year) {

        Map<String, List<StatData>> res = new HashMap<>();

        res.put("booksByTag", top10(getStatDetail(userId, showDiscarded, year, "booksByTag")));
        res.put("pagesByTag", top10(getStatDetail(userId, showDiscarded, year, "pagesByTag")));
        res.put("booksByAuthor", top10(getStatDetail(userId, showDiscarded, year, "booksByAuthor")));
        res.put("pagesByAuthor", top10(getStatDetail(userId, showDiscarded, year, "pagesByAuthor")));

        List<StatData> booksByYear = jdbcTemplate.query(SQL_READ_YEAR, new StatRowMapper("YEAR", "NB"), userId, year, year);
        List<StatData> pagesByYear = jdbcTemplate.query(SQL_READ_YEAR, new StatRowMapper("YEAR", "PAGES"), userId, year, year);
        booksByYear.sort(Comparator.comparing(StatData::getKey));
        pagesByYear.sort(Comparator.comparing(StatData::getKey));
        res.put("booksByYear", booksByYear);
        res.put("pagesByYear", pagesByYear);

        List<StatData> booksByMonth = jdbcTemplate.query(SQL_READ_MONTH, new StatRowMapper("MONTH", "NB"), userId, year, year);
        List<StatData> pagesByMonth = jdbcTemplate.query(SQL_READ_MONTH, new StatRowMapper("MONTH", "PAGES"), userId, year, year);
        res.put("booksByMonth", toMonthStat(booksByMonth));
        res.put("pagesByMonth", toMonthStat(pagesByMonth));

        log.info(res.toString());
        return res;
    }

    private static String SQL_TAG = SqlUtils.readSql(StatService.class.getResourceAsStream("/sql/stats/TopByTag.sql"));
    private static String SQL_AUTHOR = SqlUtils.readSql(StatService.class.getResourceAsStream("/sql/stats/TopByAuthor.sql"));
    private static String SQL_READ_YEAR = SqlUtils.readSql(StatService.class.getResourceAsStream("/sql/stats/ReadPerYear.sql"));
    private static String SQL_READ_MONTH = SqlUtils.readSql(StatService.class.getResourceAsStream("/sql/stats/ReadPerMonth.sql"));

    /**
     * Return the data for one given stat.
     */
    private List<StatData> innerStatDetail(String userId, Boolean showDiscarded, Integer year, String statName) {
        log.info("Getting stat for year " + year);
        var discardedFlag = showDiscarded ? 1 : 0;
        return switch (statName.toLowerCase()) {
            case "booksbytag" -> jdbcTemplate.query(SQL_TAG, new StatRowMapper("TAG", "NB"), userId, discardedFlag, year, year);
            case "pagesbytag" -> jdbcTemplate.query(SQL_TAG, new StatRowMapper("TAG", "PAGES"), userId, discardedFlag, year, year);
            case "booksbyauthor" -> jdbcTemplate.query(SQL_AUTHOR, new StatRowMapper("AUTHOR", "NB"), userId, discardedFlag, year, year);
            case "pagesbyauthor"-> jdbcTemplate.query(SQL_AUTHOR, new StatRowMapper("AUTHOR", "PAGES"), userId, discardedFlag, year, year);
            default -> throw new IllegalArgumentException("Stat doesn't exist " + statName);
        };
    }

    /**
     * Return the data for one given stat.
     */
    public List<StatData> getStatDetail(String userId, Boolean showDiscarded, Integer year, String statName) {
        return innerStatDetail(userId, showDiscarded, year, statName)
                .stream()
                .sorted(Comparator.comparing(StatData::getValue).reversed())
                .limit(30)
                .collect(Collectors.toList());
    }


    private List<StatData> top10(List<StatData> list) {
        return top(list, 10);
    }

    /**
     * Return the top of a given list of StatData (ordered by value desc).
     */
    private List<StatData> top(List<StatData> list, int nb) {
        return list.stream()
                .sorted(Comparator.comparing(StatData::getValue).reversed())
                .limit(nb)
                .collect(Collectors.toList());
    }


    private List<StatData> toMonthStat(List<StatData> datas) {
        List<StatData> res = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String month = new DateFormatSymbols().getMonths()[i - 1];
            StatData sd = new StatData(month, 0);
            res.add(sd);
        }
        for (StatData data : datas) {
            int month = Integer.parseInt(data.key);
            res.get(month - 1).setValue(data.value);
        }
        return res;
    }


    public static class StatRowMapper implements RowMapper<StatData> {
        String key;
        String val;

        public StatRowMapper(String key, String val) {
            this.key = key;
            this.val = val;
        }

        @Override
        public StatData mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new StatData(rs.getString(key), rs.getInt(val));
        }
    }

    /**
     * Represents a stat (e.g. Nb of books per tag).
     */
    public static class StatData {
        public String key;
        public int value;

        public StatData(String key, int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

    }

}
