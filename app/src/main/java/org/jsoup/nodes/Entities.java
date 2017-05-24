package org.jsoup.nodes;

import org.jsoup.SerializationException;
import org.jsoup.helper.DataUtil;
import org.jsoup.helper.StringUtil;
import org.jsoup.parser.CharacterReader;
import org.jsoup.parser.Parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import static org.jsoup.nodes.Entities.EscapeMode.base;
import static org.jsoup.nodes.Entities.EscapeMode.extended;


public class Entities {
    private static final int empty = -1;
    private static final String emptyName = "";
    static final int codepointRadix = 36;

    public enum EscapeMode {
        
        xhtml("entities-xhtml.properties", 4),
        
        base("entities-base.properties", 106),
        
        extended("entities-full.properties", 2125);

        
        private String[] nameKeys;
        private int[] codeVals; 

        
        private int[] codeKeys; 
        private String[] nameVals;

        EscapeMode(String file, int size) {
            load(this, file, size);
        }

        int codepointForName(final String name) {
            int index = Arrays.binarySearch(nameKeys, name);
            return index >= 0 ? codeVals[index] : empty;
        }

        String nameForCodepoint(final int codepoint) {
            final int index = Arrays.binarySearch(codeKeys, codepoint);
            if (index >= 0) {
                
                
                return (index < nameVals.length - 1 && codeKeys[index + 1] == codepoint) ?
                    nameVals[index + 1] : nameVals[index];
            }
            return emptyName;
        }

        private int size() {
            return nameKeys.length;
        }
    }

    private static final HashMap<String, String> multipoints = new HashMap<String, String>(); 

    private Entities() {
    }

    
    public static boolean isNamedEntity(final String name) {
        return extended.codepointForName(name) != empty;
    }

    
    public static boolean isBaseNamedEntity(final String name) {
        return base.codepointForName(name) != empty;
    }

    
    public static Character getCharacterByName(String name) {
        return (char) extended.codepointForName(name);
    }

    
    public static String getByName(String name) {
        String val = multipoints.get(name);
        if (val != null)
            return val;
        int codepoint = extended.codepointForName(name);
        if (codepoint != empty)
            return new String(new int[]{codepoint}, 0, 1);
        return emptyName;
    }

    public static int codepointsForName(final String name, final int[] codepoints) {
        String val = multipoints.get(name);
        if (val != null) {
            codepoints[0] = val.codePointAt(0);
            codepoints[1] = val.codePointAt(1);
            return 2;
        }
        int codepoint = extended.codepointForName(name);
        if (codepoint != empty) {
            codepoints[0] = codepoint;
            return 1;
        }
        return 0;
    }

    static String escape(String string, Document.OutputSettings out) {
        StringBuilder accum = new StringBuilder(string.length() * 2);
        try {
            escape(accum, string, out, false, false, false);
        } catch (IOException e) {
            throw new SerializationException(e); 
        }
        return accum.toString();
    }

    
    static void escape(Appendable accum, String string, Document.OutputSettings out,
                       boolean inAttribute, boolean normaliseWhite, boolean stripLeadingWhite) throws IOException {

        boolean lastWasWhite = false;
        boolean reachedNonWhite = false;
        final EscapeMode escapeMode = out.escapeMode();
        final CharsetEncoder encoder = out.encoder();
        final CoreCharset coreCharset = CoreCharset.byName(encoder.charset().name());
        final int length = string.length();

        int codePoint;
        for (int offset = 0; offset < length; offset += Character.charCount(codePoint)) {
            codePoint = string.codePointAt(offset);

            if (normaliseWhite) {
                if (StringUtil.isWhitespace(codePoint)) {
                    if ((stripLeadingWhite && !reachedNonWhite) || lastWasWhite)
                        continue;
                    accum.append(' ');
                    lastWasWhite = true;
                    continue;
                } else {
                    lastWasWhite = false;
                    reachedNonWhite = true;
                }
            }
            
            if (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                final char c = (char) codePoint;
                
                switch (c) {
                    case '&':
                        accum.append("&amp;");
                        break;
                    case 0xA0:
                        if (escapeMode != EscapeMode.xhtml)
                            accum.append("&nbsp;");
                        else
                            accum.append("&#xa0;");
                        break;
                    case '<':
                        
                        if (!inAttribute || escapeMode == EscapeMode.xhtml)
                            accum.append("&lt;");
                        else
                            accum.append(c);
                        break;
                    case '>':
                        if (!inAttribute)
                            accum.append("&gt;");
                        else
                            accum.append(c);
                        break;
                    case '"':
                        if (inAttribute)
                            accum.append("&quot;");
                        else
                            accum.append(c);
                        break;
                    default:
                        if (canEncode(coreCharset, c, encoder))
                            accum.append(c);
                        else
                            appendEncoded(accum, escapeMode, codePoint);
                }
            } else {
                final String c = new String(Character.toChars(codePoint));
                if (encoder.canEncode(c)) 
                    accum.append(c);
                else
                    appendEncoded(accum, escapeMode, codePoint);
            }
        }
    }

