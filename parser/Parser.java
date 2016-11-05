package parser;

/**
 * the recursive descent parser that does not use variables.
 */
public class Parser {
	// 标识符的类型
	final int NONE = 0;
	final int DELIMITER = 1;	// 表示分隔符, 如 [  + - & % ? ^ ( )]
	final int VARIABLE = 2;		// 表示变量
	final int NUMBER = 3;		// 表示数值
	
	// 语法错误的类型
	final int SYNTAX = 0;		// 语法错误
	final int UNBALPARENS = 1;	// 括号不匹配
	final int NOEXP = 2;		// 空输入
	final int DIVBYZERO = 3;	// 除零
	
	final String EOE = "\0";	//表示表达式的结束
	
	private String exp;	// 要计算的表达式
	private int expIdx;	// 表达式的当前索引
	private String token;	// 当前的标识符
	private int tokType;	// 当前标识符的类型
	
	/**
	 * return true if c is a delimiter
	 */
	private boolean isDelim(char c){
		return " +-/*%^=()".indexOf(c) != -1;
	}
	
	/**
	 * 获取下一个标识符
	 */
	private void getToken(){
		tokType = NONE;
		token = "";
		
		if(expIdx == exp.length()){
			token = EOE;
			return;
		}
		// 跳过空格
		while(expIdx < exp.length() && Character.isWhitespace(exp.charAt(expIdx))){
			++expIdx;
		}
		
		// 表达式末尾是空格
		if(expIdx == exp.length()){
			token = EOE;
			return;
		}
		
		if(isDelim(exp.charAt(expIdx))){	//分隔符
			token += exp.charAt(expIdx);
			expIdx++;
			tokType = DELIMITER;
		}else if(Character.isLetter(exp.charAt(expIdx))){	// 变量
			while(expIdx < exp.length() && !isDelim(exp.charAt(expIdx))){
				token += exp.charAt(expIdx);
				expIdx++;
			}
			tokType = VARIABLE;
		}else if(Character.isDigit(exp.charAt(expIdx))){	// 数值
			while(expIdx < exp.length() && !isDelim(exp.charAt(expIdx))){
				token += exp.charAt(expIdx);
				expIdx++;
			}
			tokType = NUMBER;
		}else{	// 未知类型，表达式终止
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
		
		// 解析表达式
		result = exec2();
		
		if(!token.equals(EOE)) handleErr(SYNTAX);
		return result;
	}
	
	// 加减两项
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
	
	// 乘除两个因数
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
	
	
	// 处理指数
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
	
	// 处理一元加减
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
	
	// 处理括号里的表达式
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
	
	// 获取 token 的值
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
		// 变量待处理
		default:
			handleErr(SYNTAX);
			break;
		}
		
		return result;
	}

}
