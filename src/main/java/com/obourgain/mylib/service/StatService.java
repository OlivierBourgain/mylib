package com.obourgain.mylib.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.obourgain.mylib.util.SqlUtils;

@Service
public class StatService {
	private static final Logger log = LogManager.getLogger(StatService.class);

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public StatService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
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

	/**
	 * Return a Map containing all stats for the library.
	 * 
	 * It contains : - booksByTag - pagesByTag - booksByAuthor - pagesByAuthor
	 * 
	 * @param showDiscarded
	 */
	public Map<String, List<StatData>> getAllStat(String userId, Boolean showDiscarded) {

		Map<String, List<StatData>> res = new HashMap<>();

		String sqlTag = SqlUtils.readSql(this.getClass().getResourceAsStream("/sql/stats/TopByTag.sql"));
		List<Map<String, Object>> byTag = jdbcTemplate.queryForList(sqlTag, userId, showDiscarded ? 1 : 0);
		log.info(byTag);
		res.put("booksByTag", toStat(byTag, x -> x.get("TAG").toString(), x -> Integer.parseInt(x.get("NB").toString())));
		res.put("pagesByTag", toStat(byTag, x -> x.get("TAG").toString(), x -> Integer.parseInt(x.get("PAGES").toString())));

		String sqlAuthor = SqlUtils.readSql(this.getClass().getResourceAsStream("/sql/stats/TopByAuthor.sql"));
		List<Map<String, Object>> byAuthor = jdbcTemplate.queryForList(sqlAuthor, userId, showDiscarded ? 1 : 0);
		log.info(byAuthor);
		res.put("booksByAuthor", toStat(byAuthor, x -> x.get("AUTHOR").toString(), x -> Integer.parseInt(x.get("NB").toString())));
		res.put("pagesByAuthor", toStat(byAuthor, x -> x.get("AUTHOR").toString(), x -> Integer.parseInt(x.get("PAGES").toString())));

		String sqlReadYear = SqlUtils.readSql(this.getClass().getResourceAsStream("/sql/stats/ReadPerYear.sql"));
		List<Map<String, Object>> readYear = jdbcTemplate.queryForList(sqlReadYear, userId);
		log.info(readYear);

		String sqlReadMonth = SqlUtils.readSql(this.getClass().getResourceAsStream("/sql/stats/ReadPerMonth.sql"));
		List<Map<String, Object>> readMonth = jdbcTemplate.queryForList(sqlReadMonth, userId);
		log.info(readMonth);

		log.info(res);
		return res;
	}

	/**
	 * Extract the List of Stat from the Raw result of the SQL request.
	 */
	private List<StatData> toStat(List<Map<String, Object>> datas, Function<Map<String, Object>, String> keyFunc,
			Function<Map<String, Object>, Integer> valueFunc) {
		return datas
				.stream()
				.map(line -> new StatData(keyFunc.apply(line), valueFunc.apply(line)))
				.sorted(Comparator.comparing(StatData::getValue).reversed())
				.limit(10)
				.collect(Collectors.toList());
	}

}
