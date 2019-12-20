package engine.samples;

import engine.core.Core;
import engine.core.Input;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class QuitOnEscapeBehavior extends Behavior {

    @Override
    public void onStep() {
        if (Input.keyJustPressed(GLFW_KEY_ESCAPE)) {
            Core.stopGame();
        }
    }
}
