package clov.drunkmessages;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Random;

public class MakeDrunkText {

    private static final Random RANDOM = new Random();

    public static void initialize() {
        // Message catcher
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {

            float alcohol = getAlcohol(sender);

            // log
            System.out.println("[BreweryDrunk] Player check " + sender.getName().getString() + " | Alcohol_level: " + alcohol);

            // alcohol level check
            if (alcohol < 5.0f) {
                return true; 
            }

            String original = message.getContent().getString();
            String drunk = distort(original, alcohol);

            MinecraftServer server = sender.getCommandSource().getServer();
            
            // Send message
            server.getPlayerManager().broadcast(
                    Text.literal("<" + sender.getName().getString() + "> " + drunk),
                    false
            );

            // Deny original
            return false; 
        });
    }

    // BREWERY REFLECT

    private static float getAlcohol(ServerPlayerEntity player) {
        if (player == null) return 0f;

        try {
            // Find AlcoholManager
            Class<?> alcoholManagerClass = Class.forName("eu.pb4.brewery.drink.AlcoholManager");
            
            // Dynamic of finder
            java.lang.reflect.Method ofMethod = null;
            for (java.lang.reflect.Method m : alcoholManagerClass.getMethods()) {
                if (m.getName().equals("of") && m.getParameterCount() == 1) {
                    ofMethod = m;
                    break;
                }
            }
            
            if (ofMethod == null) {
                System.out.println("[BreweryDrunk] Method of() in AlcoholManager not found!");
                return 0f;
            }
            
            // Call Manager
            Object managerInstance = ofMethod.invoke(null, player);
            
            if (managerInstance != null) {
                // Get alcohol level
                java.lang.reflect.Field alcoholLevelField = alcoholManagerClass.getField("alcoholLevel");
                double alcoholLevel = alcoholLevelField.getDouble(managerInstance);
                
                return (float) alcoholLevel;
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[BreweryDrunk] Brewery is not found");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[BreweryDrunk] Error occurred while accessing Brewery data");
        }

        return 0f;
    }

    // Text distortion

    private static String distort(String input, float alcohol) {
        StringBuilder out = new StringBuilder();
        
        // get alcohol strength
        float strength = Math.min(alcohol / 40f, 12.0f);

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                out.append(c);
                continue;
            }

            // Distortion chance
            float distortionChance = Math.min(0.20f + (strength * 0.05f), 0.75f);

            if (RANDOM.nextFloat() < distortionChance) {
                // Randomly choose distortion type
                int roll = RANDOM.nextInt(10); 

                if (roll < 7) { // 70% distortion chance
                    // Choose a random number of repeats based on alcohol strength
                    int repeats = 2 + RANDOM.nextInt(Math.max(1, (int)(strength / 3)));
                    for (int i = 0; i < repeats; i++) {
                        // Randomly capitalize some letters for extra slurring effect
                        if (RANDOM.nextFloat() < 0.15f) {
                            out.append(Character.toUpperCase(c));
                        } else {
                            out.append(c);
                        }
                    }
                } 
                else if (roll == 7) { // 20% chance — stuttering effect (e.g. "h-hello")
                    out.append(c).append("-");
                    if (RANDOM.nextFloat() < 0.5f) {
                        out.append(c).append("-");
                    }
                    out.append(c);
                } 
                else if (roll == 8) { // 10% chance
                    out.append(Character.toUpperCase(c));
                } 
                else { // 10% chance — complete character replacement with a random symbol
                    out.append("*");
                }
            } else {
                out.append(c);
            }
        }

        // If very drunk, add a trailing slurred phrase
        if (strength > 3.0f) {
            float trailingRoll = RANDOM.nextFloat();
            if (trailingRoll < 0.25f) {
                out.append("... ");
            //} else if (trailingRoll < 0.50f) {
                //out.append(" ... ик!");
            //} else if (trailingRoll < 0.70f) {
                //out.append(" ---т-о");
            }
        }

        return out.toString();
    }

}