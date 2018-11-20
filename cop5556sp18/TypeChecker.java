package cop5556sp18;

import java.util.Stack;
import java.util.HashMap;
import java.util.ArrayList;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;


class ScopeDeclarationPair {

	  int scope;
	  Declaration declaration;
	  public ScopeDeclarationPair(int s, Declaration dec)
	  {
		  this.scope = s;
		  this.declaration = dec;
	  }
	  public int getScope()
	  {
		  return this.scope;
	  }
	  public Declaration getDec()
	  {
		  return this.declaration;
	  }
}

class SymbolTable{
	
	int  currentScope, nextScope;
	Stack <Integer> scopeStack = new Stack<Integer>();
	HashMap <String, ArrayList<ScopeDeclarationPair>> hm = new HashMap <String, ArrayList<ScopeDeclarationPair>>();
	
	public SymbolTable() 
	{
		this.currentScope = 0;
		this.nextScope = 1;
		scopeStack.push(this.currentScope);
	}
	
	public void enterScope()
	{
		this.currentScope = this.nextScope++; 
		scopeStack.push(this.currentScope);
	}
	
	public void leaveScope()
	{		
		this.scopeStack.pop();
		this.currentScope = this.scopeStack.peek();
	}
	
	public boolean insert(String ident, Declaration declaration)
	{
		ArrayList<ScopeDeclarationPair> ps = new ArrayList<ScopeDeclarationPair>();
		ScopeDeclarationPair p = new ScopeDeclarationPair(currentScope, declaration);
		if(hm.containsKey(ident))
		{
			ps = hm.get(ident);
			for(ScopeDeclarationPair it: ps)
			{
				if(it.getScope()==currentScope)
					return false;
			}
		}
		ps.add(p);
		hm.put(ident, ps);		
		return true;
	}
	
	public Declaration lookup(String ident)
	{
		if(!hm.containsKey(ident))
			return null;
		
		Declaration dec=null;
		ArrayList<ScopeDeclarationPair> ps = hm.get(ident);
		for(int i=ps.size()-1;i>=0;i--)
		{
			int temp_scope = ps.get(i).getScope();
			if(scopeStack.contains(temp_scope))
			{
				dec = ps.get(i).getDec();
				break;
			}
		}
		return dec;
	}
	
}



public class TypeChecker implements ASTVisitor {

	SymbolTable symbolTable;

	public TypeChecker() {
		symbolTable= new SymbolTable();
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symbolTable.enterScope();
		
		for(ASTNode node : block.decsOrStatements){
			node.visit(this, arg);
		}
		
		symbolTable.leaveScope();

		return null;
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = declaration.name;
		if (declaration.width != null) {
			declaration.width.visit(this, null);
		}
		if (declaration.height != null) {
			declaration.height.visit(this, null);
		}
		if(symbolTable.hm.containsKey(name)){
			ArrayList<ScopeDeclarationPair> al = symbolTable.hm.get(name);
			for(ScopeDeclarationPair p : al){
				if(symbolTable.currentScope==p.scope)
					this.throwError(declaration.firstToken,"Error in declaration" );
			}
		}
		
		if(declaration.width !=null ){
//			declaration.width.visit(this, arg);
			if(declaration.width.type != Type.INTEGER)
				this.throwError(declaration.firstToken,"Error in declaration" );
			if(Types.getType(declaration.type) != Type.IMAGE)
				this.throwError(declaration.firstToken,"Error in declaration" );			
		}
		
		if(declaration.height !=null ){
//			declaration.height.visit(this, arg);
			if(declaration.height.type != Type.INTEGER)
				this.throwError(declaration.firstToken,"Error in declaration" );
			if(Types.getType(declaration.type) != Type.IMAGE)
				this.throwError(declaration.firstToken,"Error in declaration" );			
		}
		
		if((declaration.width==null) != (declaration.height==null))
			this.throwError(declaration.firstToken,"Error in declaration" );
		
		symbolTable.insert(declaration.name, declaration);
		
		return null;
		
	}

