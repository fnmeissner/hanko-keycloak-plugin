package io.hanko.client.java.json.jackson;

import io.hanko.client.java.json.HankoJsonParser;
import io.hanko.client.java.json.HankoJsonParserFactory;

public class HankoJsonParserFactoryJackson implements HankoJsonParserFactory {
    @Override
    public HankoJsonParser create() {
        return new HankoJsonParserJackson();
    }
}
