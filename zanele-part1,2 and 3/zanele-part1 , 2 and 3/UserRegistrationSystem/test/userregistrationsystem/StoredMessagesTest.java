
package userregistrationsystem;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class StoredMessagesTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private StoredMessages storedMessages;
    private String jsonFile;

    @Before
    public void setUp() throws Exception {
        File file = tempFolder.newFile("testMessages.json");
        jsonFile = file.getAbsolutePath();
        storedMessages = new StoredMessages(jsonFile);
    }

    // TEST 1: Add a sent message
    @Test
    public void testAddSentMessage() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Hello world",
                "Sent"
        );

        assertEquals(1, storedMessages.getSentMessages().length);
        assertEquals("Hello world",
                storedMessages.getSentMessages()[0]);
    }

    // TEST 2: Add a stored message
    @Test
    public void testAddStoredMessage() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Stored message",
                "Stored"
        );

        assertEquals(1, storedMessages.getStoredMessages().length);
        assertEquals("Stored message",
                storedMessages.getStoredMessages()[0]);
    }

    // TEST 3: Add a disregarded message
    @Test
    public void testAddDisregardedMessage() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Disregarded message",
                "Disregarded"
        );

        assertEquals(1,
                storedMessages.getDisregardedMessages().length);
    }

    // TEST 4: Display sender and recipient
    @Test
    public void testDisplaySendersAndRecipients() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Stored message",
                "Stored"
        );

        String result =
                storedMessages.displaySendersAndRecipients();

        assertTrue(result.contains("Sender: Developer"));
        assertTrue(result.contains("Recipient: +27821234567"));
    }

    // TEST 5: Display longest message
    @Test
    public void testGetLongestMessage() {

        storedMessages.addMessage(
                "1111111111",
                "11:1:HI",
                "+27821234567",
                "Hi",
                "Sent"
        );

        storedMessages.addMessage(
                "2222222222",
                "22:2:LONGMESSAGE",
                "+27821234567",
                "This is the longest message",
                "Sent"
        );

        assertEquals("This is the longest message",
                storedMessages.getLongestMessage());
    }

    // TEST 6: Search message using message ID
    @Test
    public void testGetMessageByID() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Hello world",
                "Sent"
        );

        assertEquals("Hello world",
                storedMessages.getMessageByID("1234567890"));
    }

    // TEST 7: Search recipient using message ID
    @Test
    public void testGetRecipientByID() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Hello world",
                "Sent"
        );

        assertEquals("+27821234567",
                storedMessages.getRecipientByID("1234567890"));
    }

    // TEST 8: Search for messages by recipient
    @Test
    public void testGetMessagesForRecipient() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "First message",
                "Sent"
        );

        storedMessages.addMessage(
                "0987654321",
                "09:2:SECONDMESSAGE",
                "+27821234567",
                "Second message",
                "Sent"
        );

        String[] results =
                storedMessages.getMessagesForRecipient("+27821234567");

        assertEquals(2, results.length);
    }

    // TEST 9: Delete message by hash
    @Test
    public void testDeleteMessageByHash() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Hello world.",
                "Sent"
        );

        String result =
                storedMessages.deleteByMessageHash("12:1:HELLOWORLD");

        assertTrue(result.contains("successfully deleted"));
        assertNull(storedMessages.getMessageByID("1234567890"));
    }

    // TEST 10: Display message report
    @Test
    public void testDisplayReport() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Report message",
                "Sent"
        );

        String report = storedMessages.displayReport();

        assertTrue(report.contains("MESSAGE REPORT"));
        assertTrue(report.contains("Message ID: 1234567890"));
        assertTrue(report.contains("Recipient: +27821234567"));
        assertTrue(report.contains("Report message"));
    }

    // TEST 11: Save stored messages to JSON
    @Test
    public void testSaveStoredMessagesToJSON() throws Exception {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Saved message",
                "Stored"
        );

        Path file = Path.of(jsonFile);

        assertTrue(Files.exists(file));

        String content = Files.readString(file);

        assertTrue(content.contains("1234567890"));
        assertTrue(content.contains("Saved message"));
    }

    // TEST 12: Load stored messages from JSON
    @Test
    public void testLoadStoredMessagesFromJSON() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Saved message",
                "Stored"
        );

        StoredMessages loadedMessages =
                new StoredMessages(jsonFile);

        assertEquals(1,
                loadedMessages.getStoredMessages().length);

        assertEquals("Saved message",
                loadedMessages.getStoredMessages()[0]);
    }

    // TEST 13: Search for a message that does not exist
    @Test
    public void testSearchForNonExistingMessage() {

        assertNull(
                storedMessages.getMessageByID("9999999999"));
    }

    // TEST 14: Display empty stored messages
    @Test
    public void testDisplayEmptyStoredMessages() {

        String result =
                storedMessages.displaySendersAndRecipients();

        assertEquals("There are no stored messages.", result);
    }

    // TEST 15: Ensure disregarded messages are excluded
    // from the longest message result
    @Test
    public void testDisregardedMessageExcludedFromLongest() {

        storedMessages.addMessage(
                "1111111111",
                "11:1:SHORT",
                "+27821234567",
                "Short message",
                "Sent"
        );

        storedMessages.addMessage(
                "2222222222",
                "22:2:LONG",
                "+27821234567",
                "This is a very long disregarded message",
                "Disregarded"
        );

        assertEquals("Short message",
                storedMessages.getLongestMessage());
    }

    // TEST 16: Check that getters return defensive copies
    @Test
    public void testGetStoredMessagesReturnsCopy() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLOWORLD",
                "+27821234567",
                "Stored message",
                "Stored"
        );

        String[] messages =
                storedMessages.getStoredMessages();

        messages[0] = "Changed message";

        assertEquals("Stored message",
                storedMessages.getStoredMessages()[0]);
    }

    // TEST 17: Display longest message
    @Test
    public void testDisplayLongestMessage() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLO",
                "+27821234567",
                "Hello",
                "Sent"
        );

        String result = storedMessages.displayLongestMessage();

        assertTrue(result.contains("Longest message"));
        assertTrue(result.contains("Hello"));
    }

    // TEST 18: Search by message ID
    @Test
    public void testSearchByMessageID() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLO",
                "+27821234567",
                "Hello world",
                "Sent"
        );

        String result =
                storedMessages.searchByMessageID("1234567890");

        assertTrue(result.contains("+27821234567"));
        assertTrue(result.contains("Hello world"));
    }

    // TEST 19: Search by recipient
    @Test
    public void testSearchByRecipient() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLO",
                "+27821234567",
                "Hello world",
                "Sent"
        );

        String result =
                storedMessages.searchByRecipient("+27821234567");

        assertTrue(result.contains("Hello world"));
    }

    // TEST 20: Get message IDs
    @Test
    public void testGetMessageIDs() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLO",
                "+27821234567",
                "Hello world",
                "Sent"
        );

        assertEquals(
                "1234567890",
                storedMessages.getMessageIDs()[0]
        );
    }

    // TEST 21: Get message hashes
    @Test
    public void testGetMessageHashes() {

        storedMessages.addMessage(
                "1234567890",
                "12:1:HELLO",
                "+27821234567",
                "Hello world",
                "Sent"
        );

        assertEquals(
                "12:1:HELLO",
                storedMessages.getMessageHashes()[0]
        );
    }
}

