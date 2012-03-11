package cz.pavel.uudownloader.utils;

import org.apache.log4j.Logger;

public class LogUtils {
	public static final Logger getLogger() {
		StackTraceElement [] ste = Thread.currentThread().getStackTrace();
		return Logger.getLogger(ste[2].getClassName());
	}

}
