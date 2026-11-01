import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * The RPG store keeps its stock while the player is away.
 * Sold-out items get a chance to return each time the player re-enters.
 */
class Store {
    private static final Random random = new Random();
    private static final String[] ITEMS = {"Potion", "Iron Sword", "Health Herb"};
    private static final int RESTOCK_CHANCE_PERCENT = 50;

    private final HashMap<String, Integer> stock = new HashMap<>();
    private final HashMap<String, Integer> prices = new HashMap<>();

    Store() {
        prices.put("Potion", 10);
        prices.put("Iron Sword", 35);
        prices.put("Health Herb", 6);
        restockMissingItems();
    }

    void enter(game.Player player, Scanner scanner) {
        // Only sold-out items can be restocked when the player re-enters.
        restockMissingItems();

        boolean shopping = true;
        while (shopping) {
            System.out.println("\n--- Traveling Merchant ---");
            System.out.println("Your gold: " + player.getGold());
            if (stock.isEmpty()) {
                System.out.println("The store is sold out.");
            } else {
                int number = 1;
                for (Map.Entry<String, Integer> item : stock.entrySet()) {
                    System.out.println(number + ". " + item.getKey() + " - "
                            + prices.get(item.getKey()) + " gold (" + item.getValue() + " left)");
                    number++;
                }
            }
            System.out.println("0. Leave store");
            System.out.print("> ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) {
                shopping = false;
                continue;
            }

            int selected;
            try {
                selected = Integer.parseInt(choice) - 1;
            } catch (NumberFormatException exception) {
                System.out.println("Enter the number of an item or 0 to leave.");
                continue;
            }

            String item = getItemByNumber(selected);
            if (item == null) {
                System.out.println("That item is not available.");
                continue;
            }

            int price = prices.get(item);
            if (player.getGold() < price) {
                System.out.println("You do not have enough gold.");
                continue;
            }

            player.spendGold(price);
            player.addItem(item, 1);
            stock.put(item, stock.get(item) - 1);
            if (stock.get(item) == 0) {
                stock.remove(item);
                System.out.println("You bought the last " + item + ". Re-enter later to see if it restocks.");
            } else {
                System.out.println("You bought 1 " + item + ".");
            }
        }
    }

    private void restockMissingItems() {
        for (String item : ITEMS) {
            if (!stock.containsKey(item) && random.nextInt(100) < RESTOCK_CHANCE_PERCENT) {
                stock.put(item, 1 + random.nextInt(3));
            }
        }
    }

    private String getItemByNumber(int selected) {
        int number = 0;
        for (String item : stock.keySet()) {
            if (number == selected) {
                return item;
            }
            number++;
        }
        return null;
    }
}