	private void throwError(Token t, String message) throws SemanticException {
		throw new SemanticException(t, message);
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
//		statementWrite.destDec.visit(this, arg);
//		statementWrite.sourceDec.visit(this, arg);
		statementWrite.sourceDec=symbolTable.lookup(statementWrite.sourceName);
		if(statementWrite.sourceDec==null)
			this.throwError(statementWrite.firstToken,"Error in statementWrite");
		
		statementWrite.destDec=symbolTable.lookup(statementWrite.destName);
		if(statementWrite.destDec==null)
			this.throwError(statementWrite.firstToken,"Error in statementWrite");
		
		if(Types.getType(statementWrite.sourceDec.type)!=Type.IMAGE)
			this.throwError(statementWrite.firstToken,"Error in statementWrite");
		
		if(Types.getType(statementWrite.destDec.type)!=Type.FILE)
			this.throwError(statementWrite.firstToken,"Error in statementWrite");
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		statementInput.e.visit(this, arg);
		statementInput.dec=this.symbolTable.lookup(statementInput.destName);
		if(statementInput.dec==null)
			this.throwError(statementInput.firstToken, "Error in statementInput");
		if(statementInput.e.type!=Type.INTEGER)
			this.throwError(statementInput.firstToken, "Error in statementInput");
		
		return null;
	}
//here
	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		pixelSelector.ex.visit(this, null);
		pixelSelector.ey.visit(this, null);
		if(pixelSelector.ex.type != pixelSelector.ey.type) {
			this.throwError(pixelSelector.firstToken, "Error in pixelSelector");
		}
		if(!(pixelSelector.ex.type == Type.INTEGER || pixelSelector.ex.type == Type.FLOAT)) {
			this.throwError(pixelSelector.firstToken, "Error in pixelSelector");
		}
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.guard.visit(this, arg);
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);
		if(!(expressionConditional.guard.type == Type.BOOLEAN)) {
			this.throwError(expressionConditional.firstToken, "Error in expressionConditional");
		}
		if(expressionConditional.trueExpression.type != expressionConditional.falseExpression.type) {
			this.throwError(expressionConditional.firstToken, "Error in expressionConditional");
		}
		expressionConditional.type = expressionConditional.trueExpression.type;
		return null;
	}

	protected boolean isKind(Kind[] kinds, Kind kind) {
		for (Kind k : kinds) {
			if (k == kind)
				return true;
		}
		return false;
	}
	

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, null);
		expressionBinary.rightExpression.visit(this, null);
		
		if(expressionBinary.leftExpression.type == Type.INTEGER && 	expressionBinary.rightExpression.type == Type.INTEGER) {
			if(isKind(new Kind[] {Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_DIV, Kind.OP_MOD, Kind.OP_TIMES, Kind.OP_POWER, Kind.OP_AND, Kind.OP_OR}, expressionBinary.op)) {
				expressionBinary.type = Type.INTEGER;
			}
			else if(isKind(new Kind[] {Kind.OP_AND, Kind.OP_OR}, expressionBinary.op)) {
				expressionBinary.type = Type.INTEGER;
			}
			else if(isKind(new Kind[] {Kind.OP_EQ, Kind.OP_NEQ,Kind.OP_GE, Kind.OP_GT, Kind.OP_LE, Kind.OP_LT}, expressionBinary.op)) {
				expressionBinary.type = Type.BOOLEAN;
			}
			else {
				this.throwError(expressionBinary.firstToken, "Error in expressionBinary first");
			}
		}
		
		else if(expressionBinary.leftExpression.type == Type.FLOAT &&	expressionBinary.rightExpression.type == Type.FLOAT) {
			if(isKind(new Kind[] {Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_DIV, Kind.OP_TIMES, Kind.OP_POWER}, expressionBinary.op)) {
				expressionBinary.type = Type.FLOAT;
			}
			else if(isKind(new Kind[] {Kind.OP_EQ, Kind.OP_NEQ,Kind.OP_GE, Kind.OP_GT, Kind.OP_LE, Kind.OP_LT}, expressionBinary.op)) {
				expressionBinary.type = Type.BOOLEAN;
			}
			else {
				this.throwError(expressionBinary.firstToken, "Error in expressionBinary second");
			}
		}
		
		else if(expressionBinary.leftExpression.type == Type.FLOAT &&	expressionBinary.rightExpression.type == Type.INTEGER) {
			if(isKind(new Kind[] {Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_DIV, Kind.OP_TIMES, Kind.OP_POWER}, expressionBinary.op)) {
				expressionBinary.type = Type.FLOAT;
			}
			else {
				this.throwError(expressionBinary.firstToken, "Error in expressionBinary third");
			}
		}
		
		else if(expressionBinary.leftExpression.type == Type.INTEGER &&	expressionBinary.rightExpression.type == Type.FLOAT) {
			if(isKind(new Kind[] {Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_DIV, Kind.OP_TIMES, Kind.OP_POWER}, expressionBinary.op)) {
				expressionBinary.type = Type.FLOAT;
			}
			else {
	
				this.throwError(expressionBinary.firstToken, "Error in expressionBinary fourth" + expressionBinary.op);
			}
		}
		
		else if(expressionBinary.leftExpression.type == Type.BOOLEAN &&	expressionBinary.rightExpression.type == Type.BOOLEAN) {
			if(isKind(new Kind[] {Kind.OP_AND, Kind.OP_OR}, expressionBinary.op)) {
				expressionBinary.type = Type.BOOLEAN;
			}
			else if(isKind(new Kind[] {Kind.OP_EQ, Kind.OP_NEQ,Kind.OP_GE, Kind.OP_GT, Kind.OP_LE, Kind.OP_LT}, expressionBinary.op)) {
				expressionBinary.type = Type.BOOLEAN;
			}
			else {
				this.throwError(expressionBinary.firstToken, "Error in expressionBinary fifth");
			}	
		}
		else {
			this.throwError(expressionBinary.firstToken, "Error in expressionBinary sixth");		
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		expressionUnary.type = expressionUnary.expression.type;
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.type=Type.INTEGER;
		return null;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.type=Type.BOOLEAN;
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		expressionPredefinedName.type=Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.type=Type.FLOAT;
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		expressionFunctionAppWithExpressionArg.e.visit(this, null);
		if(expressionFunctionAppWithExpressionArg.e.type == Type.INTEGER) {
			if(isKind(new Kind[] {Kind.KW_abs, Kind.KW_red, Kind.KW_green, Kind.KW_blue, Kind.KW_alpha}, expressionFunctionAppWithExpressionArg.function)) {
				expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			}
			else if(Kind.KW_float==expressionFunctionAppWithExpressionArg.function) {
				expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			}
			else if(Kind.KW_int==expressionFunctionAppWithExpressionArg.function) {
				expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			}
			else {
				this.throwError(expressionFunctionAppWithExpressionArg.firstToken, "Error in expressionFunctionAppWithExpressionArg");
			}
		}
		
		else if(expressionFunctionAppWithExpressionArg.e.type == Type.FLOAT) {
			if(isKind(new Kind[] {Kind.KW_abs, Kind.KW_sin, Kind.KW_cos, Kind.KW_atan,Kind.KW_log}, expressionFunctionAppWithExpressionArg.function)) {
				expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			}
			else if(Kind.KW_float==expressionFunctionAppWithExpressionArg.function) {
				expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			}
			else if(Kind.KW_int==expressionFunctionAppWithExpressionArg.function) {
				expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			}
			else {
				this.throwError(expressionFunctionAppWithExpressionArg.firstToken, "Error in expressionFunctionAppWithExpressionArg");
			}
		}
		
		else if(expressionFunctionAppWithExpressionArg.e.type == Type.IMAGE) {
			if(isKind(new Kind[] {Kind.KW_width, Kind.KW_height}, expressionFunctionAppWithExpressionArg.function)) {
				expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			}
			else {
				this.throwError(expressionFunctionAppWithExpressionArg.firstToken, "Error in expressionFunctionAppWithExpressionArg");
			}
		}
		
		else {
			this.throwError(expressionFunctionAppWithExpressionArg.firstToken, "Error in expressionFunctionAppWithExpressionArg");
		}
		
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		expressionFunctionAppWithPixel.e0.visit(this, arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);
		if((expressionFunctionAppWithPixel.name == Kind.KW_cart_x) || (expressionFunctionAppWithPixel.name == Kind.KW_cart_y)) {
			if((expressionFunctionAppWithPixel.e0.type == Type.FLOAT) && (expressionFunctionAppWithPixel.e1.type == Type.FLOAT)) {
				expressionFunctionAppWithPixel.type = Type.INTEGER;
			}
			else {
				this.throwError(expressionFunctionAppWithPixel.firstToken, "Error in expressionFunctionAppWithPixel");
			}
		}
		
		else if((expressionFunctionAppWithPixel.name == Kind.KW_polar_a) ||	(expressionFunctionAppWithPixel.name == Kind.KW_polar_r)) {
			if((expressionFunctionAppWithPixel.e0.type == Type.INTEGER) &&	(expressionFunctionAppWithPixel.e1.type == Type.INTEGER)) {
				expressionFunctionAppWithPixel.type = Type.FLOAT;
			}
			else {
				this.throwError(expressionFunctionAppWithPixel.firstToken, "Error in expressionFunctionAppWithPixel");
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		
		if(expressionPixelConstructor.alpha.type != Type.INTEGER)
			this.throwError(expressionPixelConstructor.firstToken, "Error in expressionPixelConstructor");
		if(expressionPixelConstructor.red.type != Type.INTEGER)
			this.throwError(expressionPixelConstructor.firstToken, "Error in expressionPixelConstructor");
		if(expressionPixelConstructor.green.type != Type.INTEGER)
			this.throwError(expressionPixelConstructor.firstToken, "Error in expressionPixelConstructor");
		if(expressionPixelConstructor.blue.type != Type.INTEGER) {
			this.throwError(expressionPixelConstructor.firstToken, "Error in expressionPixelConstructor");
		}
		expressionPixelConstructor.type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
//		statementAssign.lhs.dec=this.symbolTable.lookup(statementAssign.lhs);
		if(statementAssign.lhs.type!=statementAssign.e.type)
			this.throwError(statementAssign.firstToken, "Error in statementAssign");
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		statementShow.e.visit(this, arg);
		Type type=statementShow.e.type;
		if(type==null || type==Type.NONE || type==type.FILE)
			this.throwError(statementShow.firstToken, "Error in statementShow");
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		expressionPixel.pixelSelector.visit(this, null);
		expressionPixel.dec = symbolTable.lookup(expressionPixel.name);
		if(expressionPixel.dec == null) {
			this.throwError(expressionPixel.firstToken, "Error in expressionPixel");
		}
		if(Types.getType(expressionPixel.dec.type) != Type.IMAGE) {
			this.throwError(expressionPixel.firstToken, "Error in expressionPixel");
		}
		expressionPixel.type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		expressionIdent.dec = symbolTable.lookup(expressionIdent.name);
		if(expressionIdent.dec == null) {
			this.throwError(expressionIdent.firstToken, "Error in expressionIdent");
		}
		expressionIdent.type = Types.getType(expressionIdent.dec.type);
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		lhsSample.pixelSelector.visit(this, arg);
		lhsSample.dec=symbolTable.lookup(lhsSample.name);
		if(lhsSample.dec==null)
			this.throwError(lhsSample.firstToken,"Error in lhsSample");
		if(Types.getType(lhsSample.dec.type)!=Type.IMAGE)
			this.throwError(lhsSample.firstToken,"Error in lhsSample");
		lhsSample.type =Type.INTEGER;
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		lhsPixel.pixelSelector.visit(this, null);
		lhsPixel.dec = symbolTable.lookup(lhsPixel.name);
		if(lhsPixel.dec == null) {
			this.throwError(lhsPixel.firstToken, "Error in lhsPixel");
		}
		if(Types.getType(lhsPixel.dec.type) != Type.IMAGE) {
			this.throwError(lhsPixel.firstToken, "Error in lhsPixel");
		}
		lhsPixel.type = Type.INTEGER;
		return null;
		
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		lhsIdent.dec = symbolTable.lookup(lhsIdent.name);
		if (lhsIdent.dec == null) {
			this.throwError(lhsIdent.firstToken,"Error in lhsIdent");
		}
		lhsIdent.type = Types.getType(lhsIdent.dec.type);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		statementIf.guard.visit(this, arg);
		statementIf.b.visit(this, arg);
		if(statementIf.guard.type!=Type.BOOLEAN)
			this.throwError(statementIf.firstToken, "Error in statementIf");
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		statementWhile.guard.visit(this,arg);
		statementWhile.b.visit(this,arg);
		Type type=statementWhile.guard.type;
		if(type!=Type.BOOLEAN)
			this.throwError(statementWhile.firstToken, "Error in statementWhile");
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		statementSleep.duration.visit(this,arg);
		if(statementSleep.duration.type != Type.INTEGER)
			this.throwError(statementSleep.firstToken, "Error in statementSleep");
		return null;
	}


}
