import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** A simple desktop window for the text-based RPG. */
class GameGUI extends JFrame {
    private final Random random = new Random();
    private final JTextArea story = new JTextArea();
    private final JLabel status = new JLabel();
    private final JButton exploreButton = new JButton("Explore");
    private final JButton campButton = new JButton("Rest at Camp");
    private final JButton storeButton = new JButton("Visit Store");
    private final JButton inventoryButton = new JButton("Inventory");
    private final HashMap<String, Integer> inventory = new HashMap<>();
    private final HashMap<String, Integer> storeStock = new HashMap<>();
    private final HashMap<String, Integer> prices = new HashMap<>();
    private String playerName;
    private int hp = 40;
    private int maxHp = 40;
    private int level = 1;
    private int gold = 20;
    private int experience = 0;
    private boolean campAvailable = true;
    private boolean storeAvailable = true;

    private GameGUI() {
        super("Console RPG - Desktop Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 450);
        setLocationRelativeTo(null);

        story.setEditable(false);
        story.setLineWrap(true);
        story.setWrapStyleWord(true);
        add(status, BorderLayout.NORTH);
        add(new JScrollPane(story), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 4, 8, 8));
        buttons.add(exploreButton);
        buttons.add(campButton);
        buttons.add(storeButton);
        buttons.add(inventoryButton);
        add(buttons, BorderLayout.SOUTH);

        exploreButton.addActionListener(event -> explore());
        campButton.addActionListener(event -> rest());
        storeButton.addActionListener(event -> visitStore());
        inventoryButton.addActionListener(event -> showInventory());
    }

    static void launch() {
        SwingUtilities.invokeLater(() -> {
            GameGUI game = new GameGUI();
            game.startGame();
        });
    }

    private void startGame() {
        playerName = JOptionPane.showInputDialog(this, "What is your hero's name?", "Create Hero",
                JOptionPane.QUESTION_MESSAGE);
        if (playerName == null) {
            System.exit(0);
        }
        playerName = playerName.trim();
        if (playerName.isEmpty()) {
            playerName = "Hero";
        }
        inventory.put("Potion", 2);
        prices.put("Potion", 10);
        prices.put("Iron Sword", 35);
        prices.put("Health Herb", 6);
        restockMissingItems();
        write("Welcome, " + playerName + "! Your adventure begins.");
        updateButtons();
        setVisible(true);
    }

    private void explore() {
        write("\nYou travel into the wilderness...");
        int event = random.nextInt(100);
        if (event < 70) {
            fight(new Enemy("Goblin", 18 + level * 4, 4 + level * 2));
        } else if (event < 90) {
            int found = 5 + random.nextInt(11);
            gold += found;
            write("You found " + found + " gold.");
        } else {
            heal(8);
            write("You found a healing spring and recovered 8 HP.");
        }
        refreshAvailability();
    }

    private void fight(Enemy enemy) {
        while (hp > 0 && enemy.hp > 0) {
            Object[] choices = {"Attack", "Use Potion", "Run"};
            int choice = JOptionPane.showOptionDialog(this,
                    playerName + " HP: " + hp + "/" + maxHp + "\n"
                            + enemy.name + " HP: " + enemy.hp,
                    "Battle", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, choices, choices[0]);
            if (choice == 0) {
                int damage = 5 + random.nextInt(6) + level;
                enemy.hp -= damage;
                write("You dealt " + damage + " damage to the " + enemy.name + ".");
            } else if (choice == 1) {
                if (getItemCount("Potion") == 0) {
                    write("You have no potions.");
                    continue;
                }
                removeItem("Potion", 1);
                heal(15);
                write("You used a potion and recovered 15 HP.");
            } else if (choice == 2) {
                if (random.nextBoolean()) {
                    write("You escaped safely.");
                    return;
                }
                write("You failed to escape.");
            } else {
                return;
            }
            if (enemy.hp > 0) {
                int damage = enemy.attackPower + random.nextInt(4);
                hp -= damage;
                write(enemy.name + " dealt " + damage + " damage.");
            }
        }
        if (hp <= 0) {
            JOptionPane.showMessageDialog(this, "You have fallen in battle. Game over!");
            disableButtons();
        } else if (enemy.hp <= 0) {
            experience += level * 10;
            gold += 5 + random.nextInt(8);
            addItem("Potion", 1);
            write("You defeated the " + enemy.name + " and found a Potion.");
            if (experience >= level * 30) {
                level++;
                maxHp += 8;
                hp = maxHp;
                write("Level up! You are now level " + level + ".");
            }
        }
        updateButtons();
    }

