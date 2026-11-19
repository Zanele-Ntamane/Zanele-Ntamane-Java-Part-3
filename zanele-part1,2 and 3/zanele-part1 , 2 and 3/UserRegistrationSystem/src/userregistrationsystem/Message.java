package userregistrationsystem;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Message {

    private static final int MAX_LENGTH = 250;

   
// Message Variables
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String message;
    private final String messageHash;
    private String messageStatus;

    // Messages sent, disregarded and stored while the program runs
    private static final ArrayList<Message> sentMessages = new ArrayList<>();
    private static final ArrayList<Message> disregardedMessages = new ArrayList<>();
    private static final ArrayList<Message> storedMessages = new ArrayList<>();

    // Total number of messages sent
    private static int totalMessages = 0;
    
    //Constructors

    public Message(String messageID, int messageNumber, String recipient, String message) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.message = message;
        this.messageHash = createMessageHash();
        this.messageStatus = "Created";
    }

    // =======================================================================
    // QUICKCHAT MENU (called after a successful login in Part 1)
    // =======================================================================

    public static void startQuickChat(Scanner input) {

        System.out.println("\nWelcome to QuickChat.");

        // Ask the user decides how many messages they want to enter
        int numberOfMessages = 0;
        while (numberOfMessages < 1) {
            System.out.print("How many messages would you like to enter? ");
            try {
                numberOfMessages = Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                numberOfMessages = 0;
            }
            if (numberOfMessages < 1) {
                System.out.println("Please enter a whole number greater than 0.");
            }
        }

          boolean messagesEntered = false;
          boolean running = true;
        StoredMessages storedMessages=new StoredMessages();

        // The application runs until the user selects quit
        while (running) {

            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim();

            if (choice.equals("1")) {
                if (messagesEntered) {
                    System.out.println("You have already entered all " + numberOfMessages + " messages.");
                } else {
                    sendMessages(input, numberOfMessages,storedMessages);
                    messagesEntered = true;
                }
            } else if (choice.equals("2")) {
                System.out.println(printMessages());
            } else if (choice.equals("3")) {
                System.out.println("Thank you for using QuickChat. Goodbye!");
                running = false;
            } else if (choice.equals("4")){
                storedMessages.showStoredMessagesMenu(input);
            }
            
            else {
                System.out.println("Invalid option. Please enter 1, 2 ,3 or 4.");
            }
        } 
    }

    // ==========================================
    // SEND MESSAGES
    // ==========================================

    public static void sendMessages(Scanner input, int numberOfMessages, StoredMessages storedMessages) {

        // The for loop only lets the user enter the number of messages they chose.
        // The loop counter i is used as the message number.
        for (int i = 0; i < numberOfMessages; i++) {

            System.out.println("\n--- MESSAGE " + (i + 1) + " of " + numberOfMessages + " ---");

            String messageID = generateMessageID();
            System.out.println("Message ID generated: " + messageID);

            // Recipient (same rules as the Part 1 cell phone check)
            String recipient;
            while (true) {
                System.out.print("Enter recipient cellphone number (with international code): ");
                recipient = input.nextLine().trim();
                String result = checkRecipientCell(recipient);
                System.out.println(result);
                if (result.equals("Cell phone number successfully captured.")) {
                    break;
                }
            }

            // Message text
            String messageText;
            while (true) {
                System.out.print("Enter your message: ");
                messageText = input.nextLine();

                if (messageText.trim().isEmpty()) {
                    System.out.println("Message cannot be empty.");
                } else if (messageText.length() > MAX_LENGTH) {
                 System.out.println("Please enter a message of 250 characters or fewer.");
                    System.out.println(checkMessageLength(messageText));
                } else {
                    System.out.println("Message sent");
                    break;
                }
            }

            Message newMessage = new Message(messageID, i + 1, recipient, messageText);
            System.out.println("Message Hash: " + newMessage.getMessageHash());

            // Send / disregard / store
            String result;
            do {
                result = newMessage.SentMessage(input);
            } while (result.equals("Invalid option, please try again."));

            System.out.println("\n" + result);

            // A disregarded message is removed when the user presses 0
           if (newMessage.messageStatus.equals("Disregarded")) {

    String deleteOption;

    do {
        System.out.println("Press 0 to delete the message.");
        deleteOption = input.nextLine().trim();

    } while (!deleteOption.equals("0"));

    newMessage.deleteMessage();

    System.out.println("Message deleted.");
}

// Add the message to Part 3
storedMessages.addMessage(newMessage);

            // Full details are shown after a message is sent
            if (newMessage.messageStatus.equals("Sent")) {
                System.out.println("\n" + newMessage.getDetails());
            }
        }

        System.out.println("\nTotal messages sent: " + returnTotalMessages());
    }

    // ==========================================
    // MESSAGE ID
    // ==========================================

    // Builds a random ten-digit ID one digit at a time using substring and a loop counter
    public static String generateMessageID() {
        String numbers = "0123456789";
        Random random = new Random();
        String generatedID = "";

        for (int i = 0; i < 10; i++) {
            int position = random.nextInt(numbers.length());
            generatedID = generatedID + numbers.substring(position, position + 1);
        }
        return generatedID;
    }

    // The message ID may not be more than ten characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    public String displayMessageID() {
        return "Message ID generated: " + messageID;
    }

    // ==========================================
    // VALIDATION
    // ==========================================

    // Reuses the Part 1 cell phone rules (international code required)
    public static String checkRecipientCell(String cell) {
        Login login = new Login("", "");
        if (login.checkCellPhoneNumber(cell)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public static String checkMessageLength(String text) {
        if (text.length() <= MAX_LENGTH) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (text.length() - MAX_LENGTH)
                + "; please reduce the size.";
    }

    // ==========================================
    // MESSAGE HASH: first two ID digits : message number : FIRSTLAST
    // ==========================================

    public String createMessageHash() {
        String firstTwoNumbers = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;

        String firstWord = "";
        String lastWord = "";

        if (message != null && !message.trim().isEmpty()) {
            String[] words = message.trim().split("\\s+");
            firstWord = words[0].replaceAll("[^a-zA-Z0-9]", "");
            lastWord = words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "");
        }

        return (firstTwoNumbers + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    // ==========================================
    // SEND / DISREGARD / STORE
    // ==========================================

    // Asks the user what to do with the message
    public String SentMessage(Scanner input) {
        System.out.println("\n1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        System.out.print("Choose an option: ");

        String option = input.nextLine().trim();

        if (option.matches("[123]")) {
            return SentMessage(Integer.parseInt(option));
        }
        return "Invalid option, please try again.";
    }

    // Does the work for the chosen option (also used by the unit tests)
    public String SentMessage(int option) {
        switch (option) {
            case 1:
                messageStatus = "Sent";
                sentMessages.add(this);
                totalMessages++;
                return "Message successfully sent.";
            case 2:
                messageStatus = "Disregarded";
                disregardedMessages.add(this);
                return "Press 0 to delete the message.";
            case 3:
                messageStatus = "Stored";
                storedMessages.add(this);
                storeMessagesInJson();
                return "Message successfully stored.";
            default:
                return "Invalid option, please try again.";
        }
    }

    public void deleteMessage() {
        disregardedMessages.remove(this);
        messageStatus = "Deleted";
    }

    // ==========================================
    // DISPLAY
    // ==========================================

    // Details in the required order: ID, Hash, Recipient, Message
    public String getDetails() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient
                + "\nMessage: " + message;
    }

    // Returns all messages sent while the program is running
    public static String printMessages() {
        StringBuilder allMessages = new StringBuilder();
        for (Message current : sentMessages) {
            allMessages.append(current.getDetails()).append("\n\n");
        }
        return allMessages.toString();
    }

    public static int returnTotalMessages() {
        return totalMessages;
    }

    // Used between unit tests
    public static void resetMessages() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        totalMessages = 0;
    }

    public String getMessageID() { return messageID; }
    public String getMessageHash() { return messageHash; }
    
    //Accessors for StoredMessages
     public String getRecipient()     { return recipient; }
     public String getMessageText()   { return message; }
     public String getStatus()        { return messageStatus; }
     public int    getMessageNumber() { return messageNumber; }

    // ==========================================
    // STORE MESSAGES IN JSON FILE
    // ==========================================

    /*
     * Attribution: the JSON layout follows the format described on
     * json.org (Ecma International, 2017) and the FileWriter usage
     * was adapted from the Oracle (2024) Java SE FileWriter documentation.
     */
    private static void storeMessagesInJson() {
        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < storedMessages.size(); i++) {
            Message stored = storedMessages.get(i);

            json.append("  {\n");
            json.append("    \"messageID\": \"").append(escapeJson(stored.messageID)).append("\",\n");
            json.append("    \"messageHash\": \"").append(escapeJson(stored.messageHash)).append("\",\n");
            json.append("    \"recipient\": \"").append(escapeJson(stored.recipient)).append("\",\n");
            json.append("    \"message\": \"").append(escapeJson(stored.message)).append("\"\n");
            json.append("  }");
            if (i < storedMessages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]");

        try (FileWriter writer = new FileWriter("storedMessages.json")) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.out.println("The messages could not be stored: " + e.getMessage());
        }
    }

    // Stops quotes and backslashes from breaking the JSON file
    private static String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}
