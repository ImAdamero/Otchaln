package pl.otchlan;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otchlan.gui.OtchlanScreen;

public class OtchlanClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("otchlan");

    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        OtchlanConfig.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.otchlan.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.otchlan"));

        // Reaguje TYLKO na wpisana komende (np. /otchlan). Inne komendy sa ignorowane.
        ClientSendMessageEvents.COMMAND.register(AutoDumper::onCommand);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new OtchlanScreen(null));
            }
            AutoDumper.tick(client);
        });
    }
}
