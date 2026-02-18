package projetCROMBEZ;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean enterPressed;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_Z)      upPressed = true;
        if (code == KeyEvent.VK_S)      downPressed = true;
        if (code == KeyEvent.VK_Q)      leftPressed = true;
        if (code == KeyEvent.VK_D)      rightPressed = true;
        if (code == KeyEvent.VK_ENTER)  enterPressed = true;

        // Flèches directionnelles en alternative
        if (code == KeyEvent.VK_UP)     upPressed = true;
        if (code == KeyEvent.VK_DOWN)   downPressed = true;
        if (code == KeyEvent.VK_LEFT)   leftPressed = true;
        if (code == KeyEvent.VK_RIGHT)  rightPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_Z)      upPressed = false;
        if (code == KeyEvent.VK_S)      downPressed = false;
        if (code == KeyEvent.VK_Q)      leftPressed = false;
        if (code == KeyEvent.VK_D)      rightPressed = false;
        if (code == KeyEvent.VK_ENTER)  enterPressed = false;

        if (code == KeyEvent.VK_UP)     upPressed = false;
        if (code == KeyEvent.VK_DOWN)   downPressed = false;
        if (code == KeyEvent.VK_LEFT)   leftPressed = false;
        if (code == KeyEvent.VK_RIGHT)  rightPressed = false;
    }
}
