/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import java.io.*;
import java.util.*;


/**
 *
 * @author mrgob
 */
public class DOCX_handler {
    XWPFDocument wordDoc;
    XWPFWordExtractor extractor;
    
    DOCX_handler(File docxFile) throws IOException {
        InputStream instream = new FileInputStream(docxFile);
        System.out.println("Created input stream");
        wordDoc = new XWPFDocument(instream);
        System.out.println("Created WordDoc");
        extractor = new XWPFWordExtractor(wordDoc);
    }
    
    private List<String> stringToWordList(String input, String delimiter){
        Scanner scan = new Scanner(input);
        scan.useDelimiter(delimiter);
        
        List<String> list = new ArrayList<String>();
        while(scan.hasNext()){
            String str = scan.next();
            str = str.toLowerCase().trim();
            if(str.length() > 0){
                list.add(str);
            }
        }
        scan.close();
        return list;
    }
    
    public List<String> extractText(String delimiter){
        String text = extractor.getText();
        List<String> list = stringToWordList(text, delimiter);
        return list;
    }
    
    public static void main(String[] args){
        if(args.length!=1){
            System.out.println("Improper command: Proper command is:\n\t"
                                + "java DOCX_handler <Input Filename>\n"
                                + "Quotation marks are necessary to put around the filenames if they contain spaces.\n");
            return;
	}
        try{
            File f = new File(args[0]);
            if(!(f.exists() && f.canRead())){
                System.out.println("File Unreadable or does not exist");
            }
            System.out.println("Creating DOCX_handler...");
            DOCX_handler handler = new DOCX_handler(f);
            System.out.println("Extracting Text...");
            List<String> list = handler.extractText("\\p{javaWhitespace}");
            System.out.println("Extracted Text:\n");
            for(String s: list){
                System.out.println(s);
            }
        }catch(IOException e){
            System.out.println("Program failed with an IOException.\nEnsure your filenames are correct.\n");
        }
    }
}
