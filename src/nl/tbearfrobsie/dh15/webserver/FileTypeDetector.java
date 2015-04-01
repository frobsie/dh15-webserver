package nl.tbearfrobsie.dh15.webserver;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.tika.Tika;

/**
 * Custom implementation of a FileTypeDetector
 * which converts the default detector to now
 * use Apache Tika to find a file's mimetype.
 * See https://odoepner.wordpress.com/2013/07/29/transparently-improve-java-7-mime-type-recognition-with-apache-tika/
 *
 */
public class FileTypeDetector extends java.nio.file.spi.FileTypeDetector {
	
	/** Apache Tika intance */
    private final Tika tika = new Tika();
 
    @Override
    public String probeContentType(Path path) throws IOException {
        return tika.detect(path.toFile());
    }
}
