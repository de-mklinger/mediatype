package de.mklinger.micro.mediatype;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
			fail("Expected exception not thrown");
		} catch (final MediaTypeParseException e) {
			// expected
		}
	}

	@Test
	public void constructorTest() {
		assertEquals("*/*", new MediaType(null, null).toString());
		assertEquals("foo/bar", new MediaType("foo", "bar").toString());
		assertEquals("foo/bar", new MediaType("foo", "bar", null).toString());
		assertEquals("foo/bar", new MediaType("foo", "bar", Collections.emptyMap()).toString());
		assertEquals("foo/bar;x=y", new MediaType("foo", "bar", Collections.singletonMap("x", "y")).toString());
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
		assertEquals("*/*", MediaType.valueOf("*/*").toString());
		assertEquals("*/*", MediaType.valueOf("*").toString());
		assertEquals("*/*;foo=bar", MediaType.valueOf("*/*;foo=bar").toString());
		assertEquals("*/*;foo=bar", MediaType.valueOf("*;foo=bar").toString());
		assertEquals("foo/bar", MediaType.valueOf("foo/bar").toString());
		assertEquals("foo/bar;x=a", MediaType.valueOf("foo/bar;x=a").toString());
		assertEquals("foo/bar;x=a", MediaType.valueOf("foo/bar;x=\"a\"").toString());
		assertEquals("foo/bar;x=a;y=b", MediaType.valueOf("foo/bar;x=a;y=b").toString());
		assertEquals("foo/bar;y=a;x=b", MediaType.valueOf("foo/bar;y=a;x=b").toString());
		assertEquals("foo/bar;x=a;y=b", MediaType.valueOf("foo/bar; x=a; y=b").toString());
		assertEquals("foo/bar;x=\"a\nb\"", MediaType.valueOf("foo/bar;x=\"a\nb\"").toString());
		assertEquals("foo/bar;x=\"a;b\"", MediaType.valueOf("foo/bar;x=\"a;b\"").toString());
		assertEquals("foo/bar;x=\"a=b\"", MediaType.valueOf("foo/bar;x=\"a=b\"").toString());
	}

	@Test
	public void equalsTest() {
		assertTrue(MediaType.valueOf("*/*").equals(MediaType.valueOf("*/*")));
		assertTrue(MediaType.valueOf("foo/bar").equals(MediaType.valueOf("Foo/Bar")));
		assertTrue(MediaType.valueOf("foo/bar;x=y").equals(MediaType.valueOf("foo/bar; X=y")));
		assertTrue(MediaType.valueOf("foo/bar;x=y").equals(MediaType.valueOf("foo/bar; X=a;x=y")));

		assertFalse(MediaType.valueOf("foo/baz").equals(MediaType.valueOf("foo/bar")));
		assertFalse(MediaType.valueOf("foo/baz").equals(MediaType.valueOf("Foo/Bar")));
		assertFalse(MediaType.valueOf("foo/bar;x=a").equals(MediaType.valueOf("foo/bar; X=y")));
		assertFalse(MediaType.valueOf("foo/bar;x=z").equals(MediaType.valueOf("foo/bar; X=a;x=y")));
		assertFalse(MediaType.valueOf("foo/bar;x=y").equals(MediaType.valueOf("foo/bar; x=Y")));
	}

	@Test
	public void isWildcardTypeTest() {
		assertTrue(new MediaType(null, null).isWildcardType());
		assertTrue(new MediaType("*", null).isWildcardType());
		assertTrue(new MediaType(null, "*").isWildcardType());
		assertTrue(new MediaType("*", "*").isWildcardType());
		assertFalse(new MediaType("foo", "*").isWildcardType());
	}

	@Test
	public void isWildcardSubtypeTest() {
		assertTrue(new MediaType(null, null).isWildcardSubtype());
		assertTrue(new MediaType("*", null).isWildcardSubtype());
		assertTrue(new MediaType(null, "*").isWildcardSubtype());
		assertTrue(new MediaType("*", "*").isWildcardSubtype());
		assertTrue(new MediaType("foo", "*").isWildcardSubtype());
		assertFalse(new MediaType("foo", "bar").isWildcardSubtype());
	}

	@Test
	public void withoutParametersTest() {
		assertEquals("foo/bar", MediaType.valueOf("foo/bar;x=y").withoutParameters().toString());
		assertEquals("foo/bar", MediaType.valueOf("foo/bar").withoutParameters().toString());
	}

	@Test
	public void withoutParameters2Test() {
		assertEquals("foo/bar;a=b", MediaType.valueOf("foo/bar;a=b;x=y").withoutParameters("x").toString());
		assertEquals("foo/bar", MediaType.valueOf("foo/bar").withoutParameters("x").toString());
	}

	@Test
	public void withParameterTest() {
		assertEquals("foo/bar;a=b", MediaType.valueOf("foo/bar").withParameter("a", "b").toString());
		assertEquals("foo/bar;x=y;a=b", MediaType.valueOf("foo/bar;x=y").withParameter("a", "b").toString());
	}

	@Test
	public void withParametersTest() {
		assertEquals("foo/bar", MediaType.valueOf("foo/bar").withParameters(Collections.emptyMap()).toString());

		final Map<String, String> additional = new LinkedHashMap<>();
		additional.put("a", "b");
		additional.put("c", "d");

		assertEquals("foo/bar;a=b;c=d", MediaType.valueOf("foo/bar").withParameters(additional).toString());
		assertEquals("foo/bar;x=y;a=b;c=d", MediaType.valueOf("foo/bar;x=y").withParameters(additional).toString());
	}

	@Test
	public void isCompatibleTest() {
		assertTrue(MediaType.valueOf("foo/bar").isCompatible(MediaType.valueOf("foo/bar")));
		assertTrue(MediaType.valueOf("foo/bar;x=y").isCompatible(MediaType.valueOf("foo/bar")));
		assertTrue(MediaType.valueOf("foo/bar").isCompatible(MediaType.valueOf("foo/bar;x=y")));
		assertTrue(MediaType.valueOf("foo/*").isCompatible(MediaType.valueOf("foo/bar")));
		assertTrue(MediaType.valueOf("foo/bar").isCompatible(MediaType.valueOf("foo/*")));
		assertTrue(MediaType.valueOf("*/*").isCompatible(MediaType.valueOf("foo/bar")));
		assertTrue(MediaType.valueOf("*/*").isCompatible(MediaType.valueOf("foo/*")));
		assertTrue(MediaType.valueOf("*/*").isCompatible(MediaType.valueOf("*/*")));
		assertTrue(MediaType.valueOf("foo/bar").isCompatible(MediaType.valueOf("*/*")));
		assertTrue(MediaType.valueOf("foo/*").isCompatible(MediaType.valueOf("*/*")));

		assertFalse(MediaType.valueOf("foo/bar").isCompatible(MediaType.valueOf("foo/baz")));
		assertFalse(MediaType.valueOf("foo/bar").isCompatible(MediaType.valueOf("application/pdf")));
		assertFalse(MediaType.valueOf("foo/*").isCompatible(MediaType.valueOf("application/*")));
	}

	@Test
	public void hashCodeTest() {
		assertEquals(MediaType.valueOf("foo/bar").hashCode(), MediaType.valueOf("foo/bar").hashCode());
		assertEquals(MediaType.valueOf("foo/bar;x=y").hashCode(), MediaType.valueOf("foo/bar;x=y").hashCode());
	}

	@Test
	public void getFullTypeTest() {
		assertEquals("foo/bar", MediaType.valueOf("foo/bar").getFullType());
		assertEquals("foo/bar", MediaType.valueOf("foo/bar;x=y").getFullType());
	}

}
