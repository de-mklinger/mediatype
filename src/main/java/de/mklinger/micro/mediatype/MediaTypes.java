package de.mklinger.micro.mediatype;

public interface MediaTypes {
	MediaType XML = MediaType.valueOf("application/xml");
	MediaType HTML = MediaType.valueOf("text/html");
	MediaType TXT = MediaType.valueOf("text/plain");
	MediaType TXT_UTF8 = MediaType.valueOf("text/plain;charset=utf-8");
	MediaType JSON = MediaType.valueOf("application/json");
	MediaType PDF = MediaType.valueOf("application/pdf");
	MediaType PS = MediaType.valueOf("application/postscript");
	MediaType PNG = MediaType.valueOf("image/png");
	MediaType JPEG = MediaType.valueOf("image/jpeg");
	MediaType GIF = MediaType.valueOf("image/gif");
	MediaType TIFF = MediaType.valueOf("image/tiff");
	MediaType BMP = MediaType.valueOf("image/bmp");
}
