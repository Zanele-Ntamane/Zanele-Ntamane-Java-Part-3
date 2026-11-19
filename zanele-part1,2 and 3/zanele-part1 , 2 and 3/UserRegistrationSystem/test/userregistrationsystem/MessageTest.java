package userregistrationsystem;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageTest {

    // Test data from the brief
    private static final String ID_1 = "0012345678";
    private static final String CELL_1 = "+27718693002";
    private static final String MSG_1 =
            "Hi Hamiday, can you join us for salah tonight?";

    private static final String ID_2 = "1198765432";
    private static final String CELL_2 = "08575975889";
    private static final String MSG_2 =
            "Hi Amina, did you receive the abaya?";

    @Before
    public void setUp() {
        Message.resetMessages();
    }

    // TEST 1: Message length success
    @Test
    public void messageLengthSuccess() {

        assertEquals(
                "Message ready to send.",
                Message.checkMessageLength(MSG_1)
        );
    }

    // TEST 2: Message length failure
    @Test
    public void messageLengthFailure() {

        String longMessage = "a".repeat(260);

        assertEquals(
                "Message exceeds 250 characters by 10; please reduce the size.",
                Message.checkMessageLength(longMessage)
        );
    }

    // TEST 3: Recipient number success
    @Test
    public void recipientNumberSuccess() {

        assertEquals(
                "Cell phone number successfully captured.",
                Message.checkRecipientCell(CELL_1)
        );
    }

    // TEST 4: Recipient number failure
    @Test
    public void recipientNumberFailure() {

        assertEquals(
                "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                Message.checkRecipientCell(CELL_2)
        );
    }

    // TEST 5: Message hash test case 1
    @Test
    public void messageHashTestCase1() {

        Message m = new Message(ID_1, 0, CELL_1, MSG_1);

        assertEquals(
                "00:0:HITONIGHT",
                m.createMessageHash()
        );
    }

    // TEST 6: Message hash for remaining messages in loop
    @Test
    public void messageHashRemainingMessagesInLoop() {

        String[] ids = {ID_1, ID_2};
        String[] msgs = {MSG_1, MSG_2};

        String[] expected = {
                "00:0:HITONIGHT",
                "11:1:HIABAYA"
        };

        for (int i = 0; i < msgs.length; i++) {

            Message m = new Message(
                    ids[i],
                    i,
                    CELL_1,
                    msgs[i]
            );

            assertEquals(
                    expected[i],
                    m.createMessageHash()
            );
        }
    }

    // TEST 7: Message ID is created
    @Test
    public void messageIdIsCreated() {

        String id = Message.generateMessageID();

        Message m = new Message(
                id,
                0,
                CELL_1,
                MSG_1
        );

        assertEquals(
                "Message ID generated: " + id,
                m.displayMessageID()
        );

        assertEquals(10, id.length());

        assertTrue(m.checkMessageID());
    }

    // TEST 8: Message ID too long fails
    @Test
    public void messageIdTooLongFails() {

        Message m = new Message(
                "12345678901",
                0,
                CELL_1,
                MSG_1
        );

        assertFalse(m.checkMessageID());
    }

    // TEST 9: Send message
    @Test
    public void sendMessage() {

        Message m = new Message(
                ID_1,
                0,
                CELL_1,
                MSG_1
        );

        assertEquals(
                "Message successfully sent.",
                m.SentMessage(1)
        );
    }

    // TEST 10: Disregard message
    @Test
    public void disregardMessage() {

        Message m = new Message(
                ID_2,
                1,
                CELL_2,
                MSG_2
        );

        assertEquals(
                "Press 0 to delete the message.",
                m.SentMessage(2)
        );
    }

    // TEST 11: Store message
    @Test
    public void storeMessage() {

        Message m = new Message(
                ID_1,
                0,
                CELL_1,
                MSG_1
        );

        assertEquals(
                "Message successfully stored.",
                m.SentMessage(3)
        );
    }

    // TEST 12: Total messages sent
    @Test
    public void totalMessagesSent() {

        new Message(
                ID_1,
                0,
                CELL_1,
                MSG_1
        ).SentMessage(1);

        new Message(
                ID_2,
                1,
                CELL_2,
                MSG_2
        ).SentMessage(2);

        assertEquals(
                1,
                Message.returnTotalMessages()
        );
    }
    // TEST 13: Invalid message option
@Test
public void invalidMessageOption() {

    Message m = new Message(
            ID_1,
            0,
            CELL_1,
            MSG_1
    );

    assertNotNull(m.SentMessage(5));
}
// TEST 14: Get message ID
@Test
public void testGetMessageID() {

    Message m = new Message(
            ID_1,
            0,
            CELL_1,
            MSG_1
    );

    assertEquals(ID_1, m.getMessageID());
}
// TEST 15: Get message hash
@Test
public void testGetMessageHash() {

    Message m = new Message(
            ID_1,
            0,
            CELL_1,
            MSG_1
    );

    assertEquals(
            m.createMessageHash(),
            m.getMessageHash()
    );
}
// TEST 16: Get recipient
@Test
public void testGetRecipient() {

    Message m = new Message(
            ID_1,
            0,
            CELL_1,
            MSG_1
    );

    assertEquals(CELL_1, m.getRecipient());
}
// TEST 17: Get message text
@Test
public void testGetMessageText() {

    Message m = new Message(
            ID_1,
            0,
            CELL_1,
            MSG_1
    );

    assertEquals(MSG_1, m.getMessageText());
}
// TEST 18: Get message status
@Test
public void testGetStatus() {

    Message m = new Message(
            ID_1,
            0,
            CELL_1,
            MSG_1
    );

    m.SentMessage(1);

    assertEquals("Sent", m.getStatus());
}
// TEST 19: Reset total messages
@Test
public void testResetMessages() {

    new Message(
            ID_1,
            0,
            CELL_1,
            MSG_1
    ).SentMessage(1);

    assertEquals(1, Message.returnTotalMessages());

    Message.resetMessages();

    assertEquals(0, Message.returnTotalMessages());
}
    
}

