package parser;

import java.util.HashMap;
import java.util.Map;

/**
 * the recursive descent parser that does not use variables.
 */
public class Parser {
	// ±êÊ¶·ûµÄÀàÐÍ
	final int NONE = 0;
	final int DELIMITER = 1;	// ±íÊ¾·Ö¸ô·û, Èç [  + - & % ? ^ ( )]
	final int VARIABLE = 2;		// ±íÊ¾±äÁ¿
	final int NUMBER = 3;		// ±íÊ¾ÊýÖµ
	
	// Óï·¨´íÎóµÄÀàÐÍ
	final int SYNTAX = 0;		// Óï·¨´íÎó
	final int UNBALPARENS = 1;	// À¨ºÅ²»Æ¥Åä
	final int NOEXP = 2;		// ¿ÕÊäÈë
	final int DIVBYZERO = 3;	// ³ýÁã
	
	final String EOE = "\0";	//±íÊ¾±í´ïÊ½µÄ½áÊø
	
	private String exp;	// Òª¼ÆËãµÄ±í´ïÊ½
	private int expIdx;	// ±í´ïÊ½µÄµ±Ç°Ë÷Òý
	private String token;	// µ±Ç°µÄ±êÊ¶·û
	private int tokType;	// µ±Ç°±êÊ¶·ûµÄÀàÐÍ
	
	// ´æ´¢±äÁ¿µÄÖµ
	private Map<String, Double> varMap = new HashMap<>();
	
	/**
	 * parse and execute the expression
	 * @param expstr
	 * @return the value of the expression
	 * @throws ParserException
	 */
	public double execute(String expstr) throws ParserException{
		double result;
		exp = expstr;
		expIdx = 0;
		
		getToken();
		
		if(token.equals(EOE)) handleErr(NOEXP);
		
		// ½âÎö±í´ïÊ½
		result = exec1();
		
		if(!token.equals(EOE)) handleErr(SYNTAX);
		return result;
	}
	
	private double exec1() throws ParserException{
		double result;
		int temp_tokType;
		String temp_token;
		
		if(tokType == VARIABLE){
			temp_token = new String(token);
			temp_tokType = tokType;
		
			getToken();
			
			if(!token.equals("=")){
				putback();
				token = new String(temp_token);
				tokType = temp_tokType;
			}else{
				getToken();
				result = exec2();
				varMap.put(temp_token, result);
				return result;
			}
		}
		return exec2();
	}
	
	// ¼Ó¼õÁ½Ïî
	private double exec2() throws ParserException{
		char op;
		double result;
		double partialResult;
		
		result = exec3();
		
		while((op = token.charAt(0)) == '+' || op == '-'){
			getToken();
			partialResult = exec3();
			switch(op){
			case '-': 
				result -= partialResult;
				break;
			case '+': 
				result += partialResult;
				break;
			}
		}
		
		return result;
	}
	
	// ³Ë³ýÁ½¸öÒòÊý
	private double exec3() throws ParserException{
		char op;
		double result;
		double partialResult;
		
		result = exec4();
		
		while((op = token.charAt(0)) == '*' || op == '/' || op == '%'){
			getToken();
			partialResult = exec4();
			switch(op){
			case '*':
				result *= partialResult;
				break;
			case '/':
				if(partialResult == 0.0) handleErr(DIVBYZERO);
				result /= partialResult;
				break;
			case '%':
				if(partialResult == 0.0) handleErr(DIVBYZERO);
				result %= partialResult;
				break;
			}
		}
		return result;
	}
	
	
	// ´¦ÀíÖ¸Êý
	private double exec4() throws ParserException{
		double result;
		double partialResult;
		double ex;
		
		result = exec5();
		
		if((token.equals("^"))){
			getToken();
			partialResult = exec4();
			ex = result;
			if(partialResult == 0.0) result = 1.0;
			else{
				for(int t=(int)partialResult - 1; t > 0; --t) result *= ex;
			}
		}
		
		return result;
	}
	
	// ´¦ÀíÒ»Ôª¼Ó¼õ
	private double exec5() throws ParserException{
		double result;
		String op = "";
		
		if((tokType == DELIMITER) && token.equals("+") || token.equals("-")){
			op = token;
			getToken();
		}
		result = exec6();
		if(op.equals("-")) result = -result;
		
		return result;
	}
	
	// ´¦ÀíÀ¨ºÅÀïµÄ±í´ïÊ½
	private double exec6() throws ParserException{
		double result;
		
		if(token.equals("(")){
			getToken();
			result = exec1();
			if(!token.equals(")")) handleErr(UNBALPARENS);
			getToken();
		}
		else result = atom();
		
		return result;
	}
	
	private void putback(){
		if(token == EOE) return;
		for(int i = 0; i < token.length(); ++i) expIdx--;
	}
	
	/**
	 * return true if c is a delimiter
	 */
	private boolean isDelim(char c){
		return " +-/*%^=()".indexOf(c) != -1;
	}
	
	/**
	 * »ñÈ¡ÏÂÒ»¸ö±êÊ¶·û
	 */
	private void getToken(){
		tokType = NONE;
		token = "";
		
		if(expIdx == exp.length()){
			token = EOE;
			return;
		}
		// Ìø¹ý¿Õ¸ñ
		while(expIdx < exp.length() && Character.isWhitespace(exp.charAt(expIdx))){
			++expIdx;
		}
		
		// ±í´ïÊ½Ä©Î²ÊÇ¿Õ¸ñ
		if(expIdx == exp.length()){
			token = EOE;
			return;
		}
		
		if(isDelim(exp.charAt(expIdx))){	//·Ö¸ô·û
			token += exp.charAt(expIdx);
			expIdx++;
			tokType = DELIMITER;
		}else if(Character.isLetter(exp.charAt(expIdx))){	// ±äÁ¿
			while(expIdx < exp.length() && !isDelim(exp.charAt(expIdx))){
				token += exp.charAt(expIdx);
				expIdx++;
			}
			tokType = VARIABLE;
		}else if(Character.isDigit(exp.charAt(expIdx))){	// ÊýÖµ
			while(expIdx < exp.length() && !isDelim(exp.charAt(expIdx))){
				token += exp.charAt(expIdx);
				expIdx++;
			}
			tokType = NUMBER;
		}
		else{	// Î´ÖªÀàÐÍ£¬±í´ïÊ½ÖÕÖ¹
			token = EOE;
			return;
		}
	}
	
	/**
	 * process exception
	 * @param error
	 * @throws ParserException
	 */
	private void handleErr(int error) throws ParserException{
		String[] err = {"Systax Error", "Unbalanced Parentheses", "No Expression", 
						"Division by Zero"};
		throw new ParserException(err[error]);
	}
	
	// »ñÈ¡ token µÄÖµ
	private double atom() throws ParserException{
		double result = 0.0;
		
		switch(tokType){
		case NUMBER:
			try{
				result = Double.parseDouble(token);
			}catch (NumberFormatException e){
				handleErr(SYNTAX);
			}
			getToken();
			break;
		case VARIABLE:
			result = findVar(token);
			getToken();
			break;
		default:
			handleErr(SYNTAX);
			break;
		}
		
		return result;
	}
	
	private double findVar(String vname) throws ParserException{
		if(!varMap.containsKey(vname)){
			handleErr(SYNTAX);
			return 0.0;
		}
		return varMap.get(vname);
	}

}
