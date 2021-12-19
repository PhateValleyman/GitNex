package org.mian.gitnex.helpers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author qwerty287
 */
public class AppUtilTest {

	@Test
	public void getFileType() {
		assertEquals(AppUtil.FileType.AUDIO, AppUtil.getFileType("mp3"));
		assertEquals(AppUtil.FileType.IMAGE, AppUtil.getFileType("png"));
		assertEquals(AppUtil.FileType.EXECUTABLE, AppUtil.getFileType("deb"));
		assertEquals(AppUtil.FileType.TEXT, AppUtil.getFileType("JSON"));
		assertEquals(AppUtil.FileType.DOCUMENT, AppUtil.getFileType("PDF"));
	}

	@Test
	public void checkStringsWithAlphaNumeric() {
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("string"), true);
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("123"), true);
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("123 with string"), false);
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("string 123"), false);
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("string-123"), false);
	}

	@Test
	public void checkIntegers() {
		assertEquals(AppUtil.checkIntegers("string"), false);
		assertEquals(AppUtil.checkIntegers("123"), true);
		assertEquals(AppUtil.checkIntegers("123 with string"), false);
		assertEquals(AppUtil.checkIntegers("string 123"), false);
	}

	@Test
	public void parseSSHUrl() {
		assertEquals("codeberg.org", AppUtil.parseSSHUrl("ssh://git@codeberg.org:gitnex/GitNex"));
		assertEquals("codeberg.org", AppUtil.parseSSHUrl("codeberg.org:gitnex/GitNex"));
		// this would fail because it does not return the host, but with this scheme Uri.parse(url).getHost() returns the correct value
		assertEquals("codeberg.org/gitnex/GitNex", AppUtil.parseSSHUrl("ssh://git@codeberg.org/gitnex/GitNex"));
		assertEquals("codeberg.org", AppUtil.parseSSHUrl("ssh://git@codeberg.org:gitnex/GitNex.git"));
		assertEquals("codeberg.org", AppUtil.parseSSHUrl("codeberg.org:gitnex/GitNex.git"));
	}

}
