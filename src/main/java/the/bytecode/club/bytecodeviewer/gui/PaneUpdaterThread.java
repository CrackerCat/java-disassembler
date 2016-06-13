package the.bytecode.club.bytecodeviewer.gui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import the.bytecode.club.bytecodeviewer.BytecodeViewer;
import the.bytecode.club.bytecodeviewer.api.ExceptionUI;
import the.bytecode.club.bytecodeviewer.decompilers.Decompiler;

import javax.swing.*;
import java.awt.*;

/**
 * Updates a pane
 *
 * @author Konloch
 */
public class PaneUpdaterThread extends Thread
{

    private Decompiler decompiler;
    private int paneId;
    private JPanel target;
    private ClassViewer viewer;
    private JButton button;

    public PaneUpdaterThread(ClassViewer viewer, Decompiler decompiler, int paneId, JPanel target, JButton button)
    {
        this.decompiler = decompiler;
        this.paneId = paneId;
        this.target = target;
        this.viewer = viewer;
        this.button = button;
    }

    public void run()
    {
        try
        {
            final byte[] b = BytecodeViewer.getClassBytes(viewer.container, viewer.cn.name + ".class");
            if (decompiler != Decompiler.HEXCODE)
            {
                RSyntaxTextArea panelArea = new RSyntaxTextArea();
                panelArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                panelArea.setCodeFoldingEnabled(true);
                panelArea.setAntiAliasingEnabled(true);
                final RTextScrollPane scrollPane = new RTextScrollPane(panelArea);
                panelArea.setText(decompiler.decompileClassNode(viewer.cn, b));
                panelArea.setCaretPosition(0);
                panelArea.setEditable(viewer.isPaneEditable(paneId));
                scrollPane.setColumnHeaderView(new JLabel(decompiler.getName() + " Decompiler - Editable: " + panelArea.isEditable()));
                panelArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, (int) BytecodeViewer.viewer.fontSpinner.getValue()));

                SwingUtilities.invokeLater(() -> target.add(scrollPane));
                viewer.updatePane(paneId, panelArea, decompiler);
            }
            else
            {
                // initialize fallback
            }
        }
        catch (Exception e)
        {
            new ExceptionUI(e);
        }
        finally
        {
            viewer.resetDivider();
            BytecodeViewer.viewer.setIcon(false);
            if (button != null)
                button.setEnabled(true);
        }
    }
}