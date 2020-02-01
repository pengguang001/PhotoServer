package pengguang.photoserver;

import org.apache.log4j.Logger;

public class Log {
	private static Logger logger = Logger.getLogger("photo");
	public static void d(String tag, String s) {
		logger.debug(tag+" - "+s);
	}
	public static void d(String s) {
		logger.debug(s);
	}
	public static void d(String phone, String id, String s) {
		d(String.format("%s(%s)", phone, id), s);
	}
}
