package beige_engine.samples;

import beige_engine.core.Core;
import beige_engine.core.Input;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class QuitOnEscapeBehavior extends Behavior {

    @Override
    public void onStep() {
        if (Input.keyJustPressed(GLFW_KEY_ESCAPE)) {
            Core.stopGame();
        }
    }
}
