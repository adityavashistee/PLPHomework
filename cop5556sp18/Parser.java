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
import cop5556sp18.AST.*;
import java.util.ArrayList;
import static cop5556sp18.Scanner.Kind.*;

import java.util.List;


public class Parser {

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

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException {
		Program p=this.program();
		matchEOF();
		return p;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token name = t;
		match(IDENTIFIER);
		Block block =block();
		return new Program(name, name, block);
	}

	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */

	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {Kind.KW_input, Kind.KW_while,Kind.KW_if, Kind.IDENTIFIER,
			Kind.KW_red,Kind.KW_blue,Kind.KW_green,Kind.KW_alpha, Kind.KW_write,
			Kind.KW_sleep,Kind.KW_show};
	//	Kind[] type={Kind.KW_int,Kind.KW_float, Kind.KW_filename};

	public Block block() throws SyntaxException {
		List<ASTNode> list = new ArrayList<>();
		Token firstToken=t;
		match(LBRACE);
		while (isKind(firstDec) | isKind(firstStatement)) {
			ASTNode astNode=null;
			if (isKind(firstDec)) {
				astNode=declaration();
			} else if (isKind(firstStatement)) {
				astNode=statement();
			}
			list.add(astNode);
			match(SEMI);
		}
		match(RBRACE);
		return new Block(firstToken, list);
	}

	public Declaration declaration() throws SyntaxException {
		Expression height = null;
		Expression width = null;
		Token firstToken=t;
		Token identifier;
//		if(t.kind != Kind.KW_image && t.kind==)
		if(t.kind==Kind.KW_image){
			consume();
			identifier=t;
			match(Kind.IDENTIFIER);
			if(isKind(Kind.LSQUARE)){
				match(Kind.LSQUARE);
				height = expression();
				match(Kind.COMMA);
				width = expression();
				match(Kind.RSQUARE);
			}
		}
		else{
			this.consume();
			identifier=t;
			if(t.kind==Kind.LSQUARE)
				throw new SyntaxException(t, "Error in Declaration");
			match(Kind.IDENTIFIER);
		}
		return new Declaration(firstToken,firstToken,identifier,height, width);
	}

	public Expression expression() throws SyntaxException {
		Token firstToken =t;
		Expression exp2=null;
		Expression exp3=null;
		Expression exp1=this.orExpression();
		if(isKind(Kind.OP_QUESTION)){
			this.consume();
			exp2=this.expression();
			this.match(Kind.OP_COLON);
			exp3=this.expression();
			exp1=new ExpressionConditional(firstToken,exp1, exp2, exp3);
		}
		return exp1;
	}


	private Expression orExpression() throws SyntaxException {
		Token firstToken=t;
		Token op=null;
		Expression exp2 = null;
		Expression exp1=this.andExpression();
		if(isKind(Kind.OP_OR)){
			while(isKind(Kind.OP_OR)){
				op =t;
				consume();
				exp2=this.andExpression();
				exp1=new ExpressionBinary(firstToken,exp1,op,exp2);
			}
		}
		return exp1;
	}


	private Expression andExpression() throws SyntaxException {
		Token firstToken=t;
		Token op=null;
		Expression exp2 = null;
		Expression exp1=this.eqExpression();
		if(isKind(Kind.OP_AND)){
			while(isKind(Kind.OP_AND)){
				op=t;
				this.consume();
				exp2=this.eqExpression();
				exp1=new ExpressionBinary(firstToken,exp1,op,exp2);
			}
		}
		return exp1;
	}


