package nz.govt.natlib.tools.sip.generation

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import nz.govt.natlib.tools.sip.Sip
import nz.govt.natlib.tools.sip.SipTestHelper
import org.junit.Before
import org.junit.Test

/**
 * Tests {@link SipJsonGenerator}.
 */
class SipJsonGeneratorTest {
    Sip testSip

    @Before
    void setup() {
        testSip = SipTestHelper.sipOne()
    }

    @Test
    void generatesJsonForSipJson1FileCorrectly() {
        boolean prettyPrint = true
        String jsonString = SipJsonGenerator.toJson(testSip, prettyPrint)

        JsonParser jsonParser = new JsonParser()
        JsonElement expectedJson = jsonParser.parse(SipTestHelper.getTextFromResourceOrFile(SipTestHelper.TEST_SIP_JSON_1_FILENAME))
        JsonElement actualJson = jsonParser.parse(jsonString)
        actualJson.getAsJsonObject().get("fileWrappers").each { JsonObject fileWrapperObject ->
            JsonElement fileObject = fileWrapperObject.get("file")
            if (fileObject != null && (fileObject in JsonPrimitive)) {
                JsonPrimitive fileStringPrimitive = (JsonPrimitive) fileObject
                // support testing in Windows. Replace the 'file' object with a path-adjusted file object.
                String fileString =  fileStringPrimitive.getAsString().replace("\\", "/")
                if (fileString.toLowerCase().startsWith("c:")) {
                    fileString = fileString.substring(2)
                }
                fileWrapperObject.addProperty("file", fileString)
            }
        }

        assertThat("Generated JSON matches expected JSON for file=${SipTestHelper.TEST_SIP_JSON_1_FILENAME}",
            expectedJson, is(actualJson))
    }
}
