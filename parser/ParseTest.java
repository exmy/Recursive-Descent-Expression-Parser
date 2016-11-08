package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ParseTest {

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Parser parser = new Parser();
		
		System.out.println("Enter an empty expression to stop: ");
		
		while(true){
			System.out.print(">>> ");
			String exp = br.readLine();
			if(exp.equals("")) break;
			try{
				System.out.println(parser.execute(exp));
			}catch(ParserException e){
				System.out.println(e.getMessage());
			}
		}
		System.out.println("end");
	}

}
