package com.rsomeara.artifact.repo.versions.type;

import java.io.IOException;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

/**
 * JSON-Simple content handler boilerplate. Reads keys as they are encountered and provides simpler API to implement for
 * clients which just need to read primitive key values
 *
 * @author romeara
 *
 */
public abstract class AbstractContentHandler implements ContentHandler {

    private String currentKey;

    /**
     * @return True if all values of interest have been read and the parser should stop streaming data
     */
    protected abstract boolean isComplete();

    /**
     * @return The current JSON key of the values being provided
     */
    public String getCurrentKey() {
        return currentKey;
    }

    @Override
    public void startJSON() throws ParseException, IOException {
        currentKey = null;
    }

    @Override
    public void endJSON() throws ParseException, IOException {
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        return !isComplete();
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        return !isComplete();
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        currentKey = key;
        return !isComplete();
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return !isComplete();
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        return !isComplete();
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        return !isComplete();
    }

}
