package org.mian.gitnex.helpers;

/**
 * Author 6543
 */

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

	@Test
	public void equal() {
		Assert.assertFalse(new Version("1.12.0").equals("1.12.0"));
		Assert.assertFalse(new Version("1.12.0").equals(new Version("1.12.0")));
		Assert.assertFalse(new Version("1.12.0").equals("1.12"));
		Assert.assertFalse(new Version("1.12.0").equals("1.12.0+dev-211-g316db0fe7"));
		Assert.assertFalse(new Version("1.12.0").equals("v1.12"));
		Assert.assertFalse(new Version("v1.12.0").equals("1.12.0"));

	}

}
