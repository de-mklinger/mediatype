package de.mklinger.micro.mediatype;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class MediaTypesTest {
	@Test
	public void test() throws IllegalArgumentException, IllegalAccessException {
		for (final Field f : MediaTypes.class.getFields()) {
			if (f.getType() == MediaType.class) {
				final MediaType mediaType = (MediaType) f.get(null);
				assertNotNull(mediaType);
			}
		}
	}
}
