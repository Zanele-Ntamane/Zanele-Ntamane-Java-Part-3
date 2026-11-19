package userregistrationsystem;

import java.util.Scanner;

/** Part 2: the QuickChat menu. Only reachable after a successful login. */
public class QuickChat {

    public static void run(Scanner input) {
        System.out.println("Welcome to QuickChat.");

        int numMessages = readInt(input, "How many messages do you wish to enter? ", 1);
        boolean messagesEntered = false;
        boolean running = true;

        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            int option = readInt(input, "Choose an option (1-3): ", 1);

            switch (option) {
                case 1:
                    if (messagesEntered) {
                        System.out.println("You have already entered all " + numMessages + " messages.");
                    } else {
                        enterMessages(input, numMessages);
                        messagesEntered = true;
                        System.out.println("Total messages sent: " + Message.returnTotalMessages());
                    }
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                default:
                    System.out.println("Please choose 1, 2 or 3.");
            }
        }
    }

    // For loop: the user can only enter the number of messages they set
    private static void enterMessages(Scanner input, int numMessages) {
        for (int i = 0; i < numMessages; i++) {
            System.out.println("\n--- Message " + (i + 1) + " of " + numMessages + " ---");

            String id = Message.generateMessageId();

            String recipient;
            while (true) {
                System.out.print("Recipient cell number (with international code): ");
                recipient = input.nextLine().trim();
                String result = Message.checkRecipientCell(recipient);
                System.out.println(result);
                if (result.equals("Cell phone number successfully captured.")) {
                    break;
                }
            }

            String text;
            while (true) {
                System.out.print("Enter your message (max 250 characters): ");
                text = input.nextLine();
                if (text.trim().isEmpty()) {
                    System.out.println("Message cannot be empty.");
                } else if (text.length() > 250) {
                    System.out.println("Please enter a message of less than 250 characters.");
                } else {
                    System.out.println("Message sent");
                    break;
                }
            }

            Message message = new Message(id, i, recipient, text);
            System.out.println("Message ID generated: " + message.getMessageId());

            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int choice = readInt(input, "Choose an option (1-3): ", 1);
            while (choice < 1 || choice > 3) {
                choice = readInt(input, "Please choose 1, 2 or 3: ", 1);
            }

            System.out.println(message.sentMessage(choice));

            if (choice == 1) {
                System.out.println("\n" + message.getDetails());
            } else if (choice == 2) {
                String del = input.nextLine().trim();
                while (!del.equals("0")) {
                    System.out.println("Press 0 to delete the message.");
                    del = input.nextLine().trim();
                }
                System.out.println("Message deleted.");
            }
        }
    }

    // Reads a whole number without crashing on bad input
    private static int readInt(Scanner input, String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to message below
            }
            System.out.println("Please enter a valid number.");
        }
    }
}