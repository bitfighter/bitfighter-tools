package org.bitfighter.logbot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {
	public static String convertStreamToString(InputStream is) {
		StringBuilder sb = null;
		char[] tmp = new char[ 4096 ];

		try {
			sb = new StringBuilder( Math.max( 16, is.available() ) );
			InputStreamReader reader = new InputStreamReader( is, "UTF-8" );
			for( int cnt; ( cnt = reader.read( tmp ) ) > 0; )
				sb.append( tmp, 0, cnt );
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
