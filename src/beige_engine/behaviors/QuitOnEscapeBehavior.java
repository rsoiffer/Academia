package beige_engine.behaviors;

import beige_engine.engine.Behavior;
import beige_engine.engine.Core;
import beige_engine.engine.Input;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class QuitOnEscapeBehavior extends Behavior {

    @Override
    public void step() {
        if (Input.keyJustPressed(GLFW_KEY_ESCAPE)) {
            Core.stopGame();
        }
    }
}
