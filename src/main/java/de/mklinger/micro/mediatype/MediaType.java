package de.mklinger.micro.mediatype;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import de.mklinger.micro.annotations.Nullable;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class MediaType {
	private static final String WILDCARD = "*";
	private final String type;
	private final String subtype;
	private final Map<String, String> parameters;

	/**
	 * Creates a new instance of {@code MediaType} with the supplied type, subtype and
	 * parameters.
	 *
	 * @param type       the primary type, {@code null} is equivalent to
	 *                   {@link #WILDCARD}.
	 * @param subtype    the subtype, {@code null} is equivalent to
	 *                   {@link #WILDCARD}.
	 * @param parameters a map of media type parameters, {@code null} is the same as an
	 *                   empty map.
	 */
	public MediaType(@Nullable final String type, @Nullable final String subtype, @Nullable final Map<String, String> parameters) {
		this(type, subtype, newParameterMap(parameters), true);
	}

	private static Map<String, String> newParameterMap(final Map<String, String> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return Collections.emptyMap();
		} else {
			final LinkedCaseInsensitiveMap<String> parameterMap = new LinkedCaseInsensitiveMap<>(Locale.US);
			parameterMap.putAll(parameters);
			return parameterMap;
		}
	}

	private MediaType(@Nullable final String type, @Nullable final String subtype, final Map<String, String> parameters, final boolean discriminator) {
		if (type == null) {
			this.type = WILDCARD;
		} else {
			this.type = type;
		}

		if (subtype == null) {
			this.subtype = WILDCARD;
		} else {
			this.subtype = subtype;
		}

		if (WILDCARD.equals(this.type) && !WILDCARD.equals(this.subtype)) {
			throw new IllegalArgumentException("Media type wildcard type is legal only in '*/*' (all mime types)");
		}

		this.parameters = Collections.unmodifiableMap(parameters);
	}

	/**
	 * Creates a new instance of {@code MediaType} with the supplied type and subtype.
	 *
	 * @param type    the primary type, {@code null} is equivalent to
	 *                {@link #WILDCARD}
	 * @param subtype the subtype, {@code null} is equivalent to
	 *                {@link #WILDCARD}
	 */
	public MediaType(@Nullable final String type, @Nullable final String subtype) {
		this(type, subtype, null);
	}

	/**
	 * Creates a new instance of {@code MediaType} by parsing the supplied string.
	 *
	 * @param type the media type string.
	 * @return the newly created MediaType.
	 * @throws IllegalArgumentException if the supplied string cannot be parsed
	 *                                  or is {@code null}.
	 */
	public static MediaType valueOf(final String type) {
		return parse(type);
	}


	/**
	 * Getter for primary type.
	 *
	 * @return value of primary type.
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Checks if the primary type is a wildcard.
	 *
	 * @return true if the primary type is a wildcard.
	 */
	public boolean isWildcardType() {
		return this.getType().equals(WILDCARD);
	}

	/**
	 * Getter for subtype.
	 *
	 * @return value of subtype.
	 */
	public String getSubtype() {
		return this.subtype;
	}

	/**
	 * Checks if the subtype is a wildcard.
	 *
	 * @return true if the subtype is a wildcard.
	 */
	public boolean isWildcardSubtype() {
		return this.getSubtype().equals(WILDCARD);
	}

	/**
	 * Getter for a read-only parameter map. Keys are case-insensitive.
	 *
	 * @return an immutable map of parameters.
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Get an instance with same type and sub-type but without parameters.
	 */
	public MediaType withoutParameters() {
		if (parameters == null || parameters.isEmpty()) {
			return this;
		}
		return new MediaType(type, subtype);
	}

	/**
	 * Get an instance with same type and sub-type and parameters, but with
	 * given parameters removed.
	 */
	public MediaType withoutParameters(final String... names) {
		// In these cases, we expect the method above being called.
		// If not, maybe rename one of the methods.
		assert names != null;
		assert names.length > 0;

		if (parameters == null || parameters.isEmpty()) {
			return this;
		}

		final Map<String, String> newParameters = newParameterMap(parameters);
		for (final String name : names) {
			newParameters.remove(name);
		}

		return new MediaType(type, subtype, newParameters, true);
	}

	/**
	 * Get an instance with same type, sub-type and parameters with an
	 * additional parameter as given. If there was already a parameter
	 * with the same name, its value will be replaced by the new value.
	 */
	public MediaType withParameter(final String name, final String value) {
		return withParameters(Collections.singletonMap(name, value));
	}

	/**
	 * Get an instance with same type, sub-type and parameters with
	 * additional parameters as given. If there where already parameters
	 * with the same names, its values will be replaced by the new values.
	 */
	public MediaType withParameters(final Map<String, String> additionalParameters) {
		if (additionalParameters == null || additionalParameters.isEmpty()) {
			return this;
		}

		if (parameters == null || parameters.isEmpty()) {
			return new MediaType(type, subtype, additionalParameters);
		}

		final Map<String, String> allParameters = newParameterMap(parameters);
		allParameters.putAll(additionalParameters);
		return new MediaType(type, subtype, allParameters, true);
	}

	/**
	 * Check if this media type is compatible with another media type. E.g.
	 * image/* is compatible with image/jpeg, image/png, etc. Media type
	 * parameters are ignored. The function is commutative.
	 *
	 * @param other the media type to compare with.
	 * @return true if the types are compatible, false otherwise.
	 */
	public boolean isCompatible(final MediaType other) {
		return other != null && // return false if other is null, else
				(type.equals(WILDCARD) || other.type.equals(WILDCARD) || // both are wildcard types, or
						(type.equalsIgnoreCase(other.type) && (subtype.equals(WILDCARD)
								|| other.subtype.equals(WILDCARD))) || // same types, wildcard sub-types, or
						(type.equalsIgnoreCase(other.type) && this.subtype.equalsIgnoreCase(other.subtype))); // same types & sub-types
	}

	/**
	 * Compares {@code obj} to this media type to see if they are the same by comparing
	 * type, subtype and parameters. Note that the case-sensitivity of parameter
	 * values is dependent on the semantics of the parameter name, see
	 * {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7">HTTP/1.1</a>}.
	 * This method assumes that values are case-sensitive.
	 * <p/>
	 * Note that the {@code equals(...)} implementation does not perform
	 * a class equality check ({@code this.getClass() == obj.getClass()}). Therefore
	 * any class that extends from {@code MediaType} class and needs to override
	 * one of the {@code equals(...)} and {@link #hashCode()} methods must
	 * always override both methods to ensure the contract between
	 * {@link Object#equals(java.lang.Object)} and {@link Object#hashCode()} does
	 * not break.
	 *
	 * @param obj the object to compare to.
	 * @return true if the two media types are the same, false otherwise.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof MediaType)) {
			return false;
		}

		final MediaType other = (MediaType) obj;
		return (this.type.equalsIgnoreCase(other.type)
				&& this.subtype.equalsIgnoreCase(other.subtype)
				&& this.parameters.equals(other.parameters));
	}

	/**
	 * Generate a hash code from the type, subtype and parameters.
	 * <p/>
	 * Note that the {@link #equals(java.lang.Object)} implementation does not perform
	 * a class equality check ({@code this.getClass() == obj.getClass()}). Therefore
	 * any class that extends from {@code MediaType} class and needs to override
	 * one of the {@link #equals(Object)} and {@code hashCode()} methods must
	 * always override both methods to ensure the contract between
	 * {@link Object#equals(java.lang.Object)} and {@link Object#hashCode()} does
	 * not break.
	 *
	 * @return a generated hash code.
	 */
	@Override
	public int hashCode() {
		return (this.type.toLowerCase() + this.subtype.toLowerCase()).hashCode() + this.parameters.hashCode();
	}

	/**
	 * Convert the media type to a string suitable for use as the value of a
	 * corresponding HTTP header.
	 *
	 * @return a string version of the media type.
	 */
	@Override
	public String toString() {
		return toString(this, true);
	}

	/**
	 * Convert the media type to a string without parameters.
	 * @return a string version of the media type without parameters.
	 */
	public String getFullType() {
		return toString(this, false);
	}

	private static String toString(final MediaType type, final boolean withParameters) {
		if (type == null) {
			throw new IllegalArgumentException("param was null");
		}

		int len = type.getType().length()
				+ 1 // slash
				+ type.getSubtype().length();
		for (final Entry<String, String> parameter : type.getParameters().entrySet()) {
			len += parameter.getKey().length()
					+ parameter.getValue().length()
					+ 4; // semicolon, equals and quotes
		}

		final StringBuilder buf = new StringBuilder(len);

		buf.append(type.getType().toLowerCase());
		buf.append("/");
		buf.append(type.getSubtype().toLowerCase());
		if (!withParameters || type.getParameters() == null || type.getParameters().isEmpty()) {
			return buf.toString();
		}
		for (final Entry<String, String> parameter : type.getParameters().entrySet()) {
			buf.append(';');
			buf.append(parameter.getKey());
			final String val = parameter.getValue();
			if (!val.isEmpty()) {
				buf.append('=');
				if (needsQuotation(val)) {
					buf.append('"');
					buf.append(val);
					buf.append('"');
				} else {
					buf.append(val);
				}
			}
		}
		return buf.toString();
	}

	private static final char[] charsNeedingQuotation = "()<>@,;:\\\"/[]?= \t\r\n".toCharArray();

	private static boolean needsQuotation(final String str) {
		for (int idx = 0; idx < str.length(); idx++) {
			for (final char q : charsNeedingQuotation) {
				if (str.charAt(idx) == q) {
					return true;
				}
			}
		}
		return false;
	}

	private static MediaType parse(final String mediaType) {
		if (mediaType == null || mediaType.isEmpty()) {
			throw new MediaTypeParseException("Media type must not be empty");
		}

		final int semicolonIdx = mediaType.indexOf(';');

		String fullType;
		if (semicolonIdx >= 0) {
			fullType = mediaType.substring(0, semicolonIdx).trim();
		} else {
			fullType = mediaType.trim();
		}

		if (fullType.isEmpty()) {
			throw new MediaTypeParseException("Media type must not be empty");
		}

		// java.net.HttpURLConnection returns a *; q=.2 Accept header
		if (WILDCARD.equals(fullType)) {
			fullType = "*/*";
		}
		final int subIndex = fullType.indexOf('/');
		if (subIndex == -1) {
			throw new MediaTypeParseException("Media type does not contain '/': '" + mediaType + "'");
		}
		if (subIndex == fullType.length() - 1) {
			throw new MediaTypeParseException("Media type does not contain subtype after '/': '" + mediaType + "'");
		}

		final String type = requireToken(fullType.substring(0, subIndex), () -> "Invalid type in media type: '" + mediaType + "'");
		final String subtype = requireToken(fullType.substring(subIndex + 1, fullType.length()), () -> "Invalid sub-type in media type: '" + mediaType + "'");

		if (WILDCARD.equals(type) && !WILDCARD.equals(subtype)) {
			throw new MediaTypeParseException("Media type wildcard type is legal only in '*/*' (all mime types)");
		}

		final Map<String, String> parameters = parseParameters(mediaType, semicolonIdx);

		return new MediaType(type, subtype, parameters, true);
	}

	private static String requireToken(final String token, final Supplier<String> messageSupplier) {
		if (token.isEmpty()) {
			throw new MediaTypeParseException(messageSupplier.get());
		}
		for (int idx = 0; idx < token.length(); idx++) {
			final char c = token.charAt(idx);
			switch (c) {
			case '(':
			case ')':
			case '<':
			case '>':
			case '@':
			case ',':
			case ';':
			case ':':
			case '\\':
			case '\'':
			case '/':
			case '[':
			case ']':
			case '?':
			case '=':
				throw new MediaTypeParseException(messageSupplier.get());
			default:
				if (c <= 32 || c >= 127) {
					throw new MediaTypeParseException(messageSupplier.get());
				}
			}
		}
		return token;
	}

	private static Map<String, String> parseParameters(final String mediaType, final int initialStartIndex) {
		int startIdx = initialStartIndex;

		Map<String, String> parameters = null;

		while(startIdx >= 0 && startIdx < mediaType.length()) {
			if (mediaType.charAt(startIdx) != ';') {
				throw new IllegalStateException();
			}

			final int eqIdx = mediaType.indexOf('=', startIdx + 1);
			if (eqIdx == -1) {
				throw new MediaTypeParseException("Missing '=' in media type parameters: '" + mediaType + "'");
			}

			final String name = mediaType.substring(startIdx + 1, eqIdx).trim();
			if (name.isEmpty()) {
				throw new MediaTypeParseException("Missing name in media type parameters: '" + mediaType + "'");
			}

			String value;
			final int valueStartIdx = eqIdx + 1;
			if (valueStartIdx >= mediaType.length()) {
				throw new MediaTypeParseException("Missing value in media type parameters: '" + mediaType + "'");
			}
			final char firstValueChar = mediaType.charAt(valueStartIdx);
			if (firstValueChar == '"') {
				// quoted value
				final int quoteEndIdx = mediaType.indexOf('"', valueStartIdx + 1);
				if (quoteEndIdx == -1) {
					throw new MediaTypeParseException("Illegal value quotation in media type parameters: '" + mediaType + "'");
				}
				value = mediaType.substring(valueStartIdx + 1, quoteEndIdx);
				startIdx = quoteEndIdx + 1;

				// skip whitespace:
				while (startIdx < mediaType.length() && mediaType.charAt(startIdx) == ' ') {
					startIdx++;
				}
			} else {
				final int endIdx = mediaType.indexOf(';', valueStartIdx);
				if (endIdx == -1) {
					value = mediaType.substring(valueStartIdx);
				} else {
					value = mediaType.substring(valueStartIdx, endIdx);
				}
				startIdx = endIdx;
			}

			if (parameters == null) {
				parameters = new LinkedCaseInsensitiveMap<>(Locale.US);
			}
			parameters.put(name, value);
		}

		return newParameterMap(parameters);
	}

	public Optional<String> getParameter(final String name) {
		final String value = getParameters().get(name);
		if (value == null || value.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(value);
	}

	public Optional<Integer> getIntegerParameter(final String name) {
		final String value = getParameters().get(name);
		if (value == null || value.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(Integer.valueOf(value));
	}

	public Optional<Boolean> getBooleanParameter(final String name) {
		final String value = getParameters().get(name);
		if (value == null || value.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(Boolean.valueOf(value));
	}

	public boolean getBooleanParameter(final String name, final boolean defaultValue) {
		return getBooleanParameter(name).orElse(defaultValue);
	}
}