    private static void appendEncoded(Appendable accum, EscapeMode escapeMode, int codePoint) throws IOException {
        final String name = escapeMode.nameForCodepoint(codePoint);
        if (name != emptyName) 
            accum.append('&').append(name).append(';');
        else
            accum.append("&#x").append(Integer.toHexString(codePoint)).append(';');
    }

    static String unescape(String string) {
        return unescape(string, false);
    }

    
    static String unescape(String string, boolean strict) {
        return Parser.unescapeEntities(string, strict);
    }

    
    private static boolean canEncode(final CoreCharset charset, final char c, final CharsetEncoder fallback) {
        
        switch (charset) {
            case ascii:
                return c < 0x80;
            case utf:
                return true; 
            default:
                return fallback.canEncode(c);
        }
    }

    private enum CoreCharset {
        ascii, utf, fallback;

        private static CoreCharset byName(String name) {
            if (name.equals("US-ASCII"))
                return ascii;
            if (name.startsWith("UTF-")) 
                return utf;
            return fallback;
        }
    }

    private static final char[] codeDelims = {',', ';'};

    private static void load(EscapeMode e, String file, int size) {
        e.nameKeys = new String[size];
        e.codeVals = new int[size];
        e.codeKeys = new int[size];
        e.nameVals = new String[size];
        InputStream stream = null;
        if (file.equals("entities-base.properties")){
            stream = new ByteArrayInputStream(EntitiesStream.getBase().getBytes(StandardCharsets.UTF_8));
        } else if (file.equals("entities-full.properties")){
            stream = new ByteArrayInputStream(EntitiesStream.getFull().getBytes(StandardCharsets.UTF_8));
        } else if (file.equals("entities-xhtml.properties")){
            stream = new ByteArrayInputStream(EntitiesStream.getXHTML().getBytes(StandardCharsets.UTF_8));
        }
        if (stream == null)
            throw new IllegalStateException("Could not read resource " + file + ". Make sure you copy resources for " + Entities.class.getCanonicalName());

        int i = 0;
        try {
            ByteBuffer bytes = DataUtil.readToByteBuffer(stream, 0);
            String contents = Charset.forName("ascii").decode(bytes).toString();
            CharacterReader reader = new CharacterReader(contents);

            while (!reader.isEmpty()) {
                

                final String name = reader.consumeTo('=');
                reader.advance();
                final int cp1 = Integer.parseInt(reader.consumeToAny(codeDelims), codepointRadix);
                final char codeDelim = reader.current();
                reader.advance();
                final int cp2;
                if (codeDelim == ',') {
                    cp2 = Integer.parseInt(reader.consumeTo(';'), codepointRadix);
                    reader.advance();
                } else {
                    cp2 = empty;
                }
                String indexS = reader.consumeTo('\n');
                
                if (indexS.charAt(indexS.length() - 1) == '\r') {
                    indexS = indexS.substring(0, indexS.length() - 1);
                }
                final int index = Integer.parseInt(indexS, codepointRadix);
                reader.advance();

                e.nameKeys[i] = name;
                e.codeVals[i] = cp1;
                e.codeKeys[index] = cp1;
                e.nameVals[index] = name;

                if (cp2 != empty) {
                    multipoints.put(name, new String(new int[]{cp1, cp2}, 0, 2));
                }
                i++;


            }
        } catch (IOException err) {
            throw new IllegalStateException("Error reading resource " + file);
        }
    }
}
