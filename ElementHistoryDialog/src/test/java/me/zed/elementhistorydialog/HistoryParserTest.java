package me.zed.elementhistorydialog;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import me.zed.elementhistorydialog.elements.Relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests the parsed result from the OsmParser for a /history call
 */
public class HistoryParserTest {

    /**
     * Parses a saved result and verifies correct parsing
     */
    @Test
    public void readXml() {
        InputStream input = getClass().getResourceAsStream("/historytestdata.osm");
        OsmParser parser = new OsmParser();
        try {
            assertNotNull(input);
            parser.start(input);
            Storage storage = parser.getStorage();
            List<Relation> rl = storage.getRelationList();
            assertEquals(rl.size(), 31);
            assertEquals(rl.get(0).getMembers().size(), 3);

        } catch (SAXException | IOException | ParserConfigurationException | IllegalArgumentException | IllegalStateException e) {
            fail(e.getMessage());
        }
    }
}
