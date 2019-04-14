package de.mklinger.micro.mediatype;

/**
 * Exception thrown when parsing a media type string fails.
 *
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class MediaTypeParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MediaTypeParseException(final String message) {
		super(message);
	}
}