	private Expression eqExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		Token firstToken=t;
		Token op=null;
		Expression exp2 = null;
		Expression exp1=this.relExpression();
		if(isKind(new Kind[] {Kind.OP_NEQ,Kind.OP_EQ})){
			while(isKind(new Kind[] {Kind.OP_NEQ,Kind.OP_EQ})){
				op=t;
				this.consume();
				exp2=this.relExpression();
				exp1=new ExpressionBinary(firstToken,exp1,op,exp2);
			}
		}
		return exp1;
	}


	private Expression relExpression() throws SyntaxException {
		Token firstToken=t;
		Token op=null;
		Expression exp2 = null;
		Expression exp1=this.addExpression();
		if(isKind(new Kind[] {Kind.OP_GE,Kind.OP_GT,Kind.OP_LE,Kind.OP_LT})){
			while(isKind(new Kind[] {Kind.OP_GE,Kind.OP_GT,Kind.OP_LE,Kind.OP_LT})){
				op=t;
				this.consume();
				exp2=this.addExpression();
				exp1=new ExpressionBinary(firstToken,exp1,op,exp2);
			}
		}
		return exp1;
	}


	private Expression addExpression() throws SyntaxException {
		Token firstToken=t;
		Token op=null;
		Expression exp2 = null;
		Expression exp1=this.multExpression();
		while(isKind(new Kind[] {Kind.OP_PLUS,Kind.OP_MINUS})){
			op=t;
			this.consume();
			exp2=this.multExpression();
			exp1=new ExpressionBinary(firstToken,exp1,op,exp2);;
		}
		return exp1;
	}


	private Expression multExpression() throws SyntaxException {
		Token firstToken=t;
		Token op=null;
		Expression exp2 = null;
		Expression exp1=this.powerExpression();
		while(isKind(new Kind[] {Kind.OP_MOD,Kind.OP_TIMES,Kind.OP_DIV})){
			op=t;
			this.consume();
			exp2=this.powerExpression();
			exp1=new ExpressionBinary(firstToken,exp1,op,exp2);
		}
		return exp1;
	}


	private Expression powerExpression() throws SyntaxException {
		Token firstToken=t;
		Token op=null;
		Expression exp2 = null;
		Expression exp1=this.unaryExpression();
		if(isKind(Kind.OP_POWER)){
			op=t;
			this.consume();
			exp2=this.powerExpression();
			exp1=new ExpressionBinary(firstToken,exp1,op,exp2);
		}
		return exp1;
	}


	private Expression unaryExpression() throws SyntaxException {
		Token firstToken=t;
		Expression exp=null;
		Token op=null;
		if(isKind(new Kind[] {Kind.OP_PLUS,Kind.OP_MINUS})){
			op=t;
			this.consume();
			exp=this.unaryExpression();
			exp=new ExpressionUnary(firstToken,op,exp);
		}
		else{
			exp=this.unaryExpressionNotPlusMinus();
		}
		return exp;
	}


	private Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Token firstToken=t;
		Expression exp=null;
		Token op=null;
		if(isKind(Kind.OP_EXCLAMATION)){
			op=t;
			this.consume();
			exp=this.unaryExpression();
			exp=new ExpressionUnary(firstToken,op,exp);
		}
		else {
			exp=this.primary();
		}
		return exp;
	}


	private Expression primary() throws SyntaxException {
		Expression expToReturn=null;
		if(isKind(new Kind[] {Kind.BOOLEAN_LITERAL,Kind.IDENTIFIER,Kind.FLOAT_LITERAL,Kind.INTEGER_LITERAL})){
			if(isKind(Kind.IDENTIFIER)){
				Token ident=t;
				this.consume();
				if(isKind(Kind.LSQUARE)){
					PixelSelector ps = this.pixelSelector();
					expToReturn= new ExpressionPixel(ident,ident,ps);
				}
				else 
					expToReturn= new ExpressionIdent(ident,ident);
			}
			else if(isKind(Kind.BOOLEAN_LITERAL)){
				Token bool=t;
				this.consume();
				expToReturn= new ExpressionBooleanLiteral(bool,bool);
			}
			else if(isKind(Kind.FLOAT_LITERAL)){
				Token fl=t;
				this.consume();
				expToReturn= new ExpressionFloatLiteral(fl,fl);
			}
			else if(isKind(Kind.INTEGER_LITERAL)){
				Token il=t;
				this.consume();
				expToReturn= new ExpressionIntegerLiteral(il,il);
			}
		}
		else if(isKind(Kind.LPAREN)){
			consume();
			Expression exp = this.expression();
			this.match(Kind.RPAREN);
			expToReturn= exp;
		}
		else if(isKind(Kind.LPIXEL)){
			expToReturn= this.pixelConstructor();
		}
		else if(isKind(new Kind[] {Kind.KW_Z,Kind.KW_default_height,Kind.KW_default_width})){
			Token firstToken=t;
			this.consume();
			expToReturn= new ExpressionPredefinedName(firstToken,firstToken);
		}
		else if(this.isFunctionName()){
			expToReturn= this.functionApplication();
		}
		else throw new SyntaxException(t,"Syntax Error");
		return expToReturn;
	}


	private Expression pixelConstructor() throws SyntaxException {
		// TODO Auto-generated method stub
		Token firstToken=t;
		this.consume();
		Expression alpha=this.expression();
		this.match(Kind.COMMA);
		Expression red=this.expression();
		this.match(Kind.COMMA);
		Expression green=this.expression();
		this.match(Kind.COMMA);
		Expression blue=this.expression();
		this.match(Kind.RPIXEL);
		return new ExpressionPixelConstructor(firstToken, alpha, red, green,blue);
		
	}


	private Expression functionApplication() throws SyntaxException {
		Token firstToken=t;
		this.consume();
		Expression expToReturn=null;
		if(isKind(Kind.LPAREN)){
//			Token functionName=t;
			consume();
			Expression exp=this.expression();
			match(Kind.RPAREN);
			expToReturn= new ExpressionFunctionAppWithExpressionArg(firstToken,firstToken,exp);
		}
		else if(isKind(Kind.LSQUARE)){
//			Token functionName=t;
			consume();
			Expression exp1=this.expression();
			this.match(Kind.COMMA);
			Expression exp2=this.expression();
			this.match(Kind.RSQUARE);
			expToReturn= new ExpressionFunctionAppWithPixel(firstToken,firstToken,exp1, exp2);
		}
		else throw new SyntaxException(t,"Syntax Error");
		return expToReturn;
	}


	private boolean isFunctionName() {

		return(this.isKind(new Kind[] {Kind.KW_cart_x,Kind.KW_cart_y,Kind.KW_cos,Kind.KW_sin
				,Kind.KW_atan,Kind.KW_abs,Kind.KW_log, Kind.KW_polar_a,Kind.KW_polar_r,
				Kind.KW_int,Kind.KW_float,Kind.KW_width,Kind.KW_height,Kind.KW_red,Kind.KW_green,
				Kind.KW_blue,Kind.KW_alpha}));
	}


	private PixelSelector pixelSelector() throws SyntaxException {
		// TODO Auto-generated method stub
		Token firstToken=t;
		this.consume();
		Expression exp1=this.expression();
		this.match(Kind.COMMA);
		Expression exp2=this.expression();
		this.match(Kind.RSQUARE);
		return new PixelSelector(firstToken,exp1,exp2);
	}

	public Statement statement() throws SyntaxException {
		Statement statement=null;
		if(isKind(Kind.KW_input)){
			statement=this.statementInput();
		}
		else if(isKind(Kind.KW_write)){
			statement=this.statementWrite();
		}
		else if(isKind(Kind.KW_while)){
			statement=this.statementWhile();
		}
		else if(isKind(Kind.KW_if)){
			statement=this.statementIf();
		}
		else if(isKind(Kind.KW_show)){
			statement=this.statementShow();
		}
		else if(isKind(Kind.KW_sleep)){
			statement=this.statementSleep();
		}
		else if(isKind(new Kind[] {Kind.IDENTIFIER,Kind.KW_red,Kind.KW_blue,Kind.KW_green,Kind.KW_alpha})){
			statement=this.statementAssignment();
		}
		else throw new SyntaxException(t, "Error in statement");
		return statement;
	}

	private Statement statementAssignment() throws SyntaxException {
		Expression exp = null;
		Token firstToken=t;
		LHS lhs=new LHSIdent(firstToken,firstToken);
		if(isKind(IDENTIFIER)){
			this.consume();
			if(this.isKind(Kind.LSQUARE)){
				PixelSelector ps =this.pixelSelector();
				lhs=new LHSPixel(firstToken,firstToken,ps);
			}
		}
		else {
			Token color=t;
			this.consume();
			this.match(Kind.LPAREN);
			firstToken=t;
			this.match(IDENTIFIER);
			PixelSelector ps=this.pixelSelector();
			this.match(Kind.RPAREN);
			lhs= new LHSSample(firstToken,firstToken,ps,color);
			
		}
		this.match(Kind.OP_ASSIGN);
		exp = this.expression();		
		return new StatementAssign(firstToken,lhs,exp);
	}


	private Statement statementWhile() throws SyntaxException {
		// TODO Auto-generated method stub
		Token firstToken=t;
		this.consume();
		this.match(Kind.LPAREN);
		Expression exp=this.expression();
		this.match(Kind.RPAREN);
		Block block=this.block();
		return new StatementWhile(firstToken,exp,block);
	}


	private Statement statementIf() throws SyntaxException {
		// TODO Auto-generated method stub
		Token firstToken=t;
		this.consume();
		this.match(Kind.LPAREN);
		Expression exp=this.expression();
		this.match(Kind.RPAREN);
		Block block=this.block();
		return new StatementIf(firstToken,exp,block);
	}


	private Statement statementSleep() throws SyntaxException {
		// TODO Auto-generated method stub
		Token firstToken =t;
		this.consume();
		Expression exp =this.expression();
		return new StatementSleep(firstToken,exp);
	}


	private Statement statementShow() throws SyntaxException {
		// TODO Auto-generated method stub
		Token firstToken=t;
		this.consume();
		Expression exp=this.expression();
		return new StatementShow(firstToken,exp);
	}


	private Statement statementWrite() throws SyntaxException {
		// TODO Auto-generated method stub
		Token firstToken=t;
		this.consume();
		Token source=t;
		this.match(IDENTIFIER);
		this.match(Kind.KW_to);
		Token destination=t;
		this.match(IDENTIFIER);
		return new StatementWrite(firstToken,source,destination);
	}


	private Statement statementInput() throws SyntaxException {
		// TODO Auto-generated method stub
		Token firstToken=t;
		this.consume();
		Token name=t;
		this.match(IDENTIFIER);
		this.match(Kind.KW_from);
		this.match(Kind.OP_AT);
		Expression exp=this.expression();
		return new StatementInput(firstToken, name, exp);
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

