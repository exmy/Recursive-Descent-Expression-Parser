package parser;

/**
 * the recursive descent parser that does not use variables.
 */
public class Parser {
	// ��ʶ��������
	final int NONE = 0;
	final int DELIMITER = 1;	// ��ʾ�ָ���, �� [  + - & % ? ^ ( )]
	final int VARIABLE = 2;		// ��ʾ����
	final int NUMBER = 3;		// ��ʾ��ֵ
	
	// �﷨���������
	final int SYNTAX = 0;		// �﷨����
	final int UNBALPARENS = 1;	// ���Ų�ƥ��
	final int NOEXP = 2;		// ������
	final int DIVBYZERO = 3;	// ����
	
	final String EOE = "\0";	//��ʾ���ʽ�Ľ���
	
	private String exp;	// Ҫ����ı��ʽ
	private int expIdx;	// ���ʽ�ĵ�ǰ����
	private String token;	// ��ǰ�ı�ʶ��
	private int tokType;	// ��ǰ��ʶ��������
	
	/**
	 * return true if c is a delimiter
	 */
	private boolean isDelim(char c){
		return " +-/*%^=()".indexOf(c) != -1;
	}
	
	/**
	 * ��ȡ��һ����ʶ��
	 */
	private void getToken(){
		tokType = NONE;
		token = "";
		
		if(expIdx == exp.length()){
			token = EOE;
			return;
		}
		// �����ո�
		while(expIdx < exp.length() && Character.isWhitespace(exp.charAt(expIdx))){
			++expIdx;
		}
		
		// ���ʽĩβ�ǿո�
		if(expIdx == exp.length()){
			token = EOE;
			return;
		}
		
		if(isDelim(exp.charAt(expIdx))){	//�ָ���
			token += exp.charAt(expIdx);
			expIdx++;
			tokType = DELIMITER;
		}else if(Character.isLetter(exp.charAt(expIdx))){	// ����
			while(expIdx < exp.length() && !isDelim(exp.charAt(expIdx))){
				token += exp.charAt(expIdx);
				expIdx++;
			}
			tokType = VARIABLE;
		}else if(Character.isDigit(exp.charAt(expIdx))){	// ��ֵ
			while(expIdx < exp.length() && !isDelim(exp.charAt(expIdx))){
				token += exp.charAt(expIdx);
				expIdx++;
			}
			tokType = NUMBER;
		}else{	// δ֪���ͣ����ʽ��ֹ
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
		
		// �������ʽ
		result = exec2();
		
		if(!token.equals(EOE)) handleErr(SYNTAX);
		return result;
	}
	
	// �Ӽ�����
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
	
	// �˳���������
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
	
	
	// ����ָ��
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
	
	// ����һԪ�Ӽ�
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
	
	// ����������ı��ʽ
	private double exec6() throws ParserException{
		double result;
		
		if(token.equals("(")){
			getToken();
			result = exec2();
			if(!token.equals(")")) handleErr(UNBALPARENS);
			getToken();
		}
		else result = atom();
		
		return result;
	}
	
	// ��ȡ token ��ֵ
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
		// ����������
		default:
			handleErr(SYNTAX);
			break;
		}
		
		return result;
	}

}
