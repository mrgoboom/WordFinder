package src;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.*;
import java.awt.Desktop;

public class WordFinder {
	
    enum state{COLOUR,NO_COLOUR};

    private List<Pattern> words;
    //Pattern pattern;
    private File document;
    private String outputFilename;
    private static final int MAXSENTENCELENGTH = 7;

    WordFinder(String wordlist, String doc){
        super();
        List<String> patterns = fileToWordList(new File(wordlist),",");
        this.words=new ArrayList<>();
        for(String s : patterns){
            this.words.add(Pattern.compile("[\\p{Punct}]*".concat(s.concat("[\\p{Punct}]*"))));
        }
        this.document=new File(doc);
        //this.document=fileToWordList(new File(doc),"\\p{javaWhitespace}");
        Pattern filename = Pattern.compile("([\\w]+)(\\.[\\w]+)?");
        Matcher m = filename.matcher(doc);
        this.outputFilename=m.replaceAll("$1.html");

    }

    WordFinder(String wordlist, File doc){
        super();
        List<String> patterns = fileToWordList(new File(wordlist),",");

        this.words=new ArrayList<Pattern>();
        for(String s : patterns){
            this.words.add(Pattern.compile("[\\p{Punct}]*".concat(s.concat("[\\p{Punct}]*"))));
        }
        this.document=doc;

        Pattern filename = Pattern.compile("([\\w]+)(\\.[\\w]+)?");
        Matcher m = filename.matcher(doc.getName());
        this.outputFilename=m.replaceAll("$1.html");
    }

    WordFinder(String wordlist, File inDoc, File outDoc){
        super();

        List<String> patterns = fileToWordList(new File(wordlist),",");

        this.words=new ArrayList<Pattern>();
        for(String s : patterns){
            this.words.add(Pattern.compile("[\\p{Punct}]*".concat(s.concat("[\\p{Punct}]*"))));
        }
        this.document=inDoc;
        this.outputFilename=outDoc.getAbsolutePath();
    }

    private List<String> fileToWordList(File f,String delimiter){
        Scanner s;
        try{
            List<String> list = new ArrayList<String>();
            s = new Scanner(f);
            s.useDelimiter(delimiter);
            while(s.hasNext()){
                String str = s.next();
                str=str.toLowerCase().trim();
                if(str.length()>0){
                    list.add(str);
                }
            }
            s.close();
            return list;
        }catch(FileNotFoundException e){
            return new ArrayList<String>();
        }
    }

    private boolean[] allFalse(int size){
            boolean[] ret = new boolean[size];
            for(int i=0;i<ret.length;i++){
                    ret[i]=false;
            }
            return ret;
    }

    private void writeHeader(StringBuilder sb){
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<style>\n");
        sb.append(".word { background-color: yellow; }\n");
        sb.append(".sentence { background-color: aqua; }\n");
        //sb.append("span\n{\nbackground-color:yellow;\n}\n");
        sb.append("table\n{\nborder-collapse:collapse;\ntext-align:center;\n}\n");
        sb.append("td, th\n{\npadding:5px;\n}\n");
        sb.append("table, th, td\n{\nborder:1px solid black;\n}\n");
        sb.append("</style>\n</head>\n<body>\n");
    }

    private void writeFooter(StringBuilder sb){
        sb.append("</body>\n</html>\n");
    }

    //Prepares a string to be output as html
    private String convertToHTMLString(String input){
        StringBuilder builder = new StringBuilder();
        boolean prevWasSpace = false;
        
        for( int i = 0; i < input.length(); ++i) {
            char ch = input.charAt(i);
            if (ch == ' ') {
                if (prevWasSpace) {
                    builder.append("&nbsp;");
                    prevWasSpace = false;
                    continue;
                }
                prevWasSpace = true;
            } else {
                prevWasSpace = false;
            }
            
            if (ch == '<'){
                if (input.length() > i+3 && input.substring(i,i+4).equals("<br>")) { //<br>
                    i += 3;
                    builder.append("<br>");
                    continue;
                }
            }
            switch(ch) {
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '&':
                    builder.append("&amp;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&#39;");
                    break;
                case '\n':
                    builder.append("<br>\n");
                    break;
                case '\t':
                    builder.append("&nbsp; &nbsp; &nbsp; ");
                    break;
                default:
                    if(ch < 128) {
                        builder.append(ch);
                    } else {
                        builder.append("&#").append((int)ch).append(";");
                    }
            }
        }
        
        return builder.toString();
    }
    
