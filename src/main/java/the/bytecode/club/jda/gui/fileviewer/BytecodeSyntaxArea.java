package the.bytecode.club.jda.gui.fileviewer;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;

import javax.swing.event.HyperlinkEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytecodeSyntaxArea extends RSyntaxTextArea {
    public Map<Fold, List<Token>> tokenIndex;
    public boolean foldsBuilt;

    public BytecodeSyntaxArea() {
        setSyntaxEditingStyle(BytecodeTokenizer.SYNTAX_STYLE_BYTECODE);

        setLinkScanningMask(0);
        setLinkGenerator(new BytecodeLinkGenerator());
        addHyperlinkListener(this::processClick);

        foldsBuilt = false;
        getFoldManager().addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals(FoldManager.PROPERTY_FOLDS_UPDATED)
                    && evt.getNewValue() != null)
                parseLabels();
        });
    }

    private void processClick(HyperlinkEvent e) {
        URL url = e.getURL();
        String data = url.getFile();
        switch (url.getProtocol()) {
            case "setcaret":
                setCaretPosition(Integer.parseInt(data));
                break;
        }
    }

    private void parseLabels() {
        tokenIndex = new HashMap<>();
        FoldManager foldManager = getFoldManager();
        if (foldManager.getFoldCount() != 1) {
            System.err.println("Fold count isn't 1, rather " + foldManager.getFoldCount());
            return;
        }
        parseLabels(null, foldManager.getFold(0));
        foldsBuilt = true;
    }

    private void parseLabels(Fold parent, Fold f) {
        for (Token t = getTokenListForLine(f.getStartLine()); t != null; t = t.getNextToken()) {
            if (t.getType() == BytecodeTokenizer.TOKENTYPE_LABEL) {
                List<Token> methodTokens = tokenIndex.computeIfAbsent(parent, k -> new ArrayList<>());
                methodTokens.add(new TokenImpl(t));
                if (methodTokens.size() != Integer.parseInt(t.getLexeme().substring(1)))
                    throw new IllegalArgumentException("Invalid token numbering: " + methodTokens.size() + "vs" + Integer.parseInt(t.getLexeme().substring(1)));
                break;
            }
        }
        for (int i = 0; i < f.getChildCount(); i++) {
            parseLabels(f, f.getChild(i));
        }
    }

    private Fold getMethodFold(Token t) {
        FoldManager foldManager = getFoldManager();
        Fold rootFold = foldManager.getFold(0);
        Fold curFold = foldManager.getDeepestFoldContaining(t.getOffset());
        while (curFold != null) {
            Fold parentFold = curFold.getParent();
            if (parentFold == rootFold)
                return curFold;
            curFold = parentFold;
        }
        throw new IllegalArgumentException("Token is not parented in top-level (class def) fold");
    }

    private Token findLabelDefinition(Token label) {
        if (label.getType() != BytecodeTokenizer.TOKENTYPE_LABEL)
            throw new IllegalArgumentException("Token is not a label");
        Fold parentFold = getMethodFold(label);
        for (Token t : tokenIndex.get(parentFold)) {
            if (t.getLexeme().equals(label.getLexeme()))
                return t;
        }
        return null;
    }

    private class BytecodeLinkGenerator implements LinkGenerator {
        @Override
        public LinkGeneratorResult isLinkAtOffset(RSyntaxTextArea textArea, int offs) {
            Token t = textArea.modelToToken(offs);
            if (foldsBuilt && t.getType() == BytecodeTokenizer.TOKENTYPE_LABEL) {
                Token labelDef = findLabelDefinition(t);
                if (labelDef == null)
                    return null;
                int caretTarget = labelDef.getOffset();
                return new SetCaretLinkResult(caretTarget, textArea, offs);
            }
            return null;
        }
    }

    private class SetCaretLinkResult implements LinkGeneratorResult {
        private final int caretTarget;
        private final RSyntaxTextArea textArea;
        private final int offs;

        public SetCaretLinkResult(int caretTarget, RSyntaxTextArea textArea, int offs) {
            this.caretTarget = caretTarget;
            this.textArea = textArea;
            this.offs = offs;
        }

        @Override
        public HyperlinkEvent execute() {
            try {
                URL url = new URL(null, "setcaret:" + caretTarget, new JDAURLHandler());
                return new HyperlinkEvent(textArea, HyperlinkEvent.EventType.ACTIVATED, url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public int getSourceOffset() {
            return offs;
        }
    }
}
