package de.mklinger.micro.mediatype;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class MediaTypeTest {
	@Test
	public void valueOfErrorTest() {
		expectParseException(() -> MediaType.valueOf(""));
		expectParseException(() -> MediaType.valueOf("/"));
		expectParseException(() -> MediaType.valueOf("x/"));
		expectParseException(() -> MediaType.valueOf("/y"));
		expectParseException(() -> MediaType.valueOf("*/x"));
		expectParseException(() -> MediaType.valueOf("x/ y"));
		expectParseException(() -> MediaType.valueOf("x /y"));
		expectParseException(() -> MediaType.valueOf("x/y;"));
		expectParseException(() -> MediaType.valueOf("x/y;a"));
		expectParseException(() -> MediaType.valueOf("x/y;a="));
		expectParseException(() -> MediaType.valueOf("x/y;=x"));
		expectParseException(() -> MediaType.valueOf("x/y;="));
		expectParseException(() -> MediaType.valueOf("x/y;a=\"xxx"));
		expectParseException(() -> MediaType.valueOf("x/x/y"));
		expectParseException(() -> MediaType.valueOf("x:x/y"));
		expectParseException(() -> MediaType.valueOf("x x/y"));
		expectParseException(() -> MediaType.valueOf("x/y:y"));
		expectParseException(() -> MediaType.valueOf("x/y y"));
		expectParseException(() -> MediaType.valueOf("böärghh/hui"));
		expectParseException(() -> MediaType.valueOf("hui/böärghh"));
	}

	private static void expectParseException(final Runnable r) {
		try {
			r.run();
			Assert.fail("Expected exception not thrown");
		} catch (final MediaTypeParseException e) {
			// expected
		}
	}

	@Test
	public void constructorTest() {
		Assert.assertEquals("*/*", new MediaType(null, null).toString());
		Assert.assertEquals("foo/bar", new MediaType("foo", "bar").toString());
		Assert.assertEquals("foo/bar", new MediaType("foo", "bar", null).toString());
		Assert.assertEquals("foo/bar", new MediaType("foo", "bar", Collections.emptyMap()).toString());
		Assert.assertEquals("foo/bar;x=y", new MediaType("foo", "bar", Collections.singletonMap("x", "y")).toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorTestError1() {
		new MediaType(null, "bar");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorTestError2() {
		new MediaType("*", "bar");
	}

	@Test
	public void valueOfTest() {
		Assert.assertEquals("*/*", MediaType.valueOf("*/*").toString());
		Assert.assertEquals("foo/bar", MediaType.valueOf("foo/bar").toString());
		Assert.assertEquals("foo/bar;x=a", MediaType.valueOf("foo/bar;x=a").toString());
		Assert.assertEquals("foo/bar;x=a", MediaType.valueOf("foo/bar;x=\"a\"").toString());
		Assert.assertEquals("foo/bar;x=a;y=b", MediaType.valueOf("foo/bar;x=a;y=b").toString());
		Assert.assertEquals("foo/bar;x=a;y=b", MediaType.valueOf("foo/bar; x=a; y=b").toString());
		Assert.assertEquals("foo/bar;x=\"a\nb\"", MediaType.valueOf("foo/bar;x=\"a\nb\"").toString());
		Assert.assertEquals("foo/bar;x=\"a;b\"", MediaType.valueOf("foo/bar;x=\"a;b\"").toString());
		Assert.assertEquals("foo/bar;x=\"a=b\"", MediaType.valueOf("foo/bar;x=\"a=b\"").toString());
	}

	@Test
	public void equalsTest() {
		Assert.assertTrue(MediaType.valueOf("*/*").equals(MediaType.valueOf("*/*")));
		Assert.assertTrue(MediaType.valueOf("foo/bar").equals(MediaType.valueOf("Foo/Bar")));
		Assert.assertTrue(MediaType.valueOf("foo/bar;x=y").equals(MediaType.valueOf("foo/bar; X=y")));
		Assert.assertTrue(MediaType.valueOf("foo/bar;x=y").equals(MediaType.valueOf("foo/bar; X=a;x=y")));

		Assert.assertFalse(MediaType.valueOf("foo/baz").equals(MediaType.valueOf("foo/bar")));
		Assert.assertFalse(MediaType.valueOf("foo/baz").equals(MediaType.valueOf("Foo/Bar")));
		Assert.assertFalse(MediaType.valueOf("foo/bar;x=a").equals(MediaType.valueOf("foo/bar; X=y")));
		Assert.assertFalse(MediaType.valueOf("foo/bar;x=z").equals(MediaType.valueOf("foo/bar; X=a;x=y")));
		Assert.assertFalse(MediaType.valueOf("foo/bar;x=y").equals(MediaType.valueOf("foo/bar; x=Y")));
	}

	@Test
	public void isWildcardTypeTest() {
		Assert.assertTrue(new MediaType(null, null).isWildcardType());
		Assert.assertTrue(new MediaType("*", null).isWildcardType());
		Assert.assertTrue(new MediaType(null, "*").isWildcardType());
		Assert.assertTrue(new MediaType("*", "*").isWildcardType());
		Assert.assertFalse(new MediaType("foo", "*").isWildcardType());
	}

	@Test
	public void isWildcardSubtypeTest() {
		Assert.assertTrue(new MediaType(null, null).isWildcardSubtype());
		Assert.assertTrue(new MediaType("*", null).isWildcardSubtype());
		Assert.assertTrue(new MediaType(null, "*").isWildcardSubtype());
		Assert.assertTrue(new MediaType("*", "*").isWildcardSubtype());
		Assert.assertTrue(new MediaType("foo", "*").isWildcardSubtype());
		Assert.assertFalse(new MediaType("foo", "bar").isWildcardSubtype());
	}

	@Test
	public void withoutParametersTest() {
		Assert.assertEquals("foo/bar", MediaType.valueOf("foo/bar;x=y").withoutParameters().toString());
		Assert.assertEquals("foo/bar", MediaType.valueOf("foo/bar").withoutParameters().toString());
	}

	@Test
	public void withoutParameters2Test() {
		Assert.assertEquals("foo/bar;a=b", MediaType.valueOf("foo/bar;a=b;x=y").withoutParameters("x").toString());
		Assert.assertEquals("foo/bar", MediaType.valueOf("foo/bar").withoutParameters("x").toString());
	}

	@Test
	public void withParameterTest() {
		Assert.assertEquals("foo/bar;a=b", MediaType.valueOf("foo/bar").withParameter("a", "b").toString());
		Assert.assertEquals("foo/bar;x=y;a=b", MediaType.valueOf("foo/bar;x=y").withParameter("a", "b").toString());
	}

	@Test
	public void withParametersTest() {
		Assert.assertEquals("foo/bar", MediaType.valueOf("foo/bar").withParameters(Collections.emptyMap()).toString());

		final Map<String, String> additional = new LinkedHashMap<>();
		additional.put("a", "b");
		additional.put("c", "d");

		Assert.assertEquals("foo/bar;a=b;c=d", MediaType.valueOf("foo/bar").withParameters(additional).toString());
		Assert.assertEquals("foo/bar;x=y;a=b;c=d", MediaType.valueOf("foo/bar;x=y").withParameters(additional).toString());
	}
}