    public void highlight(boolean highlightHits) throws IOException{
        BufferedReader br;
        String fname = this.document.getName().toLowerCase();
        if(fname.matches(".*\\.docx")){
            DOCX_handler handler = new DOCX_handler(this.document);
            br = new BufferedReader(new StringReader(handler.getText()));
        }else{
            br = new BufferedReader(new FileReader(this.document));
        }
        StringBuilder input = new StringBuilder();
        String buffer;
        while((buffer=br.readLine())!=null){
            input.append(buffer+"\n");
        }
        br.close();

        String missStartTag = "<span class=\"word\">";
        String missEndTag = "</span>";
        String longStartTag = "<div class=\"sentence\">";
        String longEndTag = "</div>";
        
        int numWords=0;
        int numSentences=0;
        int numHits=0;
        int numLongSentences=0;

        StringBuilder body = new StringBuilder("");
        StringBuilder sentence = new StringBuilder("");
        
        int sentenceLength=0;
        boolean isMiss = false;
        int lastWordEndIndex=0;
        
        Pattern wordPattern = Pattern.compile("\\s*(\\S+)\\s*");
        Matcher wordMatcher = wordPattern.matcher(input);
        while (wordMatcher.find()){
            String word = wordMatcher.group(0);
            numWords++;
            boolean matches = false;
            for(Pattern pattern:this.words){
                Matcher patternMatcher = pattern.matcher(word.toLowerCase().trim());
                if(patternMatcher.matches()){
                    matches = true;
                    break;
                }
            }
            if(matches){
                numHits++;
                if(isMiss){
                    sentence.insert(lastWordEndIndex,missEndTag);
                    isMiss = false;
                }
            }else if (!isMiss){
                sentence.append(missStartTag);
                isMiss = true;
            }
            sentence.append(convertToHTMLString(word));
            sentenceLength++;
            lastWordEndIndex=sentence.length()-(word.length()-wordMatcher.group(1).length());
            if(word.matches(".*[\\!\\.\\?].*")){
                if(isMiss){
                    sentence.insert(lastWordEndIndex,missEndTag);
                    isMiss = false;
                }
                numSentences++;
                if(sentenceLength>WordFinder.MAXSENTENCELENGTH){
                    numLongSentences++;
                    sentence.insert(0,longStartTag);
                    sentence.append(longEndTag);
                }
                body.append(sentence);
                sentence = new StringBuilder("");
                sentenceLength=0;
            }
        }
        
        if(sentenceLength>0){
            if(isMiss){
                sentence.insert(lastWordEndIndex,missEndTag);
            }
            numSentences++;
            if(sentenceLength>WordFinder.MAXSENTENCELENGTH){
                numLongSentences++;
                sentence.insert(0,longStartTag);
                sentence.append(longEndTag);
            }
            body.append(sentence);
        }
        
        StringBuilder output = new StringBuilder("");
        writeHeader(output);
        
        output.append("========================================\n<h3>Statistics</h3>\n");
        output.append("<table>\n<tr>\n<th>Total Words</th>\n<th>Matches</th>\n<th>Misses</th>\n<th>Hit Percentage</th>\n<th>Total Sentences</th>\n<th>Long Sentences</th>\n<th>Long Sentence %</th>\n</tr>\n<tr>\n<td>"
                + numWords + "</td>\n<td>" + numHits + "</td>\n<td>" + (numWords-numHits) + "</td>\n<td>" + String.format("%.2f",100.0*((float)numHits)/(float)numWords)+ "%</td>\n<td>" 
                + numSentences + "</td>\n<td>" + numLongSentences + "</td>\n<td>" + String.format("%.2f", 100.0*((float)numLongSentences)/(float)numSentences) + "%</td>\n</tr>\n</table>\n");

        output.append("<p>========================================</p>\n");
        output.append(body);
        writeFooter(output);
        
        PrintWriter writer = new PrintWriter(outputFilename,"UTF-8");
        writer.print(output.toString());
        writer.close();
        File toOpen = new File(outputFilename);
        Desktop.getDesktop().open(toOpen);
    }

    public static void main(String[] args){
        /*for(String s:args){
                System.out.println(s);
        }*/
        if(args.length!=2){
            System.out.println("Improper command: Proper command is:\n\t"
                            + "java WordFinder <Word List Filename> <Document Filename>\n"
                            + "Quotation marks are necessary to put around the filenames if they contain spaces.\n");
            return;
        }
        WordFinder wf = new WordFinder(args[0],args[1]);
        try{
            wf.highlight(true);
        }catch(IOException e){
            System.out.println("Program failed with an IOException.\nEnsure your filenames are correct.\n");
        }
    }
}