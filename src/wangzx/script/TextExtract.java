package wangzx.script;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.h2.tools.SimpleResultSet;
import org.h2.value.ValueResultSet;

/**
 * @author wangzaixiang
 * 
 *  provide a H2 helper function: textextract(filename, regular-pattern)
 *  so you can simple using:
 * 
 * 	create alias textextract for "wangzx.script.TextExtract.textExtract"
 *  
 *  select * from textextract('file.txt',
 *         $$(?<year>\d{4})-(?<month>\d{2})-(?<day>\d{2})$$)
 * 
 */
public class TextExtract {

	/**
	 * options like "encoding=gbk,not-match=error/ignore"
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static ResultSet textExtract(String filename, String pattern, String options) throws SQLException, IOException{
		
		Map<String, String> optionMap = new HashMap<String, String>();
		if(options != null){
			StringTokenizer st = new StringTokenizer(options, ",");
			while(st.hasMoreTokens()){
				String option = st.nextToken();
				String[] split = option.split("=");
				if(split.length==2){
					String name = split[0].trim();
					String value = split[1].trim();
					optionMap.put(name, value);
				}
			}
		}
		return run(filename, pattern, optionMap);
	}
	
	public static ResultSet textExtract(String filename, String pattern) throws SQLException, IOException{		
		return run(filename, pattern, new HashMap<String,String>());
	}
	
	private static ResultSet run(String filename, String pattern, Map<String, String> options) throws SQLException, IOException {

		Pattern re = Pattern.compile(pattern);

		String[] names = getNames(re);
		if (names == null)
			throw new SQLException("text extract expect JDK7 since it using the new Pattern's name group feature");

		if (names.length == 0)
			throw new SQLException("invalid pattern, no named group defind");

		String encoding = System.getProperty("file.encoding");
		if(options.containsKey("encoding"))
			encoding = options.get("encoding");
		
		boolean ignoreNotMatched = true;
		if("error".equalsIgnoreCase(options.get("not-match")))
			ignoreNotMatched = false;
		
		try (	FileInputStream fin = new FileInputStream(filename);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fin, encoding))) {
			
			SimpleResultSet rs = new SimpleResultSet();
			for (String name : names)
				rs.addColumn(name, Types.VARCHAR, 8092, 0);

			int lineno = 0;
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;

				lineno++;
				Matcher matcher = re.matcher(line);
				if (matcher.matches() == false) {
					if(ignoreNotMatched)
						continue;
					else
						throw new SQLException("line:" + lineno + " not matched the pattern");
				}

				Object[] columns = new String[names.length];
				int i = 0;
				for (String name : names)
					columns[i++] = matcher.group(name);

				rs.addRow(columns);

			}

			return rs;
		}

	}

	private static String[] getNames(Pattern p) {
		try {
			Method method = Pattern.class.getDeclaredMethod("namedGroups");
			method.setAccessible(true);

			Map<String, Integer> result = (Map<String, Integer>) method.invoke(p);
			SortedMap<Integer, String> positions = new TreeMap<>();
			for (String key : result.keySet())
				positions.put(result.get(key), key);

			return positions.values().toArray(new String[positions.size()]);
		} catch (NoSuchMethodException ex) {
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
