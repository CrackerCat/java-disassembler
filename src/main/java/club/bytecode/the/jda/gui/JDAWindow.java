package club.bytecode.the.jda.gui;

import club.bytecode.the.jda.settings.IPersistentWindow;
import org.objectweb.asm.tree.ClassNode;
import club.bytecode.the.jda.FileChangeNotifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;

/**
 * Used to represent all the panes inside of Bytecode Viewer, this is temp code
 * that was included from porting in J-RET, this needs to be re-written.
 *
 * @author Konloch
 * @author WaterWolf
 */

public abstract class JDAWindow extends JInternalFrame implements FileChangeNotifier, IPersistentWindow {
    private String windowId;

    public Point unmaximizedPos;
    public Dimension unmaximizedSize; // unmaximized size for when JDA is maximized
    public Dimension smallUnmaxSize; // unmaximized size for when JDA is unmaximized

    protected final MainViewerGUI viewer;

    public JDAWindow(final String id, final String title, final Icon icon, final MainViewerGUI viewer) {
        super(title, true, true, true, true);
        windowId = id;
        setName(title);
        setFrameIcon(icon);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        this.viewer = viewer;

        unmaximizedPos = getDefaultPosition();
        unmaximizedSize = getDefaultSize();
        smallUnmaxSize = unmaximizedSize;

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!isMaximum()) {
                    if (viewer.isMaximized)
                        unmaximizedSize = getSize();
                    else
                        smallUnmaxSize = getSize();
                }
                super.componentResized(e);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (!isMaximum())
                    unmaximizedPos = getLocation();
                super.componentMoved(e);
            }
        });

        addPropertyChangeListener(evt ->
        {
            if (isNormalState()) {
                setSize(viewer.isMaximized ? unmaximizedSize : smallUnmaxSize);
                setLocation(unmaximizedPos);
            }
        });
    }

    public void onJDAResized() {
        smallUnmaxSize = new Dimension(unmaximizedSize);
        Dimension d = getDesktopPane().getSize();
        if (unmaximizedPos.getX() < d.width) {
            if (unmaximizedPos.getX() + smallUnmaxSize.width > d.width)
                smallUnmaxSize.width = d.width - (int) unmaximizedPos.getX();
            if (unmaximizedPos.getY() + smallUnmaxSize.height > d.height)
                smallUnmaxSize.height = d.height - (int) unmaximizedPos.getY();
        }
        if (isNormalState())
            setSize(smallUnmaxSize);
    }

    public void onJDAMaximized() {
        if (isNormalState())
            setSize(unmaximizedSize);
    }

    @Override
    public abstract void openClassFile(final String name, String container, final ClassNode cn);

    @Override
    public abstract void openFile(final String name, String container, byte[] contents);

    protected static Dimension defaultDimensions;
    protected static Point defaultPosition;

    public abstract Dimension getDefaultSize();

    public abstract Point getDefaultPosition();

    @Override
    public String getWindowId() {
        return windowId;
    }

    public static int
            MAXIMIZED = 1 << 0,
            MINIMIZED = 1 << 1,
            VISIBLE = 1 << 2;

    @Override
    public int getState() {
        int state = 0;
        if (isMaximum())
            state |= MAXIMIZED;
        if (isIcon())
            state |= MINIMIZED;
        if (isVisible())
            state |= VISIBLE;
        return state;
    }

    @Override
    public void restoreState(int state) {
        try {
            setMaximum((state & MAXIMIZED) != 0);
            setIcon((state & MINIMIZED) != 0);
            setVisible((state & VISIBLE) != 0);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Point getPersistentPosition() {
        return unmaximizedPos;
    }

    @Override
    public void restorePosition(Point pos) {
        unmaximizedPos = pos;
        if (isNormalState())
            setLocation(pos);
    }

    @Override
    public Dimension getPersistentSize() {
        return unmaximizedSize;
    }

    @Override
    public void restoreSize(Dimension size) {
        unmaximizedSize = size;
        smallUnmaxSize = size;
        if (isNormalState()) {
            setPreferredSize(size);
            pack();
        }
    }

    @Override
    public boolean isNormalState() {
        return !isMaximum() && !isIcon();
    }
}
