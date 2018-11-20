package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */


import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;


public class SimpleParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}

	/*
	 * Program ::= Identifier Block
	 */
	public void program() throws SyntaxException {
		match(IDENTIFIER);
		block();
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {Kind.KW_input, Kind.KW_while,Kind.KW_if, Kind.IDENTIFIER,
			Kind.KW_red,Kind.KW_blue,Kind.KW_green,Kind.KW_alpha, Kind.KW_write,
			Kind.KW_sleep,Kind.KW_show};
//	Kind[] type={Kind.KW_int,Kind.KW_float, Kind.KW_filename};

	public void block() throws SyntaxException {
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			declaration();
		} else if (isKind(firstStatement)) {
			statement();
		}
			match(SEMI);
		}
		match(RBRACE);

	}
		
	public void declaration() throws SyntaxException {
			if(t.kind==Kind.KW_image){
				consume();
				match(Kind.IDENTIFIER);
				if(isKind(Kind.LSQUARE)){
					match(Kind.LSQUARE);
					expression();
					match(Kind.COMMA);
					expression();
					match(Kind.RSQUARE);
				}
			}
			else{
				consume();
				match(Kind.IDENTIFIER);
			}
		
	}
	
	private void expression() throws SyntaxException {
		this.orExpression();
		if(isKind(Kind.OP_QUESTION)){
			consume();
			this.expression();
			match(Kind.OP_COLON);
			this.expression();
		}
	}


	private void orExpression() throws SyntaxException {
		this.andExpression();
		if(isKind(Kind.OP_OR)){
			while(isKind(Kind.OP_OR)){
				consume();
				this.andExpression();
			}
		}
	}


	private void andExpression() throws SyntaxException {
		this.eqExpression();
		if(isKind(Kind.OP_AND)){
			while(isKind(Kind.OP_AND)){
				consume();
				this.eqExpression();
			}
		}
	}


	private void eqExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		this.relExpression();
		if(isKind(new Kind[] {Kind.OP_NEQ,Kind.OP_EQ})){
			while(isKind(new Kind[] {Kind.OP_NEQ,Kind.OP_EQ})){
				consume();
				this.relExpression();
			}
		}
	}


	private void relExpression() throws SyntaxException {
		this.addExpression();
		if(isKind(new Kind[] {Kind.OP_GE,Kind.OP_GT,Kind.OP_LE,Kind.OP_LT})){
			while(isKind(new Kind[] {Kind.OP_GE,Kind.OP_GT,Kind.OP_LE,Kind.OP_LT})){
				consume();
				this.addExpression();
			}
		}
	}


	private void addExpression() throws SyntaxException {
		this.multExpression();
		while(isKind(new Kind[] {Kind.OP_PLUS,Kind.OP_MINUS})){
			consume();
			this.multExpression();
		}
	}


	private void multExpression() throws SyntaxException {
		this.powerExpression();
		while(isKind(new Kind[] {Kind.OP_MOD,Kind.OP_TIMES,Kind.OP_DIV})){
			consume();
			this.powerExpression();
		}
	}


	private void powerExpression() throws SyntaxException {
		this.unaryExpression();
		if(isKind(Kind.OP_POWER)){
			consume();
			this.powerExpression();
		}
	}


	private void unaryExpression() throws SyntaxException {
		if(isKind(new Kind[] {Kind.OP_PLUS,Kind.OP_MINUS})){
			consume();
			this.unaryExpression();
		}
		else{
			this.unaryExpressionNotPlusMinus();
		}
	}


	private void unaryExpressionNotPlusMinus() throws SyntaxException {
		if(isKind(Kind.OP_EXCLAMATION)){
			this.consume();
			this.unaryExpression();
		}
		else {
			this.primary();
		}
	}


	private void primary() throws SyntaxException {
		if(isKind(new Kind[] {Kind.BOOLEAN_LITERAL,Kind.IDENTIFIER,Kind.FLOAT_LITERAL,Kind.INTEGER_LITERAL})){
			if(isKind(Kind.IDENTIFIER)){
				consume();
				if(isKind(Kind.LSQUARE)){
					this.pixelSelector();
				}
			}
			else consume();
		}
		else if(isKind(Kind.LPAREN)){
			consume();
			this.expression();
			this.match(Kind.RPAREN);
		}
		else if(isKind(Kind.LPIXEL)){
			this.pixelConstructor();
		}
		else if(isKind(new Kind[] {Kind.KW_Z,Kind.KW_default_height,Kind.KW_default_width})){
			consume();
		}
		else if(this.isFunctionName()){
			this.functionApplication();
		}
		else throw new SyntaxException(t,"Syntax Error");
	}


	private void pixelConstructor() throws SyntaxException {
		// TODO Auto-generated method stub
		consume();
		this.expression();
		this.match(Kind.COMMA);
		this.expression();
		this.match(Kind.COMMA);
		this.expression();
		this.match(Kind.COMMA);
		this.expression();
		this.match(Kind.RPIXEL);
	}


	private void functionApplication() throws SyntaxException {
		this.consume();
		if(isKind(Kind.LPAREN)){
			consume();
			this.expression();
			match(Kind.RPAREN);
		}
		else if(isKind(Kind.LSQUARE)){
			consume();
			this.expression();
			this.match(Kind.COMMA);
			this.expression();
			this.match(Kind.RSQUARE);
		}
	}


	private boolean isFunctionName() {
		
		return(this.isKind(new Kind[] {Kind.KW_cart_x,Kind.KW_cart_y,Kind.KW_cos,Kind.KW_sin
				,Kind.KW_atan,Kind.KW_abs,Kind.KW_log, Kind.KW_polar_a,Kind.KW_polar_r,
				Kind.KW_int,Kind.KW_float,Kind.KW_width,Kind.KW_height,Kind.KW_red,Kind.KW_green,
				Kind.KW_blue,Kind.KW_alpha}));
	}


	private void pixelSelector() throws SyntaxException {
		// TODO Auto-generated method stub
		consume();
		this.expression();
		this.match(Kind.COMMA);
		this.expression();
		this.match(Kind.RSQUARE);
	}
	
	public void statement() throws SyntaxException {
		if(isKind(Kind.KW_input)){
			this.statementInput();
		}
		else if(isKind(Kind.KW_write)){
			this.statementWrite();
		}
		else if(isKind(Kind.KW_while)){
			this.statementWhile();
		}
		else if(isKind(Kind.KW_if)){
			this.statementIf();
		}
		else if(isKind(Kind.KW_show)){
			this.statementShow();
		}
		else if(isKind(Kind.KW_sleep)){
			this.statementSleep();
		}
		else if(isKind(new Kind[] {Kind.IDENTIFIER,Kind.KW_red,Kind.KW_blue,Kind.KW_green,Kind.KW_alpha})){
			this.statementAssignment();
		}
		else throw new UnsupportedOperationException();
	}

	private void statementAssignment() throws SyntaxException {
		// TODO Auto-generated method stub
		if(isKind(IDENTIFIER)){
			this.consume();
			if(this.isKind(Kind.LSQUARE))
				this.pixelSelector();
		}
		else {
			this.consume();
			this.match(Kind.LPAREN);
			this.match(IDENTIFIER);
			this.pixelSelector();
			this.match(Kind.RPAREN);
		}
		this.match(Kind.OP_ASSIGN);
		this.expression();
	}


	private void statementWhile() throws SyntaxException {
		// TODO Auto-generated method stub
		this.consume();
		this.match(Kind.LPAREN);
		this.expression();
		this.match(Kind.RPAREN);
		this.block();
	}


	private void statementIf() throws SyntaxException {
		// TODO Auto-generated method stub
		this.consume();
		this.match(Kind.LPAREN);
		this.expression();
		this.match(Kind.RPAREN);
		this.block();
	}


	private void statementSleep() throws SyntaxException {
		// TODO Auto-generated method stub
		this.consume();
		this.expression();
	}


	private void statementShow() throws SyntaxException {
		// TODO Auto-generated method stub
		this.consume();
		this.expression();
	}


	private void statementWrite() throws SyntaxException {
		// TODO Auto-generated method stub
		this.consume();
		this.match(IDENTIFIER);
		this.match(Kind.KW_to);
		this.match(IDENTIFIER);
	}


	private void statementInput() throws SyntaxException {
		// TODO Auto-generated method stub
		consume();
		this.match(IDENTIFIER);
		this.match(Kind.KW_from);
		this.match(Kind.OP_AT);
		this.expression();
	}


	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!  
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}
	

}

