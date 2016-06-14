package the.bytecode.club.jda.api;

import the.bytecode.club.jda.JDA;
import the.bytecode.club.jda.Resources;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A simple class designed to show exceptions in the UI.
 *
 * @author Konloch
 */

public class ExceptionUI extends JFrame
{

    private static final long serialVersionUID = -5230501978224926296L;

    /**
     * @param e The exception to be shown
     */
    public ExceptionUI(Throwable e)
    {
        setup(e, "https://github.com/ecx86/jda/issues");
    }

    /**
     * @param e The exception to be shown
     */
    public ExceptionUI(String e)
    {
        setup(e, "https://github.com/ecx86/jda/issues");
    }

    /**
     * @param e      The exception to be shown
     * @param author the author of the plugin throwing this exception.
     */
    public ExceptionUI(Throwable e, String author)
    {
        setup(e, author);
    }

    /**
     * @param e      The exception to be shown
     * @param author the author of the plugin throwing this exception.
     */
    public ExceptionUI(String e, String author)
    {
        setup(e, author);
    }

    private void setup(Throwable e, String author)
    {
        this.setIconImages(Resources.iconList);
        setSize(new Dimension(600, 400));
        setTitle("Java DisAssembler " + JDA.version + " - Stack Trace - Send this to " + author);
        getContentPane().setLayout(new CardLayout(0, 0));

        JTextArea txtrBytecodeViewerIs = new JTextArea();
        txtrBytecodeViewerIs.setDisabledTextColor(Color.BLACK);
        txtrBytecodeViewerIs.setWrapStyleWord(true);
        getContentPane().add(new JScrollPane(txtrBytecodeViewerIs), "name_140466576080695");
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        e.printStackTrace();

        txtrBytecodeViewerIs.setText("Java DisAssembler Version: " + JDA.version +
                ", Preview Copy: " + JDA.previewCopy +
                JDA.nl + JDA.nl + sw.toString());
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void setup(String e, String author)
    {
        this.setIconImages(Resources.iconList);
        setSize(new Dimension(600, 400));
        setTitle("Java DisAssembler " + JDA.version + " - Stack Trace - Send this to " + author);
        getContentPane().setLayout(new CardLayout(0, 0));

        JTextArea txtrBytecodeViewerIs = new JTextArea();
        txtrBytecodeViewerIs.setDisabledTextColor(Color.BLACK);
        txtrBytecodeViewerIs.setWrapStyleWord(true);
        getContentPane().add(new JScrollPane(txtrBytecodeViewerIs), "name_140466576080695");
        txtrBytecodeViewerIs.setText(e);
        System.err.println(e);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

}
