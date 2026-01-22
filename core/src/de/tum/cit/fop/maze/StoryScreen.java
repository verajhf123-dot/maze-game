package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StoryScreen implements Screen {

    private final MazeRunnerGame game;
    private final SpriteBatch batch;
    private final Texture[] storyImages;
    private final Texture titleLogo; // 替换 BitmapFont，使用你的艺术字图片
    private int currentIndex = 0;

    public StoryScreen(MazeRunnerGame game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // 1. 加载你的艺术字图片 (请确保文件放在 assets/slides/ 目录下)
        titleLogo = new Texture(Gdx.files.internal("slides/title_logo.png"));

        // 2. 加载背景图序列 (slide0 到 slide3)
        storyImages = new Texture[4];
        for (int i = 0; i < storyImages.length; i++) {
            storyImages[i] = new Texture(Gdx.files.internal("slides/slide" + i + ".png"));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 点击切换逻辑
        if (Gdx.input.justTouched()) {
            currentIndex++;
            if (currentIndex >= storyImages.length) {
                game.setScreen(new MenuScreen(game, game.getSettingsManager()));
                return;
            }
        }

        batch.begin();
        if (currentIndex < storyImages.length) {
            // 首先绘制全屏背景图
            batch.draw(storyImages[currentIndex], 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            // 3. 核心修改：仅在第 0 张图绘制艺术字图片
            if (currentIndex == 0) {
                drawArtisticTitle();
            }
        }
        batch.end();
    }

    private void drawArtisticTitle() {
        // 1. 字体再放大一点点：从 0.55f 增加到 0.62f
        float logoWidth = Gdx.graphics.getWidth() * 0.62f;

        // 2. 保持比例计算高度
        float logoHeight = logoWidth * ((float) titleLogo.getHeight() / titleLogo.getWidth());

        // 3. 水平居中
        float x = (Gdx.graphics.getWidth() - logoWidth) / 2;

        // 4. 垂直位置：在正中心的基础上往上挪一点点
        // (Gdx.graphics.getHeight() - logoHeight) / 2 是绝对正中心
        // 我们在后面加上屏幕高度的 5% (0.05f)，这样它就会稍微偏上一点点，视觉上更舒服
        float y = ((Gdx.graphics.getHeight() - logoHeight) / 2) + (Gdx.graphics.getHeight() * 0.05f);

        // 5. 绘制
        batch.draw(titleLogo, x, y, logoWidth, logoHeight);
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleLogo.dispose(); // 记得销毁图片资源
        for (Texture tex : storyImages) {
            tex.dispose();
        }
    }

    // 其他生命周期方法保持为空
    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}