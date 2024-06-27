package utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil
{
	private DateUtil()
	{
	}
	public static String getCurrentTimestamp()
	{
		var simpleFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		var date = new Date();
		return simpleFormat.format(date);
	}
}
