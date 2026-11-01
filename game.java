import java.util.Random;
import java.util.Scanner;

/**
 * A small text-based RPG that runs in the console.
 */
public class game {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("       THE BEGINNING OF A QUEST  ");
        System.out.println("=================================");

        System.out.print("What is your hero's name? ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = "Hero";
        }

        Player player = new Player(name);
        System.out.println("\nWelcome, " + player.name + "! Your adventure begins.");

        boolean playing = true;
        while (playing && player.isAlive()) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Explore");
            System.out.println("2. View character");
            System.out.println("3. Rest at camp (+10 HP)");
            System.out.println("4. Quit");
            System.out.print("> ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    explore(player);
                    break;
                case "2":
                    player.showStatus();
                    break;
                case "3":
                    player.rest();
                    break;
                case "4":
                    playing = false;
                    System.out.println("You leave the adventure for another day.");
                    break;
                default:
                    System.out.println("Please enter 1, 2, 3, or 4.");
            }
        }

        if (!player.isAlive()) {
            System.out.println("\nYou have fallen in battle. Game over!");
        }
        scanner.close();
    }

    private static void explore(Player player) {
        System.out.println("\nYou travel into the wilderness...");
        int event = random.nextInt(100);

        if (event < 70) {
            Enemy enemy = createEnemy(player.level);
            System.out.println("A " + enemy.name + " appears!");
            battle(player, enemy);
        } else if (event < 90) {
            int goldFound = 5 + random.nextInt(11);
            player.gold += goldFound;
            System.out.println("You find " + goldFound + " gold on the road.");
        } else {
            player.heal(8);
            System.out.println("You discover a healing spring and recover 8 HP.");
        }
    }

    private static Enemy createEnemy(int playerLevel) {
        String[] names = {"Goblin", "Wolf", "Bandit", "Skeleton"};
        String name = names[random.nextInt(names.length)];
        int level = Math.max(1, playerLevel + random.nextInt(3) - 1);
        return new Enemy(name, level, 18 + level * 4, 4 + level * 2);
    }

    private static void battle(Player player, Enemy enemy) {
        while (player.isAlive() && enemy.isAlive()) {
            System.out.println("\n" + player.name + " HP: " + player.hp + "/" + player.maxHp
                    + " | " + enemy.name + " HP: " + enemy.hp);
            System.out.println("1. Attack");
            System.out.println("2. Use potion (" + player.potions + " left)");
            System.out.println("3. Run");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                int damage = player.attack();
                enemy.hp -= damage;
                System.out.println("You deal " + damage + " damage.");
            } else if (choice.equals("2")) {
                if (!player.usePotion()) {
                    System.out.println("You do not have any potions.");
                    continue;
                }
                System.out.println("You drink a potion and recover 15 HP.");
            } else if (choice.equals("3")) {
                if (random.nextBoolean()) {
                    System.out.println("You escaped safely.");
                    return;
                }
                System.out.println("You failed to escape!");
            } else {
                System.out.println("Choose 1, 2, or 3.");
                continue;
            }

            if (enemy.isAlive()) {
                int damage = enemy.attack();
                player.hp -= damage;
                System.out.println(enemy.name + " hits you for " + damage + " damage.");
            }
        }

        if (player.isAlive()) {
            int experience = enemy.level * 10;
            int gold = 3 + random.nextInt(8);
            player.experience += experience;
            player.gold += gold;
            System.out.println("You defeated the " + enemy.name + "! You gained "
                    + experience + " XP and " + gold + " gold.");
            player.checkLevelUp();
        }
    }

    private static class Player {
        private final String name;
        private int level = 1;
        private int experience = 0;
        private int maxHp = 40;
        private int hp = maxHp;
        private int gold = 0;
        private int potions = 2;

        private Player(String name) {
            this.name = name;
        }

        private boolean isAlive() {
            return hp > 0;
        }

        private int attack() {
            return 5 + random.nextInt(6) + level;
        }

        private boolean usePotion() {
            if (potions == 0) {
                return false;
            }
            potions--;
            heal(15);
            return true;
        }

        private void heal(int amount) {
            hp = Math.min(maxHp, hp + amount);
        }

        private void rest() {
            heal(10);
            System.out.println("You rest at camp and recover some health. HP: " + hp + "/" + maxHp);
        }

        private void checkLevelUp() {
            int needed = level * 30;
            if (experience >= needed) {
                level++;
                maxHp += 8;
                hp = maxHp;
                System.out.println("Level up! You are now level " + level + " and fully healed.");
            }
        }

        private void showStatus() {
            System.out.println("\n--- Character ---");
            System.out.println("Name: " + name);
            System.out.println("Level: " + level + " (" + experience + " XP)");
            System.out.println("HP: " + hp + "/" + maxHp);
            System.out.println("Gold: " + gold);
            System.out.println("Potions: " + potions);
        }
    }

    private static class Enemy {
        private final String name;
        private final int level;
        private int hp;
        private final int attackPower;

        private Enemy(String name, int level, int hp, int attackPower) {
            this.name = name;
            this.level = level;
            this.hp = hp;
            this.attackPower = attackPower;
        }

        private boolean isAlive() {
            return hp > 0;
        }

        private int attack() {
            return attackPower + random.nextInt(4);
        }
    }
}
