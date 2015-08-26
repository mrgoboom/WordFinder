package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.*;


public class WordFinder {
	
	enum state{COLOUR,NO_COLOUR};
	
	List<Pattern> words;
	//Pattern pattern;
	File document;
	String outputFilename;
	
	WordFinder(String wordlist, String doc){
		super();
		List<String> patterns = fileToWordList(new File(wordlist),",");
		/*String bigPattern="(";
		boolean first=true;
		for(String p:patterns){
			if(first){
				first=false;
			}else{
				bigPattern=bigPattern.concat("|");
			}
			bigPattern=bigPattern.concat("\\b").concat(p).concat("\\b");
		}
		bigPattern=bigPattern.concat(")");
		this.pattern=Pattern.compile(bigPattern);*/
		this.words=new ArrayList<Pattern>();
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
	
        private void writeHeader(StringBuffer sb){
            sb.append("<!DOCTYPE html>\n<html>\n<head>\n<style>\n");
            sb.append("span\n{\nbackground-color:yellow;\n}\n");
            sb.append("table\n{\nborder-collapse:collapse;\ntext-align:center;\n}\n");
            sb.append("td, th\n{\npadding:5px;\n}\n");
            sb.append("table, th, td\n{\nborder:1px solid black;\n}\n");
            sb.append("</style>\n</head>\n<body>\n");
        }
        
        private void writeFooter(StringBuffer sb){
            sb.append("</body>\n</html>\n");
        }
        
	public void highlight(boolean highlightHits) throws IOException{
		
		List<String> docWordList = fileToWordList(document,"\\p{javaWhitespace}");
		boolean[] inList = allFalse(docWordList.size());
		boolean match;
                int count=0;
		for(int index=0;index<docWordList.size();++index){
			for(Pattern p:words){
				Matcher m = p.matcher(docWordList.get(index));
				match=m.matches();
				if(match){
					inList[index]=true;
                                        ++count;
					break;
				}
			}
		}
		//Idea: mark transitions between states. Store in list. Count until that word token in list and insert there
		boolean first = inList[0];
		boolean last=first;
		List<Integer> transitions = new ArrayList<>();
		for(int i=1;i<inList.length;i++){
			if(inList[i]!=last){
				transitions.add(i);
				last=inList[i];
			}
		}
		
		BufferedReader br = new BufferedReader(new FileReader(document));
		StringBuffer input=new StringBuffer();
		String buffer;
		while((buffer=br.readLine())!=null){
			input.append(buffer+"<br>\n");
		}		
		br.close();
		String inString=input.toString().toLowerCase();
		
		String sSpan = "<span>";
		String eSpan = "</span>";
		StringBuffer output = new StringBuffer("");
                writeHeader(output);
                output.append("========================================\n<h3>Statistics</h3>\n");
                output.append("<table>\n<tr>\n<th>Total Words</th>\n<th>Matches</th>\n<th>Misses</th>\n<th>Hit Percentage</th>\n</tr>\n<tr>\n<td>" + docWordList.size() + "</td>\n<td>" + count + "</td>\n<td>"
                        + (docWordList.size()-count) + "</td>\n<td>" + (((float)docWordList.size())/(float)count) + "%</td>\n</tr>\n</table>\n");
                
                output.append("<p>========================================</p>");
                
		state current;
		if(!(first^highlightHits)){
			output.append(sSpan);
			current=state.COLOUR;
		}else{
			current=state.NO_COLOUR;
		}
		
		int startIndex=0;
		int endIndex;
		for(Integer i:transitions){
			if(i==null)break;
			endIndex=inString.indexOf(docWordList.get(i),startIndex);
			if(endIndex<0)break;
			output.append(input.substring(startIndex, endIndex));
			startIndex=endIndex;
			switch(current){
				case NO_COLOUR:
					output.append(sSpan);
					current=state.COLOUR;
					break;
				case COLOUR:
					output.append(eSpan);
					current=state.NO_COLOUR;
					break;
			}
		}
		output.append(input.substring(startIndex));
                writeFooter(output);
		
		PrintWriter writer = new PrintWriter(outputFilename,"UTF-8");
		writer.print(output.toString());
		writer.close();
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