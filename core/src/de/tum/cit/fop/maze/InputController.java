package de.tum.cit.fop.maze;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {
    public boolean up, down, left, right;
    public boolean run;

    /**
     * 每一帧调用这个方法来检查按键状态
     */
    public void update() {
        // 1. 重置状态
        up = false;
        down = false;
        left = false;
        right = false;
        run = false;

        // 2. 检查键盘输入 (支持 WASD 和 方向键)
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            up = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            down = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            left = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            right = true;
        }

        // 3. 检查是否加速 (Shift)
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            run = true;
        }
    }
}