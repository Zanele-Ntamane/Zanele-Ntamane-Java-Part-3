package userregistrationsystem;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StoredMessages {
   

    // ---- Status vocabulary — exactly what Message.SentMessage(int) sets ----
    private static final String SENT        = "Sent";
    private static final String STORED      = "Stored";
    private static final String DISREGARDED = "Disregarded";
    private static final String DELETED     = "Deleted";

    // ---- Default file name MUST match Message.storeMessagesInJson() ----
    private static final String DEFAULT_JSON_FILE = "storedMessages.json";

    private static final Pattern JSON_OBJECT =
            Pattern.compile("\\{([^{}]*)\\}", Pattern.DOTALL);
    private static final Pattern JSON_FIELD =
            Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private final String jsonFile;

    // ---- Part 3 required arrays 
    private String[] sentMessage         = new String[0];
    private String[] disregardedMessages = new String[0];
    private String[] storedMessages      = new String[0];
    private String[] messageHash         = new String[0];
    private String[] messageID           = new String[0];

    // ---- Additional parallel arrays ----
    private String[] recipients = new String[0];
    private String[] statuses   = new String[0];

    // ---- Internal record mirrors Message's fields 1:1 ----
    private final List<MessageRecord> messages = new ArrayList<>();

    public StoredMessages() {
        this(DEFAULT_JSON_FILE);
    }

    public StoredMessages(String jsonFile) {
        this.jsonFile = jsonFile;
        loadStoredMessagesFromJSON();
        populateArrays();
    }

    /**
     * Internal record — field names mirror Message's fields.
 
     */
    private static class MessageRecord {
        String id;
        String hash;
        String recipient;
        String text;
        String status;

        MessageRecord(String id, String hash, String recipient,
                      String text, String status) {
            this.id        = id;
            this.hash      = hash;
            this.recipient = recipient;
            this.text      = text;
            this.status    = status;
        }
    }

    // ==================================================================
    // ALIGNED ENTRY POINT — consumes a Message directly
    // ==================================================================

    /** Primary entry point: register a Message created via Message.sendMessages().
     * @param msg */
    public void addMessage(Message msg) {
        if (msg == null) return;
        addMessage(
                msg.getMessageID(),
                msg.getMessageHash(),
                msg.getRecipient(),
                msg.getMessageText(),
                msg.getStatus());
    }

    /**
     * Low-level overload — kept for unit tests and for callers that
     * don't yet have a Message instance.
     * @param id
     * @param hash
     * @param recipient
     * @param text
     * @param status
     */
    public void addMessage(String id, String hash, String recipient,
                           String text, String status) {
        messages.add(new MessageRecord(id, hash, recipient, text,
                normalizeStatus(status)));
        populateArrays();
        if (STORED.equalsIgnoreCase(normalizeStatus(status))) {
            // Message already persists to storedMessages.json; nothing to add here
            // unless a caller injected via the String overload, in which case we
            // ensure the file stays in sync:
            saveStoredMessagesToJSON();
        }
    }

    /** Message uses "Created" → treat as pending; "Deleted" → treat as disregarded. */
    private static String normalizeStatus(String status) {
        if (status == null) return STORED;
        String s = status.trim();
        if (s.equalsIgnoreCase("Disregard") || s.equalsIgnoreCase("Deleted")
                || s.equalsIgnoreCase(DISREGARDED)) return DISREGARDED;
        if (s.equalsIgnoreCase(SENT))        return SENT;
        if (s.equalsIgnoreCase(STORED))      return STORED;
        if (s.equalsIgnoreCase("Created"))   return "Created"; // keep distinct
        return s;
    }

    // ==================================================================
    // populateArrays
    // ==================================================================
    public final void populateArrays() {
        List<String> sent        = new ArrayList<>();
        List<String> disregarded = new ArrayList<>();
        List<String> stored      = new ArrayList<>();
        List<String> hashes      = new ArrayList<>();
        List<String> ids         = new ArrayList<>();
        List<String> recips      = new ArrayList<>();
        List<String> stats       = new ArrayList<>();

        for (MessageRecord m : messages) {
            hashes.add(m.hash);
            ids.add(m.id);
            recips.add(m.recipient);
            stats.add(m.status);
            if (SENT.equalsIgnoreCase(m.status)) {
                sent.add(m.text);
            } else if (isDisregarded(m.status)) {
                disregarded.add(m.text);
            } else if (STORED.equalsIgnoreCase(m.status)) {
                stored.add(m.text);
            }
        }

        sentMessage         = sent.toArray(String[]::new);
        disregardedMessages = disregarded.toArray(String[]::new);
        storedMessages      = stored.toArray(String[]::new);
        messageHash         = hashes.toArray(String[]::new);
        messageID           = ids.toArray(String[]::new);
        recipients          = recips.toArray(String[]::new);
        statuses            = stats.toArray(String[]::new);
    }

    // ---- Defensive copies ----
    public String[] getSentMessages()        { return sentMessage.clone(); }
    public String[] getDisregardedMessages() { return disregardedMessages.clone(); }
    public String[] getStoredMessages()      { return storedMessages.clone(); }
    public String[] getMessageHashes()       { return messageHash.clone(); }
    public String[] getMessageIDs()          { return messageID.clone(); }

    // ==================================================================
    // HELPERS
    // ==================================================================
    private static boolean isDisregarded(String status) {
        return status != null && (DISREGARDED.equalsIgnoreCase(status)
                || DELETED.equalsIgnoreCase(status)
                || "Disregard".equalsIgnoreCase(status));
    }

    private static int indexOf(String[] array, String value) {
        if (value == null) return -1;
        String needle = value.trim();
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null && array[i].equalsIgnoreCase(needle)) return i;
        }
        return -1;
    }

    private int findActiveIndexByID(String id) {
        if (id == null) return -1;
        int i = indexOf(messageID, id);
        return (i == -1 || isDisregarded(statuses[i])) ? -1 : i;
    }

    // ==================================================================
    // 2a. Sender & recipient — Message has no sender, so use "Developer"
    //     to keep the output format the brief expects.
    // ==================================================================
    public String displaySendersAndRecipients() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < statuses.length; i++) {
            if (STORED.equalsIgnoreCase(statuses[i])) {
                result.append("Sender: Developer")
                      .append(" | Recipient: ").append(recipients[i]).append("\n");
            }
        }
        return result.length() == 0 ? "There are no stored messages." : result.toString().trim();
    }

    // ==================================================================
    // 2b. Longest message
    // ==================================================================
    public String getLongestMessage() {
        String longest = "";
        for (MessageRecord m:messages) {
            String text = m.text;
            if (!isDisregarded(m.status) && text != null && text.length() > longest.length()) {
                longest = text;
            }
        }
        return longest;
    }

    public String displayLongestMessage() {
        String longest = getLongestMessage();
        return longest.isEmpty() ? "There are no messages."
                                 : "Longest message: \"" + longest + "\"";
    }

    // ==================================================================
    // 2c. Search by ID
    // ==================================================================
    public String getMessageByID(String id) {
        int i = findActiveIndexByID(id);
        return i == -1 ? null : messages.get(i).text;
    }

    public String getRecipientByID(String id) {
        int i = findActiveIndexByID(id);
        return i == -1 ? null : recipients[i];
    }

    public String searchByMessageID(String id) {
        int i = findActiveIndexByID(id);
        if (i == -1) return "No message found with that ID.";
        return "Recipient: " + recipients[i]
                + "\nMessage: \"" + messages.get(i).text + "\"";
    }

    // ==================================================================
    // 2d. Messages for a recipient
    // ==================================================================
    public String[] getMessagesForRecipient(String recipient) {
        List<String> found = new ArrayList<>();
        String target = recipient == null ? "" : recipient.trim();
        for (int i = 0; i < recipients.length; i++) {
            if (!isDisregarded(statuses[i]) && recipients[i].equals(target)) {
                found.add(messages.get(i).text);
            }
        }
        return found.toArray(String[]::new);
    }

    public String searchByRecipient(String recipient) {
        String[] found = getMessagesForRecipient(recipient);
        if (found.length == 0) return "No messages found for that recipient.";
        StringBuilder sb = new StringBuilder();
        for (String t : found) sb.append("\"").append(t).append("\"\n");
        return sb.toString().trim();
    }

    // ==================================================================
    // 2e. Delete by hash
    // ==================================================================
    public String deleteByMessageHash(String hash) {
        int i = indexOf(messageHash, hash);
        if (i == -1) return "No message found with hash: " + hash;
        MessageRecord removed = messages.remove(i);
        populateArrays();
        if (STORED.equalsIgnoreCase(removed.status)) {
            saveStoredMessagesToJSON();
        }
        String shown = removed.text.endsWith(".")
                ? removed.text.substring(0, removed.text.length() - 1)
                : removed.text;
        return "Message: \"" + shown + "\" successfully deleted.";
    }

    // ==================================================================
    // 2f. Report
    // ==================================================================
    public String displayReport() {
        StringBuilder report = new StringBuilder("===== MESSAGE REPORT =====\n");
        int count = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (isDisregarded(statuses[i])) continue;
            count++;
            report.append("\nMessage ").append(count).append("\n")
                  .append("Message Hash: ").append(messageHash[i]).append("\n")
                  .append("Message ID: ").append(messageID[i]).append("\n")
                  .append("Sender: Developer\n")
                  .append("Recipient: ").append(recipients[i]).append("\n")
                  .append("Message: ").append(messages.get(i).text).append("\n")
                  .append("Status: ").append(statuses[i]).append("\n");
        }
        return count == 0 ? "There are no messages to report." : report.toString();
    }

    // ==================================================================
    // JSON READ — keys match Message.storeMessagesInJson()
    //   messageID, messageHash, recipient, message
    //   (no status, no sender — infer status = "Stored")
    // ==================================================================
    private void loadStoredMessagesFromJSON() {
        Path path = Paths.get(jsonFile);
        if (!Files.exists(path)) return;
        String json;
        try {
            json = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("The stored messages could not be read: " + e.getMessage());
            return;
        }
        Matcher om = JSON_OBJECT.matcher(json);
        while (om.find()) {
            Map<String, String> fields = new LinkedHashMap<>();
            Matcher fm = JSON_FIELD.matcher(om.group(1));
            while (fm.find()) fields.put(fm.group(1), unescapeJSON(fm.group(2)));
            if (fields.containsKey("message")) {
                 messages.add(new MessageRecord( 
                         fields.getOrDefault("messageID", ""),
                         fields.getOrDefault("messageHash", ""),
                         fields.getOrDefault("recipient", ""), 
                         fields.get("message"), 
                         fields.getOrDefault("status", STORED)));
            }
        }
    }

    // ==================================================================
    // JSON WRITE — matches Message.storeMessagesInJson() exactly
    // ==================================================================
    private void saveStoredMessagesToJSON() {
        StringBuilder json = new StringBuilder("[\n");
        boolean first = true;
        for (MessageRecord m : messages) {
            if (!STORED.equalsIgnoreCase(m.status)) continue;
            if (!first) json.append(",\n");
            first = false;
            json.append("  {\n")
                .append("    \"messageID\": \"").append(escapeJSON(m.id)).append("\",\n")
                .append("    \"messageHash\": \"").append(escapeJSON(m.hash)).append("\",\n")
                .append("    \"recipient\": \"").append(escapeJSON(m.recipient)).append("\",\n")
                .append("    \"message\": \"").append(escapeJSON(m.text)).append("\"\n")
                .append("  }");
        }
        json.append("\n]");
        try {
            Files.writeString(Paths.get(jsonFile), json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("The stored messages could not be saved: " + e.getMessage());
        }
    }

    private static String escapeJSON(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r")
                .replace("\t", "\\t").replace("\b", "\\b").replace("\f", "\\f");
    }

    private static String unescapeJSON(String v) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (c == '\\' && i + 1 < v.length()) {
                i++;
                switch (v.charAt(i)) {
                    case 'n' -> r.append('\n');
                    case 'r' -> r.append('\r');
                    case 't' -> r.append('\t');
                    case 'b' -> r.append('\b');
                    case 'f' -> r.append('\f');
                    case '"' -> r.append('"');
                    case '\\' -> r.append('\\');
                    default -> r.append(v.charAt(i));
                }
            } else r.append(c);
        }
        return r.toString();
    }

    // ==================================================================
    // MENU — unchanged, all calls compile against the aligned API
    // ==================================================================
    public void showStoredMessagesMenu(Scanner input) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- STORED MESSAGES ---");
            System.out.println("a) Display the sender and recipient of all stored messages");
            System.out.println("b) Display the longest message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search for all messages for a recipient");
            System.out.println("e) Delete a message using the message hash");
            System.out.println("f) Display the message report");
            System.out.println("g) Back to the main menu");
            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim().toLowerCase();
            switch (choice) {
                case "a" -> System.out.println(displaySendersAndRecipients());
                case "b" -> System.out.println(displayLongestMessage());
                case "c" -> {
                    System.out.print("Enter the message ID: ");
                    System.out.println(searchByMessageID(input.nextLine()));
                }
                case "d" -> {
                    System.out.print("Enter the recipient cell number: ");
                    System.out.println(searchByRecipient(input.nextLine()));
                }
                case "e" -> {
                    System.out.print("Enter the message hash: ");
                    System.out.println(deleteByMessageHash(input.nextLine()));
                }
                case "f" -> System.out.println(displayReport());
                case "g" -> back = true;
                default -> System.out.println("Invalid option. Please enter a letter from a to g.");
            }
        }
    }
}