    private void rest() {
        if (!campAvailable) {
            write("Camp is unavailable right now. Explore and try again later.");
            return;
        }
        heal(10);
        write("You rested at camp and recovered 10 HP.");
        refreshAvailability();
    }

    private void visitStore() {
        if (!storeAvailable) {
            write("The merchant is not here right now. Explore and try again later.");
            return;
        }
        restockMissingItems();
        while (!storeStock.isEmpty()) {
            String[] items = storeStock.keySet().toArray(new String[0]);
            String selected = (String) JOptionPane.showInputDialog(this,
                    "Gold: " + gold + "\nChoose an item or Cancel to leave:",
                    "Traveling Merchant", JOptionPane.PLAIN_MESSAGE, null, items, items[0]);
            if (selected == null) {
                break;
            }
            int price = prices.get(selected);
            if (gold < price) {
                write("You do not have enough gold for a " + selected + ".");
            } else {
                gold -= price;
                addItem(selected, 1);
                storeStock.put(selected, storeStock.get(selected) - 1);
                if (storeStock.get(selected) == 0) {
                    storeStock.remove(selected);
                    write("You bought the last " + selected + ". It may restock next time.");
                } else {
                    write("You bought a " + selected + ".");
                }
            }
        }
        refreshAvailability();
    }

    private void showInventory() {
        write("\n--- Character ---\nLevel: " + level + "\nHP: " + hp + "/" + maxHp
                + "\nXP: " + experience + "\nGold: " + gold + "\nInventory: " + inventory);
    }

    private void refreshAvailability() {
        campAvailable = random.nextInt(100) < 65;
        storeAvailable = random.nextInt(100) < 65;
        updateButtons();
    }

    private void updateButtons() {
        campButton.setEnabled(campAvailable && hp > 0);
        storeButton.setEnabled(storeAvailable && hp > 0);
        exploreButton.setEnabled(hp > 0);
        inventoryButton.setEnabled(true);
        status.setText(playerName + " | HP: " + hp + "/" + maxHp + " | Gold: " + gold
                + " | Camp: " + (campAvailable ? "Open" : "Closed")
                + " | Store: " + (storeAvailable ? "Open" : "Closed"));
    }

    private void disableButtons() {
        exploreButton.setEnabled(false);
        campButton.setEnabled(false);
        storeButton.setEnabled(false);
    }

    private void restockMissingItems() {
        for (String item : prices.keySet()) {
            if (!storeStock.containsKey(item) && random.nextInt(100) < 50) {
                storeStock.put(item, 1 + random.nextInt(3));
            }
        }
    }

    private void addItem(String item, int amount) {
        inventory.put(item, getItemCount(item) + amount);
    }

    private void removeItem(String item, int amount) {
        int remaining = getItemCount(item) - amount;
        if (remaining <= 0) inventory.remove(item);
        else inventory.put(item, remaining);
    }

    private int getItemCount(String item) {
        return inventory.getOrDefault(item, 0);
    }

    private void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    private void write(String message) {
        story.append(message + "\n");
        updateButtons();
    }

    private static class Enemy {
        private final String name;
        private int hp;
        private final int attackPower;

        private Enemy(String name, int hp, int attackPower) {
            this.name = name;
            this.hp = hp;
            this.attackPower = attackPower;
        }
    }
}
